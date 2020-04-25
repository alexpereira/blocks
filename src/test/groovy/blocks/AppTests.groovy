package blocks

import org.neo4j.ogm.support.FileUtils
import spock.lang.Specification

import java.nio.file.Paths
import java.util.logging.Logger

class AppTests extends Specification {

    final static Logger logger = Logger.getLogger(AppTests.class.getName());
    final static Provider provider = new Provider()
    final static String TEST_DATA_PATH = 'data/test'

    def setupSpec() {
        logger.info('Starting AppTests')
        provider.init(TEST_DATA_PATH)
    }

    def cleanupSpec() {
        logger.info('Shutting down database')
        provider.graphService().shutdown()

        logger.info('Delete test data')
        FileUtils.deleteDirectory(Paths.get(TEST_DATA_PATH))
    }
}
