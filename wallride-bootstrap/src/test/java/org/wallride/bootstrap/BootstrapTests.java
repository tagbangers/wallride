package org.wallride.bootstrap;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.wallride.autoconfigure.WallRideProperties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for the {@link Bootstrap}.
 *
 * @author Takeshi Ogawa
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
public class BootstrapTests {

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@BeforeClass
	public static void beforeClass() {
		System.setProperty(WallRideProperties.HOME_PROPERTY, System.getProperty("java.io.tmpdir"));
	}

	@Test
	public void test() {
		ResponseEntity<String> response = this.restTemplate.getForEntity("http://localhost:" + this.port + "/_admin/setup", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

}