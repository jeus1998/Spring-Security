package spring.security;

import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class MethodController {
    @GetMapping("/")
    public String index(){
        return "index";
    }
    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String admin(){
        return "admin";
    }
    @GetMapping("/user")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public String user(){
        return "user";
    }
    @GetMapping("/isAuthenticated")
    @PreAuthorize("isAuthenticated()")
    public String isAuthenticated(){
        return "isAuthenticated";
    }
    @GetMapping("/user/{id}")
    @PreAuthorize("#id == authentication.name")
    public String authentication(@PathVariable String id){
        return "id";
    }
    @GetMapping("/owner")
    @PostAuthorize("returnObject.owner == authentication.name and hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public Account owner(String name){
        return new Account(name, false);
    }
    @GetMapping("/isSecure")
    @PostAuthorize("returnObject.owner == authentication.name and returnObject.isSecure")
    public Account isSecure(String name, String isSecure){
        return new Account(name, "Y".equals(isSecure));
    }
}
