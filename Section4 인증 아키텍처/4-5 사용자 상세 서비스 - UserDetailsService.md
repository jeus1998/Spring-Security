# 사용자 상세 서비스 - UserDetailsService

### 시큐리티 인증 / 인가 흐름도

![19.png](Image%2F19.png)

### UserDetailsService

- `UserDetailsService`의 주요 기능은 사용자와 관련된 상세 데이터를 로드(from DB/Memory)하는 것이며 사용자의 
  신원, 권한, 자격 증명 등과 같은 정보를 포함할 수 있다
- 이 인터페이를 사용하는 클래스는 주로 `AuthenticationProvider`이며 사용자가 시스템에 존재하는지 여부와 
  사용자 데이터를 검색하고 인증 과정을 수행한다

![20.png](Image%2F20.png)

### UserDetailsService 흐름도

![21.png](Image%2F21.png)

### UserDetailsService 사용 방법

![22.png](Image%2F22.png)
- managerBuilder.userDetailsService(customUserDetailsService());
- http.userDetailsService(customUserDetailsService());
- 이런 방법들은 `@Bean` 등록을 하면 할 필요 없다.
- 하지만 `AuthenticationProvider`와 함께 커스트 마이징 할 경우 직접 주입해서 사용한다. 

### UserDetailsService - 커스텀 적용 

CustomUserDetailsService 
```java
public class CustomUserDetailsService implements  UserDetailsService {
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
        return User.withUsername("user").password("{noop}1111").roles("USER").build();
    }
}
```

SecurityConfig
```java
@EnableWebSecurity(debug = false) // default: false
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/logoutSuccess").permitAll()
                        .anyRequest().authenticated())
                .formLogin(Customizer.withDefaults());

        return  http.build();
    }
    @Bean
    public UserDetailsService userDetailsService(){
       return new CustomUserDetailsService();
    }
}
```
- `DaoAuthenticationProvider`에 자동적으로 `CustomUserDetailsService`가 있고
  사용자 정보를 loadUserByUsername(username)을 통해서 사용자 정보를 조회한다. 
- 실무에서는 보통 Provider 커스텀과 같이 UserDetailsService 또한 커스텀 하여 커스텀한 Provider 주입하여 사용

### UserDetailsService, AuthenticationProvider 커스텀 적용 

CustomUserDetailsService 
```java
public class CustomUserDetailsService implements  UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
        return User.withUsername("user").password("{noop}1111").roles("USER").build();
    }
}
```

CustomAuthenticationProvider
```java
@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsService userDetailsService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        String loginId = authentication.getName();
        String password = (String)authentication.getCredentials();

        //아이디 검증
        UserDetails user = userDetailsService.loadUserByUsername(loginId);
        if(user == null) throw new UsernameNotFoundException("UsernameNotFoundException");
        //비밀번호 검증

        return new UsernamePasswordAuthenticationToken
                (user.getUsername(),  user.getPassword(), user.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.isAssignableFrom(UsernamePasswordAuthenticationToken.class);
    }
}
```

SecurityConfig
```java
@EnableWebSecurity(debug = false) // default: false
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/logoutSuccess").permitAll()
                        .anyRequest().authenticated())
                .formLogin(Customizer.withDefaults());

        return  http.build();
    }
    @Bean
    public UserDetailsService userDetailsService(){
       return new CustomUserDetailsService();
    }
}
```



