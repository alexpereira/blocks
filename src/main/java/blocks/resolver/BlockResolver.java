package blocks.resolver;

import blocks.Provider;
import blocks.model.Block;
import graphql.schema.DataFetcher;
import org.neo4j.graphdb.Direction;

import java.util.*;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;
import static graphql.schema.idl.TypeRuntimeWiring.Builder;

public class BlockResolver extends Provider {

    public static Builder apply() {
        return newTypeWiring("Block")
                .dataFetcher("blocks", BlockResolver.readBlockBlocks());
    }

    public static DataFetcher writeBlock() {
        return dataFetchingEnvironment -> {
            Map<String, Object> blockArgs = dataFetchingEnvironment.getArgument("block");

            Integer id = (Integer) blockArgs.get("id");
            String label = (String) blockArgs.get("label");
            Map<String, Object> properties = (Map<String, Object>) blockArgs.get("properties");

            if (id == null) {
                return graphService.createBlock(label, properties);
            } else {
                return graphService.updateBlock(id, label, properties);
            }
        };
    }

    public static DataFetcher readBlocks() {
        return dataFetchingEnvironment -> {
            Map<String, Object> blockArgs = dataFetchingEnvironment.getArgument("block");
            Integer id = (Integer) blockArgs.get("id");
            String label = (String) blockArgs.get("label");

            if (id != null) {
                List<Block> blocks = new ArrayList<>();
                blocks.add(graphService.getBlockById(id));
                return blocks;
            } else {
                return graphService.getBlockByLabel(label);
            }
        };
    }

    public static DataFetcher readBlockBlocks() {
        return dataFetchingEnvironment -> {
            Block source = dataFetchingEnvironment.getSource();
            String connectionType = dataFetchingEnvironment.getArgument("connectionType");
            String direction = dataFetchingEnvironment.getArgument("direction");
            String targetLabel = dataFetchingEnvironment.getArgument("targetLabel");

            return graphService.getBlocksByConnection(source, connectionType, Direction.valueOf(direction), targetLabel);
        };
    }
}
