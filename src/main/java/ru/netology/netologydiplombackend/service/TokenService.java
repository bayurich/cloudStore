package ru.netology.netologydiplombackend.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecureDigestAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.netology.netologydiplombackend.exception.InvalidCredentialsException;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;

@Service
//@ConfigurationProperties("jwt")
public class TokenService {

    private final static String TOKEN_SUBJECT = "userInfo";
    private final static String TOKEN_ISSUER = "cloudStore";
    private final static String TOKEN_CLAIM_LOGIN = "login";

    private SecretKey secretKey;
    private JwtParser jwtParser;

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

        //System.out.println("=================== secret: " + secret);
        System.out.println("=================== secretKey: " + secretKey);
        Date now = new Date();
        System.out.println("=================== now: " + now);
        System.out.println("=================== expiration: " + expiration);
        Date expirationDate = new Date(now.getTime() + expiration.toMillis());
        //Date expirationDate = now;
        System.out.println("=================== expirationDate: " + expirationDate);

        //Claims claims = Jwts.claims().build().;
        //claims.cput(TOKEN_CLAIM_LOGIN, login);

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
            throw new InvalidCredentialsException("Claims is null");
        }
        if (!TOKEN_ISSUER.equals(claims.getIssuer())) {
            throw new InvalidCredentialsException("Incorrect issuer: " + claims.getIssuer());
        }
        if (!TOKEN_SUBJECT.equals(claims.getSubject())) {
            throw new InvalidCredentialsException("Incorrect subject: " + claims.getSubject());
        }
        if (new Date().after(claims.getExpiration())) {
            throw new InvalidCredentialsException("Token is expire at " + claims.getExpiration());
        }
        Object login = claims.get(TOKEN_CLAIM_LOGIN);
        if (login == null) {
            throw new InvalidCredentialsException("Incorrect claim: " + TOKEN_CLAIM_LOGIN + " is null");
        }
        return login.toString();
    }

    private Claims getClaims(String token) {
        try {
            return jwtParser.parseSignedClaims(token).getPayload();
        }
        catch (Exception e) {
            System.out.println("validateToken: error: " + e);
            throw new InvalidCredentialsException(e.getMessage());
        }
    }
}
