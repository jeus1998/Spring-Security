package spring.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Slf4j
public class MyCustomFilter extends OncePerRequestFilter {
    private boolean flag;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if(flag){
            try {
                String username = request.getParameter("username");
                String password = request.getParameter("password");
                request.login(username, password);
            }
            catch (Exception e){
                log.info(e.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }
    public void setFlag(boolean flag){
        this.flag = flag;
    }
}
