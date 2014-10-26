package org.wallride.core.domain;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.springframework.util.DigestUtils;

import javax.persistence.*;
import java.io.UnsupportedEncodingException;

@Entity
@Table(name = "user")
@DynamicInsert
@DynamicUpdate
@Indexed
@SuppressWarnings("serial")
public class User extends DomainObject<Long> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(name = "login_id", length = 100, nullable = false, unique = true)
	@Field(analyze = Analyze.NO)
	private String loginId;

	@Column(name = "login_password", length = 500, nullable = false)
	private String loginPassword;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "firstName", column = @Column(name = "name_first", length = 50, nullable = false)),
			@AttributeOverride(name = "lastName", column = @Column(name = "name_last", length = 50, nullable = false)),
	})
	@IndexedEmbedded
	private PersonalName name = new PersonalName();

	@Column(length = 500)
	@Field
	private String nickname;

	@Column(length = 200, nullable = false, unique = true)
	private String email;

	@Lob
	@Column
	private String description;

//	@Formula("(" +
//			"select count(distinct article.id) from user " +
//			"inner join post post on post.author_id = user.id " +
//			"inner join article article on article.id = post.id " +
//			"where post.author_id = id " +
//			"and post.status = 'PUBLISHED')")
//	private int articleCount;

	@Override
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLoginId() {
		return loginId;
	}

	public void setLoginId(String loginId) {
		this.loginId = loginId;
	}

	public String getLoginPassword() {
		return loginPassword;
	}

	public void setLoginPassword(String loginPassword) {
		this.loginPassword = loginPassword;
	}

	public PersonalName getName() {
		return name;
	}

	public void setName(PersonalName name) {
		this.name = name;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	//	public int getArticleCount() {
//		return articleCount;
//	}

	public String getGravatarUrl(int size) throws UnsupportedEncodingException {
		String hash = DigestUtils.md5DigestAsHex(getEmail().getBytes("CP1252"));
		return String.format("https://secure.gravatar.com/avatar/%s?size=%d&d=mm", hash, size);
	}

	@Override
	public String toString() {
		return (getName() != null) ? getName().toString() : "";
	}
}
