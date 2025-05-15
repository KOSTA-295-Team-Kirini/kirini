package business.service.board;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import dto.board.PostDTO;
import repository.dao.board.BoardDAO;

public class BoardService {
    private BoardDAO boardDAO;
    
    public BoardService() {
        this.boardDAO = new BoardDAO();
    }
    
    /**
     * 사용자 ID로 게시글 목록 조회
     */
    public List<PostDTO> getPostsByUserId(long userId, String boardType, int page, int pageSize) throws SQLException {
        return boardDAO.getPostsByUserId(userId, boardType, page, pageSize);
    }
    
    /**
     * 사용자 ID로 게시글 총 개수 조회
     */
    public int getTotalPostCountByUserId(long userId, String boardType) throws SQLException {
        return boardDAO.getTotalPostCountByUserId(userId, boardType);
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