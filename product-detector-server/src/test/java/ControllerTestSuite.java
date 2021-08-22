import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import productdetector.controller.UserControllerIT;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        UserControllerIT.class,
})
public class ControllerTestSuite {
    @Test
    public void contextLoads() {
    }
}