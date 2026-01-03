package dev.earlydreamer.kirini.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "keyboard_taglist")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KeyboardTaglist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "taglist_uid")
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tag_type", nullable = false)
    private TagType tagType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_uid", nullable = false)
    private KeyboardTag tag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "keyboard_information_uid", nullable = false)
    private KeyboardInformation keyboardInformation;

    public enum TagType {
        ADMIN, USER
    }
}

