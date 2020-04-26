package blocks;

import blocks.resolver.BlockResolver;
import blocks.resolver.ConnectionResolver;
import blocks.resolver.Mutation;
import blocks.resolver.Query;
import blocks.service.GraphService;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import graphql.GraphQL;
import graphql.scalars.ExtendedScalars;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.*;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URL;

import static blocks.App.SCHEMA_RESOURCE;
import static blocks.App.GRAPH_DATA_PATH;

@Component
public class Provider {

    private GraphQL graphQL;
    public static GraphService graphService;

    @PostConstruct
    public void init() throws IOException {
        init(GRAPH_DATA_PATH);
    }

    public void init(String customDataFile) throws IOException {
        URL url = Resources.getResource(SCHEMA_RESOURCE);
        String sdl = Resources.toString(url, Charsets.UTF_8);
        GraphQLSchema graphQLSchema = buildSchema(sdl);
        this.graphQL = GraphQL.newGraphQL(graphQLSchema).build();
        graphService = GraphService.get(customDataFile);
    }

    private GraphQLSchema buildSchema(String sdl) {
        TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(sdl);
        RuntimeWiring runtimeWiring = buildWiring();
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        return schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);
    }

    private RuntimeWiring buildWiring() {
        return RuntimeWiring.newRuntimeWiring()
                .scalar(ExtendedScalars.Object)
                .type(Query.apply())
                .type(Mutation.apply())
                .type(BlockResolver.apply())
                .type(ConnectionResolver.apply())
                .build();
    }

    @Bean
    public GraphQL graphQL() {
        return this.graphQL;
    }

    public GraphService graphService() {
        return graphService;
    }
}
