package com.assesment.lottofun.repository;

import com.assesment.lottofun.entity.Draw;
import com.assesment.lottofun.entity.DrawStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DrawRepository extends JpaRepository<Draw, Long> {

    Optional<Draw> findFirstByStatusOrderByDrawDateAsc(DrawStatus status);

    List<Draw> findByStatusAndDrawDateBefore(DrawStatus status, LocalDateTime dateTime);

    Page<Draw> findByStatusInOrderByDrawDateDesc(List<DrawStatus> statuses, Pageable pageable);

    @Query("SELECT d FROM Draw d WHERE d.status = :status ORDER BY d.drawDate ASC")
    Optional<Draw> findNextActiveDrawByStatus(DrawStatus status);

    Optional<Draw> findTopByOrderByIdDesc();
}
