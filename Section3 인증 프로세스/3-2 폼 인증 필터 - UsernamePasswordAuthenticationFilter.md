# 폼 인증 필터 - UsernamePasswordAuthenticationFilter

### 개요, 구조 

- 스프링 시큐리티는 `AbstractAuthenticationProcessingFilter` 클래스를 사용자의 자격 증명을 인증하는 기본 필터로 사용 한다
- `UsernamePasswordAuthenticationFilter`는 `AbstractAuthenticationProcessingFilter`를 확장한 클래스
- `HttpServletRequest`에서 제출된 사용자 이름과 비밀번호로부터 인증을 수행
- 인증 프로세스가 초기화 될 때 로그인 페이지와 로그아웃 페이지 생성을 위한 `DefaultLoginPageGeneratingFilter` 및 
  `DefaultLogoutPageGeneratingFilter`가 초기화 된다

![3.png](Image%2F3.png)
- `attemptAuthentication()`: 커스텀하게 구현하라면 해당 메서드를 구현해야함 

### 흐름도 

![4.png](Image%2F4.png)