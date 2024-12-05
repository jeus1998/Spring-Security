package spring.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class SessionInfoService {
    private final SessionRegistry sessionRegistry;
    public void sessionInfo(){
        sessionRegistry
                .getAllPrincipals().stream().
                forEach(principal -> {
                    sessionRegistry.getAllSessions(principal, false).stream()
                            .forEach(sessionInfo -> System.out.println("사용자 " + principal + " 세션:" + sessionInfo.getSessionId()));
                });
    }
}
