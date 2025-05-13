CREATE TABLE `keyboard_score` (
	`keyboard_score_uid`	int	NOT NULL,
	`score_value`	int	NULL,
	`score_review`	text	NULL,
	`score_writetime`	datetime	NULL,
	`keyboard_information_uid`	int	NOT NULL,
	`user_uid`	int	NOT NULL
);

CREATE TABLE `freeboard_comment` (
	`freeboard_comment_uid`	int	NOT NULL,
	`freeboard_comment_contents`	varchar(500)	NULL,
	`freeboard_comment_writetime`	datetime	NULL,
	`freeboard_comment_modifytime`	datetime	NULL,
	`freeboard_comment_author_ip`	varchar(20)	NULL,
	`freeboard_uid`	int	NOT NULL,
	`user_uid`	int	NOT NULL
);

CREATE TABLE `user_penalty` (
	`penalty_uid`	int	NOT NULL,
	`penalty_reason`	text	NOT NULL,
	`penalty_start_date`	datetime	NOT NULL,
	`penalty_end_date`	datetime	NULL,
	`penalty_status`	enum('active', 'inactive')	NOT NULL,
	`penalty_duration`	enum('temporary', 'permanent')	NOT NULL,
	`user_uid`	int	NOT NULL
);

CREATE TABLE `chatboard` (
	`chatboard_uid`	int	NOT NULL,
	`chatboard_title`	varchar(50)	NULL,
	`chatboard_writetime`	datetime	NULL,
	`chatboard_modify_time`	datetime	NULL,
	`chatboard_author_ip`	varchar(20)	NULL,
	`chatboard_deleted`	enum('maintained', 'deleted')	NULL,
	`user_uid`	int	NOT NULL
);

CREATE TABLE `log_delete_comment` (
	`log_delete_uid`	int	NOT NULL,
	`log_delete_boardtype`	enum('freeboard','news','notice','inquiry','chatboard')	NOT NULL,
	`log_delete_date`	datetime	NOT NULL,
	`log_deleted_comment_uid`	int	NOT NULL,
	`user_uid`	int	NOT NULL
);

CREATE TABLE `keyboard_category` (
	`keyboard_category_uid`	int	NOT NULL,
	`keyboard_category_name`	varchar(50)	NULL
);

CREATE TABLE `log_delete_post` (
	`log_delete_uid`	int	NOT NULL,
	`log_delete_boardtype`	enum('freeboard','news','notice','inquiry','chatboard')	NOT NULL,
	`log_delete_date`	datetime	NOT NULL,
	`log_deleted_post_uid`	int	NOT NULL,
	`user_uid`	int	NOT NULL
);

CREATE TABLE `log_recommend` (
	`log_recommend_uid`	int	NOT NULL,
	`log_recommend_boardtype`	enum('freeboard','news','notice','inquiry','chatboard')	NOT NULL,
	`log_recommend_post_id`	int	NOT NULL,
	`log_recommend_date`	datetime	NOT NULL,
	`user_uid`	int	NOT NULL
);

CREATE TABLE `log_modify_comment` (
	`log_modify_uid`	int	NOT NULL,
	`log_modify_boardtype`	enum('freeboard','news','notice','inquiry','chatboard')	NOT NULL,
	`log_modify_date`	datetime	NOT NULL,
	`log_modify_comment_uid`	int	NOT NULL,
	`user_uid`	int	NOT NULL
);

CREATE TABLE `keyboard_information` ( 
	`keyboard_information_uid`	int	NOT NULL,
	`keyboard_information_name`	varchar(50)	NULL,
	`keyboard_information_price`	int	NULL,
	`keyboard_category_uid`	int	NOT NULL
);

CREATE TABLE `keyboard_tag` (
	`tag_uid`	int	NOT NULL,
	`tag_name`	varchar(20)	NOT NULL,
	`tag_approve`	enum('waiting','approved')	NOT NULL
);

CREATE TABLE `notice` (
	`notice_uid`	int	NOT NULL,
	`notice_title`	varchar(50)	NULL,
	`notice_contents`	varchar(500)	NULL,
	`notice_read`	int	NULL,
	`notice_recommend`	int	NULL,
	`notice_writetime`	datetime	NULL,
	`notice_modify_time`	datetime	NULL,
	`notice_author_ip`	varchar(20)	NULL,
	`notice_deleted`	enum('maintained', 'deleted')	NULL,
	`user_uid`	int	NOT NULL
);

