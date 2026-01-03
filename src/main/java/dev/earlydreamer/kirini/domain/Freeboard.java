package dev.earlydreamer.kirini.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "freeboard",
        indexes = {
                @Index(name = "idx_freeboard_uid", columnList = "freeboard_uid"),
                @Index(name = "idx_account_uid", columnList = "account_uid"),
                @Index(name = "idx_freeboard_notify", columnList = "freeboard_notify"),
                @Index(name = "idx_freeboard_deleted", columnList = "freeboard_deleted")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Freeboard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "freeboard_uid")
    private Integer id;

    @Column(name = "freeboard_title", length = 50)
    private String title;

    @Column(name = "freeboard_contents", columnDefinition = "TEXT")
    private String contents;

    @Column(name = "freeboard_read")
    private Integer readCount;

    @Column(name = "freeboard_recommend")
    private Integer recommendCount;

    @Column(name = "freeboard_writetime")
    private LocalDateTime writeTime;

    @Column(name = "freeboard_modify_time")
    private LocalDateTime modifyTime;

    @Column(name = "freeboard_author_ip", length = 20)
    private String authorIp;

    @Enumerated(EnumType.STRING)
    @Column(name = "freeboard_notify")
    private NotifyType notifyType;

    @Enumerated(EnumType.STRING)
    @Column(name = "freeboard_deleted")
    private DeleteStatus deleteStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_uid", nullable = false)
    private User user;

    @OneToMany(mappedBy = "freeboard", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FreeboardComment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "freeboard", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FreeboardAttach> attachments = new ArrayList<>();

    public enum NotifyType {
        COMMON, NOTIFICATION
    }

    public enum DeleteStatus {
        MAINTAINED, DELETED
    }
}

