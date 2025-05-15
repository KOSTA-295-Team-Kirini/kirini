package business.service.board;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import dto.board.PostDTO;
import repository.dao.board.FreeboardDAO;

public class BoardService {
    private FreeboardDAO freeboardDAO;
    
    public BoardService() {
        this.freeboardDAO = new FreeboardDAO();
    }
    
    /**
     * 사용자 ID로 게시글 목록 조회
     * FreeboardDAO를 활용하여 PostDTO 형태로 변환하여 반환
     */
    public List<PostDTO> getPostsByUserId(long userId, String boardType, int page, int pageSize) throws SQLException {
        List<PostDTO> results = new ArrayList<>();
        
        // 실제 구현은 나중에 필요에 따라 FreeboardDAO에 메서드를 추가해야 함
        // 현재는 임시로 빈 리스트 반환
        
        return results;
    }
    
    /**
     * 사용자 ID로 게시글 총 개수 조회
     * FreeboardDAO를 활용하여 구현
     */
    public int getTotalPostCountByUserId(long userId, String boardType) throws SQLException {
        // 실제 구현은 나중에 필요에 따라 FreeboardDAO에 메서드를 추가해야 함
        return 0;
    }
    
    /**
     * 게시판 종류 목록 조회
     */
    public List<String> getBoardTypes() {
        List<String> boardTypes = new ArrayList<>();
        boardTypes.add("freeboard");
        boardTypes.add("news");
        boardTypes.add("notice");
        boardTypes.add("inquiry");
        return boardTypes;
    }
}