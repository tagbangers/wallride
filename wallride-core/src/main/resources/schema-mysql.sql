CREATE TABLE `article` (
  `id` BIGINT NOT NULL,
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB;

CREATE TABLE `blog` (
  `id`                                  BIGINT       NOT NULL AUTO_INCREMENT,
  `code`                                VARCHAR(200) NOT NULL,
  `default_language`                    VARCHAR(3)   NOT NULL,
  `ga_tracking_id`                      VARCHAR(100),
  `ga_profile_id`                       VARCHAR(100),
  `ga_custom_dimension_index`           INTEGER,
  `ga_service_account_id`               VARCHAR(300),
  `ga_service_account_p12_file_name`    VARCHAR(300),
  `ga_service_account_p12_file_content` LONGBLOB,
  `created_at`                          DATETIME     NOT NULL,
  `created_by`                          VARCHAR(100),
  `updated_at`                          DATETIME     NOT NULL,
  `updated_by`                          VARCHAR(100),
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB;

CREATE TABLE `blog_language` (
  `id`         BIGINT     NOT NULL AUTO_INCREMENT,
  `blog_id`    BIGINT     NOT NULL,
  `language`   VARCHAR(3) NOT NULL,
  `title`      LONGTEXT   NOT NULL,
  `created_at` DATETIME   NOT NULL,
  `created_by` VARCHAR(100),
  `updated_at` DATETIME   NOT NULL,
  `updated_by` VARCHAR(100),
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB;

CREATE TABLE `category` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `parent_id`   BIGINT,
  `code`        VARCHAR(200) NOT NULL,
  `language`    VARCHAR(3)   NOT NULL,
  `name`        VARCHAR(200) NOT NULL,
  `description` LONGTEXT,
  `lft`         INTEGER      NOT NULL,
  `rgt`         INTEGER      NOT NULL,
  `created_at`  DATETIME     NOT NULL,
  `created_by`  VARCHAR(100),
  `updated_at`  DATETIME     NOT NULL,
  `updated_by`  VARCHAR(100),
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB;

create table `custom_field` (
  `id` bigint not null auto_increment,
  `created_at` datetime not null,
  `created_by` varchar(100),
  `updated_at` datetime not null,
  `updated_by` varchar(100),
  `default_value` varchar(200),
  `description` longtext,
  `field_type` varchar(50) not null,
  `idx` integer,
  `language` varchar(3) not null,
  `name` varchar(200),
  primary key (`id`)
)
  ENGINE=InnoDB;

create table `custom_field_value` (
  `id` bigint not null auto_increment,
  `created_at` datetime not null,
  `created_by` varchar(100),
  `updated_at` datetime not null,
  `updated_by` varchar(100),
  `date_value` date,
  `datetime_value` datetime,
  `number_value` bigint,
  `string_value` varchar(255),
  `text_value` longtext,
  `custom_field_id` bigint not null,
  `post_id` bigint not null,
  primary key (`id`)
)
  ENGINE=InnoDB;

create table `custom_field_option` (
  `custom_field_id` bigint not null,
  `language` varchar(3) not null,
  `name` varchar(200) not null,
  `idx` integer not null,
  primary key (`custom_field_id` , `idx`)
)
  ENGINE=InnoDB;

CREATE TABLE `comment` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `post_id`     BIGINT       NOT NULL,
  `author_id`   BIGINT,
  `author_name` VARCHAR(200) NOT NULL,
  `date`        DATETIME     NOT NULL,
  `content`     LONGTEXT     NOT NULL,
  `approved`    BIT          NOT NULL,
  `created_at`  DATETIME     NOT NULL,
  `created_by`  VARCHAR(100),
  `updated_at`  DATETIME     NOT NULL,
  `updated_by`  VARCHAR(100),
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB;

CREATE TABLE `media` (
  `id`            VARCHAR(50) NOT NULL,
  `mime_type`     VARCHAR(50) NOT NULL,
  `original_name` VARCHAR(500),
  `created_at`    DATETIME    NOT NULL,
  `created_by`    VARCHAR(100),
  `updated_at`    DATETIME    NOT NULL,
  `updated_by`    VARCHAR(100),
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB;

CREATE TABLE `navigation_item` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `parent_id`   BIGINT,
  `page_id`     BIGINT,
  `category_id` BIGINT,
  `language`    VARCHAR(255) NOT NULL,
  `type`        VARCHAR(31)  NOT NULL,
  `sort`        INTEGER      NOT NULL,
  `created_at`  DATETIME     NOT NULL,
  `created_by`  VARCHAR(100),
  `updated_at`  DATETIME     NOT NULL,
  `updated_by`  VARCHAR(100),
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB;

CREATE TABLE `page` (
  `id`        BIGINT  NOT NULL,
  `parent_id` BIGINT,
  `lft`       INTEGER NOT NULL,
  `rgt`       INTEGER NOT NULL,
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB;

CREATE TABLE `password_reset_token` (
  `token`      VARCHAR(50)  NOT NULL,
  `user_id`    BIGINT       NOT NULL,
  `email`      VARCHAR(200) NOT NULL,
  `expired_at` DATETIME     NOT NULL,
  `created_at` DATETIME     NOT NULL,
  `created_by` VARCHAR(100),
  `updated_at` DATETIME     NOT NULL,
  `updated_by` VARCHAR(100),
  PRIMARY KEY (`token`)
)
  ENGINE = InnoDB;

CREATE TABLE `popular_post` (
  `id`         BIGINT      NOT NULL AUTO_INCREMENT,
  `post_id`    BIGINT      NOT NULL,
  `language`   VARCHAR(3)  NOT NULL,
  `type`       VARCHAR(50) NOT NULL,
  `rank`       INTEGER     NOT NULL,
  `views`      BIGINT      NOT NULL,
  `created_at` DATETIME    NOT NULL,
  `created_by` VARCHAR(100),
  `updated_at` DATETIME    NOT NULL,
  `updated_by` VARCHAR(100),
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB;

CREATE TABLE `post` (
  `id`              BIGINT      NOT NULL AUTO_INCREMENT,
  `code`            VARCHAR(200),
  `language`        VARCHAR(3)  NOT NULL,
  `status`          VARCHAR(50) NOT NULL,
  `date`            DATETIME,
  `title`           VARCHAR(200),
  `body`            LONGTEXT,
  `cover_id`        VARCHAR(50),
  `author_id`       BIGINT,
  `drafted_id`      BIGINT,
  `drafted_code`    VARCHAR(200),
  `seo_title`       VARCHAR(500),
  `seo_description` LONGTEXT,
  `seo_keywords`    LONGTEXT,
  `views`           BIGINT      NOT NULL,
  `created_at`      DATETIME    NOT NULL,
  `created_by`      VARCHAR(100),
  `updated_at`      DATETIME    NOT NULL,
  `updated_by`      VARCHAR(100),
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB;

CREATE TABLE `post_category` (
  `post_id`     BIGINT NOT NULL,
  `category_id` BIGINT NOT NULL,
  PRIMARY KEY (`post_id`, `category_id`)
)
  ENGINE = InnoDB;

CREATE TABLE `post_media` (
  `post_id`  BIGINT      NOT NULL,
  `media_id` VARCHAR(50) NOT NULL,
  `index`    INTEGER     NOT NULL,
  PRIMARY KEY (`post_id`, `index`)
)
  ENGINE = InnoDB;

CREATE TABLE `post_related_post` (
  `post_id`    BIGINT NOT NULL,
  `related_id` BIGINT NOT NULL,
  PRIMARY KEY (`related_id`, `post_id`)
)
  ENGINE = InnoDB;

CREATE TABLE `post_tag` (
  `post_id` BIGINT NOT NULL,
  `tag_id`  BIGINT NOT NULL,
  PRIMARY KEY (`post_id`, `tag_id`)
)
  ENGINE = InnoDB;

CREATE TABLE `tag` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT,
  `language`   VARCHAR(3)   NOT NULL,
  `name`       VARCHAR(200) NOT NULL,
  `created_at` DATETIME     NOT NULL,
  `created_by` VARCHAR(100),
  `updated_at` DATETIME     NOT NULL,
  `updated_by` VARCHAR(100),
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB;

CREATE TABLE `user` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT,
  `login_id`       VARCHAR(100) NOT NULL,
  `login_password` VARCHAR(500) NOT NULL,
  `name_first`     VARCHAR(50)  NOT NULL,
  `name_last`      VARCHAR(50)  NOT NULL,
  `nickname`       VARCHAR(500),
  `email`          VARCHAR(200) NOT NULL,
  `description`    LONGTEXT,
  `created_at`     DATETIME     NOT NULL,
  `created_by`     VARCHAR(100),
  `updated_at`     DATETIME     NOT NULL,
  `updated_by`     VARCHAR(100),
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB;

CREATE TABLE `user_invitation` (
  `token`       VARCHAR(50)  NOT NULL,
  `email`       VARCHAR(500) NOT NULL,
  `message`     LONGTEXT,
  `expired_at`  DATETIME     NOT NULL,
  `accepted`    BIT          NOT NULL,
  `accepted_at` DATETIME,
  `created_at`  DATETIME     NOT NULL,
  `created_by`  VARCHAR(100),
  `updated_at`  DATETIME     NOT NULL,
  `updated_by`  VARCHAR(100),
  PRIMARY KEY (`token`)
)
  ENGINE = InnoDB;

CREATE TABLE `user_role` (
  `user_id` BIGINT      NOT NULL,
  `role`    VARCHAR(20) NOT NULL,
  PRIMARY KEY (`user_id`, `role`)
)
  ENGINE = InnoDB;

ALTER TABLE `blog` ADD CONSTRAINT UK_398ypeix0usuwxip7hl30tl95 UNIQUE (`code`);
ALTER TABLE `blog_language` ADD CONSTRAINT `UKjvbtdcpruai93kkn9en48os1j` UNIQUE (`blog_id`, `language`);
ALTER TABLE `category` ADD CONSTRAINT `UKbcyxs660s0fku8sf6pgy137ai` UNIQUE (`code`, `language`);
ALTER TABLE `custom_field` ADD CONSTRAINT `UKix3po6weuk4wvhvc95n5rk5ch` UNIQUE (`name`, `language`);
ALTER TABLE `custom_field` ADD CONSTRAINT UK_l6uj9qracv5sa03gb9g6amy19 UNIQUE (`idx`);
ALTER TABLE `custom_field_value` ADD CONSTRAINT `UKnn598oul2m13aiorw3e5clc1i` UNIQUE (`post_id`, `custom_field_id`);
ALTER TABLE `popular_post` ADD CONSTRAINT `UKevl12yr4xxkydmkvigjq82iui` UNIQUE (`language`, `type`, `rank`);
ALTER TABLE `post` ADD CONSTRAINT `UKl52i0qo9maim4jb28sahyaf02` UNIQUE (`code`, `language`);
ALTER TABLE `tag` ADD CONSTRAINT `UKk25qstev2lpae13bk95lxny1y` UNIQUE (`name`, `language`);
ALTER TABLE `user` ADD CONSTRAINT UK_ob8kqyqqgmefl0aco34akdtpe UNIQUE (`email`);
ALTER TABLE `user` ADD CONSTRAINT UK_6ntlp6n5ltjg6hhxl66jj5u0l UNIQUE (`login_id`);
ALTER TABLE `article` ADD CONSTRAINT `FK2v5gc16vlmfc3b7v9mug9p0nh` FOREIGN KEY (`id`) REFERENCES `post` (`id`);
ALTER TABLE `blog_language` ADD CONSTRAINT `FKm26flfhreaktwyf5x7niter6u` FOREIGN KEY (`blog_id`) REFERENCES `blog` (`id`);
ALTER TABLE `category` ADD CONSTRAINT `FKpqbj33aij72uwx8rwt086hvq2` FOREIGN KEY (`parent_id`) REFERENCES `category` (`id`);
ALTER TABLE `custom_field_value` ADD CONSTRAINT `FK68g6fssy3gjj4jovfso18uysm` foreign key (`custom_field_id`) REFERENCES `custom_field` (`id`);
ALTER TABLE `custom_field_value` ADD CONSTRAINT `FK814q6mnv98jdn8ubh5fkyy3sc` foreign key (`post_id`) REFERENCES `post` (`id`);
ALTER TABLE `custom_field_option` ADD CONSTRAINT `FKjquafa57imfqsl50qxqm29txr` foreign key (`custom_field_id`) REFERENCES `custom_field` (`id`);
ALTER TABLE `comment` ADD CONSTRAINT `FKg229tmp8ip9shg6ydifpc2mk6` FOREIGN KEY (`author_id`) REFERENCES `user` (`id`);
ALTER TABLE `comment` ADD CONSTRAINT `FKgxbwgh8hcc6k5f2q9vkmjvdps` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`);
ALTER TABLE `navigation_item` ADD CONSTRAINT `FKo9pj7oh5oc36ia8f9flji199u` FOREIGN KEY (`parent_id`) REFERENCES `navigation_item` (`id`);
ALTER TABLE `navigation_item` ADD CONSTRAINT `FK72p6vy4stfruklu8mggg6qt3s` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`);
ALTER TABLE `navigation_item` ADD CONSTRAINT `FKq2bloyhyl745v0ao2kjfjieyf` FOREIGN KEY (`page_id`) REFERENCES `page` (`id`);
ALTER TABLE `page` ADD CONSTRAINT `FK483vwi7bfr4pl0bf57g4abei7` FOREIGN KEY (`parent_id`) REFERENCES `page` (`id`);
ALTER TABLE `page` ADD CONSTRAINT `FK71xxvk6cocuigt994gx2yyohk` FOREIGN KEY (`id`) REFERENCES `post` (`id`);
ALTER TABLE `password_reset_token` ADD CONSTRAINT `FKjthxr8d7rmlunj1uv3lt1xvl5` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);
ALTER TABLE `popular_post` ADD CONSTRAINT `FKkk18uxlago62ssjyxk9p3wn4r` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`);
ALTER TABLE `post` ADD CONSTRAINT `FKlv86dv65vxnbyndhwdp9evbn5` FOREIGN KEY (`author_id`) REFERENCES `user` (`id`);
ALTER TABLE `post` ADD CONSTRAINT `FKnx17yhqhh2l6dphgxr04fno6p` FOREIGN KEY (`cover_id`) REFERENCES `media` (`id`);
ALTER TABLE `post` ADD CONSTRAINT `FKmnd7c5s0tpi8fsbtrcv3v1w75` FOREIGN KEY (`drafted_id`) REFERENCES `post` (`id`);
ALTER TABLE `post_category` ADD CONSTRAINT `FKciko9vgftyon175wslea5d88k` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`);
ALTER TABLE `post_category` ADD CONSTRAINT `FKq63x31lf6aykdrgi3llnc171y` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`);
ALTER TABLE `post_media` ADD CONSTRAINT `FKbt9h1jh7mqdrodqmy8potin0s` FOREIGN KEY (`media_id`) REFERENCES `media` (`id`);
ALTER TABLE `post_media` ADD CONSTRAINT `FK7dbnkkaarh7suxjlkwn5sh4a7` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`);
ALTER TABLE `post_related_post` ADD CONSTRAINT `FK8h4kulpvd11c5l4bdn3wfbtie` FOREIGN KEY (`related_id`) REFERENCES `post` (`id`);
ALTER TABLE `post_related_post` ADD CONSTRAINT `FKthyi9hidjpq5vmcamwaj2ap2` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`);
ALTER TABLE `post_tag` ADD CONSTRAINT `FKonr178imgjksqflate1o6ybim` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`);
ALTER TABLE `post_tag` ADD CONSTRAINT `FK8d78naxn3frlhbqyiurgbtg3v` FOREIGN KEY (`tag_id`) REFERENCES `tag` (`id`);
ALTER TABLE `user_role` ADD CONSTRAINT `FKhjx9nk20h4mo745tdqj8t8n9d` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

CREATE TABLE `persistent_logins` (
  `username`  VARCHAR(64) NOT NULL,
  `series`    VARCHAR(64) PRIMARY KEY,
  `token`     VARCHAR(64) NOT NULL,
  `last_used` TIMESTAMP   NOT NULL
);