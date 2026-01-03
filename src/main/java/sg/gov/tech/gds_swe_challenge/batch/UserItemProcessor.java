package sg.gov.tech.gds_swe_challenge.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import sg.gov.tech.gds_swe_challenge.dto.UserInput;
import sg.gov.tech.gds_swe_challenge.entity.User;
import sg.gov.tech.gds_swe_challenge.repository.UserRepository;

import java.time.LocalDateTime;

@StepScope
public class UserItemProcessor implements ItemProcessor<UserInput, User> {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserItemProcessor.class);
    private final UserRepository userRepository;

    public UserItemProcessor(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User process(UserInput input) {
        LOGGER.info("Processing user: {}", input.username());

        if (userRepository.findByUsername(input.username()).isPresent()) {
            LOGGER.warn("User {} already exists, skipping", input.username());
            return null;
        }

        User user = new User();
        user.setUsername(input.username());
        user.setCanInitiateSession(input.canInitiateSession());
        user.setCreatedBy("SYSTEM");
        user.setCreatedAt(LocalDateTime.now());

        LOGGER.info("Created user: {}", input);
        return user;
    }
}
