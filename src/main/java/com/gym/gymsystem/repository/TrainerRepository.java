package com.gym.gymsystem.repository;

import com.gym.gymsystem.entity.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainerRepository extends JpaRepository<Trainer, Long> {
    Optional<Trainer> findByUser_Username(String username);

    List<Trainer> findAllByUser_UsernameIn(List<String> username);
}