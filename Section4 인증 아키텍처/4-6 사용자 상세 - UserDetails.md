# 사용자 상세 - UserDetails

### 시큐리티 인증 / 인가 흐름도

![23.png](Image%2F23.png)

### UserDetails

- 사용자의 기본 정보를 저장하는 인터페이스로서 Spring Security 에서 사용 하는 사용자 타입이다
- 저장된 사용자 정보는 추후에 인증 절차에서 사용되기 위해 Authentication 객체에 포함되며 구현체로서 User 클래스가 제공된다
  - SecurityContextHolder/SecurityContextHolderStrategy/SecurityContext/Authentication/UserDetails
  
![24.png](Image%2F24.png)
- 서비스 인증 정책에 따라 설정이 다르다 

### UserDetails 흐름도 

![25.png](Image%2F25.png)

### UserDetails - 커스텀 

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

CustomUserDetailsService 
```java
public class CustomUserDetailsService implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AccountDto accountDto = new AccountDto("user", "{noop}1111", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        return new CustomUserDetails(accountDto);
    }
}
```

AccountDto
```java
@Getter
@AllArgsConstructor
public class AccountDto {
    private String username;
    private String password;
    private Collection<GrantedAuthority> authorities;
}
```

CustomUserDetails
```java
public class CustomUserDetails implements UserDetails {

    private final AccountDto accountDto;

    public CustomUserDetails(AccountDto accountDto){
        this.accountDto = accountDto;
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return accountDto.getAuthorities();
    }
    @Override
    public String getPassword() {
        return accountDto.getPassword();
    }
    @Override
    public String getUsername() {
        return accountDto.getUsername();
    }
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    @Override
    public boolean isEnabled() {
        return true;
    }
}
```

SecurityConfig
```java
@EnableWebSecurity
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated())
                .formLogin(Customizer.withDefaults());
        return http.build();
    }
    @Bean
    public UserDetailsService customUserDetailsService(){
        return  new CustomUserDetailsService();
    }
}
```

### 인증 프로세스 정리

```text
인증 Provider -> UserDetailsService loadUserByUsername() -> DB 조회 이후 UserDetails 객체 생성/반환 
-> 
인증 Provider 받은 UserDetails 객체 통해 아이디,비밀번호 검증 통해서 
Authentication 객체 생성하여 ProviderManager 에게 반환 
-> 
ProviderManager -> AuthenticationFilter -> SecurityContext 안에 Authentication 저장 ->
SecurityContext 스레드 로컬에 저장 
```



