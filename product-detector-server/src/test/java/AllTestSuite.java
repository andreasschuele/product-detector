import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    RepositoryTestSuite.class,
    ServiceTestSuite.class,
    ControllerTestSuite.class,
})
public class AllTestSuite {
    @Test
    public void contextLoads() {
    }
}