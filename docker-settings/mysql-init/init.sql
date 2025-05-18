drop database if exists kirini_db;
create database kirini_db;

use kirini_db;

CREATE TABLE `keyboard_score` (
	`keyboard_score_uid`	int	NOT NULL AUTO_INCREMENT,
	`score_value`	int	NULL,
	`score_review`	text	NULL,
	`score_writetime`	datetime	NULL,
	`keyboard_information_uid`	int	NOT NULL,
	`user_uid`	int	NOT NULL,
	PRIMARY KEY (`keyboard_score_uid`)
);

CREATE TABLE `freeboard_comment` (
	`freeboard_comment_uid`	int	NOT NULL AUTO_INCREMENT,
	`freeboard_comment_contents`	TEXT	NULL,
	`freeboard_comment_writetime`	datetime	NULL,
	`freeboard_comment_modifytime`	datetime	NULL,
	`freeboard_comment_author_ip`	varchar(20)	NULL,
	`freeboard_uid`	int	NOT NULL,
	`user_uid`	int	NOT NULL,
	PRIMARY KEY (`freeboard_comment_uid`)
);

CREATE TABLE `user_penalty` (
	`penalty_uid`	int	NOT NULL AUTO_INCREMENT,
	`penalty_reason`	text	NOT NULL,
	`penalty_start_date`	datetime	NOT NULL,
	`penalty_end_date`	datetime	NULL,
	`penalty_status`	enum('active', 'inactive')	NOT NULL,
	`penalty_duration`	enum('temporary', 'permanent')	NOT NULL,
	`user_uid`	int	NOT NULL,
	PRIMARY KEY (`penalty_uid`)
);

CREATE TABLE `chatboard` (
	`chatboard_uid`	int	NOT NULL AUTO_INCREMENT,
	`chatboard_title`	varchar(50)	NULL,
	`chatboard_writetime`	datetime	NULL,
	`chatboard_modify_time`	datetime	NULL,
	`chatboard_author_ip`	varchar(20)	NULL,
	`chatboard_deleted`	enum('maintained', 'deleted')	NULL,
	`user_uid`	int	NOT NULL,
	PRIMARY KEY (`chatboard_uid`)
);

CREATE TABLE `log_delete_comment` (
	`log_delete_uid`	int	NOT NULL AUTO_INCREMENT,
	`log_delete_boardtype`	enum('freeboard','news','notice','inquiry','chatboard')	NOT NULL,
	`log_delete_date`	datetime	NOT NULL,
	`log_deleted_comment_uid`	int	NOT NULL,
	`user_uid`	int	NOT NULL,
	PRIMARY KEY (`log_delete_uid`)
);

CREATE TABLE `keyboard_category` (
	`keyboard_category_uid`	int	NOT NULL AUTO_INCREMENT,
	`keyboard_category_name`	varchar(50)	NULL,
	PRIMARY KEY (`keyboard_category_uid`)
);

CREATE TABLE `log_delete_post` (
	`log_delete_uid`	int	NOT NULL AUTO_INCREMENT,
	`log_delete_boardtype`	enum('freeboard','news','notice','inquiry','chatboard')	NOT NULL,
	`log_delete_date`	datetime	NOT NULL,
	`log_deleted_post_uid`	int	NOT NULL,
	`user_uid`	int	NOT NULL,
	PRIMARY KEY (`log_delete_uid`)
);

CREATE TABLE `log_recommend` (
	`log_recommend_uid`	int	NOT NULL AUTO_INCREMENT,
	`log_recommend_boardtype`	enum('freeboard','news','notice','inquiry','chatboard')	NOT NULL,
	`log_recommend_post_id`	int	NOT NULL,
	`log_recommend_date`	datetime	NOT NULL,
	`user_uid`	int	NOT NULL,
	PRIMARY KEY (`log_recommend_uid`)
);

CREATE TABLE `log_modify_comment` (
	`log_modify_uid`	int	NOT NULL AUTO_INCREMENT,
	`log_modify_boardtype`	enum('freeboard','news','notice','inquiry','chatboard')	NOT NULL,
	`log_modify_date`	datetime	NOT NULL,
	`log_modify_comment_uid`	int	NOT NULL,
	`user_uid`	int	NOT NULL,
	PRIMARY KEY (`log_modify_uid`)
);

CREATE TABLE `keyboard_information` (
	`keyboard_information_uid`	int	NOT NULL AUTO_INCREMENT,
	`keyboard_information_name`	varchar(50)	NULL,
	`keyboard_information_price`	int	NULL,
	`keyboard_category_uid`	int	NOT NULL,
	PRIMARY KEY (`keyboard_information_uid`)
);

