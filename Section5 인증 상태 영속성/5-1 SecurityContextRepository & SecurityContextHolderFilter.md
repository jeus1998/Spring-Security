# SecurityContextRepository / SecurityContextHolderFilter

### SecurityContextRepository

- 스프링 시큐리티에서 사용자가 인증을 한 이후 요청에 대해 계속 사용자의 인증을 유지하기 위해 사용되는 클래스
- 인증 상태의 영속 메커니즘은 사용자가 인증을 하게 되면 해당 사용자의 인증 정보와 권한이 `SecurityContext`에 저장되고 
  `HttpSession`을 통해 요청 간 영속이 이루어 지는 방식이다

SecurityContext 저장: SecurityContextRepository 저장 / 스레드 로컬 저장 
```text
SecurityContext를 스레드 로컬에 저장하는건 

웹 애플리케이션에서 request per thread model을 대부분 사용하는데

1개의 클라이언트 요청이 톰캣에 들어와서 나갈때 까지 요청에 대한 인증 역속성이고 

SecurityContextRepository를 통한 명시적 시큐리티 영속성은 

사용자가 인증 이후에 다시 요청했을 때 인증이 따로 필요없이 세션에 있는 시큐리티 컨텍스트를 확인하여 시큐리티 영속성을 확장하는 방법이다

스레드 로컬에 저장을 안하고 세션을 통한 인증 방식도 가능하지만 
사용자 권한 정보를 여러 웹 계층에서 빠르게 확인하려면 스레드 로컬 저장소에서 조회가 세션에서 꺼내오는게 더 빠르다.
```

![1.png](Image%2F1.png)

### SecurityContextRepository 구조 

![2.png](Image%2F2.png)
- `HttpSessionSecurityContextRepository` - 요청 간에 HttpSession 에 보안 컨텍스트를 저장한다. 
  후속 요청 시 컨텍스트 영속성을 유지한다
- `RequestAttributeSecurityContextRepository` - ServletRequest 에 보안 컨텍스트를 저장한다. 
  후속 요청 시 컨텍스트 영속성을 유지할 수 없다
- `NullSecurityContextRepository` - 세션을 사용하지 않는 인증(JWT, OAuth2)일 경우 사용하며 
  컨텍스트 관련 아무런 처리를 하지 않는다
- `DelegatingSecurityContextRepository` - RequestAttributeSecurityContextRepository 와 
  HttpSessionSecurityContextRepository 를 동시에 사용할 수 있도록 위임된 클래스로서 초기화 시 기본으로 설정된다

### SecurityContextHolderFilter

- `SecurityContextRepository`를 사용하여 `SecurityContext`를 얻고 이를 `SecurityContextHolder`에 설정하는 필터 클래스
- 이 필터 클래스는 SecurityContextRepository.saveContext()를 강제로 실행시키지 않고 사용자가 명시적으로 호출되어야 
  `SecurityContext`를 저장할 수 있는데 이는 `SecurityContextPersistenceFilter` 와 다른점이 
  - `SecurityContextPersistenceFilter`: doFilter finally 부분에서 동작(세션에 컨텍스트 저장)한다. 
  - `SecurityContextHolderFilter`: doFilter finally 부분에서 스레드 로컬에 저장된 컨텍스트 삭제만 진행 
- 인증이 지속되어야 하는지를 각 인증 메커니즘이 독립적으로 선택할 수 있게 하여 더 나은 유연성을 제공하고 `HttpSession`에 
  필요할 때만 저장함으로써 성능을 향상시킨다

### SecurityContext 생성, 저장, 삭제

- 익명 사용자
  - `SecurityContextRepository`를 사용하여 새로운 `SecurityContext`객체를 생성하여 `SecurityContextHolder`에 저장 후 다음 필터로 전달
  - `AnonymousAuthenticationFilter`에서 `AnonymousAuthenticationToken` 객체를 `SecurityContext`에 저장
- 인증 요청
  - `SecurityContextRepository`를 사용하여 새로운 `SecurityContext`객체를 생성하여 `SecurityContextHolder`에 저장 후 다음 필터로 전달
  - `UsernamePasswordAuthenticationFilter`에서 인증 성공 후 `SecurityContext`에 `UsernamePasswordAuthentication` 객체를 `SecurityContext`에 저장
  - `SecurityContextRepository`를 사용하여 `HttpSession`에 `SecurityContext`를 저장
