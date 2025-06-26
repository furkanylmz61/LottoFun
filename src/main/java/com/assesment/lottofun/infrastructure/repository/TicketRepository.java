package com.assesment.lottofun.infrastructure.repository;

import com.assesment.lottofun.entity.Ticket;
import com.assesment.lottofun.entity.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {


    Page<Ticket> findByDrawIdAndStatus(Long drawId, TicketStatus status, Pageable pageable);

    Optional<Ticket> findByUserIdAndDrawIdAndSelectedNumbersHash(
            Long userId, Long drawId, String selectedNumbersHash);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.draw.id = :drawId AND t.matchCount = :matchCount")
    long countByDrawIdAndMatchCount(@Param("drawId") Long drawId, @Param("matchCount") Integer matchCount);

    @Query("SELECT t FROM Ticket t WHERE t.user.id = :userId AND t.draw.id = :drawId")
    List<Ticket> findByUserIdAndDrawId(@Param("userId") Long userId, @Param("drawId") Long drawId);

    boolean existsByDrawIdAndSelectedNumbersHash(Long drawId, String selectedNumbersHash);
}