CREATE TABLE `news` (
	`news_uid`	int	NOT NULL,
	`news_title`	varchar(50)	NULL,
	`news_contents`	varchar(500)	NULL,
	`news_read`	int	NULL,
	`news_recommend`	int	NULL,
	`news_writetime`	datetime	NULL,
	`news_modify_time`	datetime	NULL,
	`news_author_ip`	varchar(20)	NULL,
	`news_deleted`	enum('maintained', 'deleted')	NULL,
	`user_uid`	int	NOT NULL
);

CREATE TABLE `keyboard_taglist` (
	`taglist_uid`	int	NOT NULL,
	`tag_type`	enum('admin', 'user')	NOT NULL,
	`tag_uid`	int	NOT NULL,
	`keyboard_information_uid`	int	NOT NULL
);

CREATE TABLE `user` (
	`user_uid`	int	NOT NULL,
	`user_id`	varchar(20)	NULL,
	`user_password`	varchar(20)	NULL,
	`user_name`	varchar(20)	NULL,
	`user_email`	varchar(20)	NULL,
	`user_introduce`	varchar(200)	NULL,
	`user_authority`	enum('normal', 'armband', 'admin')	NULL,
	`user_point`	int	NULL,
	`user_icon`	text	NULL
);

CREATE TABLE `keyboard_glossary` (
	`keyboard_glossary_uid`	int	NOT NULL,
	`keyboard_glossary_title`	varchar(20)	NULL,
	`keyboard_glossary_summary`	varchar(50)	NULL,
	`keyboard_glossary_url`	varchar(50)	NULL
);

CREATE TABLE `freeboard` (
	`freeboard_uid`	int	NOT NULL,
	`freeboard_title`	varchar(50)	NULL,
	`freeboard_contents`	varchar(500)	NULL,
	`freeboard_read`	int	NULL,
	`freeboard_recommend`	int	NULL,
	`freeboard_writetime`	datetime	NULL,
	`freeboard_modify_time`	datetime	NULL,
	`freeboard_author_ip`	varchar(20)	NULL,
	`freeboard_notify`	enum('common', 'notification')	NULL,
	`freeboard_deleted`	enum('maintained', 'deleted')	NULL,
	`user_uid`	int	NOT NULL
);

CREATE TABLE `scrap` (
	`scrap_uid`	int	NOT NULL,
	`scrap_user_uid`	int	NULL,
	`scrap_date`	datetime	NULL,
	`keyboard_information_uid`	int	NOT NULL
);

CREATE TABLE `report` (
	`report_uid`	int	NOT NULL,
	`report_target_type`	varchar(50)	NULL,
	`report_reason`	text	NULL,
	`report_status`	enum('active', 'inactive')	NOT NULL,
	`report_createtime`	datetime	NOT NULL,
	`report_user_uid`	int	NOT NULL,
	`target_user_uid`	int	NOT NULL
);

CREATE TABLE `news_comment` (
	`news_comment_uid`	int	NOT NULL,
	`news_comment_contents`	varchar(500)	NULL,
	`news_comment_writetime`	datetime	NULL,
	`news_comment_modifytime`	datetime	NULL,
	`news_comment_author_ip`	varchar(20)	NULL,
	`news_uid`	int	NOT NULL,
	`user_uid`	int	NOT NULL
);

CREATE TABLE `log_modify_post` (
	`log_modify_uid`	int	NOT NULL,
	`log_modify_boardtype`	enum('freeboard','news','notice','inquiry','chatboard')	NOT NULL,
	`log_modify_date`	datetime	NOT NULL,
	`log_modify_post_uid`	int	NOT NULL,
	`user_uid`	int	NOT NULL
);

CREATE TABLE `inquiry` (
	`inquiry_uid`	int	NOT NULL,
	`inquiry_title`	varchar(50)	NOT NULL,
	`inquiry_contents`	varchar(500)	NOT NULL,
	`inquiry_writetime`	datetime	NOT NULL,
	`inquiry_modify_time`	datetime	NOT NULL,
	`inquiry_author_ip`	varchar(20)	NOT NULL,
	`inquiry_deleted`	enum('maintained', 'deleted')	NOT NULL,
	`user_uid`	int	NOT NULL,
	`inquiry_parent_uid`	int	NULL,
	`inquiry_category`	enum('question','feedback')	NOT NULL,
	`inquiry_read_status`	enum('read','unread')	NOT NULL
);

