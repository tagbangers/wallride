package org.wallride.core.domain;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="media")
@DynamicInsert
@DynamicUpdate
@SuppressWarnings("serial")
public class Media extends DomainObject<String> {

	public enum ResizeMode {
		RESIZE,
		CROP,
	}

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name="system-uuid", strategy="uuid2")
	@Column(length=50)
	private String id;

	@Column(name="mime_type", length=50, nullable=false)
	private String mimeType;

	@Column(name="original_name", length=500)
	private String originalName;

	@ManyToMany(mappedBy="medias")
	private List<Post> posts;

	@Override
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getOriginalName() {
		return originalName;
	}

	public void setOriginalName(String originalName) {
		this.originalName = originalName;
	}

	public List<Post> getPosts() {
		return posts;
	}

	public void setPosts(List<Post> posts) {
		this.posts = posts;
	}
}
