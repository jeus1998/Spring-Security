# 인증 컨텍스트 - SecurityContext&SecurityContextHolder

### 시큐리티 인증 / 인가 흐름도

![4.png](Image%2F4.png)

### SecurityContext

- `Authentication` 저장: 현재 인증된 사용자의 `Authentication` 객체를 저장 
- `ThreadLocal`저장소 사용: `SecurityContextHolder`를 통해 접근되며 `ThreadLocal`저장소를 사용해 
  각 스레드 자신만의 보안 컨텍스트를 유지한다.
- 애플리케이션 전반에 걸친 접근성: 애플리케이션 어느 곳에서나 접근 가능하며 현재 사용자의 인증 상태나 권한을 확인하는 데 사용된다 

### SecurityContextHolder

- `SecurityContext`저장: 현재 인증된 사용자의 `Authentication` 객체를 담고 있는 `SecurityContext` 객체를 저장한다
- 전략 패턴 사용: 다양한 저장 전략을 지원하기 위해 `SecurityContextHolderStrategy` 인터페이스를 사용한다 
- 기본 전략: `MODE_THREADLOCAL` - 스레드 로컬 저장소에 저장 
- 전략 모드 직접 지정: `SecurityContextHolder.setStrategyName(String)`

### SecurityContextHolder 저장 모드(전략)

- `MODE_THREADLOCAL`: 기본 모드, 각 스레드가 독립적으로 보안 컨텍스트를 가진다. 대부분의 서버 환경에 적합 
- `MODE_INHERITABLETHREADLOCAL`: 부모 스레드로부터 자식 스레드로 보안 컨텍스트가 상속되며 작업을 스레드 간 
  분산 실행하는 경우 유용 할 수 있다. 
- `MODE_GLOBAL`: 전역적으로 단일 보안 컨텍스트를 사용하여 서버환경에서는 부적합하며 주로 간단한 애플레케이션에 적합하다 
  - 나만 사용하는 애플리케이션 

### 구조 

![5.png](Image%2F5.png)

### SecurityContext 참조 및 삭제 

- `SecurityContext` 참조: `SecurityContextHolder.getContextHolderStrategy().getContext()`
- `SecurityContext` 삭제: `SecurityContextHolder.getContextHolderStrategy().clearContext()`

### SecurityContextHolder & SecurityContext 구조 

![6.png](Image%2F6.png)
- 스레드 마다 할당 되는 전용 저장소에 `SecurityContext`를 저장하기 때문에 동시성의 문제가 없다
- 스레드 풀에서 운용되는 스레드일 경우 새로운 요청이더라도 기존의 `ThreadLocal`이 재사용될 수 있기 때문에 클라이언트로 
  응답 직전에 항상 `SecurityContext`를 삭제 해 주고 있다

### SecurityContextHolderStrategy 사용하기

![7.png](Image%2F7.png)