ALTER TABLE `keyboard_score` ADD CONSTRAINT `PK_KEYBOARD_SCORE` PRIMARY KEY (
	`keyboard_score_uid`
);

ALTER TABLE `freeboard_comment` ADD CONSTRAINT `PK_FREEBOARD_COMMENT` PRIMARY KEY (
	`freeboard_comment_uid`
);

ALTER TABLE `user_penalty` ADD CONSTRAINT `PK_USER_PENALTY` PRIMARY KEY (
	`penalty_uid`
);

ALTER TABLE `chatboard` ADD CONSTRAINT `PK_CHATBOARD` PRIMARY KEY (
	`chatboard_uid`
);

ALTER TABLE `log_delete_comment` ADD CONSTRAINT `PK_LOG_DELETE_COMMENT` PRIMARY KEY (
	`log_delete_uid`
);

ALTER TABLE `keyboard_category` ADD CONSTRAINT `PK_KEYBOARD_CATEGORY` PRIMARY KEY (
	`keyboard_category_uid`
);

ALTER TABLE `log_delete_post` ADD CONSTRAINT `PK_LOG_DELETE_POST` PRIMARY KEY (
	`log_delete_uid`
);

ALTER TABLE `log_recommend` ADD CONSTRAINT `PK_LOG_RECOMMEND` PRIMARY KEY (
	`log_recommend_uid`
);

ALTER TABLE `log_modify_comment` ADD CONSTRAINT `PK_LOG_MODIFY_COMMENT` PRIMARY KEY (
	`log_modify_uid`
);

ALTER TABLE `keyboard_information` ADD CONSTRAINT `PK_KEYBOARD_INFORMATION` PRIMARY KEY (
	`keyboard_information_uid`
);

ALTER TABLE `keyboard_tag` ADD CONSTRAINT `PK_KEYBOARD_TAG` PRIMARY KEY (
	`tag_uid`
);

ALTER TABLE `notice` ADD CONSTRAINT `PK_NOTICE` PRIMARY KEY (
	`notice_uid`
);

ALTER TABLE `news` ADD CONSTRAINT `PK_NEWS` PRIMARY KEY (
	`news_uid`
);

ALTER TABLE `keyboard_taglist` ADD CONSTRAINT `PK_KEYBOARD_TAGLIST` PRIMARY KEY (
	`taglist_uid`
);

ALTER TABLE `user` ADD CONSTRAINT `PK_USER` PRIMARY KEY (
	`user_uid`
);

ALTER TABLE `keyboard_glossary` ADD CONSTRAINT `PK_KEYBOARD_GLOSSARY` PRIMARY KEY (
	`keyboard_glossary_uid`
);

ALTER TABLE `freeboard` ADD CONSTRAINT `PK_FREEBOARD` PRIMARY KEY (
	`freeboard_uid`
);

ALTER TABLE `scrap` ADD CONSTRAINT `PK_SCRAP` PRIMARY KEY (
	`scrap_uid`
);

ALTER TABLE `report` ADD CONSTRAINT `PK_REPORT` PRIMARY KEY (
	`report_uid`
);

ALTER TABLE `news_comment` ADD CONSTRAINT `PK_NEWS_COMMENT` PRIMARY KEY (
	`news_comment_uid`
);

ALTER TABLE `log_modify_post` ADD CONSTRAINT `PK_LOG_MODIFY_POST` PRIMARY KEY (
	`log_modify_uid`
);

ALTER TABLE `inquiry` ADD CONSTRAINT `PK_INQUIRY` PRIMARY KEY (
	`inquiry_uid`
);

ALTER TABLE `keyboard_score` ADD CONSTRAINT `FK_keyboard_information_TO_keyboard_score_1` FOREIGN KEY (
	`keyboard_information_uid`
)
REFERENCES `keyboard_information` (
	`keyboard_information_uid`
);

ALTER TABLE `keyboard_score` ADD CONSTRAINT `FK_user_TO_keyboard_score_1` FOREIGN KEY (
	`user_uid`
)
REFERENCES `user` (
	`user_uid`
);

ALTER TABLE `freeboard_comment` ADD CONSTRAINT `FK_freeboard_TO_freeboard_comment_1` FOREIGN KEY (
	`freeboard_uid`
)
REFERENCES `freeboard` (
	`freeboard_uid`
);

ALTER TABLE `freeboard_comment` ADD CONSTRAINT `FK_user_TO_freeboard_comment_1` FOREIGN KEY (
	`user_uid`
)
REFERENCES `user` (
	`user_uid`
);

