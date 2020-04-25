package blocks.resolver;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;
import static graphql.schema.idl.TypeRuntimeWiring.Builder;

public class Query {
    public static Builder apply() {
        return newTypeWiring("Query")
                .dataFetcher("readBlocks", BlockResolver.readBlocks());
    }
}
