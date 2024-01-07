package ru.netology.netologydiplombackend;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;

@SpringBootTest
@Testcontainers
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class AbstractTest {

    ObjectMapper objectMapper = new ObjectMapper();

    private static final String DATABASE_NAME = "postgress_test";

    protected MockMvc mvc;
    @Autowired
    WebApplicationContext webApplicationContext;

    protected void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Container
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName(DATABASE_NAME)
            .withUsername("postgres_test")
            .withPassword("postgres_test")
            .withReuse(true)
            .withExposedPorts(5432)
            .withInitScript("sql/ddl.sql")
            ;


    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        postgreSQLContainer.start();

        String url = String.format("jdbc:postgresql://localhost:%d/" + DATABASE_NAME, postgreSQLContainer.getFirstMappedPort());
        registry.add("spring.datasource.url",
                () -> url);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
    }

    protected String mapToJson(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }
    protected <T> T mapFromJson(String json, Class<T> clazz) throws JsonParseException, JsonMappingException, IOException {
        return objectMapper.readValue(json, clazz);
    }

    protected <T> T mapFromJson(String json, TypeReference<T> tTypeReference) throws JsonParseException, JsonMappingException, IOException {
        return objectMapper.readValue(json, tTypeReference);
    }
}
