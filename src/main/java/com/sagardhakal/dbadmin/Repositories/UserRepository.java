package com.sagardhakal.dbadmin.Repositories;

import com.sagardhakal.dbadmin.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    @Query(
            nativeQuery = true,
            value = "select * from users u where u.username= :username limit 1"
    )
    Optional<User> getUsersByUsername(@Param("username") String username);

    @Query(
            nativeQuery = true,
            value = "select * from users limit 1"
    )
    Optional<User> getAnyUser();
}
