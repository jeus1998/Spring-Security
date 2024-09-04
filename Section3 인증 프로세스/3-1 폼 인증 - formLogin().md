# 폼 인증 - formLogin()

### 폼 인증

- 기본적으로 스프링 시큐리티가 제공하는 기본 로그인 페이지를 사용하며 사용자 이름과 비밀번호 필드가 포함된 간단한 로그인 양식을 제공한다
- HTTP 기반의 폼 로그인 인증 메커니즘을 활성화하는 API 로서 사용자 인증을 위한 사용자 정의 로그인 페이지를 쉽게 구현할 수 있다
- 사용자는 웹 폼을 통해 자격 증명(사용자 이름과 비밀번호)을 제공하고 `Spring Security`는 `HttpServletRequest`에서 이 값을 읽어 온다

![1.png](Image%2F1.png)
- 권한❌ 흐름
  - client ➡️ 권한이 필요한 API 호출 ➡️ 권한 검사 필터(AuthorizationFilter) ➡️ 접근 예외 발생(AccessDeniedException)
    ➡️ 예외 처리 필터(ExceptionTranslationFilter) ➡️ 인증 시작(AuthenticationEntryPoint) ➡️ 로그인 페이지(리다이렉트)
- 권한⭕️ 흐름
  - client ➡️ 권한이 필요한 API 호출 ➡️ 권한 검사 필터(AuthorizationFilter) ➡️ 디스패처 서블릿 ➡️ API 정상 응답

### formLogin() API

- `FormLoginConfigurer` 설정 클래스를 통해 여러 API 들을 설정할 수 있다
- 내부적으로 `UsernamePasswordAuthenticationFilter` 가 생성되어 폼 방식의 인증 처리를 담당하게 된다

![2.png](Image%2F2.png)
- `loginPage("/loginPage")` 
  - 사용자 정의 로그인 페이지로 전환, 기본 로그인 페이지 무시 
  - 기본 경로: [GET] /login
- `loginProcessingUrl("/loginProc")`
  - 사용자의 이름과 비밀번호를 검증할 URL 지정(Form action)
  - 기본 경로: [POST] /login 
  - `<form action="/loginProc" method="post">`
- `defaultSuccessUrl("/",[alwaysUse])`
  - 로그인 성공 이후 이동 페이지
  - `alwaysUse true`: 무조건 지정된 위치로 이동 (default: false)
  - `alwaysUse false`: 인증 전에 보안이 필요한 페이지를 방문하다가 인증에 성공한 경우이면 이전 위치로 리다이렉트
- `failureUrl("/failed")`
  - 인증에 실패할 경우 사용자에게 보내질 URL을 지정
  - 기본 경로: /login?error
- `usernameParameter("username")` & `passwordParameter("password")`
  - 인증을 수행할 때 사용자 이름(아이디)을 찾기 위해 확인하는 HTTP 매개변수 설정,기본값은 `username`
  - `<input type="text" id="username" name="username" required>`
  - 인증을 수행할 때 비밀번호를 찾기 위해 확인하는 HTTP 매개변수 설정,기본값은 `password`
  - `<input type="password" id="password" name="password" required>`
- `failureHandler(AuthenticationFailureHandler)`
  - 인증 실패 시 사용할 `AuthenticationFailureHandler`를 지정
  - 기본값은 `SimpleUrlAuthenticationFailureHandler`를 사용하여 `"/login?error"`로 리다이렉션 함
- `successHandler(AuthenticationSuccessHandler)`
  - 인증 성공 시 사용할 `AuthenticationSuccessHandler`를 지정
  - 기본값은 `SavedRequestAwareAuthenticationSuccessHandler`
- `permitAll()`
  - failureUrl(),loginPage(),loginProcessingUrl()에 대한 URL에 모든 사용자의 접근을 허용 함

### formLogin() API 구현 

```java
@EnableWebSecurity(debug = false) // default: false
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/loginPage")
                        .loginProcessingUrl("/loginProc")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/failed")
                        .usernameParameter("userId")
                        .passwordParameter("passwd")
                        .successHandler((request, response, authentication) -> {
                            System.out.println("authentication = " + authentication);
                            response.sendRedirect("/home");
                        })
                        .failureHandler((request, response, exception) -> {
                            System.out.println("exception = " + exception.getMessage());
                            response.sendRedirect("/login");
                        })
                        .permitAll()
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
- loginPage("/loginPage"), usernameParameter("userId"), passwordParameter("passwd") 
  - 이런 값들 커스텀 하게 설정했다면 실제 내가 구현할 `view`에도 똑같이 적용을 해주어야 한다. 
- defaultSuccessUrl("/", true) vs successHandler 우선순위 
  - 결국 `defaultSuccessUrl`도 내부에서 `SavedRequestAwareAuthenticationSuccessHandler`를 사용한다. 
  - 내가 `successHanlder`를 커스텀하게 구현하면 해당 동작이 우선이다. 
  - 내가 만약 formLogin() API를 사용한다면 `defaultSuccessUrl`을 사용하고 `alwaysUse` 옵션을 `false`로 주어서 
    인증에 성공하면 처음 인증이 필요했던 페이지로 돌아가게 하는 스프링 시큐리티의 기본 설정을 사용할듯하다.
  - 해당 우선순위는 `failureUrl` vs `failureHandler` 또한 동일하게 적용 

설정 정리 
- AbstractAuthenticationFilterConfigurer - FormLoginConfigurer
- formLogin() 커스텀을 하게되면 `HttpSecurity` 객체는 `FormLoginConfigurer`를 통해서 필요한 작업(설정)을 하게된다.
- `super(new UsernamePasswordAuthenticationFilter(), null);`
  - 해당 코드는 FormLoginConfigurer 생성자이다. 
  - 폼 로그인은 `UsernamePasswordAuthenticationFilter` 필터를 사용해서 인증을 하기 때문에 
    해당 필터를 생성해서 넣어주는 모습 

