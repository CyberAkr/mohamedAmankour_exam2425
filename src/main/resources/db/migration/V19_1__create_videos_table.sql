CREATE TABLE videos (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        title VARCHAR(255) NOT NULL,
                        video_url VARCHAR(191) UNIQUE NOT NULL,
                        show_id BIGINT NOT NULL,
                        CONSTRAINT fk_video_show FOREIGN KEY (show_id)
                            REFERENCES shows(id)
                            ON UPDATE CASCADE
                            ON DELETE RESTRICT
);
