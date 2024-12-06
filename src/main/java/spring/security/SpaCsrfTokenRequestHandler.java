package spring.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;

import java.util.function.Supplier;

public class SpaCsrfTokenRequestHandler extends CsrfTokenRequestAttributeHandler {
    /**
     * 헤더에서 온 값은 인코딩되지 않고 그대로 온다
     * 하지만 쿠키처럼 다른 값들은 인코딩되어서 오기 때문에 인코딩된 CsrfToken 디코딩할 수 있는 XorCsrfTokenRequestAttributeHandler 위임
     */
    private final CsrfTokenRequestHandler delegate = new XorCsrfTokenRequestAttributeHandler();
    @Override
    public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
        if(StringUtils.hasText(request.getHeader(csrfToken.getHeaderName()))){
            return super.resolveCsrfTokenValue(request, csrfToken);
        }
        return delegate.resolveCsrfTokenValue(request, csrfToken);
    }
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, Supplier<CsrfToken> csrfToken) {
        delegate.handle(request, response, csrfToken);
    }
}
