create table article (
	id bigint not null,
	primary key (id)
) ENGINE=InnoDB;

create table blog (
	id bigint not null auto_increment,
	code varchar(200) not null,
	default_language varchar(3) not null,
	ga_tracking_id varchar(100),
	ga_profile_id varchar(100),
	ga_custom_dimension_index integer,
	ga_service_account_id varchar(300),
	ga_service_account_p12_file_name varchar(300),
	ga_service_account_p12_file_content longblob,
	created_at datetime not null,
	created_by varchar(100),
	updated_at datetime not null,
	updated_by varchar(100),
	primary key (id)
) ENGINE=InnoDB;

create table blog_language (
	id bigint not null auto_increment,
	blog_id bigint not null,
	language varchar(3) not null,
	title longtext not null,
	created_at datetime not null,
	created_by varchar(100),
	updated_at datetime not null,
	updated_by varchar(100),
	primary key (id)
) ENGINE=InnoDB;

create table category (
	id bigint not null auto_increment,
	code varchar(200) not null,
	language varchar(3) not null,
	name varchar(200) not null,
	description longtext,
	lft integer not null,
	rgt integer not null,
	parent_id bigint,
	created_at datetime not null,
	created_by varchar(100),
	updated_at datetime not null,
	updated_by varchar(100),
	primary key (id)
) ENGINE=InnoDB;

create table custom_field (
	id bigint not null auto_increment,
  `idx` integer not null,
	name varchar(200),
	description longtext,
  field_type varchar(50) not null,
  language varchar(3) not null,
  default_value varchar(200),
	created_at datetime not null,
	created_by varchar(100),
	updated_at datetime not null,
	updated_by varchar(100),
	primary key (id)
) ENGINE=InnoDB;

create table custom_field_option (
  custom_field_id bigint not null,
  `idx` integer not null,
	name varchar(200) not null,
	language varchar(3) not null,
  primary key (custom_field_id , `idx`)
) ENGINE=InnoDB;

create table custom_field_value (
	id bigint not null auto_increment,
  post_id bigint not null,
  custom_field_id bigint not null,
  string_value varchar(255),
  text_value longtext,
  number_value bigint,
  datetime_value datetime,
  date_value date,
	created_at datetime not null,
	created_by varchar(100),
	updated_at datetime not null,
	updated_by varchar(100),
	primary key (id)
) ENGINE=InnoDB;

create table comment (
	id bigint not null auto_increment,
	post_id bigint not null,
	author_id bigint,
	author_name varchar(200) not null,
	date datetime not null,
	content longtext not null,
	approved bit not null,
	created_at datetime not null,
	created_by varchar(100),
	updated_at datetime not null,
	updated_by varchar(100),
	primary key (id)
) ENGINE=InnoDB;

create table media (
	id varchar(50) not null,
	mime_type varchar(50) not null,
	original_name varchar(500),
	created_at datetime not null,
	created_by varchar(100),
	updated_at datetime not null,
	updated_by varchar(100),
	primary key (id)
) ENGINE=InnoDB;

create table navigation_item (
	id bigint not null auto_increment,
	type varchar(31) not null,
	language varchar(255) not null,
	category_id bigint,
	page_id bigint,
	sort integer not null,
	parent_id bigint,
	created_at datetime not null,
	created_by varchar(100),
	updated_at datetime not null,
	updated_by varchar(100),
	primary key (id)
) ENGINE=InnoDB;

create table page (
	id bigint not null,
	lft integer not null,
	rgt integer not null,
	parent_id bigint,
	primary key (id)
) ENGINE=InnoDB;

create table password_reset_token (
	token varchar(50) not null,
	user_id bigint not null,
	email varchar(200) not null,
	expired_at datetime not null,
	created_at datetime not null,
	created_by varchar(100),
	updated_at datetime not null,
	updated_by varchar(100),
	primary key (token)
) ENGINE=InnoDB;

