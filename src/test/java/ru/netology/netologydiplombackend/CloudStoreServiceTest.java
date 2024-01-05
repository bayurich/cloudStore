package ru.netology.netologydiplombackend;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.netologydiplombackend.exception.InputDataException;
import ru.netology.netologydiplombackend.exception.InvalidCredentialsException;
import ru.netology.netologydiplombackend.model.auth.Login;
import ru.netology.netologydiplombackend.model.auth.Token;
import ru.netology.netologydiplombackend.model.file.FileInfo;
import ru.netology.netologydiplombackend.repository.CloudStoreRepository;
import ru.netology.netologydiplombackend.service.CloudStoreService;
import ru.netology.netologydiplombackend.service.TokenService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CloudStoreServiceTest {

    CloudStoreService cloudStoreService;
    CloudStoreRepository cloudStoreRepository;
    TokenService tokenService;

    String correctLogin = "user";
    String correctToken = "Bearer token";
    String correctRawToken = "token";
    String wrongToken = "Bearer wrong_token";
    String wrongRawToken = "wrong_token";

    @Before
    public void setUp() {
        cloudStoreRepository = Mockito.mock(CloudStoreRepository.class);
        tokenService = Mockito.mock(TokenService.class);
        cloudStoreService = new CloudStoreService(cloudStoreRepository);
        ReflectionTestUtils.setField(cloudStoreService, "tokenService", tokenService);
    }

    @Test
    public void login_Ok_Test() {
        Mockito.reset(cloudStoreRepository);
        Mockito.reset(tokenService);

        //auth
        Mockito.when(cloudStoreRepository.getPassword("user")).thenReturn("password");
        Mockito.when(tokenService.generateToken("user")).thenReturn("generate_token_value");

        Login login = new Login("user", "password");
        Token token = cloudStoreService.login(login);
        assertEquals("generate_token_value", token.getToken());
    }

    @Test
    public void login_ErrPassword_Test() {
        Mockito.reset(cloudStoreRepository);
        Mockito.reset(tokenService);

        //auth
        Mockito.when(cloudStoreRepository.getPassword("user")).thenReturn("password");
        Mockito.when(tokenService.generateToken("user")).thenReturn("generate_token_value");

        Login login = new Login("user", "password_error");
        InvalidCredentialsException invalidCredentialsException = assertThrows(InvalidCredentialsException.class, () -> {
            cloudStoreService.login(login);
        });
        assertEquals("Bad credentials", invalidCredentialsException.getMessage());
    }

    @Test
    public void logout_Ok_Test() {
        Mockito.reset(cloudStoreRepository);

        cloudStoreService.logout("token");
        assertTrue(true);
    }

    @Test
    public void GetFiles_Ok_Test() {
        Mockito.reset(cloudStoreRepository);
        Mockito.reset(tokenService);

        //auth
        Mockito.when(tokenService.getLogin(correctRawToken)).thenReturn(correctLogin);
        Mockito.when(cloudStoreRepository.findToken(correctRawToken)).thenReturn(true);
        //getFiles
        List<FileInfo> expectedFileInfoList = new ArrayList<>();
        expectedFileInfoList.add(new FileInfo("file1.txt", 11111L));
        expectedFileInfoList.add(new FileInfo("file2.pdf", 2222L));
        expectedFileInfoList.add(new FileInfo("file3.gif", 3333333L));
        Mockito.when(cloudStoreRepository.getFiles(correctLogin, 3)).thenReturn(expectedFileInfoList);

        List<FileInfo> actualFileInfoList = cloudStoreService.getFiles(correctToken, 3);
        assertEquals(3, actualFileInfoList.size());
    }

    @Test
    public void GetFiles_Unauthorized_Test() {
        Mockito.reset(cloudStoreRepository);
        Mockito.reset(tokenService);

        //auth
        //Mockito.when(tokenService.getLogin(wrongRawToken)).thenThrow(new InvalidCredentialsException("Invalid compact JWT string"));

        InvalidCredentialsException invalidCredentialsException = assertThrows(InvalidCredentialsException.class, () -> {
            cloudStoreService.getFiles(wrongToken, 3);
        });
        assertEquals("Unauthorized error", invalidCredentialsException.getMessage());
    }

    @Test
    public void uploadFile_Ok_Test() throws IOException {
        Mockito.reset(cloudStoreRepository);
        Mockito.reset(tokenService);

        //auth
        Mockito.when(tokenService.getLogin(correctRawToken)).thenReturn(correctLogin);
        Mockito.when(cloudStoreRepository.findToken(correctRawToken)).thenReturn(true);

        File file = new File("src/test/resources/file.txt");
        FileInputStream fis = new FileInputStream(file);
        MultipartFile mpFile = new MockMultipartFile("file.txt", file.getName(), "txt/plain", Files.readAllBytes(file.toPath()));
        cloudStoreService.uploadFile(correctToken, "file.txt", mpFile);
        assertTrue(true);
    }

    @Test
    public void downloadFile_Ok_Test() throws IOException {
        Mockito.reset(cloudStoreRepository);
        Mockito.reset(tokenService);

        //auth
        Mockito.when(tokenService.getLogin(correctRawToken)).thenReturn(correctLogin);
        Mockito.when(cloudStoreRepository.findToken(correctRawToken)).thenReturn(true);
        //downloadFile
        File file = new File("src/test/resources/file.txt");
        byte[] bytes = Files.readAllBytes(file.toPath());
        Mockito.when(cloudStoreRepository.downloadFile(correctLogin, "file.txt")).thenReturn(bytes);

        byte[] expectedBytes = cloudStoreService.downloadFile(correctToken, "file.txt");
        assertEquals(expectedBytes, bytes);
    }

    @Test
    public void renameFile_Ok_Test() {
        Mockito.reset(cloudStoreRepository);
        Mockito.reset(tokenService);

        //auth
        Mockito.when(tokenService.getLogin(correctRawToken)).thenReturn(correctLogin);
        Mockito.when(cloudStoreRepository.findToken(correctRawToken)).thenReturn(true);

        FileInfo fileInfo = new FileInfo("new.txt", 1234L);
        cloudStoreService.renameFile(correctToken, "file.txt", fileInfo);
        assertTrue(true);
    }

    @Test
    public void renameFile_Unauthorized_Test() {
        Mockito.reset(cloudStoreRepository);
        Mockito.reset(tokenService);

        FileInfo fileInfo = new FileInfo("new.txt", 1234L);
        InvalidCredentialsException invalidCredentialsException = assertThrows(InvalidCredentialsException.class, () -> {
            cloudStoreService.renameFile(correctToken, "file.txt", fileInfo);
        });
        assertEquals("Unauthorized error", invalidCredentialsException.getMessage());
    }

    @Test
    public void renameFile_ErrorInputData_Test() {
        Mockito.reset(cloudStoreRepository);
        Mockito.reset(tokenService);

        //auth
        Mockito.when(tokenService.getLogin(correctRawToken)).thenReturn(correctLogin);
        Mockito.when(cloudStoreRepository.findToken(correctRawToken)).thenReturn(true);

        InputDataException inputDataException = assertThrows(InputDataException.class, () -> {
            cloudStoreService.renameFile(correctToken, "file.txt", null);
        });
        assertEquals("Error input data", inputDataException.getMessage());
    }

    @Test
    public void deleteFile_Ok_Test() {
        Mockito.reset(cloudStoreRepository);
        Mockito.reset(tokenService);

        //auth
        Mockito.when(tokenService.getLogin(correctRawToken)).thenReturn(correctLogin);
        Mockito.when(cloudStoreRepository.findToken(correctRawToken)).thenReturn(true);

        cloudStoreService.deleteFile(correctToken, "file.txt");
        assertTrue(true);
    }
}
