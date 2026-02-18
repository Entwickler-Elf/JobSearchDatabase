package com.stromsland.jobsearchdatabase.repository;

import com.stromsland.jobsearchdatabase.model.DiceJobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiceJobRepository extends JpaRepository<DiceJobEntity, String> {
    List<DiceJobEntity> findAllByOrderByPostedDateDesc();
}