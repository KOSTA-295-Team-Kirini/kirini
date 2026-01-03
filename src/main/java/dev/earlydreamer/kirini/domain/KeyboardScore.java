package dev.earlydreamer.kirini.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "keyboard_score")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KeyboardScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "keyboard_score_uid")
    private Integer id;

    @Column(name = "score_value")
    private Integer scoreValue;

    @Column(name = "score_review", columnDefinition = "TEXT")
    private String review;

    @Column(name = "score_writetime")
    private LocalDateTime writeTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "keyboard_information_uid", nullable = false)
    private KeyboardInformation keyboardInformation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_uid", nullable = false)
    private User user;
}

