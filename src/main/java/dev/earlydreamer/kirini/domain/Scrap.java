package dev.earlydreamer.kirini.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "scrap")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Scrap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "scrap_uid")
    private Integer id;

    @Column(name = "scrap_date")
    private LocalDateTime scrapDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scrap_account_uid")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "keyboard_information_uid", nullable = false)
    private KeyboardInformation keyboardInformation;
}

