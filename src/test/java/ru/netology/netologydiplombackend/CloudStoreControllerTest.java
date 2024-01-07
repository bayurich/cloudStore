package ru.netology.netologydiplombackend;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.netologydiplombackend.model.auth.Login;
import ru.netology.netologydiplombackend.model.auth.Token;
import ru.netology.netologydiplombackend.model.file.FileInfo;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CloudStoreControllerTest extends AbstractTest{

    @Override
    @Before
    public void setUp() {
        super.setUp();
    }

    @Test
    @Sql(scripts = "classpath:sql/insert_test_user.sql")
    public void integration_Test() throws Exception {

        String wrong_token = "xxxxxxxxxxxxxxxxxxxxxxxxxx";

        // login -----------------------------------------------------------------
        String uri = "/login";

        //wrong data
        mvc.perform(MockMvcRequestBuilders
                .post(uri)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError());

        //wrong user
        Login login = new Login("wrong_user", "test_password");
        mvc.perform(MockMvcRequestBuilders
                .post(uri)
                .content(mapToJson(login))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        //wrong password
        login = new Login("test_user", "wrong_password");
        mvc.perform(MockMvcRequestBuilders
                .post(uri)
                .content(mapToJson(login))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        //correct auth data
        login = new Login("test_user", "test_password");
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders
                        .post(uri)
                        .content(mapToJson(login))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn();

        int status = mvcResult.getResponse().getStatus();
        assertEquals(200, status);
        String content = mvcResult.getResponse().getContentAsString();
        Token token = mapFromJson(content, Token.class);
        assertNotNull(token);
        String tokenValue = token.getTokenValue();
        assertNotNull(tokenValue);
        assertEquals(3, tokenValue.split("\\.").length);


        // list -----------------------------------------------------------------
        uri = "/list";

        //wrong data
        mvc.perform(MockMvcRequestBuilders
                .get(uri)
                .param("limit", "3")
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        mvc.perform(MockMvcRequestBuilders
                .get(uri)
                .header("auth-token", wrong_token)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        //wrong token
        mvc.perform(MockMvcRequestBuilders
                .get(uri)
                .header("auth-token", wrong_token)
                .param("limit", "3")
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        //correct token
        mvcResult = mvc.perform(MockMvcRequestBuilders
                .get(uri)
                .header("auth-token", tokenValue)
                .param("limit", "3")
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn();
        assertEquals(200, mvcResult.getResponse().getStatus());
        content = mvcResult.getResponse().getContentAsString();
        List<FileInfo> fileInfoList = mapFromJson(content, new TypeReference<List<FileInfo>>() {});
        assertNotNull(fileInfoList);
        assertEquals(0, fileInfoList.size());


        // POST file (upload) -----------------------------------------------------------------
        uri = "/file";

        File file = new File("src/test/resources/file.txt");
        FileInputStream fis = new FileInputStream(file);
        MockMultipartFile mpFile = new MockMultipartFile("file", file.getName(), MediaType.TEXT_PLAIN_VALUE, Files.readAllBytes(file.toPath()));

        //wrong data
        mvc.perform(MockMvcRequestBuilders
                .post(uri)
                .param("filename", "file.txt")
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        mvc.perform(MockMvcRequestBuilders
                .multipart(uri)
                .header("auth-token", wrong_token)
                .param("filename", "file.txt")
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        //wrong token
        mvc.perform(MockMvcRequestBuilders
                .multipart(uri).file(mpFile)
                .header("auth-token", wrong_token)
                .param("filename", "file.txt")
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        //correct token
        mvcResult = mvc.perform(MockMvcRequestBuilders
                        .multipart(uri).file(mpFile)
                        .header("auth-token", tokenValue)
                        .param("filename", "file.txt")
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andReturn();
        assertEquals(200, mvcResult.getResponse().getStatus());
        content = mvcResult.getResponse().getContentAsString();
        assertEquals("Success upload", content);

        // list - check after upload -----------------------------------------------------------
        uri = "/list";

        //correct token
        mvcResult = mvc.perform(MockMvcRequestBuilders
                        .get(uri)
                        .header("auth-token", tokenValue)
                        .param("limit", "3")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn();
        assertEquals(200, mvcResult.getResponse().getStatus());
        content = mvcResult.getResponse().getContentAsString();
        fileInfoList = mapFromJson(content, new TypeReference<List<FileInfo>>() {});
        assertNotNull(fileInfoList);
        assertEquals(1, fileInfoList.size());
        assertEquals("file.txt", fileInfoList.get(0).getName());

        // PUT file (rename) -----------------------------------------------------------
        uri = "/file";

        FileInfo newFileInfo = new FileInfo("new_file.txt", null);

        //wrong data
        mvc.perform(MockMvcRequestBuilders
                        .put(uri)
                        .header("auth-token", wrong_token)
                        .param("filename", "file.txt")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError());
        mvc.perform(MockMvcRequestBuilders
                        .put(uri)
                        .header("auth-token", wrong_token)
                        .content(mapToJson(newFileInfo))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        mvc.perform(MockMvcRequestBuilders
                        .put(uri)
                        .param("filename", "file.txt")
                        .content(mapToJson(newFileInfo))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        //wrong token
        mvc.perform(MockMvcRequestBuilders
                .put(uri)
                .header("auth-token", wrong_token)
                .param("filename", "file.txt")
                .content(mapToJson(newFileInfo))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());


        //correct token
        mvcResult = mvc.perform(MockMvcRequestBuilders
                        .put(uri)
                        .header("auth-token", tokenValue)
                        .param("filename", "file.txt")
                        .content(mapToJson(newFileInfo))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn();
        assertEquals(200, mvcResult.getResponse().getStatus());
        content = mvcResult.getResponse().getContentAsString();
        assertEquals("Success rename", content);

        // list - check after rename -----------------------------------------------------------
        uri = "/list";

        //correct token
        mvcResult = mvc.perform(MockMvcRequestBuilders
                        .get(uri)
                        .header("auth-token", tokenValue)
                        .param("limit", "3")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn();
        assertEquals(200, mvcResult.getResponse().getStatus());
        content = mvcResult.getResponse().getContentAsString();
        fileInfoList = mapFromJson(content, new TypeReference<List<FileInfo>>() {});
        assertNotNull(fileInfoList);
        assertEquals(1, fileInfoList.size());
        assertEquals("new_file.txt", fileInfoList.get(0).getName());

        // GET file (download) -----------------------------------------------------------
        uri = "/file";

        //wrong data
        mvc.perform(MockMvcRequestBuilders
                .get(uri)
                .header("auth-token", wrong_token))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        mvc.perform(MockMvcRequestBuilders
                .get(uri)
                .param("filename", "new_file.txt"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        //wrong token
        mvc.perform(MockMvcRequestBuilders
                .get(uri)
                .header("auth-token", wrong_token)
                .param("filename", "new_file.txt"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        //correct token
        mvcResult = mvc.perform(MockMvcRequestBuilders
                        .get(uri)
                        .header("auth-token", tokenValue)
                        .param("filename", "new_file.txt"))
                .andReturn();
        assertEquals(200, mvcResult.getResponse().getStatus());
        byte[] contentByte = mvcResult.getResponse().getContentAsByteArray();
        assertEquals("Test for upload file", new String(contentByte));

        // DELETE file (delete) -----------------------------------------------------------
        uri = "/file";

        //wrong data
        mvc.perform(MockMvcRequestBuilders
                        .delete(uri)
                        .header("auth-token", wrong_token))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        mvc.perform(MockMvcRequestBuilders
                        .delete(uri)
                        .param("filename", "new_file.txt"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        //wrong token
        mvc.perform(MockMvcRequestBuilders
                        .delete(uri)
                        .header("auth-token", wrong_token)
                        .param("filename", "new_file.txt"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        //correct token
        mvcResult = mvc.perform(MockMvcRequestBuilders
                        .delete(uri)
                        .header("auth-token", tokenValue)
                        .param("filename", "new_file.txt")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn();
        assertEquals(200, mvcResult.getResponse().getStatus());
        content = mvcResult.getResponse().getContentAsString();
        assertEquals("Success deleted", content);

        // list - check after delete -----------------------------------------------------------
        uri = "/list";

        //correct token
        mvcResult = mvc.perform(MockMvcRequestBuilders
                        .get(uri)
                        .header("auth-token", tokenValue)
                        .param("limit", "3")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn();
        assertEquals(200, mvcResult.getResponse().getStatus());
        content = mvcResult.getResponse().getContentAsString();
        fileInfoList = mapFromJson(content, new TypeReference<List<FileInfo>>() {});
        assertNotNull(fileInfoList);
        assertEquals(0, fileInfoList.size());

        // logout -----------------------------------------------------------------------------
        uri = "/logout";

        //wrong data
        mvc.perform(MockMvcRequestBuilders
                .post(uri)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        //wrong token
        mvc.perform(MockMvcRequestBuilders
                .post(uri)
                .header("auth-token", wrong_token)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        //correct token
        mvcResult = mvc.perform(MockMvcRequestBuilders
                        .post(uri)
                        .header("auth-token", tokenValue)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn();
        assertEquals(200, mvcResult.getResponse().getStatus());
        content = mvcResult.getResponse().getContentAsString();
        assertEquals("Success logout", content);

        // list - check after logout -----------------------------------------------------------
        uri = "/list";

        //correct token
        mvc.perform(MockMvcRequestBuilders
                .get(uri)
                .header("auth-token", tokenValue)
                .param("limit", "3")
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }
}
