package spring.security;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class IndexController {
    private final SessionInfoService sessionInfoService;
    @GetMapping("/sessionInfo")
    public void sessionInfo(){
        sessionInfoService.sessionInfo();
    }
    @GetMapping("/")
    public String index(){
        return "index";
    }
    @GetMapping("/invalidSessionUrl")
    public String invalidSessionUrl(){
        return "invalidSessionUrl";
    }
    @GetMapping("/expiredUrl")
    public String expiredUrl(){
        return "expiredUrl";
    }
}
