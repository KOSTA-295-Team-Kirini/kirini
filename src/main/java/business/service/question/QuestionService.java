package business.service.question;

import java.sql.SQLException;
import java.util.List;

import dto.board.AnswerDTO;
import dto.board.AttachmentDTO;
import dto.board.QuestionDTO;
import dto.user.UserDTO;
import repository.dao.board.QuestionDAO;

public class QuestionService {
    private final QuestionDAO questionDAO;
    
    public QuestionService() {
        this.questionDAO = new QuestionDAO();
    }
    
    /**
     * 질문 ID로 질문 정보 조회
     */
    public QuestionDTO getQuestionById(long questionId) {
        try {
            return questionDAO.getQuestionById(questionId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 질문 생성
     */
    public boolean createQuestion(QuestionDTO question) {
        try {
            return questionDAO.createQuestion(question);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 질문 수정
     */
    public boolean updateQuestion(QuestionDTO question, long modifierId, String modifierAuthority) {
        try {
            return questionDAO.updateQuestion(question, modifierId, modifierAuthority);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 질문 삭제
     */
    public boolean deleteQuestion(long questionId, long deleterId, String reason) {
        try {
            return questionDAO.deleteQuestion(questionId, deleterId, reason);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 조회수 증가
     */
    public boolean increaseViewCount(long questionId) {
        try {
            return questionDAO.increaseViewCount(questionId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 모든 질문 목록 조회 (페이징)
     */
    public List<QuestionDTO> getAllQuestions(int page, int pageSize) {
        try {
            return questionDAO.getAllQuestions(page, pageSize);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 전체 질문 수 조회
     */
    public int getTotalQuestions() {
        try {
            return questionDAO.getTotalQuestions();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * 특정 사용자의 질문 목록 조회 (페이징)
     */
    public List<QuestionDTO> getQuestionsByUserId(long userId, int page, int pageSize) {
        try {
            return questionDAO.getQuestionsByUserId(userId, page, pageSize);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 특정 사용자의 전체 질문 수 조회
     */
    public int getTotalQuestionsByUserId(long userId) {
        try {
            return questionDAO.getTotalQuestionsByUserId(userId);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * 질문에 대한 답변 목록 조회
     */
    public List<AnswerDTO> getAnswersByQuestionId(long questionId) {
        try {
            return questionDAO.getAnswersByQuestionId(questionId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 답변 ID로 답변 정보 조회
     */
    public AnswerDTO getAnswerById(long answerId) {
        try {
            return questionDAO.getAnswerById(answerId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 답변 생성
     */
    public boolean createAnswer(AnswerDTO answer) {
        try {
            return questionDAO.createAnswer(answer);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 답변 수정
     */
    public boolean updateAnswer(AnswerDTO answer, long modifierId, String modifierAuthority) {
        try {
            return questionDAO.updateAnswer(answer, modifierId, modifierAuthority);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 답변 삭제
     */
    public boolean deleteAnswer(long answerId, long deleterId, String reason) {
        try {
            return questionDAO.deleteAnswer(answerId, deleterId, reason);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 첨부파일 추가
     */
    public boolean addAttachment(long questionId, String fileName, String filePath, long fileSize) {
        try {
            return questionDAO.addAttachment(questionId, fileName, filePath, fileSize);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 질문에 대한 첨부파일 목록 조회
     */
    public List<AttachmentDTO> getAttachmentsByQuestionId(long questionId) {
        try {
            return questionDAO.getAttachmentsByQuestionId(questionId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 첨부파일 ID로 첨부파일 정보 조회
     */
    public AttachmentDTO getAttachmentById(long attachId) {
        try {
            return questionDAO.getAttachmentById(attachId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 사용자 ID로 사용자 정보 조회
     */
    public UserDTO getUserById(long userId) {
        try {
            return questionDAO.getUserById(userId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}