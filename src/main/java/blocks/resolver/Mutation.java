package blocks.resolver;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;
import static graphql.schema.idl.TypeRuntimeWiring.Builder;

public class Mutation {
    public static Builder apply() {
        return newTypeWiring("Mutation")
                .dataFetcher("writeBlock", BlockResolver.writeBlock())
                .dataFetcher("writeConnection", ConnectionResolver.writeConnection());
    }
}
