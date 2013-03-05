-- ref: http://www.h2database.com/html/datatypes.html

DROP TABLE IF EXISTS USER;

CREATE TABLE USER (
  ID          IDENTITY      AUTO_INCREMENT  PRIMARY KEY,
  NAME        VARCHAR(255)  NOT NULL UNIQUE,
  GENDER      BOOLEAN       NOT NULL,
  BIRTHDAY    DATE,
  ID_CARD_NO  VARCHAR(18),
  EMAIL       VARCHAR(255)  NOT NULL,
  HEIGHT      INTEGER,
  AVATAR      VARBINARY,
  VERSION     BIGINT  DEFAULT 1 NOT NULL
);

INSERT INTO USER(NAME, GENDER, BIRTHDAY, ID_CARD_NO, EMAIL, HEIGHT, AVATAR) VALUES ('zhang3', true,  '1981-01-01', '11010120120101853X', 'zhang3@xxx.com',  182, FILE_READ('classpath:avatar.png'));
INSERT INTO USER(NAME, GENDER, BIRTHDAY, ID_CARD_NO, EMAIL, HEIGHT, AVATAR) VALUES ('li4',    false, '1982-01-02', '110101201201014176', 'li4@xxx.com',     165, FILE_READ('classpath:avatar.png'));
INSERT INTO USER(NAME, GENDER, BIRTHDAY, ID_CARD_NO, EMAIL, HEIGHT, AVATAR) VALUES ('wang5',  true,  '1983-03-03', '110101201201016665', 'wang5@xxx.com',   174, FILE_READ('classpath:avatar.png'));
