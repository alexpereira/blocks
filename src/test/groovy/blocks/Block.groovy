package blocks

import graphql.ExecutionResult

class Block extends AppTests {

    String createUserMutation;
    String patchUserMutation;

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

        patchUserMutation = """
            mutation {
                writeBlock(
                    block: { 
                        id: %d,
                        label: "USER",
                        properties: {
                            first_name: "Alex",
                            last_name: "Pereira"
                            city: "Boston"
                        }
                    }
                ) {
                    id
                    label
                    properties
                }
            }
        """
    }

    def "write block - create"() {
        when:
        ExecutionResult result = provider.graphQL().execute(createUserMutation)

        then:
        result.data != null
        result.data?.getAt('writeBlock')?.getAt('label') == 'USER'
        result.data?.getAt('writeBlock')?.getAt('properties')?.getAt('first_name') == 'alex'
        result.data?.getAt('writeBlock')?.getAt('properties')?.getAt('last_name') == 'pereira'
    }

    def "write block - patch"() {
        setup:
        ExecutionResult createResult = provider.graphQL().execute(createUserMutation)
        long userId = (long) createResult.data?.getAt('writeBlock')?.getAt('id')

        String mutation = String.format(patchUserMutation, userId)

        when:
        ExecutionResult result = provider.graphQL().execute(mutation)

        then:
        result.data != null
        result.data?.getAt('writeBlock')?.getAt('label') == 'USER'
        result.data?.getAt('writeBlock')?.getAt('properties')?.getAt('first_name') == 'Alex'
        result.data?.getAt('writeBlock')?.getAt('properties')?.getAt('last_name') == 'Pereira'
        result.data?.getAt('writeBlock')?.getAt('properties')?.getAt('city') == 'Boston'
    }

    def "read blocks"() {
        setup:
        provider.graphQL().execute(createUserMutation)
        provider.graphQL().execute(createUserMutation)
        provider.graphQL().execute(createUserMutation)

        String query = """
            query {
                readBlocks(
                    block: { 
                        label: "USER"
                    }
                ) {
                    id
                    label
                    properties
                }
            }
        """

        when:
        ExecutionResult result = provider.graphQL().execute(query)

        then:
        result.data != null
        (result.data?.getAt('readBlocks') as List).size() >= 3
    }
}
