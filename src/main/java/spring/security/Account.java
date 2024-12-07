package spring.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter  @Setter
public class Account {
    private String owner;
    private boolean isSecure;
}
