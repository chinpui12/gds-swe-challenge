package sg.gov.tech.gds_swe_challenge.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sg.gov.tech.gds_swe_challenge.entity.User;
import sg.gov.tech.gds_swe_challenge.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    private UserService sut;

    @BeforeEach
    void setup() {
        sut = new UserService(userRepository);
    }

    @Test
    void getUser_exists_returnsUser() {
        String username = "alice";
        User alice = new User();
        alice.setUsername(username);
        alice.setCanInitiateSession(true);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(alice));

        User result = sut.getUser(username);

        assertThat(result).isEqualTo(alice);
    }

    @Test
    void getUser_notFound_throwsException() {
        when(userRepository.findByUsername("bob")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.getUser("bob"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found: bob");
    }

    @Test
    void canInitiateSessions_true_returnsTrue() {
        User initiator = new User();
        initiator.setUsername("charlie");
        initiator.setCanInitiateSession(true);
        when(userRepository.findByUsername("charlie")).thenReturn(Optional.of(initiator));

        assertThat(sut.canInitiateSessions("charlie")).isTrue();
    }

    @Test
    void canInitiateSessions_false_returnsFalse() {
        User normalUser = new User();
        normalUser.setUsername("dave");
        normalUser.setCanInitiateSession(false);
        when(userRepository.findByUsername("dave")).thenReturn(Optional.of(normalUser));

        assertThat(sut.canInitiateSessions("dave")).isFalse();
    }

    @Test
    void getUsers_allFound_returnsList() {
        List<String> usernames = List.of("alice", "bob");
        List<User> expectedUsers = List.of(createUser("alice"), createUser("bob"));
        when(userRepository.findAllByUsernameIn(usernames)).thenReturn(expectedUsers);

        List<User> result = sut.getUsers(usernames);

        assertThat(result).isEqualTo(expectedUsers);
    }

    @Test
    void getUsers_someNotFound_returnsFoundOnly() {
        List<String> usernames = List.of("alice", "unknown");
        List<User> aliceOnly = List.of(createUser("alice"));
        when(userRepository.findAllByUsernameIn(usernames)).thenReturn(aliceOnly);

        List<User> result = sut.getUsers(usernames);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getUsername()).isEqualTo("alice");
    }

    @Test
    void getUsers_emptyInput_returnsEmptyList() {
        List<User> result = sut.getUsers(List.of());

        assertThat(result).isEmpty();
    }

    private User createUser(String username) {
        User user = new User();
        user.setUsername(username);
        return user;
    }
}
