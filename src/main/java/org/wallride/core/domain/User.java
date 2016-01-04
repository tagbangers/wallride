/*
 * Copyright 2014 Tagbangers, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wallride.core.domain;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SortNatural;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.springframework.util.DigestUtils;

import javax.persistence.*;
import java.io.UnsupportedEncodingException;
import java.util.SortedSet;
import java.util.TreeSet;

@Entity
@NamedEntityGraphs({
		@NamedEntityGraph(name = User.SHALLOW_GRAPH_NAME,
				attributeNodes = {
						@NamedAttributeNode("roles")}
		),
		@NamedEntityGraph(name = User.DEEP_GRAPH_NAME,
				attributeNodes = {
						@NamedAttributeNode("roles")})
})
@Table(name = "user")
@DynamicInsert
@DynamicUpdate
@Indexed
@SuppressWarnings("serial")
public class User extends DomainObject<Long> {

	public static final String SHALLOW_GRAPH_NAME = "USER_SHALLOW_GRAPH";
	public static final String DEEP_GRAPH_NAME = "USER_DEEP_GRAPH";

	public enum Role {
		ADMIN,
		EDITOR,
		AUTHOR,
		VIEWER,
	}

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
	@IndexedEmbedded(includeEmbeddedObjectId = true)
	private PersonalName name = new PersonalName();

	@Column(length = 500)
	@Field
	private String nickname;

	@Column(length = 200, nullable = false, unique = true)
	private String email;

	@Lob
	@Column
	private String description;

	@ElementCollection
	@SortNatural
	@JoinTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"))
	@Enumerated(EnumType.STRING)
	@Column(name = "role", length = 20, nullable = false)
	private SortedSet<Role> roles = new TreeSet<>();

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

	public SortedSet<Role> getRoles() {
		return roles;
	}

	public void setRoles(SortedSet<Role> roles) {
		this.roles = roles;
	}

	public String getGravatarUrl(int size) throws UnsupportedEncodingException {
		String hash = DigestUtils.md5DigestAsHex(getEmail().getBytes("CP1252"));
		return String.format("https://secure.gravatar.com/avatar/%s?size=%d&d=mm", hash, size);
	}

	@Override
	public String toString() {
		return (getName() != null) ? getName().toString() : "";
	}
}
