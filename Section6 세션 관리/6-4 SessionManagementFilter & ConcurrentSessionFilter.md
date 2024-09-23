# SessionManagementFilter / ConcurrentSessionFilter 

### SessionManagementFilter

- 요청이 시작된 이후 사용자가 인증되었는지 감지하고, 인증된 경우에는 세션 고정 보호 메커니즘을 활성화하거나 
  동시 다중 로그인을 확인하는 등 세션 관련 활동을 수행하기 위해 설정된 세션 인증 전략(SessionAuthenticationStrategy)을 
  호출하는 필터 클래스
- 스프링 시큐리티 6 이상에서는 `SessionManagementFilter`가 기본적으로 설정 되지 않으며 세션관리 API 를 
  설정을 통해 생성할 수 있다

### 세션 구성 요소

![13.png](Image%2F13.png)

### ConcurrentSessionFilter

- 각 요청에 대해 `SessionRegistry`에서 `SessionInformation`을 검색하고 세션이 만료로 표시되었는지 확인하고 
  만료로 표시된 경우 로그아웃 처리를 수행한다(세션 무효화)
- 각 요청에 대해 `SessionRegistry.refreshLastRequest(String)`를 호출하여 등록된 세션들이 항상 
  '마지막 업데이트' 날짜/시간을 가지도록 한다

### 흐름도

![14.png](Image%2F14.png)

### 시퀀스 다이어그램

![15.png](Image%2F15.png)

### 접속 사용자 정보 API 만들기

SessionRegistry
- 여러 사용자 세션을 관리하기 위한 인터페이스
- 주로 세션 정보를 유지하고 여러 클라이언트의 인증된 세션을 추적하는 데 사용
- 여러 사용자가 동일한 애플리케이션에 로그인할 때 각 세션을 관리할 수 있는 기능을 제공

```text
SessionRegistry의 주요 목적은 현재 애플리케이션에 로그인한 사용자의 세션을 관리하는 것
각 사용자의 세션을 추적하고, 그들의 세션 정보를 저장하여 필요할 때 조회하거나, 
강제로 세션을 만료시킬 수 있는 기능을 제공
스프링 시큐리티 세션 관리 클래스들은 SessionRegistry 활용하여 동시 세션 제어 / 세션 고정 보호 / 세션 만료 
여러가지 기능을 한다. 
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
                        .anyRequest().authenticated())
                .formLogin(Customizer.withDefaults())
                .sessionManagement(session -> session
                        .maximumSessions(2)                // 동일한 아이디 접속 2명까지 허용
                        .maxSessionsPreventsLogin(false)); // 기존 세션 만료 방식 사용 

        return  http.build();
    }
    @Bean
    public SessionRegistry sessionRegistry(){
        return new SessionRegistryImpl();
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
- `SessionRegistry` 빈으로 등록

SessionInfoService
```java
@Service
@RequiredArgsConstructor
public class SessionInfoService {

    private final SessionRegistry sessionRegistry;
    /**
     * getAllPrincipals(): List<Object> 현재 활성화된 사용자의 모든 사용자의 Principal 객체들
     * getAllSessions(): 사용자의 세션 정보들을 가져온다. 첫 번째 인자는 Principal 객체이고, 두 번째 인자는 만료된 세션을 포함할지 여부를 결정하는 boolean 값
     * 반환값: SessionInformation
     */
    public void sessionInfo() {
        for (Object principal : sessionRegistry.getAllPrincipals()) {
            List<SessionInformation> activeSessions = sessionRegistry.getAllSessions(principal, false);
            for (SessionInformation sessionInformation : activeSessions) {
                System.out.println("사용자: " + principal + " | 세션ID: " + sessionInformation.getSessionId() +
                        " | 최종 요청 시간: " + sessionInformation.getLastRequest());
            }
        }
    }
}
```

SessionInfoService - 스트림 활용 
```java
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionInfoService {
    private final SessionRegistry sessionRegistry;

    /**
     * getAllPrincipals(): List<Object> 현재 활성화된 사용자의 모든 사용자의 Principal 객체들
     * getAllSessions(): 사용자의 세션 정보들을 가져온다. 첫 번째 인자는 Principal 객체이고, 두 번째 인자는 만료된 세션을 포함할지 여부를 결정하는 boolean 값
     * 반환값: SessionInformation
     */
    public void sessionInfo(){
        sessionRegistry.getAllPrincipals().stream()
        .forEach(principal -> sessionRegistry.getAllSessions(principal, false)
        .stream()
        .forEach(sessionInformation ->
                log.info(
                    "사용자: " + principal +
                    " | 세션ID: " + sessionInformation.getSessionId() +
                    " | 최종 요청 시간: " + sessionInformation.getLastRequest())));

    }
}
```
- 만약 세션 허용 개수가 1개라면? getAllSessions() 결과는 1개 
- 지금은 2개 허용이어서 1개의 계정에서 2개의 세션값이 나온다. 

IndexController - /sessionInfo
```java 
@RestController
@RequiredArgsConstructor
public class IndexController {
    private final SessionInfoService sessionInfoService;

    @GetMapping("/sessionInfo")
    public void sessionInfo() {
        sessionInfoService.sessionInfo();
    }
}    
```

실행 결과
```text
사용자: org.springframework.security.core.userdetails.User [Username=user12, Password=[PROTECTED], Enabled=true, AccountNonExpired=true, CredentialsNonExpired=true, AccountNonLocked=true, Granted Authorities=[ROLE_USER]] | 세션ID: 817AA3F5328AFC4EAD2230746C2E8B44 | 최종 요청 시간: Mon Sep 23 17:30:22 KST 2024
사용자: org.springframework.security.core.userdetails.User [Username=user12, Password=[PROTECTED], Enabled=true, AccountNonExpired=true, CredentialsNonExpired=true, AccountNonLocked=true, Granted Authorities=[ROLE_USER]] | 세션ID: A0EF9BFCC0A707935F59CA0C8CA0B143 | 최종 요청 시간: Mon Sep 23 17:30:42 KST 2024
```