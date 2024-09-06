# 기본 인증 - httpBasic()

### HTTP Basic 인증

- HTTP 는 액세스 제어와 인증을 위한 프레임워크를 제공하며 가장 일반적인 인증 방식은 "Basic" 인증 방식
- RFC 7235 표준이며 인증 프로토콜은 HTTP 인증 헤더에 기술되어 있다

![5.png](Image%2F5.png)

1. 클라이언트는 인증정보 없이 서버로 접속을 시도
2. 서버가 클라이언트에게 인증요구를 보낼 때 401 Unauthorized 응답과 함께 WWW-Authenticate 헤더를 기술해서 realm(보안영역)과
   Basic 인증방법을 보낸다. 
3. 클라이언트가 서버로 접속할 때 Base64 로 username 과 password 를 인코딩하고 Authorization 헤더에 담아서 요청
4. 성공적으로 완료되면 정상적인 상태 코드를 반환

주의 사항 
- base-64 인코딩된 값은 디코딩이 가능하기 때문에 인증정보가 노출
- HTTP Basic 인증은 반드시 HTTPS 와 같이 TLS 기술과 함께 사용해야 한다.

### httpBasic() API

- `HttpBasicConfigurer` 설정 클래스를 통해 여러 API 들을 설정할 수 있다
- 내부적으로 `BasicAuthenticationFilter` 가 생성되어 기본 인증 방식의 인증 처리를 담당하게 된다

![6.png](Image%2F6.png)


### httpBasic() - default

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
  http
          .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
          .httpBasic(Customizer.withDefaults());

  return  http.build();
}
```
- 권한 없이 요청을 하면 401 상태 코드 반환과 WWW-Authenticate 헤더를 기술해서 realm(보안영역)과
  Basic 인증방법을 보낸다
- 이후 아이디와 비번을 제출하면 WWW-Authenticate 헤더에 base64로 인코딩하여 서버로 보낸다.
- 그럼 서버는 200 반환 

### httpBasic() - custom entry point 

CustomAuthenticationEntryPoint
```java
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setHeader("WWW-Authenticate", "Basic realm=security");
        response.sendError(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase());
    }
}
```
- commence() 메서드를 완성해야 한다. 

CustomAuthenticationEntryPoint - SecurityFilterChain 적용 
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
    http
            .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
            .httpBasic(basic -> basic.authenticationEntryPoint(new CustomAuthenticationEntryPoint()));

    return  http.build();
}
```
- 동작은 동일하다. 

AuthenticationFailureHandler VS AuthenticationEntryPoint
- `AuthenticationEntryPoint`: 인증 자체가 시도되지 않은 경우
  - 401 / UNAUTHORIZED
- `AuthenticationFailureHandler`: 인증 시도가 실패한 경우
  - 401 / UNAUTHORIZED