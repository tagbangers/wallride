package org.wallride.domain;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.wallride.autoconfigure.WallRideProperties;
import org.wallride.test.TestConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for the {@link Blog}.
 *
 * @author Takeshi Ogawa
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestConfiguration.class)
@AutoConfigurationPackage
@DataJpaTest
public class BlogTests {

	@Autowired
	private TestEntityManager entityManager;

	@BeforeClass
	public static void beforeClass() {
		System.setProperty(WallRideProperties.HOME_PROPERTY, System.getProperty("java.io.tmpdir"));
	}

	@Test
	public void mapping() {
		Blog blog = new Blog();
		blog.setCode("wallride");
		blog.setDefaultLanguage("en");

		BlogLanguage language = new BlogLanguage();
		language.setBlog(blog);
		language.setLanguage("en");
		language.setTitle("WallRide");
		blog.getLanguages().add(language);

		blog = this.entityManager.persistAndFlush(blog);
		assertThat(blog.getId()).isNotNull();
	}

}