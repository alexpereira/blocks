package blocks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App {

	public static String SCHEMA_RESOURCE = "schema.graphqls";
	public static String GRAPH_DATA_PATH = "data/graph";

	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}
}
