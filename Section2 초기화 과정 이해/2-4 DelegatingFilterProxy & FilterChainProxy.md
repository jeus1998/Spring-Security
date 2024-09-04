# DelegatingFilterProxy / FilterChainProxy

###  Filter

- 서블릿 필터는 웹 애플리케이션에서 클라이언트의 요청과 서버의 응답을 가공하거나 검사하는데 사용되는 구성 요소
- 서블릿 필터는 클라이언트의 요청이 서블릿에 도달하기 전이나 서블릿이 응답을 클라이언트에게 보내기 전에 특정 작업을 수행할 수 있다
- 서블릿 필터는 서블릿 컨테이너(WAS)에서 생성되고 실행되고 종료된다

![7.png](Image%2F7.png)

### DelegatingFilterProxy

- DelegatingFilterProxy 는 스프링에서 사용되는 특별한 서블릿 필터로, 서블릿 컨테이너와 스프링 애플리케이션 컨텍스트 간의 
  연결고리 역할을 하는 필터
- DelegatingFilterProxy 는 서블릿 필터의 기능을 수행하는 동시에 스프링의 의존성 주입 및 빈 관리 기능과 연동되도록 설계된 필터
  - 서블릿 필터에서는 스프링의 AOP / DI 기능을 사용 못한다. 
  - 그래서 스프링의 다양한 기능을 필터에서 사용하도록 DelegatingFilterProxy 필터를 생성 
- DelegatingFilterProxy 는 `springSecurityFilterChain` 이름으로 생성된 빈을 ApplicationContext 에서 찾아 요청을 위임
  - `springSecurityFilterChain` = FilterChainProxy
- 실제 보안 처리를 수행하지 않는다
  - 단순히 요청을 위임한다. 

![8.png](Image%2F8.png)


### FilterChainProxy

- `springSecurityFilterChain`의 이름으로 생성되는 필터 빈으로서 DelegatingFilterProxy 으로 부터 
   요청을 위임 받고 보안 처리 역할을 한다
- 내부적으로 하나 이상의 SecurityFilterChain 객체들을 가지고 있으며 요청 URL 정보를 기준으로 적절한 SecurityFilterChain 을 선택하여 
  필터들을 호출
- HttpSecurity 를 통해 API 추가 시 관련 필터들이 추가된다
- 사용자의 요청을 필터 순서대로 호출함으로 보안 기능을 동작시키고 필요 시 직접 필터를 생성해서 기존의 필터 전.후로 추가 가능하다

![9.png](Image%2F9.png)

![10.png](Image%2F10.png)