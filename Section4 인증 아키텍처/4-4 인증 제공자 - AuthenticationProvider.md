# 인증 제공자 - AuthenticationProvider 

### 시큐리티 인증 / 인가 흐름도

![13.png](Image%2F13.png)

### AuthenticationProvider

- 사용자의 자격 증명을 확인하고 인증 과정을 관리하는 클래스로서 사용자가 시스템에 액세스하기 위해 
  제공한 정보(예: 아이디와 비밀번호)가 유효한지 검증하는 과정을 포함한다
- 다양한 유형의 인증 메커니즘을 지원할 수 있는데, 예를 들어 표준 사용자 이름과 비밀번호를 기반으로 한 인증, 
  토큰 기반 인증, 지문 인식 등을 처리할 수 있다
- 성공적인 인증 후에는 Authentication 객체를 반환하며 이 객체에는 사용자의 신원 정보와 (권한)인증된 자격 증명을 포함한다
- 인증 과정 중에 문제가 발생한 경우 AuthenticationException 과 같은 예외를 발생시켜 문제를 알리는 역할을 한다
- AuthenticationManager 부터 인증을 위임을 받아 실제 인증을 하는 곳 

![14.png](Image%2F14.png)

### AuthenticationProvider 흐름도

![15.png](Image%2F15.png)

### AuthenticationProvider 사용 방법 - 일반 객체로 생성

![16.png](Image%2F16.png)
- 일반 객체로 생성하는 방법은 2가지가 있다. 
  - managerBuilder 사용하는 방법 
    - AuthenticationManagerBuilder 가져오기: `AuthenticationManagerBuilder managerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class)`
    - AuthenticationProvider 등록: `managerBuilder.authenticationProvider(new CustomAuthenticationProvider());`
  - HttpSecurity 객체 활용 방법 
    - AuthenticationProvider 등록: `http.authenticationProvider(new CustomAuthenticationProvider2());`
- 2가지 방법 모두 ProviderManager(AuthenticationManager)의 `providers`(인증 provider 리스트)에 들어간다.
- 이렇게 2가지 `provider`를 추가하면 총 4가지 provider가 존재한다.
  - custom 2개
  - DaoAuthenticationProvider - 초기화 과정에서 추가 - parent(Authentication) `providers`에 추가 
  - AnonymousAuthenticationProvider - 초기화 과정에서 추가

### AuthenticationProvider 사용 방법 - 빈으로 생성 - 1. 빈을 한 개만 정의할 경우

![17.png](Image%2F17.png)
- `AuthenticationProvider`를 빈으로 정의하면 `CustomProvider`가 `DaoAuthenticationProvder`를 자동으로 대체하게 된다
- 그림에서 보면 parent(ProviderManager)의 `providers`리스트에 `CustomAuthenticationProvider`만 존재하는걸 확인할 수 있다.

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManagerBuilder builder, AuthenticationConfiguration configuration) 
throws Exception {
     AuthenticationManagerBuilder managerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
     managerBuilder.authenticationProvider(customAuthenticationProvider());
     ProviderManager providerManager = (ProviderManager)configuration.getAuthenticationManager();
     providerManager.getProviders().remove(0);
     builder.authenticationProvider(new DaoAuthenticationProvider());
     http.authorizeHttpRequests(auth -> auth.anyRequest().authenticated());
     return http.build();
}
```
- `http.getSharedObject(AuthenticationManagerBuilder.class);`
  - 해당 코드는 `HttpSecurity`를 통해서 `AuthenticationManagerBuilder`를 가져온다.
  - `managerBuilder.authenticationProvider(customAuthenticationProvider());`
    - 여기서 추가하는 `AuthenticationProvider`는 `ProviderManager`가 가지는 `parent Provider Manager`가 아니다
      그래서 그림에서 보면 `ProviderManager@6958` providers 리스트에 `CustomAuthenticationProvider`가 들어간다.
- `ProviderManager providerManager = (ProviderManager)configuration.getAuthenticationManager();`
  - 해당 코드는 빈으로 생성된 `AuthenticationConfiguration` 객체를 통해 `ProviderManager`를 가져온다.
  - 해당 `ProviderManager`는 parent `ProviderManager`이다.
- `providerManager.getProviders().remove(0);`
  - parent ProvierManager에 providers 안에 있는 빈으로 등록된 CustomAuthenticationProvider를 삭제한다.
- `builder.authenticationProvider(new DaoAuthenticationProvider());`
  - 해당 ProviderManager 또한 parent이다. 
  - DaoAuthenticationProvider 추가 
- 이런 과정을 통해 최종적으로 그림과 같이 provider가 존재하게 된다.
- 여기서 핵심은 빈으로 주입된 http 객체를 통한 `ProviderManager` / (builder, configuration)을 통해 가져온 `ProviderManager`(parent)가 
  어떤 `ProvierManager`를 의미하는 것인지에 대한 파악 이다. 

### AuthenticationProvider 사용 방법 - 빈으로 생성 - 2. 빈을 두 개 이상 정의 할 경우

![18.png](Image%2F18.png)


