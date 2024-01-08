package ru.netology.netologydiplombackend.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.netology.netologydiplombackend.exception.InvalidCredentialsException;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;

@Service
//@ConfigurationProperties("jwt")
public class TokenService {

    private static final Logger log = LoggerFactory.getLogger(TokenService.class);

    private static final String TOKEN_SUBJECT = "userInfo";
    private static final String TOKEN_ISSUER = "cloudStore";
    private static final String TOKEN_CLAIM_LOGIN = "login";

    private final SecretKey secretKey;
    private final JwtParser jwtParser;

    @Value("${jwt.expiration}")
    private Duration expiration;


    public TokenService(@Value("${jwt.secret}") String secret) {
        byte[] secretKeyBytes = secret.getBytes();

        this.secretKey = Keys.hmacShaKeyFor(secretKeyBytes);
        this.jwtParser = Jwts.parser()
                .verifyWith(secretKey)
                .build();
    }

    public String generateToken(String login) {
        log.info("start generateToken: for login: {}", login);

        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration.toMillis());
        log.debug("generateToken: login: {} expirationDate: {}", login, expirationDate);

        return Jwts.builder()
                .issuer(TOKEN_ISSUER)
                .issuedAt(now)
                .subject(TOKEN_SUBJECT)
                .claim(TOKEN_CLAIM_LOGIN, login)
                .expiration(expirationDate)
                .signWith(secretKey)
                .compact();
    }

    public String getLogin(String token) {
        Claims claims = getClaims(token);
        if (claims == null) {
            log.error("getLogin: claims is null for token: {}", token);
            throw new InvalidCredentialsException("Claims is null");
        }
        if (!TOKEN_ISSUER.equals(claims.getIssuer())) {
            log.error("getLogin: incorrect issuer: {} for token: {}", claims.getIssuer(), token);
            throw new InvalidCredentialsException("Incorrect issuer: " + claims.getIssuer());
        }
        if (!TOKEN_SUBJECT.equals(claims.getSubject())) {
            log.error("getLogin: incorrect subject: {} for token: {}", claims.getSubject(), token);
            throw new InvalidCredentialsException("Incorrect subject: " + claims.getSubject());
        }
        if (new Date().after(claims.getExpiration())) {
            log.error("getLogin: is expire at: {} for token: {}", claims.getExpiration(), token);
            throw new InvalidCredentialsException("Token is expire at " + claims.getExpiration());
        }
        Object login = claims.get(TOKEN_CLAIM_LOGIN);
        if (login == null) {
            log.error("getLogin: incorrect claim: {} is null for token: {}", TOKEN_CLAIM_LOGIN, token);
            throw new InvalidCredentialsException("Incorrect claim: " + TOKEN_CLAIM_LOGIN + " is null");
        }
        return login.toString();
    }

    private Claims getClaims(String token) {
        try {
            return jwtParser.parseSignedClaims(token).getPayload();
        }
        catch (Exception e) {
            log.error("getClaims: error while validate token: {} {}", token, e);
            throw new InvalidCredentialsException(e.getMessage());
        }
    }
}
