package com.assesment.lottofun.entity;

import com.assesment.lottofun.exception.BusinessException;
import com.assesment.lottofun.infrastructure.configuration.PrizeRules;
import com.assesment.lottofun.util.DrawUtil;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Data
@Table(name = "tickets",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_draw_numbers",
                columnNames = {"user_id", "draw_id", "selected_numbers"}
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

    @Column(name = "purchase_price", nullable = false, precision = 8, scale = 2)
    private BigDecimal purchasePrice;

    @Column(name = "prize_amount", precision = 10, scale = 2)
    private BigDecimal prizeAmount;

    @Column(name = "match_count")
    private Integer matchCount;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "draw_id")
    private Draw draw;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @PrePersist
    private void generateTicketNumber() {
        this.ticketNumber = "TKT-" + System.currentTimeMillis() + "-" +
                UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public void setAsClaimed() {
        if (!isClaimable()) {
            throw new BusinessException("Only winning tickets can be claimed");
        }
        this.status = TicketStatus.PRIZE_CLAIMED;
    }

    public boolean isClaimable() {
        return this.status == TicketStatus.WON;
    }

    public void calculateResult(String winningNumbers) {
        if (this.status != TicketStatus.WAITING_FOR_DRAW) {
            throw new IllegalStateException("Ticket can only be marked as extracted from WAITING_FOR_DRAW status, current: " + this.status);
        }
        this.matchCount = getMatchCount(winningNumbers);
        this.status = matchCount >= 2 ? TicketStatus.WON : TicketStatus.NOT_WON;
        this.prizeAmount = PrizeRules.getPrize(matchCount);
    }

    private int getMatchCount(String winningNumbers) {
        Set<Integer> winningNumbersSet = DrawUtil.stringToNumbers(winningNumbers);
        Set<Integer> selectedNumbersSet = DrawUtil.stringToNumbers(selectedNumbers);
        selectedNumbersSet.retainAll(winningNumbersSet);
        return selectedNumbersSet.size();
    }


    public static Ticket createNew(
            User user,
            Draw draw,
            Set<Integer> selectedNumbers,
            BigDecimal purchasePrice
    ) {
        Ticket ticket = new Ticket();
        ticket.selectedNumbers = DrawUtil.numbersToString(selectedNumbers);
        ticket.purchasePrice = purchasePrice;
        ticket.status = TicketStatus.WAITING_FOR_DRAW;
        ticket.draw = draw;
        ticket.user = user;
        return ticket;
    }
}