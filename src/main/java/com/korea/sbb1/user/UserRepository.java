package com.korea.sbb1.user;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<SiteUser, Long> {
    Optional<SiteUser> findByusername(String username);

    Optional<SiteUser> findByUsernameAndEmail(String username, String email);
}
