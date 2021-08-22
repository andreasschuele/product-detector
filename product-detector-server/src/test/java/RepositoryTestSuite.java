import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import productdetector.repository.UserRepositoryTests;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        UserRepositoryTests.class
})
public class RepositoryTestSuite {
    @Test
    public void contextLoads() {
    }
}