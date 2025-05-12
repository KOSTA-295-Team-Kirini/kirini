package dto.board;

import java.time.LocalDateTime;
import java.util.List;

public class FreeboardDTO {
    private long postId;
    private long userId;
    private String title;
    private String content;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
    private int viewCount;
    private int recommendCount;
    private boolean isNotice;
    private boolean isHidden;
    private String hideReason;
    private List<String> attachFiles;
    
    // Getter, Setter, 생성자 등
}