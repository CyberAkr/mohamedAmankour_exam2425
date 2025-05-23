CREATE TABLE show_tag
(
    show_id BIGINT NOT NULL,
    tag_id  BIGINT NOT NULL,
    PRIMARY KEY (show_id, tag_id),
    CONSTRAINT fk_show FOREIGN KEY (show_id)
        REFERENCES shows (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_tag FOREIGN KEY (tag_id)
        REFERENCES tags (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);