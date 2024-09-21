package spring.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LoginController {
    private final AuthenticationManager authenticationManager;
    private final HttpSessionSecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
    @PostMapping("/login")
    public Authentication login(@RequestBody LoginRequest login, HttpServletRequest request, HttpServletResponse response){
        // UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(login.getUsername(), login.getPassword());
        UsernamePasswordAuthenticationToken token
                = UsernamePasswordAuthenticationToken.unauthenticated(login.getUsername(), login.getPassword());

        Authentication authenticate = authenticationManager.authenticate(token); // 인증 이후 Authentication 반환
        SecurityContext securityContext = SecurityContextHolder.getContextHolderStrategy().createEmptyContext();
        securityContext.setAuthentication(authenticate); // 시큐리티 컨텍스트에 Authentication 객체 저장
        SecurityContextHolder.getContextHolderStrategy().setContext(securityContext); // 스레드 로컬에 시큐리티 컨텍스트 저장

        securityContextRepository.saveContext(securityContext, request, response); // 인증 영속을 위해 세션에 컨텍스트 저장
        return authenticate;
    }
}
