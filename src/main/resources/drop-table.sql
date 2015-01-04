SET foreign_key_checks = 0;

drop table if exists article;
drop table if exists article_category;
drop table if exists article_tag;
drop table if exists blog;
drop table if exists blog_language;
drop table if exists category;
drop table if exists comment;
drop table if exists media;
drop table if exists navigation_item;
drop table if exists page;
drop table if exists password_reset_token;
drop table if exists popular_post;
drop table if exists post;
drop table if exists post_media;
drop table if exists post_related_post;
drop table if exists setting;
drop table if exists tag;
drop table if exists user;
drop table if exists user_invitation;
drop table if exists user_role;

drop table if exists persistent_logins;

SET foreign_key_checks = 1;
