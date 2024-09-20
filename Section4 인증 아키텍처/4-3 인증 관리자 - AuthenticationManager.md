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

두 Provider는 초기화 과정에서 Manager에 저장 
```

### Custom 테스트 

CustomAuthenticationFilter
```java
public class CustomAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private final ObjectMapper objectMapper = new ObjectMapper();
    public CustomAuthenticationFilter(HttpSecurity http) {
        super(new AntPathRequestMatcher("/api/login", "GET"));
        setSecurityContextRepository(getSecurityContextRepository(http));
    }

    private SecurityContextRepository getSecurityContextRepository(HttpSecurity http) {
        SecurityContextRepository securityContextRepository = http.getSharedObject(SecurityContextRepository.class);
        if (securityContextRepository == null) {
            securityContextRepository = new DelegatingSecurityContextRepository(
                    new RequestAttributeSecurityContextRepository(), new HttpSessionSecurityContextRepository());
        }
        return securityContextRepository;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username,password);

        return this.getAuthenticationManager().authenticate(token);
    }
}
```

CustomAuthenticationProvider
```java
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        String loginId = authentication.getName();
        String password = (String) authentication.getCredentials();

        return new UsernamePasswordAuthenticationToken(loginId, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.isAssignableFrom(UsernamePasswordAuthenticationToken.class);
    }
}
```

AuthenticationManager - http Security 활용 
```java
@EnableWebSecurity
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        AuthenticationManager authenticationManager = authenticationManagerBuilder.build();            // build() 는 최초 한번 만 호출해야 한다

        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/","/api/login").permitAll()
                        .anyRequest().authenticated())
                .authenticationManager(authenticationManager)
                .addFilterBefore(customFilter(http, authenticationManager), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    public CustomAuthenticationFilter customFilter(HttpSecurity http, AuthenticationManager authenticationManager) {
        CustomAuthenticationFilter customAuthenticationFilter = new CustomAuthenticationFilter(http);
        customAuthenticationFilter.setAuthenticationManager(authenticationManager);
        return customAuthenticationFilter;
    }

    @Bean
    public UserDetailsService userDetailsService(){
        UserDetails user = User.withUsername("user").password("{noop}1111").roles("USER").build();
        return  new InMemoryUserDetailsManager(user);
    }
}
```
- `AuthenticationManagerBuilder`를 `HttpSecurity` 객체로부터 가져와, 이를 통해 
  `AuthenticationManager`를 생성하고 사용하는 방식
- authenticationManagerBuilder.build()는 `AuthenticationManager`를 생성하는 과정. 주의할 점은, build() 메서드는 
  `AuthenticationManager`를 처음 한 번만 호출해야 하고, 이후에는 재사용해야 함
  - `AuthenticationManager authenticationManager = authenticationManagerBuilder.getObject()`
- http.authenticationManager(authenticationManager)를 통해 `HttpSecurity`에 인증 관리자를 설정

AuthenticationManager - 직접 생성 
```java
@EnableWebSecurity
@Configuration
public class SecurityConfig2 {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/","/api/login").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(customFilter(http), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    public CustomAuthenticationFilter customFilter(HttpSecurity http) {

        List<AuthenticationProvider> list1 = List.of(new DaoAuthenticationProvider());
        ProviderManager parent = new ProviderManager(list1);
        List<AuthenticationProvider> list2 = List.of(new AnonymousAuthenticationProvider("key"), new CustomAuthenticationProvider());
        ProviderManager authenticationManager = new ProviderManager(list2, parent);

        CustomAuthenticationFilter customAuthenticationFilter = new CustomAuthenticationFilter(http);
        customAuthenticationFilter.setAuthenticationManager(authenticationManager);

        return customAuthenticationFilter;

    }

    @Bean
    public UserDetailsService userDetailsService(){
        UserDetails user = User.withUsername("user").password("{noop}1111").roles("USER").build();
        return  new InMemoryUserDetailsManager(user);
    }
}
```
- Custom 필터에서는 `UsernamePasswordAuthenticationToken`을 넘기는데 시큐리티에 있는 DaoAuthenticationProvider 또한
  지원 이번에 만든 CustomAuthenticationProvider 또한 지원한다. 
- 하지만 ProviderManager 생성 과정을 보면 DaoAuthenticationProvider는 부모로 추가 
  CustomAuthenticationProvider는 ProviderManager가 가지는 providers 리스트에 들어간다. 
- 즉 둘다 인증 진행이 가능하지만 providers 리스트가 먼저 탐색되기 때문에 CustomAuthenticationProvider가 동작하게된다. 



