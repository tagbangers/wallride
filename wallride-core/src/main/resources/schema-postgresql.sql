CREATE TABLE "article" (
  "id" INT8 NOT NULL,
  PRIMARY KEY ("id")
);

CREATE TABLE "blog" (
  "id"                                  BIGSERIAL    NOT NULL,
  "code"                                VARCHAR(200) NOT NULL,
  "default_language"                    VARCHAR(3)   NOT NULL,
  "ga_tracking_id"                      VARCHAR(100),
  "ga_profile_id"                       VARCHAR(100),
  "ga_custom_dimension_index"           INT4,
  "ga_service_account_id"               VARCHAR(300),
  "ga_service_account_p12_file_name"    VARCHAR(300),
  "ga_service_account_p12_file_content" OID,
  "created_at"                          TIMESTAMP    NOT NULL,
  "created_by"                          VARCHAR(100),
  "updated_at"                          TIMESTAMP    NOT NULL,
  "updated_by"                          VARCHAR(100),
  PRIMARY KEY ("id")
);

CREATE TABLE "blog_language" (
  "id"         BIGSERIAL  NOT NULL,
  "blog_id"    INT8       NOT NULL,
  "language"   VARCHAR(3) NOT NULL,
  "title"      TEXT       NOT NULL,
  "created_at" TIMESTAMP  NOT NULL,
  "created_by" VARCHAR(100),
  "updated_at" TIMESTAMP  NOT NULL,
  "updated_by" VARCHAR(100),
  PRIMARY KEY ("id")
);

CREATE TABLE "category" (
  "id"          BIGSERIAL    NOT NULL,
  "parent_id"   INT8,
  "code"        VARCHAR(200) NOT NULL,
  "language"    VARCHAR(3)   NOT NULL,
  "name"        VARCHAR(200) NOT NULL,
  "description" TEXT,
  "lft"         INT4         NOT NULL,
  "rgt"         INT4         NOT NULL,
  "created_at"  TIMESTAMP    NOT NULL,
  "created_by"  VARCHAR(100),
  "updated_at"  TIMESTAMP    NOT NULL,
  "updated_by"  VARCHAR(100),
  PRIMARY KEY ("id")
);

CREATE TABLE "comment" (
  "id"          BIGSERIAL    NOT NULL,
  "post_id"     INT8         NOT NULL,
  "author_id"   INT8,
  "author_name" VARCHAR(200) NOT NULL,
  "date"        TIMESTAMP    NOT NULL,
  "content"     TEXT         NOT NULL,
  "approved"    BOOLEAN      NOT NULL,
  "created_at"  TIMESTAMP    NOT NULL,
  "created_by"  VARCHAR(100),
  "updated_at"  TIMESTAMP    NOT NULL,
  "updated_by"  VARCHAR(100),
  PRIMARY KEY ("id")
);

CREATE TABLE "media" (
  "id"            VARCHAR(50) NOT NULL,
  "mime_type"     VARCHAR(50) NOT NULL,
  "original_name" VARCHAR(500),
  "created_at"    TIMESTAMP   NOT NULL,
  "created_by"    VARCHAR(100),
  "updated_at"    TIMESTAMP   NOT NULL,
  "updated_by"    VARCHAR(100),
  PRIMARY KEY ("id")
);

CREATE TABLE "navigation_item" (
  "id"          BIGSERIAL    NOT NULL,
  "parent_id"   INT8,
  "page_id"     INT8,
  "category_id" INT8,
  "language"    VARCHAR(255) NOT NULL,
  "type"        VARCHAR(31)  NOT NULL,
  "sort"        INT4         NOT NULL,
  "created_at"  TIMESTAMP    NOT NULL,
  "created_by"  VARCHAR(100),
  "updated_at"  TIMESTAMP    NOT NULL,
  "updated_by"  VARCHAR(100),
  PRIMARY KEY ("id")
);

CREATE TABLE "page" (
  "id"        INT8 NOT NULL,
  "parent_id" INT8,
  "lft"       INT4 NOT NULL,
  "rgt"       INT4 NOT NULL,
  PRIMARY KEY ("id")
);

CREATE TABLE "password_reset_token" (
  "token"      VARCHAR(50)  NOT NULL,
  "user_id"    INT8         NOT NULL,
  "email"      VARCHAR(200) NOT NULL,
  "expired_at" TIMESTAMP    NOT NULL,
  "created_at" TIMESTAMP    NOT NULL,
  "created_by" VARCHAR(100),
  "updated_at" TIMESTAMP    NOT NULL,
  "updated_by" VARCHAR(100),
  PRIMARY KEY ("token")
);

CREATE TABLE "popular_post" (
  "id"         BIGSERIAL   NOT NULL,
  "post_id"    INT8        NOT NULL,
  "language"   VARCHAR(3)  NOT NULL,
  "type"       VARCHAR(50) NOT NULL,
  "rank"       INT4        NOT NULL,
  "views"      INT8        NOT NULL,
  "created_at" TIMESTAMP   NOT NULL,
  "created_by" VARCHAR(100),
  "updated_at" TIMESTAMP   NOT NULL,
  "updated_by" VARCHAR(100),
  PRIMARY KEY ("id")
);

