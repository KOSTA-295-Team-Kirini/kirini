package dev.earlydreamer.kirini.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "freeboard_comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FreeboardComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "freeboard_comment_uid")
    private Integer id;

    @Column(name = "freeboard_comment_contents", columnDefinition = "TEXT")
    private String contents;

    @Column(name = "freeboard_comment_writetime")
    private LocalDateTime writeTime;

    @Column(name = "freeboard_comment_modifytime")
    private LocalDateTime modifyTime;

    @Column(name = "freeboard_comment_author_ip", length = 20)
    private String authorIp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "freeboard_uid", nullable = false)
    private Freeboard freeboard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_uid", nullable = false)
    private User user;
}

