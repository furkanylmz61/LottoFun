package com.assesment.lottofun.repository;

import com.assesment.lottofun.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.tickets t LEFT JOIN FETCH t.draw WHERE u.email = :email")
    Optional<User> findByEmailWithTickets(@Param("email") String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.tickets t LEFT JOIN FETCH t.draw d WHERE u.email = :email AND (t.draw.id = :drawId OR t IS NULL)")
    Optional<User> findByEmailWithTicketsForDraw(@Param("email") String email, @Param("drawId") Long drawId);
}
