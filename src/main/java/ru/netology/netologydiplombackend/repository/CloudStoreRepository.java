package ru.netology.netologydiplombackend.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.netologydiplombackend.model.file.FileInfo;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Repository
@EnableAutoConfiguration
public class CloudStoreRepository {
    private static final Logger log = LoggerFactory.getLogger(CloudStoreRepository.class);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String SQL_SELECT_PASSWORD = "select password as \"PASSWORD\" from users where login = ?";

    private static final String SQL_INSERT_TOKEN = "insert into tokens (token) values (?)";

    private static final String SQL_DELETE_TOKEN = "delete from tokens where token = ?";

    private static final String SQL_SELECT_COUNT_TOKEN = "select count(*) from tokens where token = ?";

    private static final String SQL_SELECT_FILES_BY_LOGIN = "select name as \"NAME\", size as \"SIZE\" " +
            "from files where " +
            "id_user in (select id from users where login = ?) limit ?";

    private static final String SQL_SELECT_FILE_BY_LOGIN_AND_FILENAME = "select data as \"DATA\" " +
            "from files where " +
            "id_user in (select id from users where login = ?) and name = ?";

    private static final String SQL_INSERT_FILE = "insert into files (id_user , name, data, size) " +
            "values((select id from users where login = ?), ?, ?, ?)";

    private static final String SQL_UPDATE_FILE = "update files " +
            "set name = ?, date_update = CURRENT_TIMESTAMP " +
            "where " +
            "id_user in (select id from users where login = ?) and name = ?";

    private static final String SQL_DELETE_FILE = "delete from files " +
            "where " +
            "id_user in (select id from users where login = ?) and name = ?";

    public CloudStoreRepository(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    public String getPassword(String login) {
        log.debug("getPassword for login: {}", login);
        List<Map<String, Object>> sqlResult = jdbcTemplate.queryForList(SQL_SELECT_PASSWORD, login);
        if (sqlResult.size() == 1) {
            //TODO лучше хранить пароль в зашифрованном виде
            return sqlResult.get(0).containsKey("PASSWORD") ? sqlResult.get(0).get("PASSWORD").toString() : null;
        }
        else return null;
    }

    public void addToken(String token) {
        jdbcTemplate.update(SQL_INSERT_TOKEN, token);
    }

    public void deleteToken(String token) {
        jdbcTemplate.update(SQL_DELETE_TOKEN, token);
    }

    public boolean findToken(String token) {
        int count = jdbcTemplate.queryForObject(SQL_SELECT_COUNT_TOKEN, Integer.class, token);
        return count > 0;
    }

    public List<FileInfo> getFiles(String login, long limit) {
        log.debug("start getFiles: login: {} limit: {}", login, limit);
        List<FileInfo> fileInfoList = new ArrayList<>();
        try {
            List<Map<String, Object>> sqlResult = jdbcTemplate.queryForList(SQL_SELECT_FILES_BY_LOGIN, login, limit);
            log.debug("getFiles: sqlResult: {}", sqlResult);
            if (!sqlResult.isEmpty()) {
                sqlResult.forEach(entry -> {
                    String filename = entry.get("NAME").toString();
                    Long size = (Long) entry.get("SIZE");

                    fileInfoList.add(new FileInfo(filename, size));
                });
            }
            log.debug("getFiles: fileInfoList: {}", fileInfoList);
        }
        catch (Exception e) {
            log.error("getFiles: error while getting files: {}", e);
            throw new RuntimeException("Error getting file list");
        }

        return fileInfoList;
    }

    public void uploadFile(String login, String filename, MultipartFile file) throws IOException {
        if (file == null) {
            throw  new RuntimeException("file is empty");
        }
        String data = Base64.getEncoder().encodeToString(file.getBytes());
        jdbcTemplate.update(SQL_INSERT_FILE, login, filename, data, file.getSize());
    }

    public byte[] downloadFile(String login, String filename) {
        List<Map<String, Object>> sqlResult = jdbcTemplate.queryForList(SQL_SELECT_FILE_BY_LOGIN_AND_FILENAME, login, filename);
        if (sqlResult.size() == 1) {
            Object data = sqlResult.get(0).get("DATA");
            return data != null ? Base64.getDecoder().decode(data.toString()) : null;
        }
        else return new byte[0];
    }

    public void renameFile(String login, String filename, String newFilename) {
        jdbcTemplate.update(SQL_UPDATE_FILE, newFilename, login, filename);
    }

    public void deleteFile(String login, String filename) {
        jdbcTemplate.update(SQL_DELETE_FILE, login, filename);
    }
}
