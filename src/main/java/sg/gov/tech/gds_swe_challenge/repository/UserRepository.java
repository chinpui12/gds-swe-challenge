package sg.gov.tech.gds_swe_challenge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sg.gov.tech.gds_swe_challenge.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);

    List<User> findAllByUsernameIn(List<String> usernames);
}