package com.resiflow;

import com.resiflow.repository.UserRepository;
import java.lang.reflect.Proxy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.MOCK,
		properties = {
				"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
						+ "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
						+ "org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration",
				"app.jwt.secret=Zm9yLXRlc3RzLW9ubHktcmVzaWZsb3ctand0LXNlY3JldC1rZXktMzItYnl0ZXM=",
				"app.jwt.expiration-ms=3600000",
				"app.votes.scheduler.enabled=false",
				"spring.main.lazy-initialization=true"
		}
)
@AutoConfigureMockMvc
@Import(ResiflowApplicationTests.TestConfig.class)
class ResiflowApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@TestConfiguration
	static class TestConfig {

		@Bean
		UserRepository userRepository() {
			return (UserRepository) Proxy.newProxyInstance(
					UserRepository.class.getClassLoader(),
					new Class<?>[]{UserRepository.class},
					(proxy, method, args) -> {
						if ("toString".equals(method.getName())) {
							return "UserRepositoryTestProxy";
						}
						if ("hashCode".equals(method.getName())) {
							return System.identityHashCode(proxy);
						}
						if ("equals".equals(method.getName())) {
							return proxy == args[0];
						}
						throw new UnsupportedOperationException("Unsupported method: " + method.getName());
					});
		}
	}

	@Test
	void contextLoads() {
		assertThat(mockMvc).isNotNull();
	}

	@Test
	void healthEndpointReturnsOk() throws Exception {
		mockMvc.perform(get("/health"))
				.andExpect(status().isOk())
				.andExpect(content().string("OK"));
	}

}
