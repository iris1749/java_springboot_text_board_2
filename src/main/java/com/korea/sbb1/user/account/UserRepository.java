package com.korea.sbb1.user.account;


import com.korea.sbb1.user.account.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<SiteUser, Long> {
    Optional<SiteUser> findByusername(String username);

    Optional<SiteUser> findByUsernameAndEmail(String username, String email);

    Optional<SiteUser> findByEmail(String email);
}
