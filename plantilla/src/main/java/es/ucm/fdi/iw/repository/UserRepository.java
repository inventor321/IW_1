package es.ucm.fdi.iw.repository;

import es.ucm.fdi.iw.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByPhonenumber(String phonenumber);

    boolean existsByUsername(String username);

    List<User> findTopByLastlogin(LocalDateTime lastlogin);
}
