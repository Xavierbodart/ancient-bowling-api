package be.telemis.games.bowling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class AncientBowlingRestApplication {

    public static void main(String[] args) {
        SpringApplication.run(AncientBowlingRestApplication.class, args);
    }

}
