# 스프링 MVC 로그인 구현

### 스프링 MVC 로그인 구현 

- 스프링 시큐리티 필터에 의존하는 대신 수동으로 사용자를 인증하는 경우 스프링 MVC 컨트롤러 엔드포인트를 사용할 수 있다
- 요청 간에 인증을 저장하고 싶다면 HttpSessionSecurityContextRepository 를 사용하여 인증 상태를 저장 할 수있다

![6.png](Image%2F6.png)

### SecurityConfig

```java
@EnableWebSecurity(debug = false) // default: false
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login").permitAll()
                        .anyRequest().authenticated());

        return  http.build();
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception{
        return configuration.getAuthenticationManager();
    }
    @Bean
    public UserDetailsService userDetailsService(){
        UserDetails user = User.withUsername("user12")
                .password("{noop}1111")
                .roles("USER").build();
        return new InMemoryUserDetailsManager(user);
    }
}
```
- AuthenticationManager 빈 등록 
- formLogin 추가 X 
  - 추가하면 UsernamePasswordAuthenticationFilter 활성화 되어서 컨트롤러까지 못 들어온다. 
- CSRF 비활성화 

### LoginController
```java
@RestController
@RequiredArgsConstructor
public class LoginController {
    private final AuthenticationManager authenticationManager;
    private final HttpSessionSecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
    @PostMapping("/login")
    public Authentication login(@RequestBody LoginRequest login, HttpServletRequest request, HttpServletResponse response){
        // UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(login.getUsername(), login.getPassword());
        UsernamePasswordAuthenticationToken token
                = UsernamePasswordAuthenticationToken.unauthenticated(login.getUsername(), login.getPassword());

        Authentication authenticate = authenticationManager.authenticate(token); // 인증 이후 Authentication 반환
        SecurityContext securityContext = SecurityContextHolder.getContextHolderStrategy().createEmptyContext();
        securityContext.setAuthentication(authenticate); // 시큐리티 컨텍스트에 Authentication 객체 저장
        SecurityContextHolder.getContextHolderStrategy().setContext(securityContext); // 스레드 로컬에 시큐리티 컨텍스트 저장

        securityContextRepository.saveContext(securityContext, request, response); // 인증 영속을 위해 세션에 컨텍스트 저장
        return authenticate;
    }
}
```
- UsernamePasswordAuthenticationFilter 동작과 동일 
- 토큰을 만들어서 AuthenticationManager 전달 
- 코드에는 안 보이지만 AuthenticationManager DaoAuthenticationProvider 선택되어 인증 동작 
- 인증을 마치고 User 객체 반환 
- AuthenticationManager 해당 객체를 바탕으로 Authentication 반환 
- 빈 SecurityContext 생성 
- 시큐리티 컨텍스트에 Authentication 객체 저장
- 스레드 로컬에 저장 
- 세션에 저장 

### 테스트 

```http
POST http://localhost:8080/login
Content-Type: application/json

{
  "username": "user12",
  "password": "1111"
}
```