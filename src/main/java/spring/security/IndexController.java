package spring.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class IndexController {
    @GetMapping("/admin")
    public String adminPage(){
        return "adminPage";
    }
    @GetMapping("/denied")
    public String denied(){
        return "denied";
    }
    @GetMapping("/login")
    public String loginPage(){
        return "loginPage";
    }
    @GetMapping("/")
    public  Authentication index(){
        Authentication authentication = SecurityContextHolder.getContextHolderStrategy().getContext().getAuthentication();
        log.info("authentication={}", authentication);
        return authentication;
    }
    @PostMapping("/csrf")
    public String csrf(){
        return "csrf 적용";
    }

    /**
     * CsrfFiler: HTTP 메소드 상관없이 토큰을 지연 초기화를 적용해서 가져옴 by (세션/쿠키)
     * 현재 메소드는 GET 그래서 아직 초기화 과정이 진행되지 않고 넘어온 상태
     * getToken() 하는 순간 여러 Supplier 감싸진 CsrfToken 초기화 과정 진행
     */
    @GetMapping("/supplier")
    public String tokenSupplier(HttpServletRequest request){
        CsrfToken csrfToken1 = (CsrfToken) request.getAttribute("_csrf");
        CsrfToken csrfToken2 = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        log.info("{}", csrfToken1 == csrfToken2);
        return csrfToken1.getToken();
    }
    /**
     * CsrfFilter: Post 요청이어서 세션에 저장된 토큰과 클라이언트가 보낸 토큰을 이미 비교했음
     * 즉, CsrfToken 초기화 완료된 상태
     */
    @PostMapping("/formCsrf")
    public CsrfToken formCsrf(CsrfToken csrfToken){
        return csrfToken;
    }
    @PostMapping("/cookieCsrf")
    public CsrfToken cookieCsrf(CsrfToken csrfToken){
        return csrfToken;
    }
}