CREATE TABLE `keyboard_tag` (
	`tag_uid`	int	NOT NULL AUTO_INCREMENT,
	`tag_name`	varchar(20)	NOT NULL,
	`tag_approve`	enum('waiting', 'approved')	NOT NULL,
	PRIMARY KEY (`tag_uid`)
);

CREATE TABLE `notice` (
	`notice_uid`	int	NOT NULL AUTO_INCREMENT,
	`notice_title`	varchar(50)	NULL,
	`notice_contents`	TEXT	NULL,
	`notice_read`	int	NULL,
	`notice_recommend`	int	NULL,
	`notice_writetime`	datetime	NULL,
	`notice_modify_time`	datetime	NULL,
	`notice_author_ip`	varchar(20)	NULL,
	`notice_deleted`	enum('maintained', 'deleted')	NULL,
	`user_uid`	int	NOT NULL,
	PRIMARY KEY (`notice_uid`)
);

CREATE TABLE `news` (
	`news_uid`	int	NOT NULL AUTO_INCREMENT,
	`news_title`	varchar(50)	NULL,
	`news_contents`	TEXT	NULL,
	`news_read`	int	NULL,
	`news_recommend`	int	NULL,
	`news_writetime`	datetime	NULL,
	`news_modify_time`	datetime	NULL,
	`news_author_ip`	varchar(20)	NULL,
	`news_deleted`	enum('maintained', 'deleted')	NULL,
	`user_uid`	int	NOT NULL,
	PRIMARY KEY (`news_uid`)
);

CREATE TABLE `keyboard_taglist` (
	`taglist_uid`	int	NOT NULL AUTO_INCREMENT,
	`tag_type`	enum('admin', 'user')	NOT NULL,
	`tag_uid`	int	NOT NULL,
	`keyboard_information_uid`	int	NOT NULL,
	PRIMARY KEY (`taglist_uid`)
);

CREATE TABLE `user` (
	`user_uid`	int	NOT NULL AUTO_INCREMENT,
	`user_id`	varchar(20)	NULL,
	`user_password`	varchar(600)	NULL,
	`user_name`	varchar(20)	NULL,
	`user_email`	varchar(50)	NULL,
	`user_introduce`	varchar(200)	NULL,
	`user_authority`	enum('normal', 'armband', 'admin')	NULL,
	`user_point`	int	NULL,
	`user_icon`	text	NULL,
	`user_status`	ENUM('active', 'restricted', 'suspended', 'banned')	NOT NULL DEFAULT 'active',
	PRIMARY KEY (`user_uid`)
);

CREATE TABLE `keyboard_glossary` (
	`keyboard_glossary_uid`	int	NOT NULL AUTO_INCREMENT,
	`keyboard_glossary_title`	varchar(20)	NULL,
	`keyboard_glossary_summary`	TEXT	NULL,
	`keyboard_glossary_url`	varchar(50)	NULL,
	PRIMARY KEY (`keyboard_glossary_uid`)
);

CREATE TABLE `freeboard` (
	`freeboard_uid`	int	NOT NULL AUTO_INCREMENT,
	`freeboard_title`	varchar(50)	NULL,
	`freeboard_contents`	text NULL,
	`freeboard_read`	int	NULL,
	`freeboard_recommend`	int	NULL,
	`freeboard_writetime`	datetime	NULL,
	`freeboard_modify_time`	datetime	NULL,
	`freeboard_author_ip`	varchar(20)	NULL,
	`freeboard_notify`	enum('common', 'notification')	NULL,
	`freeboard_deleted`	enum('maintained', 'deleted')	NULL,
	`user_uid`	int	NOT NULL,
	PRIMARY KEY (`freeboard_uid`)
);

CREATE TABLE `scrap` (
	`scrap_uid`	int	NOT NULL AUTO_INCREMENT,
	`scrap_user_uid`	int	NULL,
	`scrap_date`	datetime	NULL,
	`keyboard_information_uid`	int	NOT NULL,
	PRIMARY KEY (`scrap_uid`)
);

CREATE TABLE `report` (
	`report_uid`	int	NOT NULL AUTO_INCREMENT,
	`report_target_type` ENUM(
                         'spam_ad',                  -- 스팸/광고성 게시물
                         'profanity_hate_speech',    -- 욕설·혐오 발언
                         'adult_content',            -- 음란물·18금 콘텐츠
                         'impersonation_fraud',      -- 사칭·사기 행위
                         'copyright_infringement'    -- 저작권·지식재산권 침해
                       )              NOT NULL,
	`report_reason`	text	NULL,
	`report_status`	enum('active', 'inactive')	NOT NULL,
	`report_createtime`	datetime	NOT NULL,
	`report_user_uid`	int	NOT NULL,
	`target_user_uid`	int	NOT NULL,
	PRIMARY KEY (`report_uid`)
);

