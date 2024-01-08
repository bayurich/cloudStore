package ru.netology.netologydiplombackend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.netologydiplombackend.exception.InputDataException;
import ru.netology.netologydiplombackend.exception.InvalidCredentialsException;
import ru.netology.netologydiplombackend.model.auth.Login;
import ru.netology.netologydiplombackend.model.auth.Token;
import ru.netology.netologydiplombackend.model.file.FileInfo;
import ru.netology.netologydiplombackend.repository.CloudStoreRepository;

import java.util.List;

@Service
public class CloudStoreService {
    @Autowired
    TokenService tokenService;
    CloudStoreRepository cloudStoreRepository;

    private static final Logger log = LoggerFactory.getLogger(CloudStoreService.class);

    public CloudStoreService(CloudStoreRepository cloudStoreRepository) {
        this.cloudStoreRepository = cloudStoreRepository;
    }

    public Token login(Login login) {
        if (login == null) {
            log.error("login: Login is null");
            throw new InputDataException("Error input data");
        }
        String userLogin = login.getLogin();
        log.info("login: for login: {}", userLogin);

        String password = cloudStoreRepository.getPassword(userLogin);
        if (password == null) {
            log.error("login: Unknown login: {}", userLogin);
            throw new InvalidCredentialsException("Bad credentials");
        }
        if (!password.equals(login.getPassword())) {
            log.error("login: Incorrect password [{}] for login: {}", login.getPassword(), login);
            throw new InvalidCredentialsException("Bad credentials");
        }

        String token = tokenService.generateToken(userLogin);
        log.debug("login: generated token: {}", token);
        cloudStoreRepository.addToken(token);

        return new Token(token);
    }

    public void logout(String token) {
        String login = getLoginByToken(token);
        log.info("logout: for login: {}", login);

        cloudStoreRepository.deleteToken(getRawToken(token));
    }


    public List<FileInfo> getFiles(String token, int limit) {
        String login = getLoginByToken(token);
        log.info("getFiles: for login: {}", login);

        return cloudStoreRepository.getFiles(login, limit);
    }

    public void uploadFile(String token, String filename, MultipartFile file) {
        String login = getLoginByToken(token);
        log.info("uploadFile: for login: {}", login);

        if (file == null) {
            log.error("uploadFile: MultipartFile is null");
            throw new InputDataException("Error input data");
        }
        //TODO добавить проверку на размер файла
        //if (file.getBytes() > )
        filename = filename.isBlank() ? file.getName() : filename;

        try {
            cloudStoreRepository.uploadFile(login, filename, file);
        }
        catch (Exception e) {
            log.error("uploadFile: error while upload file: " + e);
            throw new RuntimeException("Error upload file");
        }
    }

    public byte[] downloadFile(String token, String filename) {
        String login = getLoginByToken(token);
        log.info("downloadFile: for login: {}", login);

        if (filename.isBlank()) {
            log.error("downloadFile: filename is empty");
            throw new InputDataException("Error input data");
        }

        try {
            byte[] data = cloudStoreRepository.downloadFile(login, filename);
            if (data == null) {
                log.error("downloadFile: file {} - data is null", filename);
                throw new RuntimeException("Error download file");
            }
            //log.debug("downloadFile: data: {}", data);
            return data;
        }
        catch (Exception e) {
            log.error("downloadFile: error while download file: " + e);
            throw new RuntimeException("Error download file");
        }
    }

    public void renameFile(String token, String currentFileName, FileInfo newFileInfo) {
        String login = getLoginByToken(token);
        log.info("renameFile: for login: {}", login);

        if (currentFileName.isBlank()) {
            log.error("renameFile: current filename is empty");
            throw new InputDataException("Error input data");
        }
        if (newFileInfo == null || newFileInfo.getName().isBlank()) {
            log.error("renameFile: new file name is empty");
            throw new InputDataException("Error input data");
        }

        try {
            cloudStoreRepository.renameFile(login, currentFileName, newFileInfo.getName());
        }
        catch (Exception e) {
            log.error("renameFile: error while rename file: " + e);
            throw new RuntimeException("Error rename file");
        }
    }

    public void deleteFile(String token, String filename) {
        String login = getLoginByToken(token);
        log.info("deleteFile: for login: {}", login);

        if (filename.isBlank()) {
            log.error("deleteFile: file name is empty");
            throw new InputDataException("Error input data");
        }

        try {
            cloudStoreRepository.deleteFile(login, filename);
        }
        catch (Exception e) {
            log.error("deleteFile: error while delete: " + e);
            throw new RuntimeException("Error delete file");
        }
    }

    private String getLoginByToken(String token) {
        token = getRawToken(token);

        if (!cloudStoreRepository.isFindToken(token)) {
            log.info("getLoginByToken: not found actual token: {}", token);
            throw new InvalidCredentialsException("Unauthorized error");
        }
        return tokenService.getLogin(token);
    }

    private String getRawToken(String token) {
        return token.replaceFirst("Bearer ", "");
    }
}
