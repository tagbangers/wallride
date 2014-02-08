import com.amazonaws.services.elasticache.model.SourceType;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * Created by ogawa on 14/02/09.
 */
public class Main {

	public static void main(String[] args) {
		String jdbcConnectionString = "jdbc:mysql://localhost:3306/wallride?user=wallride&password=wallride";
		UriComponents jdbcUriComponents = UriComponentsBuilder.fromUriString(jdbcConnectionString.substring("jdbc:".length())).build();
		System.out.println(jdbcConnectionString.substring(0, jdbcConnectionString.indexOf("?")));
		System.out.println(jdbcUriComponents.getQueryParams());
		System.out.println(jdbcUriComponents.getQueryParams().getFirst("user"));
		System.out.println(jdbcUriComponents.getQueryParams().getFirst("password"));
	}
}
