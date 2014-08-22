create table article (
	id bigint not null,
	primary key (id)
) ENGINE=InnoDB;

create table article_category (
	article_id bigint not null,
	category_id bigint not null,
	primary key (article_id, category_id)
) ENGINE=InnoDB;

create table article_tag (
	article_id bigint not null,
	tag_id bigint not null,
	primary key (article_id, tag_id)
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

create table setting (
	`key` varchar(100) not null,
	value varchar(500) not null,
	created_at datetime not null,
	created_by varchar(100),
	updated_at datetime not null,
	updated_by varchar(100),
	primary key (`key`)
) ENGINE=InnoDB;

create table tag (
	id bigint not null auto_increment,
	language varchar(3) not null,
	name varchar(500) not null,
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
	email varchar(500) not null,
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

alter table article
	add index FK_3mlcrjv9clnvarg3o8fysvnkx (id),
	add constraint FK_3mlcrjv9clnvarg3o8fysvnkx
	foreign key (id)
	references post (id);

alter table article_category
	add index FK_48nv4hxwx3mte5gferj9wlj46 (article_id),
	add constraint FK_48nv4hxwx3mte5gferj9wlj46
	foreign key (article_id)
	references article (id);

alter table article_category
	add index FK_aaq7a2c3e34qghxyh34ao8r6p (category_id),
	add constraint FK_aaq7a2c3e34qghxyh34ao8r6p
	foreign key (category_id)
	references category (id);

alter table article_tag
	add index FK_pkndl0ud6fkak73gdkls858a5 (tag_id),
	add constraint FK_pkndl0ud6fkak73gdkls858a5
	foreign key (tag_id)
	references tag (id);

alter table article_tag
	add index FK_5ao70rbptu4cd93wbu7o38y1y (article_id),
	add constraint FK_5ao70rbptu4cd93wbu7o38y1y
	foreign key (article_id)
	references article (id);

alter table category
	add constraint UK_86l62kycx6uuh2dbgymn8i065 unique (code, language);

alter table category
	add index FK_81thrbnb8c08gua7tvqj7xdqk (parent_id),
	add constraint FK_81thrbnb8c08gua7tvqj7xdqk
	foreign key (parent_id)
	references category (id);

alter table navigation_item
	add index FK_e986fb2rhw2a7a2m2col2f1fg (parent_id),
	add constraint FK_e986fb2rhw2a7a2m2col2f1fg
	foreign key (parent_id)
	references navigation_item (id);

alter table navigation_item
	add index FK_qie2cbixacp4xccixia3mjd99 (category_id),
	add constraint FK_qie2cbixacp4xccixia3mjd99
	foreign key (category_id)
	references category (id);

alter table navigation_item
	add index FK_gqekdbas3sbmx3u4peurmkxl0 (page_id),
	add constraint FK_gqekdbas3sbmx3u4peurmkxl0
	foreign key (page_id)
	references page (id);

alter table page
	add index FK_e9x1tbh3hitjnkmigkv8pl24w (parent_id),
	add constraint FK_e9x1tbh3hitjnkmigkv8pl24w
	foreign key (parent_id)
	references page (id);

alter table page
	add index FK_88lc5ox4n3kvd7vc10nvx8nn6 (id),
	add constraint FK_88lc5ox4n3kvd7vc10nvx8nn6
	foreign key (id)
	references post (id);

alter table post
	add constraint UK_9awc6gvqyqbkh45ta3dq3vti  unique (code, language);

alter table post
	add index FK_ik65bluepv8oxdfvgbj5qdcsj (author_id),
	add constraint FK_ik65bluepv8oxdfvgbj5qdcsj
	foreign key (author_id)
	references user (id);

alter table post
	add index FK_lew3sxka65cx9ichkheda3m4p (cover_id),
	add constraint FK_lew3sxka65cx9ichkheda3m4p
	foreign key (cover_id)
	references media (id);

alter table post
	add constraint FK_i0a3aj3cfl77hk6skuemp2aya
	foreign key (drafted_id)
	references post (id);

alter table post_media
	add index FK_cbh3kwx9ocobb3y3jn93nth0o (media_id),
	add constraint FK_cbh3kwx9ocobb3y3jn93nth0o
	foreign key (media_id)
	references media (id);

alter table post_media
	add index FK_rmb5w9waqw5fpy31j42wjirt3 (post_id),
	add constraint FK_rmb5w9waqw5fpy31j42wjirt3
	foreign key (post_id)
	references post (id);

alter table user
	add constraint UK_6ntlp6n5ltjg6hhxl66jj5u0l unique (login_id);

create table persistent_logins (
	username varchar(64) not null,
	series varchar(64) primary key,
	token varchar(64) not null,
	last_used timestamp not null
);