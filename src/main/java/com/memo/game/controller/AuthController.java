package com.memo.game.controller;

import com.memo.game.dto.AuthRequest;
import com.memo.game.entity.MemoUsers;
import com.memo.game.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import org.mindrot.jbcrypt.BCrypt;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class AuthController {
    private final MemoUsersService gameService;
    private final TokenService tokenService;
    private final TokenBlacklistService tokenBlacklistService;
    private final VerificationTokenService verificationTokenService;
    private final EmailService emailService;
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

    public AuthController(MemoUsersService gameService, TokenService tokenService,
                            TokenBlacklistService tokenBlacklistService,
                            VerificationTokenService verificationTokenService,
                            EmailService emailService) {
        this.gameService=gameService;
        this.tokenService=tokenService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.verificationTokenService = verificationTokenService;
        this.emailService = emailService;
    }

    @PostMapping("/api/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody AuthRequest registerRequest) {
        Map<String, Object> responseMap = new HashMap<>();
        boolean existingEmail = false;
        boolean existingUsername = false;
        boolean validEmailFormat = false;
        String email = registerRequest.getEmail();
        String userName = registerRequest.getUsername();

        if(gameService.getByEmail(email)!=null) {
            existingEmail = true;
        }
        if(gameService.getByUserName(userName)!=null) {
            existingUsername = true;
        }
        if(!existingEmail && !existingUsername) {
            Pattern pattern = Pattern.compile(EMAIL_REGEX);
            Matcher matcher = pattern.matcher(email);
            if(matcher.matches()) {
                validEmailFormat = true;

                UUID token = verificationTokenService.saveVerificationToken(email);
                emailService.sendVerificationEmail(email, token);

                String hashedPassword = BCrypt.hashpw(registerRequest.getPassword(), BCrypt.gensalt());
                MemoUsers memoUser = new MemoUsers(userName, email, hashedPassword);
                gameService.saveUser(memoUser);
            }
        }

        responseMap.put("existingUsername", existingUsername);
        responseMap.put("existingEmail", existingEmail);
        responseMap.put("validEmailFormat", validEmailFormat);
        return ResponseEntity.ok(responseMap);
    }

    @PostMapping("/api/signIn")
    public ResponseEntity<?> signIn(@RequestBody AuthRequest signInRequest) {
        Map<String, Object> responseMap = new HashMap<>();
        UUID userId = null;
        MemoUsers user = null;

        if(signInRequest.getUsername()!=null) {
            user = gameService.getByUserName(signInRequest.getUsername());
        }
        if(signInRequest.getEmail()!=null) {
            user = gameService.getByEmail(signInRequest.getEmail());
        }
        if(user!=null) {
            if(BCrypt.checkpw(signInRequest.getPassword(), user.getPassword())) {
                gameService.signIn(user.getId());
                String token = tokenService.generateJwtToken(user);
                responseMap.put("token", token);
                return ResponseEntity.ok(responseMap);
            } else {
                responseMap.put("isPasswordCorrect", false);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseMap);
            }
        } else {
            responseMap.put("isEmailOrUserNameCorrect", false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseMap);
        }
    }

    @PostMapping("/api/signOut")
    public ResponseEntity<Void> signOut(HttpServletRequest request) {
        String token = tokenService.extractTokenFromRequest(request);
        if (!tokenService.isTokenValid(token)) {
            return ResponseEntity.badRequest().build();
        } else {
            gameService.signOut(tokenService.extractUserIdFromToken(token));
            tokenBlacklistService.addToBlacklist(token);
            return ResponseEntity.noContent().build();
        }
    }
}