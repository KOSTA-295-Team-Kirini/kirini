package dev.earlydreamer.kirini.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "account_penalty")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserPenalty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "penalty_uid")
    private Integer id;

    @Column(name = "penalty_reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "penalty_start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "penalty_end_date")
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "penalty_status", nullable = false)
    private PenaltyStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "penalty_duration", nullable = false)
    private PenaltyDuration duration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_uid", nullable = false)
    private User user;

    public enum PenaltyStatus {
        ACTIVE, INACTIVE
    }

    public enum PenaltyDuration {
        TEMPORARY, PERMANENT
    }
}

