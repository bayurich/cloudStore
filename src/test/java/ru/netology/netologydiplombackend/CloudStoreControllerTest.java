package ru.netology.netologydiplombackend;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.netology.netologydiplombackend.model.auth.Login;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CloudStoreControllerTest extends AbstractTest {
    @Override
    @Before
    public void setUp() {
        super.setUp();
    }

    @Test
    public void login_ok_Test() throws Exception {
        String uri = "/login";
        Login login = new Login("user", "password");
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri, login)
                .accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();

        int status = mvcResult.getResponse().getStatus();
        assertEquals(200, status);
        String content = mvcResult.getResponse().getContentAsString();
        //Product[] productlist = super.mapFromJson(content, Product[].class);
        //assertTrue(productlist.length > 0);
    }
}
