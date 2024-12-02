package spring.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.List;

public class CustomAuthenticationProvider implements AuthenticationProvider {
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String loginId = authentication.getName();
        String password = String.valueOf(authentication.getCredentials());

        // 생략된 과정: UserDetailsService 통해서 UserDetails 받고 password 검증을 해야함
        return new UsernamePasswordAuthenticationToken(
                loginId, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }
    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.isAssignableFrom(UsernamePasswordAuthenticationToken.class);
    }
}
