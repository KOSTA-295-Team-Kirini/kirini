package dev.earlydreamer.kirini.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "keyboard_tag")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KeyboardTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_uid")
    private Integer id;

    @Column(name = "tag_name", nullable = false, length = 20)
    private String tagName;

    @Enumerated(EnumType.STRING)
    @Column(name = "tag_approve", nullable = false)
    private ApproveStatus approveStatus;

    @OneToMany(mappedBy = "tag", cascade = CascadeType.ALL)
    private List<KeyboardTaglist> taglists = new ArrayList<>();

    public enum ApproveStatus {
        WAITING, APPROVED
    }
}