CREATE TABLE "post" (
  "id"              BIGSERIAL   NOT NULL,
  "code"            VARCHAR(200),
  "language"        VARCHAR(3)  NOT NULL,
  "status"          VARCHAR(50) NOT NULL,
  "date"            TIMESTAMP,
  "title"           VARCHAR(200),
  "body"            TEXT,
  "cover_id"        VARCHAR(50),
  "author_id"       INT8,
  "drafted_id"      INT8,
  "drafted_code"    VARCHAR(200),
  "seo_title"       VARCHAR(500),
  "seo_description" TEXT,
  "seo_keywords"    TEXT,
  "views"           INT8        NOT NULL,
  "created_at"      TIMESTAMP   NOT NULL,
  "created_by"      VARCHAR(100),
  "updated_at"      TIMESTAMP   NOT NULL,
  "updated_by"      VARCHAR(100),
  PRIMARY KEY ("id")
);

CREATE TABLE "post_category" (
  "post_id"     INT8 NOT NULL,
  "category_id" INT8 NOT NULL,
  PRIMARY KEY ("post_id", "category_id")
);

CREATE TABLE "post_media" (
  "post_id"  INT8        NOT NULL,
  "media_id" VARCHAR(50) NOT NULL,
  "index"    INT4        NOT NULL,
  PRIMARY KEY ("post_id", "index")
);

CREATE TABLE "post_related_post" (
  "post_id"    INT8 NOT NULL,
  "related_id" INT8 NOT NULL,
  PRIMARY KEY ("related_id", "post_id")
);

CREATE TABLE "post_tag" (
  "post_id" INT8 NOT NULL,
  "tag_id"  INT8 NOT NULL,
  PRIMARY KEY ("post_id", "tag_id")
);

CREATE TABLE "tag" (
  "id"         BIGSERIAL    NOT NULL,
  "language"   VARCHAR(3)   NOT NULL,
  "name"       VARCHAR(200) NOT NULL,
  "created_at" TIMESTAMP    NOT NULL,
  "created_by" VARCHAR(100),
  "updated_at" TIMESTAMP    NOT NULL,
  "updated_by" VARCHAR(100),
  PRIMARY KEY ("id")
);

CREATE TABLE "user" (
  "id"             BIGSERIAL    NOT NULL,
  "login_id"       VARCHAR(100) NOT NULL,
  "login_password" VARCHAR(500) NOT NULL,
  "name_first"     VARCHAR(50)  NOT NULL,
  "name_last"      VARCHAR(50)  NOT NULL,
  "nickname"       VARCHAR(500),
  "email"          VARCHAR(200) NOT NULL,
  "description"    TEXT,
  "created_at"     TIMESTAMP    NOT NULL,
  "created_by"     VARCHAR(100),
  "updated_at"     TIMESTAMP    NOT NULL,
  "updated_by"     VARCHAR(100),
  PRIMARY KEY ("id")
);

CREATE TABLE "user_invitation" (
  "token"       VARCHAR(50)  NOT NULL,
  "email"       VARCHAR(500) NOT NULL,
  "message"     TEXT,
  "expired_at"  TIMESTAMP    NOT NULL,
  "accepted"    BOOLEAN      NOT NULL,
  "accepted_at" TIMESTAMP,
  "created_at"  TIMESTAMP    NOT NULL,
  "created_by"  VARCHAR(100),
  "updated_at"  TIMESTAMP    NOT NULL,
  "updated_by"  VARCHAR(100),
  PRIMARY KEY ("token")
);

CREATE TABLE "user_role" (
  "user_id" INT8        NOT NULL,
  "role"    VARCHAR(20) NOT NULL,
  PRIMARY KEY ("user_id", "role")
);

