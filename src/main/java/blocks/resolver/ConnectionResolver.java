package blocks.resolver;

import blocks.Provider;
import graphql.schema.DataFetcher;
import graphql.schema.idl.TypeRuntimeWiring;

import java.util.Map;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

public class ConnectionResolver extends Provider {

    public static TypeRuntimeWiring.Builder apply() {
        return newTypeWiring("Connection");
    }

    public static DataFetcher writeConnection() {
        return dataFetchingEnvironment -> {
            Map<String, Object> connectionArgs = dataFetchingEnvironment.getArgument("connection");

//            Integer id = (Integer) connectionArgs.get("id");
            String type = (String) connectionArgs.get("type");
            Integer sourceId = (Integer) connectionArgs.get("source_id");
            Integer targetId = (Integer) connectionArgs.get("target_id");

            return graphService.createConnection(type, sourceId, targetId);
        };
    }
}
