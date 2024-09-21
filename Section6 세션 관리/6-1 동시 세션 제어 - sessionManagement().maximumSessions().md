# 동시 세션 제어 - sessionManagement().maximumSessions()

### 개요

- 동시 세션 제어는 동일한 사용자가 동시에 여러 세션을 생성하는 것을 관리하는 전략이다
- 이 전략은 사용자의 인증 후에 활성화된 세션의 수가 설정된 maximumSessions 값과 비교하여 제어 여부를 결정한다

간단 정리 
```text
user 계정을 가진 사용자가 컴퓨터 / 노트북 / 핸드폰 총 3개의 독립적인 클라이언트로 애플리케이션에 접근한다.
이러면 계정은 user 1개인데 세션이 총 3개가 생성된다.
이때 발생하는 이슈가 동시 세션 제어이다.
```

### 동시 세션 제어 2가지 유형

![1.png](Image%2F1.png)

### sessionManagement() API - 동시 세션 제어

![2.png](Image%2F2.png)

### 세션 만료 후 리다이렉션 전략

![3.png](Image%2F3.png)

### 테스트 

SecurityConfig
```java
@EnableWebSecurity(debug = false) // default: false
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login").permitAll()
                        .anyRequest().authenticated())
                .formLogin(Customizer.withDefaults())
                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)); // default: false 인증 차단이 아닌 만료 시키는 전략

        return http.build();
    }
}    
```
- 인증 만료 전략 
- 결과: This session has been expired (possibly due to multiple concurrent logins being attempted as the same user).

SecurityConfig
```java
@EnableWebSecurity(debug = false) // default: false
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login").permitAll()
                        .anyRequest().authenticated())
                .formLogin(Customizer.withDefaults())
                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(true)); // default: false 인증 차단이 아닌 만료 시키는 전략

        return http.build();
    }
}    
```
- 인증 차단 전략
- 결과: 인증 자체가 불가능하다. 

```java
@EnableWebSecurity(debug = false) // default: false
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/invalidSessionUrl", "/expiredUrl").permitAll()
                        .anyRequest().authenticated())
                .formLogin(Customizer.withDefaults())
                .sessionManagement(session -> session
                        .invalidSessionUrl("/invalidSessionUrl")
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false) // default: false 인증 차단이 아닌 만료 시키는 전략
                        .expiredUrl("/expiredUrl")
                );

        return http.build();
    }
}    
```
- 인증 만료 전략 
- expiredUrl("/expiredUrl"), invalidSessionUrl("/invalidSessionUrl")
- 결과: /invalidSessionUrl

