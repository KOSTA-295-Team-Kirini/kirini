package dto.log;

import java.time.LocalDateTime;

/**
 * 시스템 로그 DTO 클래스
 */
public class SystemLogDTO {
    private long logId;
    private String logLevel;
    private String logMessage;
    private String logException;
    private String logClass;
    private String logMethod;
    private LocalDateTime logTimestamp;
    private Long userId;

    public SystemLogDTO() {
    }

    public SystemLogDTO(String logLevel, String logMessage, String logClass, String logMethod) {
        this.logLevel = logLevel;
        this.logMessage = logMessage;
        this.logClass = logClass;
        this.logMethod = logMethod;
        this.logTimestamp = LocalDateTime.now();
    }

    public SystemLogDTO(String logLevel, String logMessage, String logException, String logClass, String logMethod, Long userId) {
        this.logLevel = logLevel;
        this.logMessage = logMessage;
        this.logException = logException;
        this.logClass = logClass;
        this.logMethod = logMethod;
        this.logTimestamp = LocalDateTime.now();
        this.userId = userId;
    }

    // Getters and Setters
    public long getLogId() {
        return logId;
    }

    public void setLogId(long logId) {
        this.logId = logId;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public String getLogMessage() {
        return logMessage;
    }

    public void setLogMessage(String logMessage) {
        this.logMessage = logMessage;
    }

    public String getLogException() {
        return logException;
    }

    public void setLogException(String logException) {
        this.logException = logException;
    }

    public String getLogClass() {
        return logClass;
    }

    public void setLogClass(String logClass) {
        this.logClass = logClass;
    }

    public String getLogMethod() {
        return logMethod;
    }

    public void setLogMethod(String logMethod) {
        this.logMethod = logMethod;
    }

    public LocalDateTime getLogTimestamp() {
        return logTimestamp;
    }

    public void setLogTimestamp(LocalDateTime logTimestamp) {
        this.logTimestamp = logTimestamp;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "SystemLogDTO{" +
                "logId=" + logId +
                ", logLevel='" + logLevel + '\'' +
                ", logMessage='" + logMessage + '\'' +
                ", logClass='" + logClass + '\'' +
                ", logTimestamp=" + logTimestamp +
                '}';
    }
}
