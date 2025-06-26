package com.assesment.lottofun.infrastructure.repository;

import com.assesment.lottofun.entity.Draw;
import com.assesment.lottofun.entity.DrawStatus;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DrawRepository extends JpaRepository<Draw, Long> {

    Optional<Draw> findFirstByStatusOrderByDrawDateAsc(DrawStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "0")})
    @Query("SELECT d FROM Draw d WHERE d.status = :status ORDER BY d.drawDate ASC")
    Optional<Draw> getLockDraw(DrawStatus status);

}