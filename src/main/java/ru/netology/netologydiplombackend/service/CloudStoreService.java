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

        String userLogin = login.getLogin();
        String password = cloudStoreRepository.getPassword(userLogin);
        if (password == null) {
            log.error("Unknown login: {}", userLogin);
            throw new InvalidCredentialsException("Bad credentials");
        }
        if (!password.equals(login.getPassword())) {
            log.error("Incorrect password [{}] for login: {}", login.getPassword(), login);
            throw new InvalidCredentialsException("Bad credentials");
        }

        String token = tokenService.generateToken(userLogin);
        log.debug("generate token: {}", token);
        cloudStoreRepository.addToken(token);
        return new Token(token);
    }

    public void logout(String token) {
        //TODO сначала проверить, что за токен, правильный ли, прежде чем удалять????

        cloudStoreRepository.deleteToken(getRawToken(token));
    }


    public List<FileInfo> getFiles(String token, int limit) {
        String login = getLoginByToken(token);

        return cloudStoreRepository.getFiles(login, limit);
    }

    public void uploadFile(String token, String filename, MultipartFile file) {
        String login = getLoginByToken(token);

        if (file == null) {
            throw new InputDataException("Error input data");
        }
        //TODO добавить проверку на размер файла
        //if (file.getBytes() > )
        filename = filename.isBlank() ? file.getName() : filename;

        try {
            cloudStoreRepository.uploadFile(login, filename, file);
        }
        catch (Exception e) {
            log.error("uploadFile: error: " + e);
            throw new RuntimeException("Error upload file");
        }
    }

    public byte[] downloadFile(String token, String filename) {
        String login = getLoginByToken(token);

        if (filename.isBlank()) {
            throw new InputDataException("Error input data");
        }

        try {
            byte[] data = cloudStoreRepository.downloadFile(login, filename);
            if (data == null) {
                log.error("downloadFile: file {} - data is null", filename);
                throw new RuntimeException("Error download file");
            }
            log.error("downloadFile: data: {}", data);
            return data;
        }
        catch (Exception e) {
            log.error("downloadFile: error: " + e);
            throw new RuntimeException("Error download file");
        }
    }

    public void renameFile(String token, String currentFileName, FileInfo newFileInfo) {
        String login = getLoginByToken(token);

        if (currentFileName.isBlank()) {
            throw new InputDataException("Error input data");
        }
        if (newFileInfo == null || newFileInfo.getName().isBlank()) {
            throw new InputDataException("Error input data");
        }

        try {
            cloudStoreRepository.renameFile(login, currentFileName, newFileInfo.getName());
        }
        catch (Exception e) {
            log.error("renameFile: error: " + e);
            throw new RuntimeException("Error rename file");
        }
    }

    public void deleteFile(String token, String filename) {
        String login = getLoginByToken(token);

        if (filename.isBlank()) {
            throw new InputDataException("Error input data");
        }

        try {
            cloudStoreRepository.deleteFile(login, filename);
        }
        catch (Exception e) {
            log.error("renameFile: error: " + e);
            throw new RuntimeException("Error delete file");
        }
    }

    private String getLoginByToken(String token) {
        token = getRawToken(token);

        if (!cloudStoreRepository.findToken(token)) {
            throw new InvalidCredentialsException("Unauthorized error");
        }
        return tokenService.getLogin(token);
    }

    private String getRawToken(String token) {
        return token.replaceFirst("Bearer ", "");
    }
}
