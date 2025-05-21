ALTER TABLE artists
    ADD COLUMN troupe_id BIGINT,
ADD CONSTRAINT fk_artist_troupe
    FOREIGN KEY (troupe_id)
    REFERENCES troupes(id)
    ON UPDATE CASCADE
       ON DELETE SET NULL;
