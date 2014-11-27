package org.wallride.core.domain;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.IndexedEmbedded;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "blog")
@DynamicInsert
@DynamicUpdate
public class Blog extends DomainObject<Long> {

	public static final long DEFAULT_ID = 1;

	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private long id;

	@Column(length = 200, nullable = false, unique = true)
	@Field
	private String code;

	@Column(name = "default_language", length = 3, nullable = false)
	@Field
	private String defaultLanguage;

	@Column(name = "media_url_prefix", length = 300, nullable = false)
	@Field
	private String mediaUrlPrefix;

	@Column(name = "media_path", length = 300, nullable = false)
	@Field
	private String mediaPath;

	@Embedded
	@IndexedEmbedded
	private GoogleAnalytics googleAnalytics;

	@OneToMany(mappedBy = "blog", cascade = CascadeType.ALL)
	@IndexedEmbedded
	private Set<BlogLanguage> languages = new HashSet<>();

	public Long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDefaultLanguage() {
		return defaultLanguage;
	}

	public void setDefaultLanguage(String defaultLanguage) {
		this.defaultLanguage = defaultLanguage;
	}

	public String getMediaUrlPrefix() {
		return mediaUrlPrefix;
	}

	public void setMediaUrlPrefix(String mediaUrlPrefix) {
		this.mediaUrlPrefix = mediaUrlPrefix;
	}

	public String getMediaPath() {
		return mediaPath;
	}

	public void setMediaPath(String mediaPath) {
		this.mediaPath = mediaPath;
	}

	public GoogleAnalytics getGoogleAnalytics() {
		return googleAnalytics;
	}

	public void setGoogleAnalytics(GoogleAnalytics googleAnalytics) {
		this.googleAnalytics = googleAnalytics;
	}

	public Set<BlogLanguage> getLanguages() {
		return languages;
	}

	public void setLanguages(Set<BlogLanguage> languages) {
		this.languages = languages;
	}

	public BlogLanguage getLanguage(String language) {
		for (BlogLanguage blogLanguage : getLanguages()) {
			if (blogLanguage.getLanguage().equals(language)) {
				return blogLanguage;
			}
		}
		return null;
	}

	public String getTitle() {
		return getTitle(getDefaultLanguage());
	}

	public String getTitle(String language) {
		return getLanguage(language).getTitle();
	}

	public boolean isMultiLanguage() {
		return (getLanguages().size() > 1);
	}
}
