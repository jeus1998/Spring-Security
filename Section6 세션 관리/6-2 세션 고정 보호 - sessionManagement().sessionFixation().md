# 세션 고정 보호 - sessionManagement().sessionFixation()

### 세션 고정 보호 전략

- 세션 고정 공격은 악의적인 공격자가 사이트에 접근하여 세션을 생성한 다음 다른 사용자가 같은 세션으로 
  로그인하도록 유도하는 위험을 말한다
- 스프링 시큐리티는 사용자가 로그인할 때 새로운 세션을 생성하거나 세션 ID를 변경함으로써 이러한 공격에 자동으로 대응한다

### 세션 고정 공격

![4.png](Image%2F4.png)

메커니즘 정리
- 세션 고정 보호 전략 사용 X 가정 
  - 사용자가 로그인할 때 새로운 세션 OR 세션 ID 변경 작업 X
- 공격자는 세션 ID를 일반 사용자(공격 대상)에게 세션 ID를 쿠키에 저장 
- 일반 사용자는 이런 사실을 모르고 사이트에 인증 시도 
- 서버는 기존 세션 ID는 유지하고 시큐리티 컨텍스트(인증 정보)만 일반 사용자로 갱신 
- 공격자는 자신이 발급받은 세션 ID 통해서 일반 사용자의 인증을 가지고 활동이 가능

방어 매커니즘 - 세션 고정 보호
- 공격자가 사용자에게 세션 ID를 쿠키에 저장했지만 새로운 로그인 시도를 하면 서버는 새롭게 세션 ID를 생성해서 반환 
- 세션 ID가 바뀌었기 때문에 공격자는 자신이 보유한 세션 ID를 통해서 사용자의 인증 정보로 접근 불가능 

### sessionManagement() API - 세션 고정 보호

![5.png](Image%2F5.png)

- changeSessionId()
  - 기존 세션을 유지하면서 세션 ID만 변경하여 인증 과정에서 세션 고정 공격을 방지하는 방식
  - 기본 값으로 설정
- newSession()
  - 새로운 세션을 생성하고 기존 세션 데이터를 복사하지 않는 방식이다
  - (SPRING_SECURITY_ 로 시작하는 속성은 복사한다)
- migrateSession()
  - 새로운 세션을 생성하고 모든 기존 세션 속성을 새 세션으로 복사
- none()
  - 기존 세션을 그대로 사용

## sessionManagement() API - 테스트 

### sessionFixation.none() 테스트 

```java
@EnableWebSecurity(debug = false) // default: false
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated())
                .formLogin(Customizer.withDefaults())
                .sessionManagement(session -> session
                        .sessionFixation(sessionFixation -> sessionFixation
                                .none()));

        return http.build();
    }
}    
```

![6.png](Image%2F6.png)
- 공격자의 쿠키

![7.png](Image%2F7.png)
- 사용자의 쿠키
- 공격자와 동일한 세션 ID

![8.png](Image%2F8.png)
- 사용자가 인증에 성공하고 `/` 경로로 리다이렉트 결과 
- 세션 ID가 그대로 
- `.sessionManagement(session -> session .sessionFixation(sessionFixation -> sessionFixation .none()));`

![9.png](Image%2F9.png)
- 공격자는 별도의 인증 없이 사용자의 세션 ID 통해서 `/` 경로 접근 

### sessionFixation.changeSessionId() 테스트 

```java
@EnableWebSecurity(debug = false) // default: false
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated())
                .formLogin(Customizer.withDefaults())
                .sessionManagement(session -> session
                        .sessionFixation(sessionFixation -> sessionFixation
                                .changeSessionId())); // default value 

        return http.build();
    }
}    
```
- none() ->  .changeSessionId() 변경
- 해당 값은 시큐리티 default value 
- 기능: 로그인을 하면 세션 ID를 변경 

![10.png](Image%2F10.png)
- 이전 테스트와 동일하게 공격자의 쿠키 사용자에게 전달 

![11.png](Image%2F11.png)
- 사용자가 로그인을 하자 세션 ID 변경 
- 공격자는 본래의 세션 ID로 권한이 없는 경로로 요청을 시도 하지만 인증 페이지(/login)로 리다이렉트 
- 방어 성공 




