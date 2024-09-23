# 7-1 예외 처리 - exceptionHandling()

### 개요

- 예외 처리는 필터 체인 내에서 발생하는 예외를 의미하며 크게 인증예외(AuthenticationException)와 
  인가예외(AccessDeniedException)로 나눌 수 있다
- 예외를 처리하는 필터로서 `ExceptionTranslationFilter`가 사용 되며 사용자의 인증 및 인가 상태에 따라 
  로그인 재시도, 401, 403 코드 등으로 응답할 수 있다

### 예외 처리 유형

![1.png](Image%2F1.png)
- AccessDeniedException 발생 
  - 익명 사용자 O: 인증예외처리 실행 
  - 익명 사용자 X: AccessDeniedHandler 에게 위임

### exceptionHandling() API

![2.png](Image%2F2.png)
- `AuthenticationEntryPoint`는 인증 프로세스마다 기본적으로 제공되는 클래스들이 설정된다
  - `UsernamePasswordAuthenticationFilter` - `LoginUrlAuthenticationEntryPoint`
  - `BasicAuthenticationFilter` - `BasicAuthenticationEntryPoint`
  - 아무런 인증 프로세스가 설정 되지 않으면 기본적으로 `Http403ForbiddenEntryPoint` 사용
  - 사용자 정의 `AuthenticationEntryPoint` 구현이 가장 우선적으로 수행되며 이 때는 기본 로그인 페이지 생성이 무시
- `AccessDeniedHandler`는 기본적으로 `AccessDeniedHandlerImple` 클래스가 사용된다

### exceptionHandling() API TEST - authenticationEntryPoint

```java
@EnableWebSecurity(debug = false) // default: false
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login").permitAll()
                        .anyRequest().authenticated())
                .formLogin(Customizer.withDefaults())
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            System.out.println("exception: " + authException.getMessage());
                            response.sendRedirect("/login");
                        }) // 이렇게 커스텀한 entrypoint 사용하면 로그인 페이지가 자동 생성 X
                );

        return  http.build();
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
- 인증 없이 권한이 필요한 경로에 요청 
- 스레드 로컬에 emptyContext(시큐리티 컨텍스트) 저장 
- 세션에 requestCached 저장 
- 익명 클래스로 커스텀하게 만든 authenticationEntryPoint 호출 
  - `/login` 경로로 리다이렉트 

### exceptionHandling() API TEST - accessDeniedHandler


```java
@EnableWebSecurity(debug = false) // default: false
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login").permitAll()
                        .requestMatchers("/admin").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .formLogin(Customizer.withDefaults())
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            System.out.println("exception: " + accessDeniedException.getMessage());
                            response.sendRedirect("/denied");
                        })
                );

        return  http.build();
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
- 인증 이후 `/` 경로는 접근이 가능하지만 
- `/admin` 경로는 `ADMIN` 권한이 없기 때문에 인가 에러가 발생한다. 
- 커스텀한 accessDeniedHandler 호출 
- `/denied` 경로로 리다이렉트 