# WebSecurity / HttpSecurity

### HttpSecurity

- `HttpSecurityConfiguration`에서 `HttpSecurity`를 생성하고 초기화를 진행한다
- `HttpSecurity`는 보안에 필요한 각 설정 클래스와 필터들을 생성하고 최종적으로 `SecurityFilterChain`빈 생성

![4.png](Image%2F4.png)
- build() ➡️  doBuild() ➡️ 초기화 작업 (init() ➡️ configure()) 
- SecurityFilterChain
  - 여러가지 필터를 내부에 가진다.  
  - 내부에 requestMatcher 또한 가진다. 

### SecurityFilterChain

![5.png](Image%2F5.png)

SecurityFilterChain 인터페이스 
```java
public interface SecurityFilterChain {
	boolean matches(HttpServletRequest request);
	List<Filter> getFilters();
}
```

DefaultSecurityFilterChain 구현체 
```java
public final class DefaultSecurityFilterChain implements SecurityFilterChain {
    private static final Log logger = LogFactory.getLog(DefaultSecurityFilterChain.class);
    private final RequestMatcher requestMatcher;
    private final List<Filter> filters;
    @Override
  	public List<Filter> getFilters() {
  		return this.filters;
  	}
  	@Override
  	public boolean matches(HttpServletRequest request) {
  		return this.requestMatcher.matches(request);
  	}
    // 생략 ...
}    
```
- `boolean matches(HttpServletRequest request);`
  - 요청이 현재 `SecurityFilterChain`에 의해 처리되어야 하는지 여부를 결정 
  - true 반환: 현재 요청이 이 필터 체인에 의해 처리되어야 함을 의미 
  - false 반환: 다른 필터 체인이나 처리 로직에 의해 처리되어야 함을 의미 또 다른 필터 or 디스패처 서블릿
  - 이를 통해 특정 요청에 대해 적절한 보안 필터링 로직이 적용될 수 있도록 한다. 
- `List<Filter> getFilters()`
  - 현재 `SecurityFilterChain`에 포함된 Filter 객체의 리스트를 반환 
  - 각 필터는 요청 처리 과정에서 특정 작업(인증, 권한 부여, 로깅)을 수행 

### WebSecurity

- `WebSecurityConfiguration`에서 `WebSecurity`를 생성하고 초기화를 진행
- `WebSecurity`는 `HttpSecurity` 에서 생성한 `SecurityFilterChain` 빈을 `SecurityBuilder`에 저장한다
- `WebSecurity`가 build()를 실행하면 `SecurityBuilder`에서 `SecurityFilterChain`을 꺼내어 
  `FilterChainProxy` 생성자에게 전달한다

![6.png](Image%2F6.png)

### 정리 

- 1) HttpSecurityConfiguration -> HttpSecurity 생성 

```java
http
  .csrf(withDefaults())
  .addFilter(webAsyncManagerIntegrationFilter)
  .exceptionHandling(withDefaults())
  .headers(withDefaults())
  .sessionManagement(withDefaults())
  .securityContext(withDefaults())
  .requestCache(withDefaults())
  .anonymous(withDefaults())
  .servletApi(withDefaults())
  .apply(new DefaultLoginPageConfigurer<>());
http.logout(withDefaults());
// @formatter:on
applyCorsIfAvailable(http);
applyDefaultConfigurers(http);
return http;
```
- HttpSecurity 객체 생성을 하면서 보안에 필요한 여러가지 필터를 생성
- 2) WebSecurityConfiguration -> WebSecurity 생성 
- 3) SpringBootWebSecurityConfiguration -> http.build()
  - SecurityConfigurer 초기화 진행 
  - init()
  - configure()
- 4) (Default)SecurityFilterChain 빈 생성 완료 
- 5) SecurityFilterChain 빈을 SecurityBuilder 저장
- 6) WebSecurity 가 build()를 실행하면 SecurityBuilder 에서 SecurityFilterChain을 꺼내어 FilterChainProxy 생성자에게 전달
- FilterChainProxy 생성 끝 
