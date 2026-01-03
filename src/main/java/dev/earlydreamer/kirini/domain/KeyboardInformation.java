package dev.earlydreamer.kirini.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "keyboard_information")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KeyboardInformation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "keyboard_information_uid")
    private Integer id;

    @Column(name = "keyboard_information_name", length = 50)
    private String name;

    @Column(name = "keyboard_information_price")
    private Integer price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "keyboard_category_uid", nullable = false)
    private KeyboardCategory category;

    @OneToMany(mappedBy = "keyboardInformation", cascade = CascadeType.ALL)
    private List<KeyboardScore> scores = new ArrayList<>();

    @OneToMany(mappedBy = "keyboardInformation", cascade = CascadeType.ALL)
    private List<KeyboardTaglist> taglists = new ArrayList<>();

    @OneToMany(mappedBy = "keyboardInformation", cascade = CascadeType.ALL)
    private List<Scrap> scraps = new ArrayList<>();
}

