package blocks

import graphql.ExecutionResult

class Connection extends AppTests {

    String createUserMutation;
    String createPostMutation;
    String createUserHasPostMutation;

    def setup() {
        createUserMutation = """
            mutation {
                writeBlock(
                    block: { 
                        label: "USER",
                        properties: {
                            first_name: "alex",
                            last_name: "pereira"
                        }
                    }
                ) {
                    id
                    label
                    properties
                }
            }
        """

        createPostMutation = """
            mutation {
                writeBlock(
                    block: { 
                        label: "POST",
                        properties: {
                            text: "my first post"
                        }
                    }
                ) {
                    id
                    label
                    properties
                }
            }
        """

        createUserHasPostMutation = """
            mutation {
                writeConnection(
                    connection: {
                        type: "USER_HAS_POST",
                        source_id: %d,
                        target_id: %d
                    }
                ) {
                    type
                    source_id
                    target_id
                }
            }
        """
    }

    def "write connection - create"() {
        setup:
        ExecutionResult createUserResult = provider.graphQL().execute(createUserMutation)
        ExecutionResult createPostResult = provider.graphQL().execute(createPostMutation)

        long userId = (long) createUserResult.data?.getAt('writeBlock')?.getAt('id')
        long postId = (long) createPostResult.data?.getAt('writeBlock')?.getAt('id')

        String mutation = String.format(createUserHasPostMutation, userId, postId)

        when:
        ExecutionResult result = provider.graphQL().execute(mutation)

        then:
        result.data != null
        result.data?.getAt('writeConnection')?.getAt('type') == 'USER_HAS_POST'
        result.data?.getAt('writeConnection')?.getAt('source_id') == userId
        result.data?.getAt('writeConnection')?.getAt('target_id') == postId
    }

    def "read block connections"() {
        setup:
        ExecutionResult createUserResult = provider.graphQL().execute(createUserMutation)
        ExecutionResult createPostResult = provider.graphQL().execute(createPostMutation)

        long userId = (long) createUserResult.data?.getAt('writeBlock')?.getAt('id')
        long postId = (long) createPostResult.data?.getAt('writeBlock')?.getAt('id')

        String mutation = String.format(createUserHasPostMutation, userId, postId)
        provider.graphQL().execute(mutation)

        String query = """
            query {
                readBlocks(
                    block: { 
                        id: ${userId},
                        label: "USER"
                    }
                ) {
                    id
                    label
                    properties
                    
                    blocks(
                        connectionType: "USER_HAS_POST".
                        direction: OUTGOING,
                        targetLabel: "POST"
                    ) {
                        id
                        label
                        properties
                    }
                }
            }
        """

        when:
        ExecutionResult result = provider.graphQL().execute(query)

        then:
        result.data != null
        (result.data?.getAt('readBlocks') as List)?.get(0)?.getAt("id") == userId
        (result.data?.getAt('readBlocks') as List)?.get(0)?.getAt("label") == "USER"
        (result.data?.getAt('readBlocks') as List)?.get(0)?.getAt("properties")?.getAt("first_name") == "alex"
        (result.data?.getAt('readBlocks') as List)?.get(0)?.getAt("properties")?.getAt("last_name") == "pereira"

        ((result.data?.getAt('readBlocks') as List)?.get(0)?.getAt("blocks") as List).get(0)?.getAt("id") == postId
        ((result.data?.getAt('readBlocks') as List)?.get(0)?.getAt("blocks") as List).get(0)?.getAt("label") == "POST"
        ((result.data?.getAt('readBlocks') as List)?.get(0)?.getAt("blocks") as List).get(0)?.getAt("properties")?.getAt("text") == "my first post"
    }
}
