package com.assesment.lottofun.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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

    @Column(name = "total_tickets", nullable = false)
    @Builder.Default
    private Integer totalTickets = 0;

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

    @OneToMany(mappedBy = "draw", fetch = FetchType.LAZY)
    private List<Ticket> tickets;

    public boolean canAcceptTickets() {
        return status == DrawStatus.DRAW_OPEN &&
                drawDate.isAfter(LocalDateTime.now());
    }

    public boolean isReadyForExecution() {
        return status == DrawStatus.DRAW_OPEN &&
                !drawDate.isAfter(LocalDateTime.now());
    }

    public void registerTicket(BigDecimal ticketPrice) {
        this.totalTickets++;
        this.totalPrizePool = this.totalPrizePool.add(ticketPrice);
    }
}