CREATE TABLE `news_comment` (
	`news_comment_uid`	int	NOT NULL AUTO_INCREMENT,
	`news_comment_contents`	TEXT	NULL,
	`news_comment_writetime`	datetime	NULL,
	`news_comment_modifytime`	datetime	NULL,
	`news_comment_author_ip`	varchar(20)	NULL,
	`news_uid`	int	NOT NULL,
	`user_uid`	int	NOT NULL,
	PRIMARY KEY (`news_comment_uid`)
);

CREATE TABLE `log_modify_post` (
	`log_modify_uid`	int	NOT NULL AUTO_INCREMENT,
	`log_modify_boardtype`	enum('freeboard','news','notice','inquiry','chatboard')	NOT NULL,
	`log_modify_date`	datetime	NOT NULL,
	`log_modify_post_uid`	int	NOT NULL,
	`user_uid`	int	NOT NULL,
	PRIMARY KEY (`log_modify_uid`)
);

CREATE TABLE `inquiry` (
	`inquiry_uid`	int	NOT NULL AUTO_INCREMENT,
	`inquiry_title`	varchar(50)	NOT NULL,
	`inquiry_contents`	TEXT	NOT NULL,
	`inquiry_writetime`	datetime	NOT NULL,
	`inquiry_modify_time`	datetime	NOT NULL,
	`inquiry_author_ip`	varchar(20)	NOT NULL,
	`inquiry_deleted`	enum('maintained', 'deleted')	NOT NULL,
	`user_uid`	int	NOT NULL,
	`inquiry_parent_uid`	int	NULL,
	`inquiry_category`	enum('question','feedback')	NOT NULL,
	`inquiry_read_status`	enum('read','unread')	NOT NULL,
	PRIMARY KEY (`inquiry_uid`)
);

CREATE TABLE `freeboard_attach` (
  `attach_uid` int NOT NULL AUTO_INCREMENT,
  `freeboard_uid` int NOT NULL,
  `file_name` varchar(255) NOT NULL,
  `file_path` varchar(500) NOT NULL,
  `file_size` int NOT NULL,
  `upload_date` datetime NOT NULL,
  PRIMARY KEY (`attach_uid`),
  FOREIGN KEY (`freeboard_uid`) REFERENCES `freeboard` (`freeboard_uid`)
);

-- keyboard_score
ALTER TABLE `keyboard_score` ADD CONSTRAINT `FK_keyboard_information_TO_keyboard_score` 
FOREIGN KEY (`keyboard_information_uid`) REFERENCES `keyboard_information` (`keyboard_information_uid`);
ALTER TABLE `keyboard_score` ADD CONSTRAINT `FK_user_TO_keyboard_score` 
FOREIGN KEY (`user_uid`) REFERENCES `user` (`user_uid`);

-- freeboard_comment
ALTER TABLE `freeboard_comment` ADD CONSTRAINT `FK_freeboard_TO_freeboard_comment` 
FOREIGN KEY (`freeboard_uid`) REFERENCES `freeboard` (`freeboard_uid`);
ALTER TABLE `freeboard_comment` ADD CONSTRAINT `FK_user_TO_freeboard_comment` 
FOREIGN KEY (`user_uid`) REFERENCES `user` (`user_uid`);

-- user_penalty
ALTER TABLE `user_penalty` ADD CONSTRAINT `FK_user_TO_user_penalty` 
FOREIGN KEY (`user_uid`) REFERENCES `user` (`user_uid`);

-- chatboard
ALTER TABLE `chatboard` ADD CONSTRAINT `FK_user_TO_chatboard` 
FOREIGN KEY (`user_uid`) REFERENCES `user` (`user_uid`);

-- log_delete_comment
ALTER TABLE `log_delete_comment` ADD CONSTRAINT `FK_user_TO_log_delete_comment` 
FOREIGN KEY (`user_uid`) REFERENCES `user` (`user_uid`);

-- log_delete_post
ALTER TABLE `log_delete_post` ADD CONSTRAINT `FK_user_TO_log_delete_post` 
FOREIGN KEY (`user_uid`) REFERENCES `user` (`user_uid`);

-- log_recommend
ALTER TABLE `log_recommend` ADD CONSTRAINT `FK_user_TO_log_recommend` 
FOREIGN KEY (`user_uid`) REFERENCES `user` (`user_uid`);

