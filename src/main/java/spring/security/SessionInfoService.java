package spring.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionInfoService {
    private final SessionRegistry sessionRegistry;

    /**
     * getAllPrincipals(): List<Object> 현재 활성화된 사용자의 모든 사용자의 Principal 객체들
     * getAllSessions(): 사용자의 세션 정보들을 가져온다. 첫 번째 인자는 Principal 객체이고, 두 번째 인자는 만료된 세션을 포함할지 여부를 결정하는 boolean 값
     * 반환값: SessionInformation
     */
    public void sessionInfo(){
        sessionRegistry.getAllPrincipals().stream()
        .forEach(principal -> sessionRegistry.getAllSessions(principal, false)
        .stream()
        .forEach(sessionInformation ->
                log.info(
                    "사용자: " + principal +
                    " | 세션ID: " + sessionInformation.getSessionId() +
                    " | 최종 요청 시간: " + sessionInformation.getLastRequest())));

    }
}
