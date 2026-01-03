package sg.gov.tech.gds_swe_challenge.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sg.gov.tech.gds_swe_challenge.entity.User;
import sg.gov.tech.gds_swe_challenge.repository.UserRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    /**
     * Get user by username or throw exception
     */
    public User getUser(String username) {
        return repository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
    }

    /**
     * Get users by usernames
     */
    public List<User> getUsers(List<String> usernames) {
        if (usernames == null || usernames.isEmpty()) {
            return List.of();
        }
        return repository.findAllByUsernameIn(usernames);
    }

    /**
     * Check if user can initiate sessions
     */
    public boolean canInitiateSessions(String username) {
        return getUser(username).isCanInitiateSession();
    }
}
