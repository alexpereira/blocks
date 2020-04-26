package blocks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class App {

	public static String SCHEMA_RESOURCE = "schema.graphqls";
	public static String GRAPH_DATA_PATH = "data/graph";

	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/graphql").allowedOrigins("http://localhost:3000");
			}
		};
	}
}
