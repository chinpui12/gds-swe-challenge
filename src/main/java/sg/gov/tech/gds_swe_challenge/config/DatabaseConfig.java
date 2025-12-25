package sg.gov.tech.gds_swe_challenge.config;

import org.jspecify.annotations.NullMarked;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@Configuration
public class DatabaseConfig {
    @Bean
    public AuditorAware<String> auditorProvider() {
        return new AuditorAwareImpl();
    }

    static class AuditorAwareImpl implements AuditorAware<String> {
        @Override
        @NullMarked
        public Optional<String> getCurrentAuditor() {
            // Extract from X-Username header or SecurityContext
            // For now, get from request context or default to 'system'
            return Optional.of("SYSTEM");
        }
    }
}