ALTER TABLE "blog" ADD CONSTRAINT UK_398ypeix0usuwxip7hl30tl95 UNIQUE ("code");
ALTER TABLE "blog_language" ADD CONSTRAINT "UKjvbtdcpruai93kkn9en48os1j" UNIQUE ("blog_id", "language");
ALTER TABLE "category" ADD CONSTRAINT "UKbcyxs660s0fku8sf6pgy137ai" UNIQUE ("code", "language");
ALTER TABLE "popular_post" ADD CONSTRAINT "UKevl12yr4xxkydmkvigjq82iui" UNIQUE ("language", "type", "rank");
ALTER TABLE "post" ADD CONSTRAINT "UKl52i0qo9maim4jb28sahyaf02" UNIQUE ("code", "language");
ALTER TABLE "tag" ADD CONSTRAINT "UKk25qstev2lpae13bk95lxny1y" UNIQUE ("name", "language");
ALTER TABLE "user" ADD CONSTRAINT UK_ob8kqyqqgmefl0aco34akdtpe UNIQUE ("email");
ALTER TABLE "user" ADD CONSTRAINT UK_6ntlp6n5ltjg6hhxl66jj5u0l UNIQUE ("login_id");
ALTER TABLE "article" ADD CONSTRAINT "FK2v5gc16vlmfc3b7v9mug9p0nh" FOREIGN KEY ("id") REFERENCES "post";
ALTER TABLE "blog_language" ADD CONSTRAINT "FKm26flfhreaktwyf5x7niter6u" FOREIGN KEY ("blog_id") REFERENCES "blog";
ALTER TABLE "category" ADD CONSTRAINT "FKpqbj33aij72uwx8rwt086hvq2" FOREIGN KEY ("parent_id") REFERENCES "category";
ALTER TABLE "comment" ADD CONSTRAINT "FKg229tmp8ip9shg6ydifpc2mk6" FOREIGN KEY ("author_id") REFERENCES "user";
ALTER TABLE "comment" ADD CONSTRAINT "FKgxbwgh8hcc6k5f2q9vkmjvdps" FOREIGN KEY ("post_id") REFERENCES "post";
ALTER TABLE "navigation_item" ADD CONSTRAINT "FKo9pj7oh5oc36ia8f9flji199u" FOREIGN KEY ("parent_id") REFERENCES "navigation_item";
ALTER TABLE "navigation_item" ADD CONSTRAINT "FK72p6vy4stfruklu8mggg6qt3s" FOREIGN KEY ("category_id") REFERENCES "category";
ALTER TABLE "navigation_item" ADD CONSTRAINT "FKq2bloyhyl745v0ao2kjfjieyf" FOREIGN KEY ("page_id") REFERENCES "page";
ALTER TABLE "page" ADD CONSTRAINT "FK483vwi7bfr4pl0bf57g4abei7" FOREIGN KEY ("parent_id") REFERENCES "page";
ALTER TABLE "page" ADD CONSTRAINT "FK71xxvk6cocuigt994gx2yyohk" FOREIGN KEY ("id") REFERENCES "post";
ALTER TABLE "password_reset_token" ADD CONSTRAINT "FKjthxr8d7rmlunj1uv3lt1xvl5" FOREIGN KEY ("user_id") REFERENCES "user";
ALTER TABLE "popular_post" ADD CONSTRAINT "FKkk18uxlago62ssjyxk9p3wn4r" FOREIGN KEY ("post_id") REFERENCES "post";
ALTER TABLE "post" ADD CONSTRAINT "FKlv86dv65vxnbyndhwdp9evbn5" FOREIGN KEY ("author_id") REFERENCES "user";
ALTER TABLE "post" ADD CONSTRAINT "FKnx17yhqhh2l6dphgxr04fno6p" FOREIGN KEY ("cover_id") REFERENCES "media";
ALTER TABLE "post" ADD CONSTRAINT "FKmnd7c5s0tpi8fsbtrcv3v1w75" FOREIGN KEY ("drafted_id") REFERENCES "post";
ALTER TABLE "post_category" ADD CONSTRAINT "FKciko9vgftyon175wslea5d88k" FOREIGN KEY ("post_id") REFERENCES "post";
ALTER TABLE "post_category" ADD CONSTRAINT "FKq63x31lf6aykdrgi3llnc171y" FOREIGN KEY ("category_id") REFERENCES "category";
ALTER TABLE "post_media" ADD CONSTRAINT "FKbt9h1jh7mqdrodqmy8potin0s" FOREIGN KEY ("media_id") REFERENCES "media";
ALTER TABLE "post_media" ADD CONSTRAINT "FK7dbnkkaarh7suxjlkwn5sh4a7" FOREIGN KEY ("post_id") REFERENCES "post";
ALTER TABLE "post_related_post" ADD CONSTRAINT "FK8h4kulpvd11c5l4bdn3wfbtie" FOREIGN KEY ("related_id") REFERENCES "post";
ALTER TABLE "post_related_post" ADD CONSTRAINT "FKthyi9hidjpq5vmcamwaj2ap2" FOREIGN KEY ("post_id") REFERENCES "post";
ALTER TABLE "post_tag" ADD CONSTRAINT "FKonr178imgjksqflate1o6ybim" FOREIGN KEY ("post_id") REFERENCES "post";
ALTER TABLE "post_tag" ADD CONSTRAINT "FK8d78naxn3frlhbqyiurgbtg3v" FOREIGN KEY ("tag_id") REFERENCES "tag";
ALTER TABLE "user_role" ADD CONSTRAINT "FKhjx9nk20h4mo745tdqj8t8n9d" FOREIGN KEY ("user_id") REFERENCES "user";

CREATE TABLE "persistent_logins" (
  "username"  VARCHAR(64) NOT NULL,
  "series"    VARCHAR(64) PRIMARY KEY,
  "token"     VARCHAR(64) NOT NULL,
  "last_used" TIMESTAMP   NOT NULL
);
