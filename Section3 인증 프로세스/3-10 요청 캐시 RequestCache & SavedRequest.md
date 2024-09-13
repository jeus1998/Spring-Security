# 요청 캐시 RequestCache / SavedRequest

### RequestCache - 인터페이스 

![16.png](Image%2F16.png)
- 인증 절차 문제로 리다이렉트 된 후에 이전에 했던 요청 정보를 담고 있는 `SavedRequest` 객체를 
  쿠키 혹은 세션에 저장하고 필요시 다시 가져와 실행하는 캐시 메카니즘
- RequestCache 인터페이스의 구현체로 HttpSessionRequestCache 존재 

### SavedRequest 

![17.png](Image%2F17.png)
- `SavedRequest` 로그인과 같은 인증 절차 후 사용자를 인증 이전의 원래 페이지로 안내하며 이전 요청과 관련된 여러 
  정보를 저장한다
- 즉 인증 절차에서 문제가 생겼을 때 `SavedRequest`에 거부된 요청의 정보를 저장하고 `RequestCache`에서 
  인증에 성공하면 SavedRequest 에 기반으로 이전 거부된 요청으로 리다이렉트 해준다.

### 동작 

![18.png](Image%2F18.png)

### requestCache() API

![19.png](Image%2F19.png)
- 모든 요청에서 Session에 저장된 SavedRequest 객체를 꺼내게 할 필요는 없다. 
- 우리가 원하는 동작은 인증 절차에 실패한 이후 인증에 성공했을 때 이런 절차가 필요하다.
- 이런 경로를 구분하기 위해서 `SpringSecurity`는 쿼리 스트링을 사용한다.

### RequestCacheAwareFilter

![20.png](Image%2F20.png)
- `RequestCacheAwareFilter`는 이전에 저장했던 웹 요청을 다시 불러오는 역할을 한다
- `SavedRequest`가 현재 Request 와 일치하면 이 요청을 필터 체인의 doFilter 메소드에 전달하고 SavedRequest 가 없으면 
  필터는 원래 Request 을 그대로 진행시킨다

### requestCache() API - test

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{

    HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
    requestCache.setMatchingRequestParameterName("customParam=y");
    http
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/logoutSuccess").permitAll()
                    .anyRequest().authenticated())
            .formLogin(form -> form.successHandler((request, response, authentication) -> {
                SavedRequest savedRequest = requestCache.getRequest(request, response);
                String redirectUrl = savedRequest.getRedirectUrl();
                response.sendRedirect(redirectUrl);
            }))
            .requestCache(cache -> cache.requestCache(requestCache));


    return  http.build();
}
```

### 디버깅을 통한 동작 확인

시나리오 
```text
인증 없이 인증이 필요한 경로에 요청 이후 
/login 리다이렉트 

/login post 요청 이후 동작 확인 
```


RequestCacheAwareFilter
```java
@Override
public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
    HttpServletRequest wrappedSavedRequest = this.requestCache.getMatchingRequest((HttpServletRequest) request,
            (HttpServletResponse) response);
    chain.doFilter((wrappedSavedRequest != null) ? wrappedSavedRequest : request, response);
}
```
- `this.requestCache.getMatchingRequest` 메서드 
  - requestCache
    - `HttpSessionRequestCache requestCache = new HttpSessionRequestCache();`
    - `requestCache.setMatchingRequestParameterName("customParam=y");`
- HttpSessionRequestCache.getMatchingRequest()메서드에서 만약 파라미터 `customParam=y`가 정상적으로 있고 `savedRequest`가 존재한다면 
  wrappedSavedRequest 반환 아니라면 null 반환 


HttpSessionRequestCache
```java
@Override
public HttpServletRequest getMatchingRequest(HttpServletRequest request, HttpServletResponse response) {
    if (this.matchingRequestParameterName != null) {
        if (!StringUtils.hasText(request.getQueryString())
                || !UriComponentsBuilder.fromUriString(UrlUtils.buildRequestUrl(request))
                    .build()
                    .getQueryParams()
                    .containsKey(this.matchingRequestParameterName)) {
            this.logger.trace(
                    "matchingRequestParameterName is required for getMatchingRequest to lookup a value, but not provided");
            return null;
        }
    }
    SavedRequest saved = getRequest(request, response);
    if (saved == null) {
        this.logger.trace("No saved request");
        return null;
    }
    if (!matchesSavedRequest(request, saved)) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(LogMessage.format("Did not match request %s to the saved one %s",
                    UrlUtils.buildRequestUrl(request), saved));
        }
        return null;
    }
    removeRequest(request, response);
    if (this.logger.isDebugEnabled()) {
        this.logger.debug(LogMessage.format("Loaded matching saved request %s", saved.getRedirectUrl()));
    }
    return new SavedRequestAwareWrapper(saved, request);
}
```