- 인증 후 요청
  - `SecurityContextRepository`를 사용하여 `HttpSession`에서 `SecurityContext` 꺼내어 `SecurityContextHolder`에서 저장 후 다음 필터로 전달
  - `SecurityContext` 안에 `Authentication` 객체가 존재하면 계속 인증을 유지한다
- 클라이언트 응답 시 공통
  - SecurityContextHolder.clearContext()로 컨텍스트를 삭제 한다 (스레드 풀의 스레드일 경우 반드시 필요)

### SecurityContextHolderFilter 흐름도

![3.png](Image%2F3.png)

### SecurityContextHolderFilter & SecurityContextPersistenceFilter

![4.png](Image%2F4.png)

### securityContext() API

![5.png](Image%2F5.png)

### CustomAuthenticationFilter & SecurityContextRepository

- 커스텀 한 인증 필터를 구현할 경우 인증이 완료된 후 `SecurityContext`를 `SecurityContextHolder`에 설정한 후 
  `securityContextRepository` 에 저장하기 위한 코드를 명시적으로 작성해 주어야 한다
- `securityContextRepository`는 `HttpSessionSecurityContextRepository` 혹은 `DelegatingSecurityContextRepository`를 사용하면 된다

```java
securityContextHolderStrategy.setContext(context);
securityContextRepository.saveContext(context, request, response);
```

### CustomFilter 적용하기 

CustomAuthenticationFilter - 커스텀 필터 
```java
public class CustomAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public CustomAuthenticationFilter(HttpSecurity http) {
        super(new AntPathRequestMatcher("/api/login", "GET"));
        setSecurityContextRepository(getSecurityContextRepository(http));
    }
    
    private SecurityContextRepository getSecurityContextRepository(HttpSecurity http) {
        SecurityContextRepository securityContextRepository = http.getSharedObject(SecurityContextRepository.class);
        if (securityContextRepository == null) {
            securityContextRepository = new DelegatingSecurityContextRepository(
                    new HttpSessionSecurityContextRepository(), new RequestAttributeSecurityContextRepository());
        }
        return securityContextRepository;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username,password);

        return this.getAuthenticationManager().authenticate(token);
    }
}
```
- `setSecurityContextRepository(getSecurityContextRepository(http));`
  - 부모 클래스(AbstractAuthenticationProcessingFilter)의 setSecurityContextRepository() 메서드 
- 인증에 성공하면 부모의 successfulAuthentication() 메서드 동작  
  - this.securityContextHolderStrategy.setContext(context); - 스레드 로컬에 시큐리티 컨텍스트 저장 
  - this.securityContextRepository.saveContext(context, request, response); - 세션,요청 객체에 시큐리티 컨텍스트 저장 


SecurityConfig
```java
@EnableWebSecurity
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        AuthenticationManager authenticationManager = authenticationManagerBuilder.build(); // 인증 관리자 

        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/login").permitAll()
                        .anyRequest().authenticated())
                .formLogin(Customizer.withDefaults())
                .securityContext(securityContext -> securityContext
                        .requireExplicitSave(false)) // SecurityContextHolderFilter 선택 (세션 직접 저장) 
                .authenticationManager(authenticationManager)
                .addFilterBefore(customFilter(http, authenticationManager), UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    // 커스텀 필터 생성 
    public CustomAuthenticationFilter customFilter(HttpSecurity http, AuthenticationManager authenticationManager) {
        CustomAuthenticationFilter customAuthenticationFilter = new CustomAuthenticationFilter(http);
        customAuthenticationFilter.setAuthenticationManager(authenticationManager);
        return customAuthenticationFilter;
    }

    @Bean
    public UserDetailsService userDetailsService(){
        UserDetails user = User.withUsername("user").password("{noop}1111").roles("USER").build();
        return  new InMemoryUserDetailsManager(user);
    }
}
```

### 정리

- 성능 
  - 시큐리티6는 시큐리티5에 비해서 성능을 고려 많이 했다.
  - supplier 사용: session, Authentication, ...
  - 선택적 세션 저장
    - SecurityContextHolderFilter & SecurityContextPersistenceFilter
    - securityContext -> securityContext.requireExplicitSave(false)
- 커스텀 필터 
  - 시큐리티 컨텍스트 세션 저장에 필요한 SecurityContextRepository 생성
  - AbstractAuthenticationProcessingFilter 확장해서 만들기 
- 세션 활용 X - JWT 활용이라면?
  - securityContext -> securityContext.requireExplicitSave(false)
  - SecurityContextRepository: `NullSecurityContextRepository`
