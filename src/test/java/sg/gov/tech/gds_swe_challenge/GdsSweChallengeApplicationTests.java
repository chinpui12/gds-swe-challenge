package sg.gov.tech.gds_swe_challenge;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import sg.gov.tech.gds_swe_challenge.config.TestConfig;

@SpringBootTest
@Import(TestConfig.class)
class GdsSweChallengeApplicationTests {

    @Test
    void contextLoads() {
    }

}
