# SecurityBuilder / SecurityConfigurer

### SecurityBuilder & SecurityConfigurer

- `SecurityBuilder`는 빌더 클래스로서 웹 보안을 구성하는 빈 객체와 설정클래스들을 생성하는 역할을 하며 
  대표적으로 `WebSecurity`, `HttpSecurity`가 있다
- `SecurityConfigurer`는 Http 요청과 관련된 보안처리를 담당하는 필터들을 생성하고 여러 초기화 설정에 관여한다
  - 스프링 시큐리티는 서블릿 필터를 바탕으로 하는 보안 프레임워크 
- `SecurityBuilder`는 `SecurityConfigurer`를 참조하고 있으며 인증 및 인가 초기화 작업은 
  `SecurityConfigurer`에 의해 진행

![1.png](Image%2F1.png)

![2.png](Image%2F2.png)
- (1) AutoConfiguration(자동 설정) 실행 
- (2) 빌더 클래스(SecurityBuilder) 생성
  - WebSecurity, HttpSecurity
- 설정 클래스(SecurityConfigurer) 생성
- (3) 초기화 작업 진행
  - init(B builder)
  - configure(B builder)

![3.png](Image%2F3.png)