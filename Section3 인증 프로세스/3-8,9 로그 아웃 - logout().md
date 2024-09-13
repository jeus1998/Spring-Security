# 로그 아웃 - logout()

### 로그 아웃 

- 스프링 시큐리티는 기본적으로 `DefaultLogoutPageGeneratingFilter`를 통해 로그아웃 페이지를 제공 
  `GET /logout` URL 통해 접근이 가능하다. 
- 로그아웃 실행은 기본적으로 `POST /logout` 으로만 가능하나 CSRF 기능을 비활성화 할 경우 혹은 `RequestMatcher`를 사용할 경우
  GET, PUT, DELETE 모두 가능하다 
- 로그아웃 필터를 거치지 않고 스프링 MVC 에서 커스텀 하게 구현할 수 있으며 로그인 페이지가 커스텀하게 생성될 경우 로그아웃 기능도 커스텀하게 
  구현해야 한다

### logout() API

![13.png](Image%2F13.png)
- addLogoutHandler: 기존 로그아웃 핸들러를 대체하는게 아닌 추가 작업을 위해 핸들러를 추가 

### LogoutFilter

![14.png](Image%2F14.png)
- RequestMatcher: URL + Http Method 모두 맞아야지 LogoutHandler 동작 아니면 바로 doFilter

### logout() API Test

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
    http
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/logoutSuccess").permitAll()
                    .anyRequest().authenticated())

            .formLogin(Customizer.withDefaults())
            .logout(logout -> logout
                    .logoutUrl("/logout")
                    .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST"))
                    .logoutSuccessUrl("/logoutSuccess")
                    .logoutSuccessHandler((request, response, authentication) -> {
                        response.sendRedirect("/logoutSuccess");
                    })
                    .deleteCookies("JSESSIONID", "remember-me")
                    .invalidateHttpSession(true)
                    .clearAuthentication(true)
                    .addLogoutHandler((request, response, authentication) -> {
                        HttpSession session = request.getSession();
                        session.invalidate();
                        SecurityContextHolder.getContextHolderStrategy().getContext().setAuthentication(null);
                        SecurityContextHolder.getContextHolderStrategy().clearContext();
                    })
                    .permitAll()
            );

    return  http.build();
}
```
- CSRF 설정 
  - 설정 (기본값) true
    - `/logout POST`만 가능
  - 설정 false
    - `/logout HTTP 메서드`전부 가능 
- logoutRequestMatcher 설정 
  - `.logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST"))`
  - HTTP 메서드 생략 X: 허용하는(명시한) 메서드만 가능 
  - HTTP 메서드 생략 O: 전부 가능 
- 로그아웃 성공 이후 
  - 간단하게 리다이렉트 경로만 지정: `.logoutSuccessUrl("/logoutSuccess")`
  - 여러가지 설정 적용: `logoutSuccessHandler`

### LogoutFilter - 디버깅을 통한 분석

- 설정은 logout() API Test 동일 

LogoutFilter.java 
```java
protected boolean requiresLogout(HttpServletRequest request, HttpServletResponse response) {
    if (this.logoutRequestMatcher.matches(request)) {
        return true;
    }
    if (this.logger.isTraceEnabled()) {
        this.logger.trace(LogMessage.format("Did not match request to %s", this.logoutRequestMatcher));
    }
    return false;
}

private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if (requiresLogout(request, response)) {
			Authentication auth = this.securityContextHolderStrategy.getContext().getAuthentication();
			if (this.logger.isDebugEnabled()) {
				this.logger.debug(LogMessage.format("Logging out [%s]", auth));
			}
			this.handler.logout(request, response, auth);
			this.logoutSuccessHandler.onLogoutSuccess(request, response, auth);
			return;
		}
		chain.doFilter(request, response);
	}
```
- 전체적인 흐름은 `FilterChainProxy doFilter` 부분에 브레이크 포인트를 걸면 여러가지 필터 체킹 가능 
  - 이번엔 LogoutFilter 부분만 체킹  
- client 로그아웃 폼 요청:  [GET] /logout 
- client 로그아웃 폼에서 logout 버튼 클릭: [POST] /logout
- `if (requiresLogout(request, response))`
  - request에서 요청 url을 꺼내서 로그아웃 설정에 지정해둔 경로, HTTP 메서드가 맞는지 확인
  - 맞으면 로그아웃 진행
  - 아니면 다음 필터 호출 `chain.doFilter(request, response);
- `this.handler.logout(request, response, auth);`
  - 인증 Authentication 을 가져와서 로그아웃 핸들러에 logout() 동작 시작 
  - 이때 LogoutFilter가 가지는 handler는 compositeHandler 
  - ![15.png](Image%2F15.png)
  - SpringSeucirty에서 기본적으로 넣은 Handler + 내가 넣은 Handler가 보인다. 
  - SecurityConfig@lamdaxxxx: 내가 추가한 핸들러 
- `this.logoutSuccessHandler.onLogoutSuccess(request, response, auth);`
  - `CompositeLogoutHandler` handler.logout() 끝나면 동작 
  - 로그아웃 성공 url로 리다이렉트 


CompositeLogoutHandler.java
```java
@Override
public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
    for (LogoutHandler handler : this.logoutHandlers) {
        handler.logout(request, response, authentication);
    }
}
```
- for문을 돌면서 handler.logout() 동작 
- 각 핸들러들은 쿠키 삭제/ 세션 삭제/ Security 컨텍스트 삭제 같은 여러 동작을 한다. 

