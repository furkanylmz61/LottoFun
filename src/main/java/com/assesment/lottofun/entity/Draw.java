package com.assesment.lottofun.entity;

import com.assesment.lottofun.util.DrawUtils;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "draws")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Draw {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "winning_numbers")
    private String winningNumbers;

    @Column(name = "total_prize_pool", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalPrizePool = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DrawStatus status = DrawStatus.DRAW_OPEN;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    @Column(name = "prizes_distributed_at")
    private LocalDateTime prizesDistributedAt;

    @Column(name = "draw_date", nullable = false)
    private LocalDateTime drawDate;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public boolean canAcceptTickets() {
        return status == DrawStatus.DRAW_OPEN &&
                drawDate.isAfter(LocalDateTime.now());
    }

    public void setAsClosed() {
        if (this.status != DrawStatus.DRAW_OPEN) {
            throw new IllegalStateException("Draw can only be closed from DRAW_OPEN status, current: " + this.status);
        }
        this.status = DrawStatus.DRAW_CLOSED;
    }

    public void setAsExtracted() {
        if (this.status != DrawStatus.DRAW_CLOSED) {
            throw new IllegalStateException("Draw can only be extracted from DRAW_CLOSED status, current: " + this.status);
        }
        this.winningNumbers = DrawUtils.generateWinningNumbers();
        this.executedAt = LocalDateTime.now();
        this.status = DrawStatus.DRAW_EXTRACTED;
    }

    public void setAsFinalized() {
        if (this.status != DrawStatus.DRAW_EXTRACTED) {
            throw new IllegalStateException("Draw can only be finalized from DRAW_EXTRACTED status, current: " + this.status);
        }
        this.status = DrawStatus.DRAW_FINALIZED;
        this.prizesDistributedAt = LocalDateTime.now();
    }
    public static Draw createNew(LocalDateTime scheduledDate) {
        Draw draw = new Draw();
        draw.drawDate = scheduledDate;
        draw.status = DrawStatus.DRAW_OPEN;
        return draw;
    }

    public boolean isEligibleForProcess() {
        return this.status == DrawStatus.DRAW_OPEN;
    }
}