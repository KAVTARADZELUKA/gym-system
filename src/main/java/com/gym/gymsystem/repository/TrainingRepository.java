package com.gym.gymsystem.repository;

import com.gym.gymsystem.entity.Trainee;
import com.gym.gymsystem.entity.Trainer;
import com.gym.gymsystem.entity.Training;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TrainingRepository extends JpaRepository<Training, Long> {

    List<Training> findByTraineesContaining(Trainee trainee);

    List<Training> findByTrainersContaining(Trainer trainer);


    @Query("SELECT t FROM Training t " +
            "JOIN t.trainees trainee " +
            "JOIN trainee.user user " +
            "WHERE user.username = :username " +
            "AND (:fromDate IS NULL OR t.trainingDate >= :fromDate) " +
            "AND (:toDate IS NULL OR t.trainingDate <= :toDate) " +
            "AND (:trainerName IS NULL OR EXISTS (" +
            "SELECT tr FROM t.trainers tr " +
            "WHERE tr.user.firstName LIKE %:trainerName%)) " +
            "AND (:trainingType IS NULL OR EXISTS (" +
            "SELECT tt FROM t.trainingType tt " +
            "WHERE tt.trainingTypeName = :trainingType))")
    List<Training> findTrainingsByTraineeAndCriteria(@Param("username") String username,
                                                     @Param("fromDate") LocalDateTime fromDate,
                                                     @Param("toDate") LocalDateTime toDate,
                                                     @Param("trainerName") String trainerName,
                                                     @Param("trainingType") String trainingType);
    @Query("SELECT t FROM Training t " +
            "JOIN t.trainers trainer " +
            "JOIN trainer.user user " +
            "WHERE user.username = :username " +
            "AND (:fromDate IS NULL OR t.trainingDate >= :fromDate) " +
            "AND (:toDate IS NULL OR t.trainingDate <= :toDate) " +
            "AND (:traineeName IS NULL OR EXISTS (" +
            "SELECT tr FROM t.trainees tr " +
            "WHERE tr.user.firstName LIKE %:traineeName%)) " +
            "AND (:trainingType IS NULL OR EXISTS (" +
            "SELECT tt FROM t.trainingType tt " +
            "WHERE tt.trainingTypeName = :trainingType))")
    List<Training> findTrainingsByTrainerAndCriteria(@Param("username") String username,
                                                     @Param("fromDate") LocalDateTime fromDate,
                                                     @Param("toDate") LocalDateTime toDate,
                                                     @Param("traineeName") String traineeName,
                                                     @Param("trainingType") String trainingType);

    @Query("SELECT tr FROM Trainer tr " +
            "WHERE tr.id NOT IN (SELECT trnr.id FROM Training t " +
            "JOIN t.trainees trainee " +
            "JOIN trainee.user user " +
            "JOIN t.trainers trnr " +
            "WHERE user.username = :username) " +
            "AND tr.user.isActive = true " +
            "AND EXISTS (SELECT tn FROM Trainee tn WHERE tn.user.username = :username)")
    List<Trainer> findTrainersNotAssignedToTrainee(@Param("username") String username);
}