package spring.security;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class IndexController {
    @GetMapping("/admin")
    public String admin(){
        return "adminPage";
    }
    @GetMapping("/login")
    public String login(){
        return "loginPage";
    }
    @GetMapping("/denied")
    public String denied(){
        return "deniedPage";
    }
    @GetMapping("/")
    public String index(){
        return "index";
    }
}
