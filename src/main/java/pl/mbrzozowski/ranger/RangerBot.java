package pl.mbrzozowski.ranger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.security.auth.login.LoginException;

@SpringBootApplication
public class RangerBot {

    public static void main(String[] args) throws LoginException {
        SpringApplication.run(RangerBot.class, args);
    }

}
