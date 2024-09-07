# 익명 인증 사용자 - anonymous()

### 익명 사용자

- 스프링 시큐리티에서 "익명으로 인증된" 사용자와 인증되지 않은 사용자 간에 실제 개념적 차이는 없으며 단지 액세스 제어 속성을 구성하는 
  더 편리한 방법을 제공한다고 볼 수 있다
- 익명으로 인증된 = 인증되지 않은 사용자 ➡️ 객체화를 통해 더 편하게 관리 
- `SecurityContextHolder`가 항상 `Authentication`객체를 포함하고 `null`을 포함하지 않는다는 것을 규칙을 세우게 되면 
  클래스를 더 견고하게 작성할 수 있다
- 인증 사용자와 익명 인증 사용자를 구분해서 어떤 기능을 수행하고자 할 때 유용할 수 있으며 익명 인증 객체를 세션에 저장하지 않는다
- 익명 인증 사용자의 권한을 별도로 운용할 수 있다. 즉 인증 된 사용자가 접근할 수 없도록 구성이 가능하다
  - 인증 된 사용자만 / 인증되지 않은 사용자만 / 어떤 사용자든 ➡️ 이렇게 권한을 별도로 운용 가능 

### 익명 사용자 API 및 구조

![10.png](Image%2F10.png)

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
    http
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/anonymous").hasRole("GUEST")
                    .anyRequest().authenticated())
            .formLogin(Customizer.withDefaults())
            .anonymous(anonymous -> anonymous
                    .principal("guest")
                    .authorities("ROLE_GUEST"));

    return  http.build();
}
```
- `.requestMatchers("/anonymous").hasRole("GUEST")`: 익명 사용자(로그인 X) 사용자만 접근 가능 
- 로그인을 한 즉 인증 받은 사용자가 접근하면 403 에러가 발생한다. 
- 이렇게 권한을 별도로 운용할 수 있다

### 스프링 MVC 에서 익명 인증 사용하기

![11.png](Image%2F11.png)


### AnonymousAuthenticationFilter

- `SecurityContextHolder`에 `Authentication` 객체가 없을 경우 감지하고 필요한 경우 새로운 `Authentication` 객체로 채운다

![12.png](Image%2F12.png)

