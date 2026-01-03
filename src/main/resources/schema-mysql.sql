-- MySQL 8.4.4 LTS용 스키마 (Hibernate 7.2 호환)
-- 테이블이 존재하면 생성하지 않음 (IF NOT EXISTS)

-- Account 테이블 (기존 User)
CREATE TABLE IF NOT EXISTS `account` (
    `account_uid` INT NOT NULL AUTO_INCREMENT,
    `account_id` VARCHAR(20) NULL,
    `account_password` VARCHAR(600) NULL,
    `account_name` VARCHAR(20) NULL,
    `account_email` VARCHAR(50) NULL,
    `account_introduce` VARCHAR(200) NULL,
    `account_authority` ENUM('NORMAL', 'ARMBAND', 'ADMIN') NULL DEFAULT 'NORMAL',
    `account_point` INT NULL DEFAULT 0,
    `account_icon` TEXT NULL,
    `account_status` ENUM('ACTIVE', 'RESTRICTED', 'SUSPENDED', 'BANNED') NOT NULL DEFAULT 'ACTIVE',
    PRIMARY KEY (`account_uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Account Penalty 테이블 (기존 User Penalty)
CREATE TABLE IF NOT EXISTS `account_penalty` (
    `penalty_uid` INT NOT NULL AUTO_INCREMENT,
    `penalty_reason` TEXT NOT NULL,
    `penalty_start_date` DATETIME NOT NULL,
    `penalty_end_date` DATETIME NULL,
    `penalty_status` ENUM('ACTIVE', 'INACTIVE') NOT NULL,
    `penalty_duration` ENUM('TEMPORARY', 'PERMANENT') NOT NULL,
    `account_uid` INT NOT NULL,
    PRIMARY KEY (`penalty_uid`),
    INDEX `idx_account_penalty_account_uid` (`account_uid`),
    CONSTRAINT `fk_account_penalty_account` FOREIGN KEY (`account_uid`) REFERENCES `account` (`account_uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Keyboard Category 테이블
CREATE TABLE IF NOT EXISTS `keyboard_category` (
    `keyboard_category_uid` INT NOT NULL AUTO_INCREMENT,
    `keyboard_category_name` VARCHAR(50) NULL,
    `category_type` ENUM('SWITCH', 'KEYCAP', 'CASE', 'PCB', 'PLATE', 'STABILIZER', 'OTHER') NULL,
    PRIMARY KEY (`keyboard_category_uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Keyboard Information 테이블
CREATE TABLE IF NOT EXISTS `keyboard_information` (
    `keyboard_information_uid` INT NOT NULL AUTO_INCREMENT,
    `keyboard_information_name` VARCHAR(50) NULL,
    `keyboard_information_price` INT NULL,
    `keyboard_category_uid` INT NOT NULL,
    PRIMARY KEY (`keyboard_information_uid`),
    INDEX `idx_keyboard_info_category` (`keyboard_category_uid`),
    CONSTRAINT `fk_keyboard_info_category` FOREIGN KEY (`keyboard_category_uid`) REFERENCES `keyboard_category` (`keyboard_category_uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Keyboard Score 테이블
CREATE TABLE IF NOT EXISTS `keyboard_score` (
    `keyboard_score_uid` INT NOT NULL AUTO_INCREMENT,
    `score_value` INT NULL,
    `score_review` TEXT NULL,
    `score_writetime` DATETIME NULL,
    `keyboard_information_uid` INT NOT NULL,
    `account_uid` INT NOT NULL,
    PRIMARY KEY (`keyboard_score_uid`),
    INDEX `idx_keyboard_score_info` (`keyboard_information_uid`),
    INDEX `idx_keyboard_score_account` (`account_uid`),
    CONSTRAINT `fk_keyboard_score_info` FOREIGN KEY (`keyboard_information_uid`) REFERENCES `keyboard_information` (`keyboard_information_uid`),
    CONSTRAINT `fk_keyboard_score_account` FOREIGN KEY (`account_uid`) REFERENCES `account` (`account_uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Keyboard Tag 테이블
CREATE TABLE IF NOT EXISTS `keyboard_tag` (
    `tag_uid` INT NOT NULL AUTO_INCREMENT,
    `tag_name` VARCHAR(20) NOT NULL,
    `tag_approve` ENUM('WAITING', 'APPROVED') NOT NULL,
    PRIMARY KEY (`tag_uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Keyboard Taglist 테이블
CREATE TABLE IF NOT EXISTS `keyboard_taglist` (
    `taglist_uid` INT NOT NULL AUTO_INCREMENT,
    `tag_type` ENUM('ADMIN', 'USER') NOT NULL,
    `tag_uid` INT NOT NULL,
    `keyboard_information_uid` INT NOT NULL,
    PRIMARY KEY (`taglist_uid`),
    INDEX `idx_taglist_tag` (`tag_uid`),
    INDEX `idx_taglist_info` (`keyboard_information_uid`),
    CONSTRAINT `fk_taglist_tag` FOREIGN KEY (`tag_uid`) REFERENCES `keyboard_tag` (`tag_uid`),
    CONSTRAINT `fk_taglist_info` FOREIGN KEY (`keyboard_information_uid`) REFERENCES `keyboard_information` (`keyboard_information_uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Freeboard 테이블
CREATE TABLE IF NOT EXISTS `freeboard` (
    `freeboard_uid` INT NOT NULL AUTO_INCREMENT,
    `freeboard_title` VARCHAR(50) NULL,
    `freeboard_contents` TEXT NULL,
    `freeboard_read` INT NULL DEFAULT 0,
    `freeboard_recommend` INT NULL DEFAULT 0,
    `freeboard_writetime` DATETIME NULL,
    `freeboard_modify_time` DATETIME NULL,
    `freeboard_author_ip` VARCHAR(20) NULL,
    `freeboard_notify` ENUM('COMMON', 'NOTIFICATION') NULL DEFAULT 'COMMON',
    `freeboard_deleted` ENUM('MAINTAINED', 'DELETED') NULL DEFAULT 'MAINTAINED',
    `account_uid` INT NOT NULL,
    PRIMARY KEY (`freeboard_uid`),
    INDEX `idx_freeboard_account_uid` (`account_uid`),
    INDEX `idx_freeboard_notify` (`freeboard_notify`),
    INDEX `idx_freeboard_deleted` (`freeboard_deleted`),
    CONSTRAINT `fk_freeboard_account` FOREIGN KEY (`account_uid`) REFERENCES `account` (`account_uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Freeboard Attach 테이블
CREATE TABLE IF NOT EXISTS `freeboard_attach` (
    `attach_uid` INT NOT NULL AUTO_INCREMENT,
    `freeboard_uid` INT NOT NULL,
    `file_name` VARCHAR(255) NOT NULL,
    `file_path` VARCHAR(500) NOT NULL,
    `file_size` BIGINT NULL,
    `upload_time` DATETIME NULL,
    PRIMARY KEY (`attach_uid`),
    INDEX `idx_attach_freeboard` (`freeboard_uid`),
    CONSTRAINT `fk_attach_freeboard` FOREIGN KEY (`freeboard_uid`) REFERENCES `freeboard` (`freeboard_uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Freeboard Comment 테이블
CREATE TABLE IF NOT EXISTS `freeboard_comment` (
    `freeboard_comment_uid` INT NOT NULL AUTO_INCREMENT,
    `freeboard_comment_contents` TEXT NULL,
    `freeboard_comment_writetime` DATETIME NULL,
    `freeboard_comment_modifytime` DATETIME NULL,
    `freeboard_comment_author_ip` VARCHAR(20) NULL,
    `freeboard_uid` INT NOT NULL,
    `account_uid` INT NOT NULL,
    PRIMARY KEY (`freeboard_comment_uid`),
    INDEX `idx_comment_freeboard` (`freeboard_uid`),
    INDEX `idx_comment_account` (`account_uid`),
    CONSTRAINT `fk_comment_freeboard` FOREIGN KEY (`freeboard_uid`) REFERENCES `freeboard` (`freeboard_uid`),
    CONSTRAINT `fk_comment_account` FOREIGN KEY (`account_uid`) REFERENCES `account` (`account_uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Scrap 테이블
CREATE TABLE IF NOT EXISTS `scrap` (
    `scrap_uid` INT NOT NULL AUTO_INCREMENT,
    `scrap_date` DATETIME NULL,
    `scrap_account_uid` INT NULL,
    `keyboard_information_uid` INT NULL,
    PRIMARY KEY (`scrap_uid`),
    INDEX `idx_scrap_account` (`scrap_account_uid`),
    INDEX `idx_scrap_keyboard` (`keyboard_information_uid`),
    CONSTRAINT `fk_scrap_account` FOREIGN KEY (`scrap_account_uid`) REFERENCES `account` (`account_uid`),
    CONSTRAINT `fk_scrap_keyboard` FOREIGN KEY (`keyboard_information_uid`) REFERENCES `keyboard_information` (`keyboard_information_uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

