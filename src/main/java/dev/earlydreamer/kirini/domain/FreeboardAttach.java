package dev.earlydreamer.kirini.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "freeboard_attach")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FreeboardAttach {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attach_uid")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "freeboard_uid", nullable = false)
    private Freeboard freeboard;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "file_size", nullable = false)
    private Integer fileSize;

    @Column(name = "upload_date", nullable = false)
    private LocalDateTime uploadDate;
}

