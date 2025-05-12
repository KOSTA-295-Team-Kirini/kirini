package dto.board;

import java.time.LocalDateTime;

public class CommentDTO {
    private long commentId;
    private long postId;
    private long userId;
    private String content;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
    private String boardType; // 어떤 게시판의 댓글인지 (자유게시판, 뉴스 등)
    
    // Getter, Setter, 생성자 등
}