package com.memo.game.controller;

import com.memo.game.dto.AuthRequest;
import com.memo.game.entity.MemoUser;
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

/**
 * REST controller for handling user authentication and authorization.
 * Manages HTTP requests related to user authentication, such as user sign-in, registration,
 * token generation, and user information retrieval.
 * Facilitates secure access to application resources and user account management.
 */
@RestController
public class AuthController {
    private final UserService gameService;
    private final TokenService tokenService;
    private final TokenBlacklistService tokenBlacklistService;
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

    public AuthController(UserService gameService, TokenService tokenService,
                          TokenBlacklistService tokenBlacklistService) {
        this.gameService=gameService;
        this.tokenService=tokenService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    /**
     * Registers a new user.
     *
     * This method handles a request to register a new user with the provided email, username, and password.
     * It validates the request data, ensuring that all required fields are provided and meet certain criteria.
     * If the request data is valid, it checks if the email and username are unique. If not, it returns an error message.
     * If the data is unique and meets the required criteria, it hashes the password, creates a new user object,
     * and saves it to the database.
     *
     * @param registerRequest the request body containing the email, username, and password of the user to register
     * @return a ResponseEntity indicating the outcome of the registration attempt, with an OK status if successful
     *         or a bad request status with an error message if the request data is incorrect or the email/username already exists
     */
    @PostMapping("/api/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest registerRequest) {
        Map<String, Object> responseMap = new HashMap<>();
        String email = registerRequest.getEmail();
        String userName = registerRequest.getUsername();
        String password = registerRequest.getPassword();

        if(email==null || userName==null || email.isEmpty() || userName.isEmpty()
            || password==null || password.isEmpty()) {
            return ResponseEntity.badRequest().body("Incorrect request data!");
        }
        if(gameService.getByEmail(email)!=null) {
            return ResponseEntity.badRequest().body("Existing email!");
        }
        if(gameService.getByUserName(userName)!=null) {
            return ResponseEntity.badRequest().body("Existing username!");
        }
        if(userName.length()<4 || userName.matches(".*\\s.*")) {
            return ResponseEntity.badRequest().body("Username must be at least 4 characters long with no whitespaces!");
        }
        if(password.length()<6 || password.matches(".*\\s.*")) {
            return ResponseEntity.badRequest().body("Password must be at least 6 characters long with no whitespaces!");
        }

        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        Matcher matcher = pattern.matcher(email);
        if(matcher.matches()) {
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            MemoUser memoUser = new MemoUser(userName, email, hashedPassword);
            gameService.saveUser(memoUser);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().body("Incorrect email format!");
        }
    }

    /**
     * Authenticates a user and generates a JWT token for authorization.
     *
     * This method handles a request to authenticate a user using their email or username and password.
     * It validates the request data, ensuring that either the email or username and the password are provided.
     * If the request data is valid, it checks if the user exists based on the provided email or username.
     * If the user exists, it verifies the password using bcrypt hashing.
     * If the password is correct, it generates a JWT token for the user and returns it.
     *
     * @param signInRequest the request body containing the email or username and password of the user to authenticate
     * @return a ResponseEntity containing a JWT token if authentication is successful,
     *         or an unauthorized status with an error message if the request data is incorrect or authentication fails
     */
    @PostMapping("/api/signIn")
    public ResponseEntity<?> signIn(@RequestBody AuthRequest signInRequest) {
        UUID userId = null;
        MemoUser user = null;

        if((signInRequest.getEmail()==null && signInRequest.getUsername()==null) ||
            signInRequest.getPassword()==null) {
            return ResponseEntity.badRequest().body("Incorrect request data!");
        }

        if(signInRequest.getUsername()!=null) {
            user = gameService.getByUserName(signInRequest.getUsername());
        }
        if(signInRequest.getEmail()!=null) {
            user = gameService.getByEmail(signInRequest.getEmail());
        }
        if(user!=null) {
            if(BCrypt.checkpw(signInRequest.getPassword(), user.getPassword())) {
                String token = tokenService.generateJwtToken(user);
                return ResponseEntity.ok(token);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Password is incorrect!");
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email or username is incorrect!");
        }
    }

    /**
     * Retrieves user information based on the provided JWT token.
     *
     * This method handles a request to fetch user information such as user ID and username based on the provided JWT token.
     * It extracts the token from the request, validates it, and then extracts the user ID from the token.
     * If the token is valid and the user ID is successfully extracted, it retrieves the username associated with the user ID.
     * It then constructs a response containing the user ID and username and returns it.
     *
     * @param request the HTTP request containing the JWT token
     * @return a ResponseEntity containing the user information (user ID and username) if the token is valid,
     *         or an unauthorized status with an error message if the token is invalid or the user ID cannot be extracted
     */
    @PostMapping("api/getUserInfo")
    public ResponseEntity<?> getUserInfo(HttpServletRequest request) {
        String token = tokenService.extractTokenFromRequest(request);
        if (!tokenService.isTokenValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UUID userId = tokenService.extractUserIdFromToken(token);
        if(userId==null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found!");
        }
        String userName = gameService.getUserNameById(userId);

        HashMap<String, Object> responseMap = new HashMap<>();
        responseMap.put("userId", userId);
        responseMap.put("userName", userName);
        return ResponseEntity.ok(responseMap);
    }
}