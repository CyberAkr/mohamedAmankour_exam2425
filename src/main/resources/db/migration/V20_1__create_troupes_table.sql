CREATE TABLE troupes (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         name VARCHAR(60) NOT NULL,
                         logo_url VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
