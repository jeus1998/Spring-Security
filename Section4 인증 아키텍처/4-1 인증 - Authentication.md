# 인증 – Authentication

### 시큐리티 인증 / 인가 흐름도

![1.png](Image%2F1.png)
- `AuthenticationFilter`에서 Authentication 객체(Token)을 만들어서 `AuthenticationManager`에게 전달 
- `AuthenticationManager`는 관리 역할을 하고 실제 인증은 `AuthenticationProvider`에게 위임 
- `AuthenticationProvider`는 `UserDetailService`를 통해서 사용자 정보 `UserDetails`를 가져온다.
- `UserDetails` 타입의 객체를 가져왔으면 `PasswordEncoder`를 통해 비빌먼호를 비교한다. 
- `AuthenticationProvider`는 비밀번호 매치까지 성공하면 Authentication 새로운 객체를 만든다.

### Authentication

- 인증은 특정 자원에 접근하려는 사람의 신원을 확인하는 방법을 의미한다
- 사용자 인증의 일반적인 방법은 사용자 이름과 비밀번호를 입력하게 하는 것으로서 인증이 수행되면 신원을 알고 권한 
  부여(인가 - Authorization)를 할 수 있다
- `Authentication`은 사용자의 인증 정보를 저장하는 토큰 개념의 객체로 활용되며 인증 이후 `SecurityContext`에 저장되어 
  전역적으로 참조가 가능하다

### 구조 

![2.png](Image%2F2.png)
- getPrincipal()
  - 인증 주체를 의미 
  - 인증 요청의 경우: 사용자 이름(아이디)을 가진다. 
  - 인증 후에는: UserDetails 타입의 객체 
- getCredentials()
  - 인증 주체가 올바른 것을 증멱하는 자격 증명 
  - 대부분 비밀번호를 의미 
- getAuthorities()
  - 인증 주체(principal)에게 부여된 권한(Authority)을 나타낸다 
- getDetails() 
  - 인증 요청에 대한 추가적인 세부 사항 저장. 
  - IP 주소, 인증서 일련 번호 등... 
- isAuthenticated()
  - 인증 상태 반환 
- setAuthenticated(boolean)
  - 인증 상태 설정 

### 인증 절차 흐름 

![3.png](Image%2F3.png)

