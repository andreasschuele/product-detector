package unittest;

import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

public class CleanupDatabaseTestExecutionListener
        extends AbstractTestExecutionListener {

    public final int getOrder() {
        return 2001;
    }

    private boolean alreadyCleared = false;

    @Override
    public void prepareTestInstance(TestContext testContext) throws Exception {
        if (!alreadyCleared) {
            cleanupDatabase(testContext);
            alreadyCleared = true;
        } else {
            alreadyCleared = true;
        }
    }

    @Override
    public void afterTestClass(TestContext testContext) throws Exception {
        cleanupDatabase(testContext);
    }

    @Override
    public void afterTestMethod(TestContext testContext) throws Exception {
        if(testContext.getTestMethod().getAnnotation(ClearDatabaseAfterTestMethod.class)!=null){
            cleanupDatabase(testContext);
        }
        super.afterTestMethod(testContext);
    }

    private void cleanupDatabase(TestContext testContext) throws LiquibaseException {
        ApplicationContext app = testContext.getApplicationContext();
        SpringLiquibase springLiquibase = app.getBean(SpringLiquibase.class);
        springLiquibase.setDropFirst(true);
        springLiquibase.afterPropertiesSet();
    }
}