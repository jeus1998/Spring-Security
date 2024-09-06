# 기본 인증 필터 - BasicAuthenticationFilter

### BasicAuthenticationFilter

- 이 필터는 기본 인증 서비스(http Basic)를 제공하는 데 사용된다
- `BasicAuthenticationConverter`를 사용해서 요청 헤더에 기술된 인증정보의 유효성을 체크하며 `Base64` 인코딩된 
  `username`과 `password`를 추출
  - 유효성 검증: 헤더: Authorization / value: Basic prefix 
- 인증 이후 세션을 사용하는 경우와 사용하지 않는 경우에 따라 처리되는 흐름에 차이가 있다. 
- 세션을 사용하는 경우 매 요청 마다 인증과정을 거치지 않으나 세션을 사용하지 않는 경우 매 요청마다 인증과정을 거쳐야 한다

![7.png](Image%2F7.png)