create table popular_post (
	id bigint not null auto_increment,
	language varchar(3) not null,
	type varchar(50) not null,
	rank integer not null,
	views bigint not null,
	post_id bigint not null,
	created_at datetime not null,
	created_by varchar(100),
	updated_at datetime not null,
	updated_by varchar(100),
	primary key (id)
) ENGINE=InnoDB;

create table post (
	id bigint not null auto_increment,
	code varchar(200),
	language varchar(3) not null,
	cover_id varchar(50),
	title varchar(200),
	body longtext,
	seo_title varchar(500),
	seo_description longtext,
	seo_keywords longtext,
	date datetime,
	author_id bigint,
	status varchar(50) not null,
	views bigint not null,
	drafted_id bigint,
	drafted_code varchar(200),
	created_at datetime not null,
	created_by varchar(100),
	updated_at datetime not null,
	updated_by varchar(100),
	primary key (id)
) ENGINE=InnoDB;

create table post_media (
	post_id bigint not null,
	media_id varchar(50) not null,
	`index` integer not null,
	primary key (post_id, `index`)
) ENGINE=InnoDB;

create table post_related_post (
	post_id bigint not null,
	related_id bigint not null,
	primary key (post_id, related_id)
) ENGINE=InnoDB;

create table post_category (
	post_id bigint not null,
	category_id bigint not null,
	primary key (post_id, category_id)
) ENGINE=InnoDB;

create table post_tag (
	tag_id bigint not null,
	post_id bigint not null,
	primary key (post_id, tag_id)
) ENGINE=InnoDB;

create table tag (
	id bigint not null auto_increment,
	language varchar(3) not null,
	name varchar(200) not null,
	created_at datetime not null,
	created_by varchar(100),
	updated_at datetime not null,
	updated_by varchar(100),
	primary key (id)
) ENGINE=InnoDB;

create table user (
	id bigint not null auto_increment,
	login_id varchar(100) not null,
	login_password varchar(500) not null,
	name_first varchar(50) not null,
	name_last varchar(50) not null,
	nickname varchar(500),
	email varchar(200) not null,
	description longtext,
	created_at datetime not null,
	created_by varchar(100),
	updated_at datetime not null,
	updated_by varchar(100),
	primary key (id)
) ENGINE=InnoDB;

create table user_invitation (
	token varchar(50) not null,
	email varchar(500) not null,
	message longtext,
	expired_at datetime not null,
	accepted boolean not null,
	accepted_at datetime,
	created_at datetime not null,
	created_by varchar(100),
	updated_at datetime not null,
	updated_by varchar(100),
	primary key (token)
) ENGINE=InnoDB;

create table user_role (
	user_id bigint not null,
	role varchar(20) not null,
	primary key (user_id, role)
) ENGINE=InnoDB;

alter table blog
	add constraint UK_398ypeix0usuwxip7hl30tl95  unique (code);

alter table blog_language
	add constraint UK_ad19h6nvek9854379mxcaj721  unique (blog_id, language);

alter table category
	add constraint UK_86l62kycx6uuh2dbgymn8i065  unique (code, language);

alter table custom_field
	add constraint UKtacuvalxxjhoddac1g1iitepa unique (name, language);

alter table custom_field
	add constraint UK_l6uj9qracv5sa03gb9g6amy19 unique (idx);

alter table popular_post
	add constraint UK_a5tm7yxs8io57gsrtb5tbrlcm  unique (language, type, rank);

alter table post
	add constraint UK_6khu2naokwmhyfq3lt8t8eehn  unique (code, language);

alter table tag
	add constraint UK_96k5ovyyuo8hgewc0h2f0g9et  unique (name, language);

alter table user
	add constraint UK_6ntlp6n5ltjg6hhxl66jj5u0l  unique (login_id);

alter table user
	add constraint UK_h1vneshxbwkd1ailk02vdy2qu  unique (email);