-- log_modify_comment
ALTER TABLE `log_modify_comment` ADD CONSTRAINT `FK_user_TO_log_modify_comment` 
FOREIGN KEY (`user_uid`) REFERENCES `user` (`user_uid`);

-- keyboard_information
ALTER TABLE `keyboard_information` ADD CONSTRAINT `FK_keyboard_category_TO_keyboard_information` 
FOREIGN KEY (`keyboard_category_uid`) REFERENCES `keyboard_category` (`keyboard_category_uid`);

-- notice
ALTER TABLE `notice` ADD CONSTRAINT `FK_user_TO_notice` 
FOREIGN KEY (`user_uid`) REFERENCES `user` (`user_uid`);

-- news
ALTER TABLE `news` ADD CONSTRAINT `FK_user_TO_news` 
FOREIGN KEY (`user_uid`) REFERENCES `user` (`user_uid`);

-- keyboard_taglist
ALTER TABLE `keyboard_taglist` ADD CONSTRAINT `FK_keyboard_tag_TO_keyboard_taglist` 
FOREIGN KEY (`tag_uid`) REFERENCES `keyboard_tag` (`tag_uid`);
ALTER TABLE `keyboard_taglist` ADD CONSTRAINT `FK_keyboard_information_TO_keyboard_taglist` 
FOREIGN KEY (`keyboard_information_uid`) REFERENCES `keyboard_information` (`keyboard_information_uid`);

-- freeboard
ALTER TABLE `freeboard` ADD CONSTRAINT `FK_user_TO_freeboard` 
FOREIGN KEY (`user_uid`) REFERENCES `user` (`user_uid`);

-- scrap
ALTER TABLE `scrap` ADD CONSTRAINT `FK_user_TO_scrap` 
FOREIGN KEY (`scrap_user_uid`) REFERENCES `user` (`user_uid`);
ALTER TABLE `scrap` ADD CONSTRAINT `FK_keyboard_information_TO_scrap` 
FOREIGN KEY (`keyboard_information_uid`) REFERENCES `keyboard_information` (`keyboard_information_uid`);

-- report
ALTER TABLE `report` ADD CONSTRAINT `FK_user_report_TO_report` 
FOREIGN KEY (`report_user_uid`) REFERENCES `user` (`user_uid`);
ALTER TABLE `report` ADD CONSTRAINT `FK_user_target_TO_report` 
FOREIGN KEY (`target_user_uid`) REFERENCES `user` (`user_uid`);

-- news_comment
ALTER TABLE `news_comment` ADD CONSTRAINT `FK_news_TO_news_comment` 
FOREIGN KEY (`news_uid`) REFERENCES `news` (`news_uid`);
ALTER TABLE `news_comment` ADD CONSTRAINT `FK_user_TO_news_comment` 
FOREIGN KEY (`user_uid`) REFERENCES `user` (`user_uid`);

-- log_modify_post
ALTER TABLE `log_modify_post` ADD CONSTRAINT `FK_user_TO_log_modify_post` 
FOREIGN KEY (`user_uid`) REFERENCES `user` (`user_uid`);

-- inquiry
ALTER TABLE `inquiry` ADD CONSTRAINT `FK_user_TO_inquiry` 
FOREIGN KEY (`user_uid`) REFERENCES `user` (`user_uid`);
ALTER TABLE `inquiry` ADD CONSTRAINT `FK_inquiry_TO_inquiry` 
FOREIGN KEY (`inquiry_parent_uid`) REFERENCES `inquiry` (`inquiry_uid`);

-- 데이터베이스에 인덱스 추가 (별도 SQL 스크립트로 실행)
CREATE INDEX idx_freeboard_uid ON freeboard(freeboard_uid);
CREATE INDEX idx_user_uid ON freeboard(user_uid);
CREATE INDEX idx_freeboard_notify ON freeboard(freeboard_notify);
CREATE INDEX idx_freeboard_deleted ON freeboard(freeboard_deleted);

-- 전문 검색용 인덱스 (MySQL 기준)
CREATE FULLTEXT INDEX idx_freeboard_title_contents 
ON freeboard(freeboard_title, freeboard_contents);

INSERT INTO user (user_id, user_password, user_name, user_authority) 
VALUES ('admin', 'admin', '관리자', 'admin');

INSERT INTO user (user_id, user_password, user_name, user_email, user_introduce, user_authority, user_point) 
VALUES ('testuser', 'password123', '테스트닉네임', 'test@email.com', '', 'normal', 0);

select * from user;
