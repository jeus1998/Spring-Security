# 인증 관리자 - AuthenticationManager 

### 시큐리티 인증 / 인가 흐름도

![8.png](Image%2F8.png)

### AuthenticationManager

- 인증 필터로부터 `Authentication` 객체를 전달 받아 인증을 시도하며(Provider 사용) 인증에 성공할 경우 사용자 정보,
  권한 등을 포함한 완전히 채워진 `Authentication` 객체를 반환 
- `AuthenticationManager`는 여러 `AuthenticationProvider`들을 관리하며 `AuthenticationProvider` 목록을 순회하며 
  인증 요청을 처리한다. 
- `AuthenticationProvider` 목록 중에서 인증 처리 요건에 맞는 적절한 `AuthenticationProvider`를 찾아 인증 처리를 위임
- `AuthenticationManagerBuilder`에 의해 객체가 생성되며 주로 사용하는 구현체로 `ProviderManager`가 제공된다 

### AuthenticationManagerBuilder

![9.png](Image%2F9.png)
- `AuthenticationManager` 객체를 생성 `UserDetailsService` 및 `AuthenticationProvider`를 추가할 수 있다.
- HttpSecurity.getSharedObject(AuthenticationManagerBuilder.class)를 통해 객체를 참조할 수 있다

### AuthenticationManager 흐름도

![10.png](Image%2F10.png)
- 선택적으로 부모 `AuthenticationManager`를 구성할 수 있으며 이 부모는 `AuthenticationProvider`가 인증을 수행할 수 없는 
  경우에 추가적으로 탐색할 수 있다
  - 해당 `AuthenticationManger`가 가진 `AuthenticationProvider`가 인증을 수행할 수 없는 경우에 부모가 
    가진 `AuthenticationProvider`를 추가적으로 탐색 
- 일반적으로 `AuthenticationProvider`로 부터 null 이 아닌 응답을 받을 때 까지 차례대로 시도하며 
  응답을 받지 못하면 `ProviderNotFoundException`과 함께 인증이 실패
  - 즉 성공하면 `Authentication` 인증 객체가 넘어와야 하는데 넘어오지 않으면 `ProviderNotFoundException` 예외

### AuthenticationManager 사용 방법 - HttpSecurity 사용

![11.png](Image%2F11.png)

### AuthenticationManager 사용 방법 - 직접 생성 

![12.png](Image%2F12.png)



