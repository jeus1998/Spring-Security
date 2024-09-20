# 인증 관리자 - AuthenticationManager 

### 시큐리티 인증 / 인가 흐름도

![8.png](Image%2F8.png)

### AuthenticationManager

- 인증 필터로부터 `Authentication` 객체를 전달 받아 인증을 시도하며(Provider 사용) 인증에 성공할 경우 사용자 정보,
  권한 등을 포함한 완전히 채워진 `Authentication` 객체를 반환 
- `AuthenticationManager`는 여러 `AuthenticationProvider`들을 관리하며 `AuthenticationProvider` 목록을 순회하며 
  인증 요청을 처리한다. 
- `AuthenticationProvider` 목록 중에서 인증 처리 요건에 맞는 적절한 `AuthenticationProvider`를 찾아 인증 처리를 위임
- `AuthenticationManagerBuilder`에 의해 객체가 생성되며 주로 사용하는 구현체로 `ProviderManager`가 제공된다 

### AuthenticationManagerBuilder

![9.png](Image%2F9.png)
- `AuthenticationManager` 객체를 생성 `UserDetailsService` 및 `AuthenticationProvider`를 추가할 수 있다.
- HttpSecurity.getSharedObject(AuthenticationManagerBuilder.class)를 통해 객체를 참조할 수 있다

### AuthenticationManager 흐름도

![10.png](Image%2F10.png)
- 선택적으로 부모 `AuthenticationManager`를 구성할 수 있으며 이 부모는 `AuthenticationProvider`가 인증을 수행할 수 없는 
  경우에 추가적으로 탐색할 수 있다
  - 해당 `AuthenticationManger`가 가진 `AuthenticationProvider`가 인증을 수행할 수 없는 경우에 부모가 
    가진 `AuthenticationProvider`를 추가적으로 탐색 
- 일반적으로 `AuthenticationProvider`로 부터 null 이 아닌 응답을 받을 때 까지 차례대로 시도하며 
  응답을 받지 못하면 `ProviderNotFoundException`과 함께 인증이 실패
  - 즉 성공하면 `Authentication` 인증 객체가 넘어와야 하는데 넘어오지 않으면 `ProviderNotFoundException` 예외

### AuthenticationManager 사용 방법 - HttpSecurity 사용

![11.png](Image%2F11.png)

### AuthenticationManager 사용 방법 - 직접 생성 

![12.png](Image%2F12.png)

### AuthenticationManager - Interface

```java
public interface AuthenticationManager {
	Authentication authenticate(Authentication authentication) throws AuthenticationException;
}
```
- 전달된 Authentication 객체를 인증하려고 시도하고, 성공하면 완전히 채워진 Authentication 객체(부여된 권한 포함)를 반환
- Authentication 객체는 인증 필터로부터 생성되어 전달 받는다. 

### ProviderManager - AuthenticationManager 구현체 - Class

```java
public class ProviderManager implements AuthenticationManager, MessageSourceAware, InitializingBean {
    private List<AuthenticationProvider> providers = Collections.emptyList();
    
    private AuthenticationManager parent;
    
    @Override
  	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
  		Class<? extends Authentication> toTest = authentication.getClass();
  		AuthenticationException lastException = null;
  		AuthenticationException parentException = null;
  		Authentication result = null;
  		Authentication parentResult = null;
  		int currentPosition = 0;
  		int size = this.providers.size();
        // for문을 돌면서 해당 provider가 인증을 수행할 수 있는지 판단   
  		for (AuthenticationProvider provider : getProviders()) {
  			if (!provider.supports(toTest)) {
  				continue;
  			}
  			if (logger.isTraceEnabled()) {
  				logger.trace(LogMessage.format("Authenticating request with %s (%d/%d)",
  						provider.getClass().getSimpleName(), ++currentPosition, size));
  			}
  			try {
                // provider에게 인증을 위임 
                // result 결과가 null이 아닌 Authentication 객체라면 for문 break  
  				result = provider.authenticate(authentication);
  				if (result != null) {
  					copyDetails(authentication, result);
  					break;
  				}
  			}
  			catch (AccountStatusException | InternalAuthenticationServiceException ex) {
  				prepareException(ex, authentication);
  				// SEC-546: Avoid polling additional providers if auth failure is due to
  				// invalid account status
  				throw ex;
  			}
  			catch (AuthenticationException ex) {
  				lastException = ex;
  			}
  		}
        // 현재 매니저가 가지는 providers에서 result null = 인증 수행 불가 & 부모 manager를 가지면 부모의 authenticate 호출   
  		if (result == null && this.parent != null) {
  			// Allow the parent to try.
  			try {
  				parentResult = this.parent.authenticate(authentication);
  				result = parentResult;
  			}
  			catch (ProviderNotFoundException ex) {
  				// ignore as we will throw below if no other exception occurred prior to
  				// calling parent and the parent
  				// may throw ProviderNotFound even though a provider in the child already
  				// handled the request
  			}
  			catch (AuthenticationException ex) {
  				parentException = ex;
  				lastException = ex;
  			}
  		}
  		if (result != null) {
  			if (this.eraseCredentialsAfterAuthentication && (result instanceof CredentialsContainer)) {
  				// Authentication is complete. Remove credentials and other secret data
  				// from authentication
  				((CredentialsContainer) result).eraseCredentials();
  			}
  			// If the parent AuthenticationManager was attempted and successful then it
  			// will publish an AuthenticationSuccessEvent
  			// This check prevents a duplicate AuthenticationSuccessEvent if the parent
  			// AuthenticationManager already published it
  			if (parentResult == null) {
  				this.eventPublisher.publishAuthenticationSuccess(result);
  			}
  
  			return result;
  		}
  
  		// Parent was null, or didn't authenticate (or throw an exception).
  		if (lastException == null) {
  			lastException = new ProviderNotFoundException(this.messages.getMessage("ProviderManager.providerNotFound",
  					new Object[] { toTest.getName() }, "No AuthenticationProvider found for {0}"));
  		}
  		// If the parent AuthenticationManager was attempted and failed then it will
  		// publish an AbstractAuthenticationFailureEvent
  		// This check prevents a duplicate AbstractAuthenticationFailureEvent if the
  		// parent AuthenticationManager already published it
  		if (parentException == null) {
  			prepareException(lastException, authentication);
  		}
  		throw lastException;
  	}
      
    // 나머지 생략 ...
}    
```
- `providers`: 실제 인증을 하는 `AutehenticationProvider`를 List 타입으로 갖는다. 
- `parent`: `providers`에 있는 `AutehenticationProvider`에서 인증을 수행할 수 없는 경우 추가적으로 
  `parent`에 있는 `AutehenticationProvider` 중에서 추가적으로 탐색할 수 있다. 
- authenticate(): 동작 원리 주석 확인 

formLogin API 기본 manager 
```text

ProviderManager : AnonymousAuthenticationProvider providers 존재 

또한 부모 parent에도 ProviderManager를 가지는데 
해당 ProviderManager는 DaoAuthenticationProvider를 가진다. 

그래서 총 2개의 인증 Provider를 가진다. 

폼 로그인 같은 경우 DaoAuthenticationProvider 통해서 인증을 수행 
```







