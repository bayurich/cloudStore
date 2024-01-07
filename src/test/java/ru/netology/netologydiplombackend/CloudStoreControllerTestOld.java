package ru.netology.netologydiplombackend;

import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.netology.netologydiplombackend.model.auth.Login;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
//@ContextConfiguration(initializers = {CloudStoreControllerTestOld.Initializer.class})
public class CloudStoreControllerTestOld extends AbstractTestOld {

    @Container
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("postgres")
            .withUsername("postgres")
            .withPassword("postgres")
            .withReuse(true)
            .withExposedPorts(5432)
            /*.withCreateContainerCmdModifier(cmd -> cmd.withHostConfig(
                    new HostConfig().withPortBindings(new PortBinding(Ports.Binding.bindPort(5432), new ExposedPort(5433)))))*/
            .withInitScript("sql/ddl.sql")
            ;

    /*static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "CONTAINER.PORT=" + postgreSQLContainer.getMappedPort(5432)
                    *//*"CONTAINER.USERNAME=" + postgreSQLContainer.getUsername(),
                    "CONTAINER.PASSWORD=" + postgreSQLContainer.getPassword(),
                    "CONTAINER.URL=" + postgreSQLContainer.getJdbcUrl()*//*
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }*/

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        //postgreSQLContainer.start();
        //registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        String url = String.format("jdbc:postgresql://localhost:%d/postgres", postgreSQLContainer.getFirstMappedPort());
        registry.add("spring.datasource.url",
                () -> url);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
    }

    @Override
    @Before
    public void setUp() {
        super.setUp();
        //postgreSQLContainer.start();
        //int port = postgreSQLContainer.getMappedPort(5432);
        //postgreSQLContainer.addExposedPort(5433);
    }

    @Test
    public void test_Test() {
        assertTrue(true);
    }

    @Test
    public void login_ok_Test() throws Exception {
        String uri = "/login";
        Login login = new Login("111", "111");
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders
                        .post(uri)
                        .content(mapToJson(login))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn();

        int status = mvcResult.getResponse().getStatus();
        assertEquals(200, status);
        String content = mvcResult.getResponse().getContentAsString();
        //Product[] productlist = super.mapFromJson(content, Product[].class);
        //assertTrue(productlist.length > 0);
    }
}
