# 예외 필터 - ExceptionTranslationFilter

### 예외처리 흐름도

![3.png](Image%2F3.png)
- AuthorizationFilter 인가 필터 FilterChainProxy 에서 제일 마지막 필터 
- AuthorizationManager 통해서 인증/인가 문제가 있으면 AccessDeniedException 예외를 던진다. 
- 해당 예외는 `ExceptionTranslationFilter`가 받아서 처리한다.
- 먼저 현재 사용자가 익명 사용자(Anonymous)인지 아이디 비밀번호를 사용하지 않고 RememberMe 통한 접근인지 체크 
  - 만약 하나라도 속하면 AuthenticationException 처리를한다. 
    - 인증 실패 이후 처리 (AuthenticationEntryPoint): 리다이렉트 
    - SecurityContext(null) 인증 객체 null 처리 
    - 세션에 사용자의 요청관련 정보 저장 (인증 이후 이전 요청 결과 반환을 위해)
  - 아니라면 권한이 부족한거니 그대로 AccessDeniedHandler 에게 인가 예외 처리를 위임 

