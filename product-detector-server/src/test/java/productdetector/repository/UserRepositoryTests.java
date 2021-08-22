package productdetector.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import productdetector.model.User;
import unittest.ClearDatabaseAfterTestClass;
import unittest.ClearDatabaseAfterTestMethod;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@ClearDatabaseAfterTestClass
public class UserRepositoryTests {

	@Autowired
	UserRepository userRepository;

	private User createUser(String username) {
		User user = new User();

		user.setUsername(username);
		user.setPassword("secret");

		return user;
	}

	@Test
	@ClearDatabaseAfterTestMethod
	public void testIdGenerator() {
		User user = createUser("test");

		user = userRepository.save(user);

		assertNotNull(user.getId());
		assertNotNull(user.getUsername());
		assertNotNull(user.getPassword());
	}

	@Test
	@ClearDatabaseAfterTestMethod
	public void testMandatoryFieldsAreNotNull() {
		User user = createUser("test");

		user = userRepository.save(user);

		user = userRepository.findByUsername("test").get();

		assertNotNull(user.getId());
		assertNotNull(user.getUsername());
		assertNotNull(user.getPassword());
	}

}
