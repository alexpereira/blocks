package blocks.model;

import org.neo4j.graphdb.Relationship;

public class Connection{

    public long id;
    public String type;
    public long source_id;
    public long target_id;

    public Connection(Relationship relationship) {
        id = relationship.getId();
        type = relationship.getType().name();
        source_id = relationship.getStartNodeId();
        target_id = relationship.getEndNodeId();
    }
}
