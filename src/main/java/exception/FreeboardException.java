package exception;

public class FreeboardException extends Exception {
    private String errorCode;
    
    public FreeboardException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}