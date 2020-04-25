package blocks.service;

import blocks.model.Block;
import blocks.model.Connection;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.helpers.collection.Iterators;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class GraphService {

    private static GraphDatabaseService db;
    private static GraphService instance;

    private GraphService(File dataFile) {
//        db = new GraphDatabaseFactory().newEmbeddedDatabase(dataFile);
        GraphDatabaseSettings.BoltConnector bolt = GraphDatabaseSettings.boltConnector("0");
        db = new GraphDatabaseFactory()
                .newEmbeddedDatabaseBuilder(dataFile)
                .setConfig(bolt.type, "BOLT")
                .setConfig(bolt.enabled, "true")
                .setConfig(bolt.address, "localhost:7687")
                .newGraphDatabase();
        gracefulShutdown(db);
    }

    public static GraphService get(String dataFile) {
        if (instance == null || !db.isAvailable(0)) {
            instance = new GraphService(new File(dataFile));
        }

        return instance;
    }

    private void gracefulShutdown(final GraphDatabaseService db) {
        Runtime.getRuntime().addShutdownHook( new Thread() {
            public void run() {
                db.shutdown();
            }
        });
    }

    public void shutdown() {
        db.shutdown();
    }

    public Block createBlock(String label, Map<String, Object> properties) {
        try (Transaction transaction = db.beginTx()) {
            Node node = db.createNode();
            node.addLabel(Label.label(label));

            for (Map.Entry<String, Object> property : properties.entrySet()) {
                node.setProperty(property.getKey(), property.getValue());
            }

            Block block = new Block(node);
            transaction.success();

            return block;
        }
    }

    public Block updateBlock(long id, String label, Map<String, Object> properties) {
        try (Transaction transaction = db.beginTx()) {
            Node node = db.getNodeById(id);

            Map<String, Object> currentProperties = node.getProperties();
            Label currentLabel = node.getLabels().iterator().next();
            Label newLabel = Label.label(label);

            if (!currentLabel.equals(newLabel)) {
                node.removeLabel(currentLabel);
                node.addLabel(newLabel);
            }

            if (!currentProperties.equals(properties)) {
                for (Map.Entry<String, Object> property : currentProperties.entrySet()) {
                    node.removeProperty(property.getKey());
                }

                for (Map.Entry<String, Object> property : properties.entrySet()) {
                    node.setProperty(property.getKey(), property.getValue());
                }
            }

            Block block = new Block(node);
            transaction.success();

            return block;
        }
    }

    public Block getBlockById(long id) {
        try (Transaction transaction = db.beginTx()) {
            Node node = db.getNodeById(id);
            Block block = new Block(node);
            transaction.success();
            return block;
        }
    }

    public List<Block> getBlockByLabel(String label) {
        try (Transaction transaction = db.beginTx()) {
            List<Block> blocks = new ArrayList<>();
            ResourceIterator<Node> nodes = db.findNodes(Label.label(label));

            while (nodes.hasNext()) {
                blocks.add(new Block(nodes.next()));
            }

            transaction.success();
            return blocks;
        }
    }

    public List<Block> getBlocksByConnection(Block source, String type, Direction direction, String targetLabel) {
        String incoming = Direction.INCOMING.equals(direction) ? "<" : "";
        String outgoing = Direction.OUTGOING.equals(direction) ? ">" : "";

        String sourceProperties = "{";
        sourceProperties +=  source.properties.entrySet()
                .stream()
                .map(e -> e.getKey() + ": '" + e.getValue() + "'")
                .collect(Collectors.joining(","));
        sourceProperties += "}";

        String connectionLabel = "c:" + type;
        String sourceIdentifier =  "b:" + source.label + " " + sourceProperties;
        String targetIdentifier =  "bb:" + (targetLabel != null ? targetLabel : source.label);

        String query =  "MATCH (" + sourceIdentifier + ")" + incoming + "-[" + connectionLabel + "]-" + outgoing + "(" + targetIdentifier + ") ";
        query +=        "WHERE id(b) = " + source.id + " ";
        query +=        "RETURN DISTINCT bb";

        List<Block> blocks = new ArrayList<>();

        try (Transaction transaction = db.beginTx()) {
            Result result = db.execute(query);
            Iterator<Node> bb_column = result.columnAs("bb");

            for (Node node : Iterators.asIterable(bb_column)) {
                blocks.add(new Block(node));
            }

            transaction.success();
        }

        return blocks;
    }

    public Connection createConnection(String type, long sourceId, long targetId) {
        try (Transaction transaction = db.beginTx()) {
            Node source = db.getNodeById(sourceId);
            Node target = db.getNodeById(targetId);

            Relationship relationship = source.createRelationshipTo(target, RelationshipType.withName(type));
            Connection connection = new Connection(relationship);
            transaction.success();

            return connection;
        }
    }
}
