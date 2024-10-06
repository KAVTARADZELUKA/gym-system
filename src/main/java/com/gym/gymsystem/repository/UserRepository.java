package com.gym.gymsystem.repository;

import com.gym.gymsystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u.username FROM User u WHERE u.username LIKE ?1%")
    List<String> findUsernamesByUsernameStartingWith(String baseUsername);

    Optional<User> getUserByUsername(String username);

    Optional<User> findByUsername(String username);
}