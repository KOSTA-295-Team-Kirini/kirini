package dev.earlydreamer.kirini.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "keyboard_category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KeyboardCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "keyboard_category_uid")
    private Integer id;

    @Column(name = "keyboard_category_name", length = 50)
    private String categoryName;

    @Enumerated(EnumType.STRING)
    @Column(name = "category_type")
    private CategoryType categoryType;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<KeyboardInformation> keyboardInformations = new ArrayList<>();

    public enum CategoryType {
        SWITCH, LAYOUT, CONNECT
    }
}

