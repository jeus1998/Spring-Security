package spring.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class IndexController {
    private final SessionInfoService sessionInfoService;
    @GetMapping("/sessionInfo")
    public String sessionInfo(){
        sessionInfoService.sessionInfo();
        return "sessionInfo";
    }
    @GetMapping("/")
    public  Authentication index(){
        Authentication authentication = SecurityContextHolder.getContextHolderStrategy().getContext().getAuthentication();
        log.info("authentication={}", authentication);
        return authentication;
    }
}
