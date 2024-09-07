# AnonymousAuthenticationToken Null 

- https://docs.spring.io/spring-security/reference/servlet/authentication/anonymous.html

```java
@GetMapping("/")
public String method(Authentication authentication) {
	if (authentication instanceof AnonymousAuthenticationToken) {
		return "anonymous";
	} else {
		return "not anonymous";
	}
}
```
- 왜 다른 인증 Token 들은 Authentication 객체에 저장되서 들어가 있는데 `AnonymousAuthenticationToken`은 null 인지? 
- 스프링 공식 문서를 보면 `HttpServletRequest#getPrincipal매개 변수를 사용하여 해결하기 때문`
  이렇게 나와있다. 

HttpServletRequest.java 내용 
```text
/**
 * Returns a <code>java.security.Principal</code> object containing the name of the current authenticated user. If
 * the user has not been authenticated, the method returns <code>null</code>.
 *
 * @return a <code>java.security.Principal</code> containing the name of the user making this request;
 *             <code>null</code> if the user has not been authenticated
 */
java.security.Principal getUserPrincipal();
```
- 간단 해석: 현재 인증된 사용자의 이름을 포함하는 java.security.Principal 객체를 반환합니다. 
  사용자가 인증되지 않은 경우 메서드는 null 반환

