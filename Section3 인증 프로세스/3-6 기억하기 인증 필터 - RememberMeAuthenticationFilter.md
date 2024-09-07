# 기억하기 인증 필터 - RememberMeAuthenticationFilter

### RememberMeAuthenticationFilter

- `SecurityContextHolder`에 `Authentication`이 포함되지 않은 경우 실행되는 필터이다
   - 즉 인증 상태가 아닌 경우 
- 세션이 만료되었거나 어플리케이션 종료로 인해 인증 상태가 소멸된 경우 토큰 기반 인증을 사용해 유효성을 검사하고 토큰이 검증되면 
  자동 로그인 처리를 수행

![9.png](Image%2F9.png)
- 로그인 성공 시 `RememberMeAuthenticationToken` 발급은  `RememberMeAuthenticationFilter`에서 하는게 아닌 
  `UsernamePasswordAuthenticationFilter`에서 한다.
- `RememberMeAuthenticationFilter`는 인증 상태가 아닌 경우(Authentication == null) `RememberMeAuthenticationToken` 바탕으로 
  인증을 하는 것이다. 






