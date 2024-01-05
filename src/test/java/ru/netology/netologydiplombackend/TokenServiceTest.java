package ru.netology.netologydiplombackend;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import ru.netology.netologydiplombackend.exception.InvalidCredentialsException;
import ru.netology.netologydiplombackend.service.TokenService;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class TokenServiceTest {

    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration}")
    private Duration expiration;

    TokenService tokenService;

    @Before
    public void setUp() {
        tokenService = new TokenService(secret);
        ReflectionTestUtils.setField(tokenService, "expiration", expiration);
    }


    @Test
    public void checkTokenTest() {

        String login = "testLogin";
        String token = tokenService.generateToken(login);

        assertEquals(login, tokenService.getLogin(token));
    }

    @Test
    public void checkTokenExpirationTest() {

        String login = "testLogin";
        ReflectionTestUtils.setField(tokenService, "expiration", Duration.ofMillis(1));
        String token = tokenService.generateToken(login);

        InvalidCredentialsException invalidCredentialsException = assertThrows(InvalidCredentialsException.class, () -> {
            tokenService.getLogin(token);
        });
        assertTrue(invalidCredentialsException.getMessage().startsWith("JWT expired"));
    }

    @Test
    public void checkTokenIncorrectTest() {

        String token = "eyJhbGciOiJIUzM4NCJ9LnsiaXNzIjoiY2xvdWRTdG9yZSIsImlhdCI6MTcwNDExODY2NCwic3ViIjoidXNlckluZm8iLCJsb2dpbiI6IjExMSIsImV4cCI6MTcwNDEyMjI2NH0ut8ZOh3e2as1SF4KrLQCGgZLm8h8nqTlItTC7LOTN1vHM5I0t804oaluhHtCpJgz5";

        InvalidCredentialsException invalidCredentialsException = assertThrows(InvalidCredentialsException.class, () -> {
            tokenService.getLogin(token);
        });
        assertTrue(invalidCredentialsException.getMessage().startsWith("Invalid compact JWT string"));
    }
}
