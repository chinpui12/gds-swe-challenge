package sg.gov.tech.gds_swe_challenge.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sg.gov.tech.gds_swe_challenge.constant.AppConstants;
import sg.gov.tech.gds_swe_challenge.entity.Session;
import sg.gov.tech.gds_swe_challenge.entity.User;
import sg.gov.tech.gds_swe_challenge.repository.SessionRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {
    @Mock
    private SessionRepository sessionRepository;
    @Mock
    private UserService userService;

    private SessionService sut;

    @BeforeEach
    void setup() {
        sut = new SessionService(sessionRepository, userService);
    }

    @Test
    void getOrCreateSession_sessionExistsAndOpen_returnsSession() {
        long sessionId = 1L;
        String sessionName = "team-alpha";
        Session existingSession = new Session();
        existingSession.setId(sessionId);
        existingSession.setName(sessionName);
        existingSession.setClosed(false);
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(existingSession));

        Session result = sut.getOrCreateSession(sessionId, sessionName);

        assertThat(result).isEqualTo(existingSession);
        verify(sessionRepository).findById(sessionId);
        verify(sessionRepository, never()).saveAndFlush(any(Session.class));
    }

    @Test
    void getOrCreateSession_sessionExistsButClosed_throwsException() {
        long sessionId = 2L;
        Session closedSession = new Session();
        closedSession.setId(sessionId);
        closedSession.setClosed(true);
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(closedSession));

        assertThatThrownBy(() -> sut.getOrCreateSession(sessionId, "closed-team"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Session is already closed for restaurant submissions");

        verify(sessionRepository).findById(sessionId);
        verify(sessionRepository, never()).saveAndFlush(any());
    }

    @Test
    void getOrCreateSession_sessionNotFound_createsNewSession() {
        long sessionId = 3L;
        String sessionName = "new-team";
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        Session savedSession = new Session();
        savedSession.setId(sessionId);
        savedSession.setName(sessionName);
        savedSession.setClosed(false);
        when(sessionRepository.saveAndFlush(any(Session.class))).thenReturn(savedSession);

        Session result = sut.getOrCreateSession(sessionId, sessionName);

        assertThat(result).isEqualTo(savedSession);
        verify(sessionRepository).findById(sessionId);

        ArgumentCaptor<Session> captor = ArgumentCaptor.forClass(Session.class);
        verify(sessionRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("new-team");
    }

    @Test
    void closeSession_validSession_updatesAndSaves() {
        long sessionId = 4L;
        String selectedRestaurant = "Kopitiam";
        Session openSession = new Session();
        openSession.setId(sessionId);
        openSession.setClosed(false);
        when(sessionRepository.findByIdAndIsClosedFalse(sessionId)).thenReturn(Optional.of(openSession));

        sut.closeSession(sessionId, selectedRestaurant);

        assertThat(openSession.isClosed()).isTrue();
        assertThat(openSession.getSelectedRestaurant()).isEqualTo("Kopitiam");
        verify(sessionRepository).findByIdAndIsClosedFalse(sessionId);
        verify(sessionRepository).saveAndFlush(openSession);
    }

    @Test
    void closeSession_sessionNotFound_throwsException() {
        long sessionId = 5L;
        when(sessionRepository.findByIdAndIsClosedFalse(sessionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.closeSession(sessionId, "Pizza Hut"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Unable to find open session with id: 5");

        verify(sessionRepository).findByIdAndIsClosedFalse(sessionId);
        verify(sessionRepository, never()).saveAndFlush(any());
    }

    @Test
    void getOpenSession_sessionExistsAndOpen_returnsSession() {
        long sessionId = 6L;
        Session openSession = new Session();
        openSession.setId(sessionId);
        openSession.setClosed(false);
        when(sessionRepository.findByIdAndIsClosedFalse(sessionId)).thenReturn(Optional.of(openSession));

        Session result = sut.getOpenSession(sessionId);

        assertThat(result).isEqualTo(openSession);
        verify(sessionRepository).findByIdAndIsClosedFalse(sessionId);
    }

    @Test
    void getOpenSession_sessionClosed_returnsNull() {
        long sessionId = 7L;
        Session closedSession = new Session();
        closedSession.setId(sessionId);
        closedSession.setClosed(true);
        when(sessionRepository.findByIdAndIsClosedFalse(sessionId)).thenReturn(Optional.empty());

        Session result = sut.getOpenSession(sessionId);

        assertThat(result).isNull();
        verify(sessionRepository).findByIdAndIsClosedFalse(sessionId);
    }

    @Test
    void getOpenSession_sessionNotFound_returnsNull() {
        long sessionId = 8L;
        when(sessionRepository.findByIdAndIsClosedFalse(sessionId)).thenReturn(Optional.empty());

        Session result = sut.getOpenSession(sessionId);

        assertThat(result).isNull();
        verify(sessionRepository).findByIdAndIsClosedFalse(sessionId);
    }

    @Test
    void isSessionClosed_openSession_returnsFalse() {
        long sessionId = 9L;
        Session openSession = new Session();
        openSession.setId(sessionId);
        openSession.setClosed(false);
        when(sessionRepository.findByIdAndIsClosedFalse(sessionId)).thenReturn(Optional.of(openSession));

        boolean result = sut.isSessionClosed(sessionId);

        assertThat(result).isFalse();
        verify(sessionRepository).findByIdAndIsClosedFalse(sessionId);
    }

    @Test
    void isSessionClosed_closedSession_returnsTrue() {
        long sessionId = 10L;
        Session closedSession = new Session();
        closedSession.setId(sessionId);
        closedSession.setClosed(true);
        when(sessionRepository.findByIdAndIsClosedFalse(sessionId)).thenReturn(Optional.of(closedSession));

        boolean result = sut.isSessionClosed(sessionId);

        assertThat(result).isTrue();
        verify(sessionRepository).findByIdAndIsClosedFalse(sessionId);
    }

    @Test
    void isSessionClosed_sessionNotFound_returnsFalse() {
        long sessionId = 11L;
        when(sessionRepository.findByIdAndIsClosedFalse(sessionId)).thenReturn(Optional.empty());

        boolean result = sut.isSessionClosed(sessionId);

        assertThat(result).isFalse();
    }

    @Test
    void resetSession_closedSession_resetsAndSaves() {
        long sessionId = 12L;
        Session closedSession = new Session();
        closedSession.setId(sessionId);
        closedSession.setName("team-omega");
        closedSession.setClosed(true);
        closedSession.setSelectedRestaurant("Kopitiam");
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(closedSession));

        Session savedSession = new Session();
        savedSession.setId(sessionId);
        savedSession.setClosed(false);
        savedSession.setSelectedRestaurant(null);
        when(sessionRepository.saveAndFlush(any(Session.class))).thenReturn(savedSession);

        Session result = sut.resetSession(sessionId);

        assertThat(result.getId()).isEqualTo(sessionId);
        assertThat(result.isClosed()).isFalse();
        assertThat(result.getSelectedRestaurant()).isNull();

        verify(sessionRepository).findById(sessionId);
        verify(sessionRepository).saveAndFlush(closedSession);

        ArgumentCaptor<Session> captor = ArgumentCaptor.forClass(Session.class);
        verify(sessionRepository).saveAndFlush(captor.capture());
        Session captured = captor.getValue();
        assertThat(captured.isClosed()).isFalse();
        assertThat(captured.getSelectedRestaurant()).isNull();
    }

    @Test
    void resetSession_alreadyOpen_throwsIllegalStateException() {
        long sessionId = 13L;
        Session openSession = new Session();
        openSession.setId(sessionId);
        openSession.setClosed(false);
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(openSession));

        assertThatThrownBy(() -> sut.resetSession(sessionId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Session with id 13 is already open");

        verify(sessionRepository).findById(sessionId);
        verify(sessionRepository, never()).saveAndFlush(any());
    }

    @Test
    void resetSession_notFound_throwsIllegalStateException() {
        long sessionId = 14L;
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.resetSession(sessionId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Session not found with id: 14");

        verify(sessionRepository).findById(sessionId);
        verify(sessionRepository, never()).saveAndFlush(any());
    }

    @Test
    void getSessions_returnsAllSessions() {
        List<Session> expectedSessions = List.of(
                createSession(15L, "GLOBAL", false, null),
                createSession(16L, "team-epsilon", true, "Food Republic")
        );
        when(sessionRepository.findAll()).thenReturn(expectedSessions);

        List<Session> result = sut.getSessions();

        assertThat(result).isEqualTo(expectedSessions);
        verify(sessionRepository).findAll();
    }

    @Test
    void getSessions_emptyList_returnsEmptyList() {
        when(sessionRepository.findAll()).thenReturn(List.of());

        List<Session> result = sut.getSessions();

        assertThat(result).isEmpty();
        verify(sessionRepository).findAll();
    }

    @Test
    void getSessions_repositoryThrowsException_propagatesException() {
        when(sessionRepository.findAll())
                .thenThrow(new RuntimeException("Database error"));

        assertThatThrownBy(() -> sut.getSessions())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database error");

        verify(sessionRepository).findAll();
    }


    @Test
    void inviteUser_batchInvite_validCreator_returnsUpdatedSession() {
        long sessionId = 50L;
        Session session = createSession(sessionId, "team-batch", false, null);
        session.setCreatedBy("alice");
        User creator = createUser("alice");
        List<User> invitees = List.of(
                createUser("bob"),
                createUser("charlie")
        );

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(userService.getUser("alice")).thenReturn(creator);
        when(userService.getUser("bob")).thenReturn(invitees.get(0));
        when(userService.getUser("charlie")).thenReturn(invitees.get(1));
        Session savedSession = new Session();
        savedSession.setId(sessionId);
        when(sessionRepository.saveAndFlush(session)).thenReturn(savedSession);

        Session result = sut.inviteUser(sessionId, "alice", List.of("bob", "charlie"));

        assertThat(result.getId()).isEqualTo(sessionId);
        assertThat(session.getInvitedUsers()).hasSize(2);
        assertThat(session.getInvitedUsers())
                .extracting(User::getUsername)
                .containsExactlyInAnyOrder("bob", "charlie");

        verify(sessionRepository).findById(sessionId);
        verify(sessionRepository).saveAndFlush(session);
        verify(userService).getUser("alice");
        verify(userService).getUser("bob");
        verify(userService).getUser("charlie");
    }

    @Test
    void inviteUser_nonCreator_throwsIllegalStateException() {
        long sessionId = 51L;
        Session session = createSession(sessionId, "team-51", false, null);
        session.setCreatedBy("alice");
        User nonCreator = createUser("non-creator");

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(userService.getUser("non-creator")).thenReturn(nonCreator);

        assertThatThrownBy(() -> sut.inviteUser(sessionId, "non-creator", List.of("bob")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Only session creator can invite users");

        verify(sessionRepository).findById(sessionId);
        verify(userService).getUser("non-creator");
        verify(sessionRepository, never()).saveAndFlush(any());
        verify(userService, never()).getUser("bob");
    }

    @Test
    void inviteUser_closedSession_throwsIllegalStateException() {
        long sessionId = 52L;
        Session closedSession = createSession(sessionId, "closed-team", true, "KFC");
        closedSession.setCreatedBy("alice");

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(closedSession));
        when(userService.getUser("alice")).thenReturn(createUser("alice"));

        assertThatThrownBy(() -> sut.inviteUser(sessionId, "alice", List.of("bob")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot invite to closed session");

        verify(sessionRepository).findById(sessionId);
        verify(userService).getUser("alice");
        verify(sessionRepository, never()).saveAndFlush(any());
    }

    @Test
    void inviteUser_emptyList_noUsersAdded() {
        long sessionId = 53L;
        Session session = createSession(sessionId, "empty-invite", false, null);
        session.setCreatedBy("dave");
        User creator = createUser("dave");

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(userService.getUser("dave")).thenReturn(creator);
        when(sessionRepository.saveAndFlush(session)).thenReturn(session);

        sut.inviteUser(sessionId, "dave", List.of());

        assertThat(session.getInvitedUsers()).isEmpty();
        verify(sessionRepository).saveAndFlush(session);
    }

    @Test
    void inviteUser_duplicateUsernames_noDuplicatesAdded() {
        long sessionId = 54L;
        Session session = createSession(sessionId, "dupe-test", false, null);
        session.setCreatedBy("alice");
        User bob = createUser("bob");

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(userService.getUser("alice")).thenReturn(createUser("alice"));
        when(userService.getUser("bob")).thenReturn(bob);
        when(sessionRepository.saveAndFlush(session)).thenReturn(session);

        sut.inviteUser(sessionId, "alice", List.of("bob", "bob"));

        verify(userService).getUser("bob");
    }

    @Test
    void validateUserCanSubmit_globalSession_alwaysAllowed() {
        long globalSessionId = AppConstants.GLOBAL_SESSION_ID;  // e.g., 0L
        Session globalSession = createSession(globalSessionId, "GLOBAL", true, null);  // Closed!

        sut.validateUserCanSubmit(globalSession, "anyone");
    }

    @Test
    void validateUserCanSubmit_openSession_invitedUser_passes() {
        long sessionId = 70L;
        Session openSession = createSession(sessionId, "team-open", false, null);
        openSession.getInvitedUsers().add(createUser("alice"));

        sut.validateUserCanSubmit(openSession, "alice");
    }

    @Test
    void validateUserCanSubmit_openSession_notInvited_throwsException() {
        long sessionId = 71L;
        Session openSession = createSession(sessionId, "team-71", false, null);

        assertThatThrownBy(() -> sut.validateUserCanSubmit(openSession, "alice"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot validate submission for session 71: User 'alice' not invited to session 'team-71' (ID: 71)");
    }

    @Test
    void validateUserCanSubmit_closedSession_nonGlobal_throwsException() {
        long sessionId = 72L;
        Session closedSession = createSession(sessionId, "closed-team", true, "KFC");

        assertThatThrownBy(() -> sut.validateUserCanSubmit(closedSession, "bob"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot validate submission for session 72: Session 72 is closed for submissions");
    }

    private Session createSession(Long id, String name, boolean closed, String selectedRestaurant) {
        Session session = new Session();
        session.setId(id);
        session.setName(name);
        session.setClosed(closed);
        session.setSelectedRestaurant(selectedRestaurant);
        return session;
    }

    private User createUser(String username) {
        User user = new User();
        user.setUsername(username);
        return user;
    }
}
