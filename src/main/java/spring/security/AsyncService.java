package spring.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
@Service
@Slf4j
public class AsyncService {
    @Async
    public void async(){
        Authentication authentication = SecurityContextHolder.getContextHolderStrategy().getContext().getAuthentication();
        log.info("currentThread={}", Thread.currentThread().getName());
        log.info("authentication={}", authentication);
    }
}
