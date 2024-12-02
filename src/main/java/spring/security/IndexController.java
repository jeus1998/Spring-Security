package spring.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class IndexController {
    @GetMapping("/")
    public String index(Authentication authentication, HttpServletRequest request){
        log.info("request.getUserPrincipal={}", request.getUserPrincipal());
        log.info("authentication={}", authentication);
        if(authentication instanceof AnonymousAuthenticationToken) return "anonymous";
        return "not anonymous";
    }
    @GetMapping("/test")
    public String test(@CurrentSecurityContext SecurityContext context){
        log.info("authentication={}", context.getAuthentication());
        return context.getAuthentication().getName();
    }
}
