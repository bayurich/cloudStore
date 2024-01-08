package ru.netology.netologydiplombackend;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.netologydiplombackend.model.file.FileInfo;
import ru.netology.netologydiplombackend.repository.CloudStoreRepository;

import javax.sql.DataSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class CloudStoreRepositoryTest {

    CloudStoreRepository cloudStoreRepository;

    //@Mock
    DataSource mockDataSource = Mockito.mock(DataSource.class);
    //@Mock
    JdbcTemplate mockJdbcTemplate = Mockito.mock(JdbcTemplate.class);

    @Before
    public void setUp() {
        cloudStoreRepository = new CloudStoreRepository(mockDataSource, mockJdbcTemplate);
    }


    @Test
    public void getPassword_Ok_Test() {

        //SQL_SELECT_PASSWORD
        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();
        map.put("PASSWORD", "password");
        list.add(map);
        Mockito.when(mockJdbcTemplate.queryForList(Mockito.anyString(), Mockito.eq("user"))).thenReturn(list);

        String password = cloudStoreRepository.getPassword("user");
        assertEquals("password", password);
    }

    @Test
    public void getPassword_NotFound_Test() {
        //SQL_SELECT_PASSWORD
        Mockito.when(mockJdbcTemplate.queryForList(Mockito.anyString(), ArgumentMatchers.<Object>any())).thenReturn(new ArrayList<>());

        String password = cloudStoreRepository.getPassword("user");
        assertEquals(null, password);
    }

    @Test
    public void addToken_Ok_Test() {
        cloudStoreRepository.addToken("token");
        assertTrue(true);
    }

    @Test
    public void addToken_Err_Test() {
        //SQL_INSERT_TOKEN
        Mockito.when(mockJdbcTemplate.update(Mockito.anyString(), Mockito.eq("token"))).thenThrow(new DataAccessException("SQL_INSERT_TOKEN Error"){});

        DataAccessException dataAccessException = assertThrows(DataAccessException.class, () -> {
            cloudStoreRepository.addToken("token");
        });
        assertEquals("SQL_INSERT_TOKEN Error", dataAccessException.getMessage());
    }

    @Test
    public void deleteToken_Ok_Test() {
        cloudStoreRepository.deleteToken("token");
        assertTrue(true);
    }

    @Test
    public void deleteToken_Err_Test() {
        //SQL_DELETE_TOKEN
        Mockito.when(mockJdbcTemplate.update(Mockito.anyString(), Mockito.eq("token"))).thenThrow(new DataAccessException("SQL_DELETE_TOKEN Error"){});

        DataAccessException dataAccessException = assertThrows(DataAccessException.class, () -> {
            cloudStoreRepository.deleteToken("token");
        });
        assertEquals("SQL_DELETE_TOKEN Error", dataAccessException.getMessage());
    }

    @Test
    public void findToken_Ok_Test() {
        //SQL_SELECT_COUNT_TOKEN
        Mockito.when(mockJdbcTemplate.queryForObject(Mockito.anyString(), Mockito.eq(Integer.class), Mockito.eq("token"))).thenReturn(1);
        Mockito.when(mockJdbcTemplate.queryForObject(Mockito.anyString(), Mockito.eq(Integer.class), Mockito.startsWith("wrong"))).thenReturn(0);

        boolean actualResult = cloudStoreRepository.isFindToken("token");
        assertTrue(actualResult);

        actualResult = cloudStoreRepository.isFindToken("wrong_token");
        assertFalse(actualResult);
    }

    @Test
    public void findToken_Err_Test() {
        //SQL_SELECT_COUNT_TOKEN
        Mockito.when(mockJdbcTemplate.queryForObject(Mockito.anyString(), Mockito.eq(Integer.class), Mockito.eq("token"))).thenThrow(new DataAccessException("SQL_SELECT_COUNT_TOKEN Error"){});

        DataAccessException dataAccessException = assertThrows(DataAccessException.class, () -> {
            cloudStoreRepository.isFindToken("token");
        });
        assertEquals("SQL_SELECT_COUNT_TOKEN Error", dataAccessException.getMessage());
    }

    @Test
    public void getFiles_Ok_Test() {
        //SQL_SELECT_FILES_BY_LOGIN
        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();
        map.put("NAME", "file1.txt");
        map.put("SIZE", 11111L);
        list.add(map);
        map = new HashMap<>();
        map.put("NAME", "file2.txt");
        map.put("SIZE", 222222L);
        list.add(map);
        Mockito.when(mockJdbcTemplate.queryForList(Mockito.anyString(), Mockito.eq("user"), Mockito.any())).thenReturn(list);

        List<FileInfo> fileInfoList = cloudStoreRepository.getFiles("user", 3);
        assertEquals(2, fileInfoList.size());
    }

    @Test
    public void getFiles_Err_Test() {
        //SQL_SELECT_FILES_BY_LOGIN
        Mockito.when(mockJdbcTemplate.queryForList(Mockito.anyString(), Mockito.eq("user"), Mockito.any())).thenThrow(new DataAccessException("SQL_SELECT_FILES_BY_LOGIN Error"){});

        RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> {
            cloudStoreRepository.getFiles("user", 3);
        });
        assertEquals("Error getting file list", runtimeException.getMessage());
    }

    @Test
    public void uploadFile_Ok_Test() throws Exception {
        //SQL_INSERT_FILE
        //Mockito.when(mockJdbcTemplate.update(Mockito.anyString(), Mockito.eq("user"), Mockito.eq("file,txt"), Mockito.anyString(), Mockito.anyLong()));

        File file = new File("src/test/resources/file.txt");
        FileInputStream fis = new FileInputStream(file);
        MultipartFile mpFile = new MockMultipartFile("file.txt", file.getName(), "txt/plain", Files.readAllBytes(file.toPath()));

        cloudStoreRepository.uploadFile("user", "file.txt", mpFile);
        assertTrue(true);
    }

    @Test
    public void uploadFile_Err_Test() throws IOException {
        //SQL_INSERT_FILE
        Mockito.when(mockJdbcTemplate.update(Mockito.anyString(), Mockito.eq("user"), Mockito.eq("file.txt"), Mockito.anyString(), Mockito.anyLong())).thenThrow(new DataAccessException("SQL_INSERT_FILE Error"){});

        Exception exception = assertThrows(Exception.class, () -> {
            cloudStoreRepository.uploadFile("user", "file.txt", null);
        });
        assertEquals("file is empty", exception.getMessage());

        File file = new File("src/test/resources/file.txt");
        FileInputStream fis = new FileInputStream(file);
        MultipartFile mpFile = new MockMultipartFile("file.txt", file.getName(), "txt/plain", Files.readAllBytes(file.toPath()));
        DataAccessException dataAccessException = assertThrows(DataAccessException.class, () -> {
            cloudStoreRepository.uploadFile("user", "file.txt", mpFile);
        });
        assertEquals("SQL_INSERT_FILE Error", dataAccessException.getMessage());
    }

    @Test
    public void downloadFile_Ok_Test() {

        //SQL_SELECT_FILE_BY_LOGIN_AND_FILENAME
        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();
        map.put("DATA", "VGVzdCBmb3IgdXBsb2FkIGZpbGU");
        list.add(map);
        Mockito.when(mockJdbcTemplate.queryForList(Mockito.anyString(), Mockito.eq("user"), Mockito.eq("file.txt"))).thenReturn(list);

        byte[] actualBytes = cloudStoreRepository.downloadFile("user", "file.txt");
        assertArrayEquals("Test for upload file".getBytes(StandardCharsets.UTF_8), actualBytes);
    }

    @Test
    public void downloadFile_Err_Test() {
        //SQL_SELECT_FILE_BY_LOGIN_AND_FILENAME
        Mockito.when(mockJdbcTemplate.queryForList(Mockito.anyString(), Mockito.eq("user"), Mockito.eq("file.txt"))).thenThrow(new DataAccessException("SQL_SELECT_FILE_BY_LOGIN_AND_FILENAME Error"){});

        DataAccessException dataAccessException = assertThrows(DataAccessException.class, () -> {
            cloudStoreRepository.downloadFile("user", "file.txt");
        });
        assertEquals("SQL_SELECT_FILE_BY_LOGIN_AND_FILENAME Error", dataAccessException.getMessage());
    }

    @Test
    public void renameFile_Ok_Test() throws Exception {
        //SQL_UPDATE_FILE

        cloudStoreRepository.renameFile("user", "file.txt", "new.txt");
        assertTrue(true);
    }

    @Test
    public void renameFile_Err_Test() throws Exception {
        //SQL_UPDATE_FILE
        Mockito.when(mockJdbcTemplate.update(Mockito.anyString(), Mockito.eq("new.txt"), Mockito.eq("user"), Mockito.eq("file.txt"))).thenThrow(new DataAccessException("SQL_UPDATE_FILE Error"){});

        DataAccessException dataAccessException = assertThrows(DataAccessException.class, () -> {
            cloudStoreRepository.renameFile("user", "file.txt", "new.txt");
        });
        assertEquals("SQL_UPDATE_FILE Error", dataAccessException.getMessage());
    }

    @Test
    public void deleteFile_Ok_Test() throws Exception {
        //SQL_DELETE_FILE

        cloudStoreRepository.deleteFile("user", "file.txt");
        assertTrue(true);
    }

    @Test
    public void deleteFile_Err_Test() throws Exception {
        //SQL_DELETE_FILE
        Mockito.when(mockJdbcTemplate.update(Mockito.anyString(), Mockito.eq("user"), Mockito.eq("file.txt"))).thenThrow(new DataAccessException("SQL_DELETE_FILE Error"){});

        DataAccessException dataAccessException = assertThrows(DataAccessException.class, () -> {
            cloudStoreRepository.deleteFile("user", "file.txt");
        });
        assertEquals("SQL_DELETE_FILE Error", dataAccessException.getMessage());
    }
}
