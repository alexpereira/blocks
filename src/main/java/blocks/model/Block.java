package blocks.model;

import org.neo4j.graphdb.Node;

import java.util.Map;

public class Block {

    public long id;
    public String label;
    public Map<String, Object> properties;

    public Block(Node node) {
        this.id = node.getId();
        this.label = node.getLabels().iterator().next().name();
        this.properties = node.getAllProperties();
    }
}
