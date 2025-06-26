package com.assesment.lottofun.entity;

import com.assesment.lottofun.util.NumberUtils;
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
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Data
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.valueOf(1000.00);

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Ticket> tickets;

    public boolean hasSufficientBalance(BigDecimal amount) {
        return balance.compareTo(amount) >= 0;
    }

    public void deductBalance(BigDecimal amount) {
        if (!hasSufficientBalance(amount)) {
            throw new IllegalArgumentException("Insufficient balance");
        }
        this.balance = this.balance.subtract(amount);
    }

    public void addBalance(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    public boolean hasTicketAlready(Long drawId, Set<Integer> selectedNumbers) {
        String sortedNumbers = NumberUtils.numbersToString(selectedNumbers);

        return tickets.stream()
                .anyMatch(ticket -> ticket.getDraw().getId().equals(drawId) &&
                        ticket.getSelectedNumbers().equals(sortedNumbers));
    }

    public List<Ticket> getWinningTickets() {
        return tickets.stream()
                .filter(ticket -> ticket.getStatus() == TicketStatus.WON)
                .collect(Collectors.toList());
    }

    public List<Ticket> getTicketsForDraw(Long drawId) {
        return tickets.stream()
                .filter(ticket -> ticket.getDraw().getId().equals(drawId))
                .collect(Collectors.toList());
    }

    public List<Ticket> getClaimableTickets() {
        return tickets.stream()
                .filter(Ticket::isClaimable)
                .collect(Collectors.toList());
    }

    public Ticket getTicketById(Long ticketId) {
        return tickets.stream()
                .filter(ticket -> ticket.getId().equals(ticketId))
                .findFirst()
                .orElse(null);
    }

    public List<Ticket> getAllTicketsSortedByDate() {
        return tickets.stream()
                .sorted((t1, t2) -> t2.getPurchaseTimestamp().compareTo(t1.getPurchaseTimestamp()))
                .collect(Collectors.toList());
    }

    public void claimTicket(Ticket ticket) {
        ticket.setAsClaimed();
        this.addBalance(ticket.getPrizeAmount());
    }
}