alter table article
	add constraint FK_3mlcrjv9clnvarg3o8fysvnkx
	foreign key (id)
	references post (id);

alter table post_category
	add constraint FK_qr4dx4cx1lh4jfjchabytcakl
	foreign key (post_id)
	references post (id);

alter table post_category
	add constraint FK_qly0d5oc4npxdig2fjfoshhxg
	foreign key (category_id)
	references category (id);

alter table post_tag
	add constraint FK_dt9gvgp3ws00wjmivks4hvavm
	foreign key (post_id)
	references post (id);

alter table post_tag
	add constraint FK_6xnftsvf7gg9wbir1lth8tce2
	foreign key (tag_id)
	references tag (id);

alter table blog_language
	add constraint FK_gvt8qnfjl6qwbb3am853c70ul
	foreign key (blog_id)
	references blog (id);

alter table category
	add constraint FK_81thrbnb8c08gua7tvqj7xdqk
	foreign key (parent_id)
	references category (id);

alter table custom_field_option
	add constraint FKlt17rtc1ae4xkdvb4wl2f52ok
	foreign key (custom_field_id)
	references custom_field (id);

alter table custom_field_value
	add constraint FK21ubkc63mbxa1axcbidvo0k3j
	foreign key (custom_field_id)
	references custom_field (id);

alter table custom_field_value
	add constraint FK2tygpm2w3nwsoenrm2tbitg0y
	foreign key (post_id)
	references post (id);

alter table comment
	add constraint FK_9aq5p2jgf17y6b38x5ayd90oc
	foreign key (author_id)
	references user (id);

alter table comment
	add constraint FK_f1sl0xkd2lucs7bve3ktt3tu5
	foreign key (post_id)
	references post (id);

alter table navigation_item
	add constraint FK_e986fb2rhw2a7a2m2col2f1fg
	foreign key (parent_id)
	references navigation_item (id);

alter table navigation_item
	add constraint FK_qie2cbixacp4xccixia3mjd99
	foreign key (category_id)
	references category (id);

alter table navigation_item
	add constraint FK_gqekdbas3sbmx3u4peurmkxl0
	foreign key (page_id)
	references page (id);

alter table page
	add constraint FK_e9x1tbh3hitjnkmigkv8pl24w
	foreign key (parent_id)
	references page (id);

alter table page
	add constraint FK_88lc5ox4n3kvd7vc10nvx8nn6
	foreign key (id)
	references post (id);

alter table password_reset_token
	add constraint FK_f90ivichjaokvmovxpnlm5nin
	foreign key (user_id)
	references user (id);

alter table popular_post
	add constraint FK_mjlwh0t7235c0koq6y01jdo84
	foreign key (post_id)
	references post (id);

alter table post
	add constraint FK_ik65bluepv8oxdfvgbj5qdcsj
	foreign key (author_id)
	references user (id);

alter table post
	add constraint FK_lew3sxka65cx9ichkheda3m4p
	foreign key (cover_id)
	references media (id);

alter table post
	add constraint FK_i0a3aj3cfl77hk6skuemp2aya
	foreign key (drafted_id)
	references post (id);

alter table post_media
	add constraint FK_cbh3kwx9ocobb3y3jn93nth0o
	foreign key (media_id)
	references media (id);

alter table post_media
	add constraint FK_rmb5w9waqw5fpy31j42wjirt3
	foreign key (post_id)
	references post (id);

alter table post_related_post
	add constraint FK_4yoix0vojg6k29dujxt66dlm1
	foreign key (related_id)
	references post (id);

alter table post_related_post
	add constraint FK_9h3eog304whtvhs23f3lmd5ow
	foreign key (post_id)
	references post (id);

alter table user_role
	add constraint FK_apcc8lxk2xnug8377fatvbn04
	foreign key (user_id)
	references user (id);

create table persistent_logins (
	username varchar(64) not null,
	series varchar(64) primary key,
	token varchar(64) not null,
	last_used timestamp not null
);