ALTER TABLE `user_penalty` ADD CONSTRAINT `FK_user_TO_user_penalty_1` FOREIGN KEY (
	`user_uid`
)
REFERENCES `user` (
	`user_uid`
);

ALTER TABLE `chatboard` ADD CONSTRAINT `FK_user_TO_chatboard_1` FOREIGN KEY (
	`user_uid`
)
REFERENCES `user` (
	`user_uid`
);

ALTER TABLE `log_delete_comment` ADD CONSTRAINT `FK_user_TO_log_delete_comment_1` FOREIGN KEY (
	`user_uid`
)
REFERENCES `user` (
	`user_uid`
);

ALTER TABLE `log_delete_post` ADD CONSTRAINT `FK_user_TO_log_delete_post_1` FOREIGN KEY (
	`user_uid`
)
REFERENCES `user` (
	`user_uid`
);

ALTER TABLE `log_recommend` ADD CONSTRAINT `FK_user_TO_log_recommend_1` FOREIGN KEY (
	`user_uid`
)
REFERENCES `user` (
	`user_uid`
);

ALTER TABLE `log_modify_comment` ADD CONSTRAINT `FK_user_TO_log_modify_comment_1` FOREIGN KEY (
	`user_uid`
)
REFERENCES `user` (
	`user_uid`
);

ALTER TABLE `keyboard_information` ADD CONSTRAINT `FK_keyboard_category_TO_keyboard_information_1` FOREIGN KEY (
	`keyboard_category_uid`
)
REFERENCES `keyboard_category` (
	`keyboard_category_uid`
);

ALTER TABLE `notice` ADD CONSTRAINT `FK_user_TO_notice_1` FOREIGN KEY (
	`user_uid`
)
REFERENCES `user` (
	`user_uid`
);

ALTER TABLE `news` ADD CONSTRAINT `FK_user_TO_news_1` FOREIGN KEY (
	`user_uid`
)
REFERENCES `user` (
	`user_uid`
);

ALTER TABLE `keyboard_taglist` ADD CONSTRAINT `FK_keyboard_tag_TO_keyboard_taglist_1` FOREIGN KEY (
	`tag_uid`
)
REFERENCES `keyboard_tag` (
	`tag_uid`
);

ALTER TABLE `keyboard_taglist` ADD CONSTRAINT `FK_keyboard_information_TO_keyboard_taglist_1` FOREIGN KEY (
	`keyboard_information_uid`
)
REFERENCES `keyboard_information` (
	`keyboard_information_uid`
);

ALTER TABLE `freeboard` ADD CONSTRAINT `FK_user_TO_freeboard_1` FOREIGN KEY (
	`user_uid`
)
REFERENCES `user` (
	`user_uid`
);

ALTER TABLE `scrap` ADD CONSTRAINT `FK_keyboard_information_TO_scrap_1` FOREIGN KEY (
	`keyboard_information_uid`
)
REFERENCES `keyboard_information` (
	`keyboard_information_uid`
);

ALTER TABLE `report` ADD CONSTRAINT `FK_user_TO_report_1` FOREIGN KEY (
	`report_user_uid`
)
REFERENCES `user` (
	`user_uid`
);

ALTER TABLE `report` ADD CONSTRAINT `FK_user_TO_report_2` FOREIGN KEY (
	`target_user_uid`
)
REFERENCES `user` (
	`user_uid`
);

ALTER TABLE `news_comment` ADD CONSTRAINT `FK_news_TO_news_comment_1` FOREIGN KEY (
	`news_uid`
)
REFERENCES `news` (
	`news_uid`
);

ALTER TABLE `news_comment` ADD CONSTRAINT `FK_user_TO_news_comment_1` FOREIGN KEY (
	`user_uid`
)
REFERENCES `user` (
	`user_uid`
);

ALTER TABLE `log_modify_post` ADD CONSTRAINT `FK_user_TO_log_modify_post_1` FOREIGN KEY (
	`user_uid`
)
REFERENCES `user` (
	`user_uid`
);

ALTER TABLE `inquiry` ADD CONSTRAINT `FK_user_TO_inquiry_1` FOREIGN KEY (
	`user_uid`
)
REFERENCES `user` (
	`user_uid`
);

ALTER TABLE `inquiry` ADD CONSTRAINT `FK_inquiry_TO_inquiry_1` FOREIGN KEY (
	`inquiry_parent_uid`
)
REFERENCES `inquiry` (
	`inquiry_uid`
);

