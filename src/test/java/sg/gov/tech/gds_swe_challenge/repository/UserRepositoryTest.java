package sg.gov.tech.gds_swe_challenge.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import sg.gov.tech.gds_swe_challenge.config.TestConfig;
import sg.gov.tech.gds_swe_challenge.entity.User;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestConfig.class)
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByUsername_existingUser_returnsUser() {
        User user = new User();
        user.setUsername("alice");
        user.setCanInitiateSession(true);
        userRepository.saveAndFlush(user);

        Optional<User> result = userRepository.findByUsername("alice");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("alice");
        assertThat(result.get().isCanInitiateSession()).isTrue();
    }

    @Test
    void findByUsername_nonExistingUser_returnsEmpty() {
        Optional<User> result = userRepository.findByUsername("unknown");

        assertThat(result).isNotPresent();
    }

    @Test
    void findAllByUsernameIn_allFound_returnsMatchingUsers() {
        User alice = createUser("alice");
        alice.setCanInitiateSession(true);
        User bob = createUser("bob");
        User charlie = createUser("charlie");

        entityManager.persistAndFlush(alice);
        entityManager.persistAndFlush(bob);
        entityManager.persistAndFlush(charlie);

        List<User> result = userRepository.findAllByUsernameIn(List.of("alice", "charlie", "unknown"));

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(User::getUsername)
                .containsExactlyInAnyOrder("alice", "charlie");
        assertThat(result)
                .filteredOn(u -> u.getUsername().equals("alice"))
                .first()
                .satisfies(u -> assertThat(u.isCanInitiateSession()).isTrue());
    }

    @Test
    void findAllByUsernameIn_noneFound_returnsEmptyList() {
        List<User> result = userRepository.findAllByUsernameIn(List.of("unknown1", "unknown2"));

        assertThat(result).isEmpty();
    }

    @Test
    void findAllByUsernameIn_emptyList_returnsEmptyList() {
        List<User> result = userRepository.findAllByUsernameIn(List.of());

        assertThat(result).isEmpty();
    }

    @Test
    void findAllByUsernameIn_nullInput_throwsException() {
        List<User> result = userRepository.findAllByUsernameIn(null);
        assertThat(result).isEmpty();
    }

    private User createUser(String username) {
        User user = new User();
        user.setUsername(username);
        return user;
    }
}
