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
import java.util.UUID;

@Entity
@Data
@Table(name = "tickets",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_draw_numbers",
                columnNames = {"user_id", "draw_id", "selected_numbers_hash"}
        ))
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_number", unique = true, nullable = false)
    private String ticketNumber;

    @Column(name = "selected_numbers", nullable = false)
    private String selectedNumbers;

    @Column(name = "selected_numbers_hash", nullable = false)
    private String selectedNumbersHash;

    @Column(name = "purchase_price", nullable = false, precision = 8, scale = 2)
    private BigDecimal purchasePrice;

    @Column(name = "prize_amount", precision = 10, scale = 2)
    private BigDecimal prizeAmount;

    @Column(name = "match_count")
    private Integer matchCount;

    @Column(name = "match_numbers")
    private String matchedNumbers;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TicketStatus status = TicketStatus.WAITING_FOR_DRAW;

    @CreationTimestamp
    @Column(name = "purchase_timestamp")
    private LocalDateTime purchaseTimestamp;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "claimed_at")
    private LocalDateTime claimedAt;

    // Foreign key columns
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "draw_id", nullable = false)
    private Long drawId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "draw_id", insertable = false, updatable = false)
    private Draw draw;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @PrePersist
    private void generateTicketNumber() {
        this.ticketNumber = "TKT-" + System.currentTimeMillis() + "-" +
                UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}