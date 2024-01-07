package ru.netology.netologydiplombackend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.netologydiplombackend.model.file.FileInfo;
import ru.netology.netologydiplombackend.model.auth.Login;
import ru.netology.netologydiplombackend.model.auth.Token;
import ru.netology.netologydiplombackend.service.CloudStoreService;

import javax.validation.constraints.NotBlank;
import java.util.List;

@RestController
public class CloudStoreController {
    CloudStoreService cloudStoreService;

    private static final Logger log = LoggerFactory.getLogger(CloudStoreController.class);

    public CloudStoreController(CloudStoreService cloudStoreService) {
        this.cloudStoreService = cloudStoreService;
    }

    @PostMapping("/login")
    public Token login(@RequestBody Login login) {
        log.info("Start endpoint [POST] [login] with login: {}", login != null ? login.getLogin(): "");

        return cloudStoreService.login(login);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("auth-token") @NotBlank String token) {
        log.info("Start endpoint POST logout with token: {}", token);
        cloudStoreService.logout(token);

        return new ResponseEntity<>("Success logout", HttpStatus.OK);
    }


    @GetMapping("/list")
    public List<FileInfo> getFiles(@RequestHeader("auth-token") @NotBlank String token,
                                   @RequestParam("limit") int limit) {
        log.info("Start endpoint GET list with token: {} limit: {}", token, limit);

        return cloudStoreService.getFiles(token, limit);
    }

    @PostMapping("/file")
    public ResponseEntity<String> uploadFile(@RequestHeader("auth-token") @NotBlank String token,
                                             @RequestParam("filename") String filename,
                                             @RequestParam("file") MultipartFile file) {
        log.info("Start endpoint POST file with token: {} filename: {}", token, filename);
        cloudStoreService.uploadFile(token, filename, file);

        return new ResponseEntity<>("Success upload", HttpStatus.OK);
    }

    @DeleteMapping("/file")
    public ResponseEntity<String> deleteFile(@RequestHeader("auth-token") @NotBlank String token,
                                             @RequestParam("filename") String filename) {
        log.info("Start endpoint DELETE file with token: {} filename: {}", token, filename);
        cloudStoreService.deleteFile(token, filename);

        return new ResponseEntity<>("Success deleted", HttpStatus.OK);
    }

    @GetMapping("/file")
    public ResponseEntity<byte[]> downloadFile(@RequestHeader("auth-token") @NotBlank String token,
                                             @RequestParam("filename") String filename) {
        log.info("Start endpoint GET file with token: {} filename: {}", token, filename);

        byte[] result = cloudStoreService.downloadFile(token, filename);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping("/file")
    public ResponseEntity<String> renameFile(@RequestHeader("auth-token") @NotBlank String token,
                                             @RequestParam("filename") String currentFileName,
                                             @RequestBody FileInfo newFileInfo) {
        log.info("Start endpoint PUT file with token: {} filename: {}", token, currentFileName);
        cloudStoreService.renameFile(token, currentFileName, newFileInfo);

        return new ResponseEntity<>("Success rename", HttpStatus.OK);
    }




}
