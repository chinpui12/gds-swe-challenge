package sg.gov.tech.gds_swe_challenge.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@TestConfiguration
@ActiveProfiles("test")
@Import({DatabaseConfig.class})
public class TestConfig {
}
