package com.magicapp.resource;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.magicapp.domain.HttpResponse;
import com.magicapp.domain.User;
import com.magicapp.domain.UserPrincipal;
import com.magicapp.exception.ExceptionHandling;
import com.magicapp.exception.domain.EmailExistException;
import com.magicapp.exception.domain.EmailNotFoundException;
import com.magicapp.exception.domain.UserNotFoundException;
import com.magicapp.exception.domain.UsernameExistException;
import com.magicapp.service.UserService;
import com.magicapp.utility.JWTTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

import static com.magicapp.constant.FileConstant.FORWARD_SLASH;
import static com.magicapp.constant.FileConstant.USER_FOLDER;
import static com.magicapp.constant.SecurityConstant.JWT_TOKEN_HEADER;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;

@RestController
//@CrossOrigin(origins = "*")
@RequestMapping(path = { "/", "/user"})
public class UserResource extends ExceptionHandling {
    private static final String EMAIL_SENT = "New password sent to email: ";
    public static final String USER_DELETED_SUCCESFULLY = "User deleted succesfully";
    private Logger LOGGER = LoggerFactory.getLogger(getClass());
    private AuthenticationManager authenticationManager;
    private UserService userService;
    private JWTTokenProvider jwtTokenProvider;

    @Autowired
    public UserResource(AuthenticationManager authenticationManager, UserService userService, JWTTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody User user) {
        authenticate(user.getUsername(), user.getPassword());
        User loginUser = userService.findUserByUsername(user.getUsername());
        UserPrincipal userPrincipal = new UserPrincipal(loginUser);
        HttpHeaders jwtHeader = getJwtHeader(userPrincipal);
        return new ResponseEntity<>(loginUser, jwtHeader, OK);
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) throws UserNotFoundException, UsernameExistException, EmailExistException, MessagingException {
        User newUser = userService.register(user.getFirstName(), user.getLastName(), user.getUsername(), user.getEmail());
        return new ResponseEntity<>(newUser, OK);
    }

    @PostMapping("/add")
    public ResponseEntity<User> addNewUser(@RequestParam("firstName") String firstName,
                                          @RequestParam("lastName") String lastName,
                                          @RequestParam("username") String username,
                                          @RequestParam("email") String email,
                                          @RequestParam("role") String role,
                                          @RequestParam("isActive") String isActive,
                                          @RequestParam("isNonLocked") String isNonLocked,
                                          @RequestParam(value = "profileImage", required = false) MultipartFile profileImage)
            throws UserNotFoundException, EmailExistException, IOException, UsernameExistException {
        User newUser = userService.addNewUser(firstName, lastName, username,email, role,
                Boolean.parseBoolean(isNonLocked), Boolean.parseBoolean(isActive), profileImage);
        return new ResponseEntity<>(newUser, OK);

    }

    @PreAuthorize("#username == authentication.name")
    @PostMapping("/update")
    public ResponseEntity<User> updateNewUser(@RequestParam("firstName") String firstName,
                                          @RequestParam("currentUsername") String currentUsername,
                                          @RequestParam("lastName") String lastName,
                                          @RequestParam("username") String username,
                                          @RequestParam("email") String email,
                                          @RequestParam("role") String role,
                                          @RequestParam("isActive") String isActive,
                                          @RequestParam("isNonLocked") String isNonLocked,
                                          @RequestParam(value = "profileImage", required = false) MultipartFile profileImage)
            throws UserNotFoundException, EmailExistException, IOException, UsernameExistException {
        User updatedUser = userService.updateUser(currentUsername, firstName, lastName, username,email, role,
                Boolean.parseBoolean(isNonLocked), Boolean.parseBoolean(isActive), profileImage);
        return new ResponseEntity<>(updatedUser, OK);

    }

    @GetMapping("/find/{username}")
    public ResponseEntity<User> getUser(@PathVariable("username") String username){
        User user = userService.findUserByUsername(username);
        return new ResponseEntity<>(user, OK);
    }

    @GetMapping("/list")
    public ResponseEntity<List<User>> getAllUsers(){
        List<User> users = userService.getUsers();
        return new ResponseEntity<>(users, OK);
    }

    @GetMapping("/resetpassword/{email}")
    public ResponseEntity<HttpResponse> resetPassword(@PathVariable("email") String email) throws MessagingException, EmailNotFoundException {
        userService.resetPassword(email);
        return response(OK, EMAIL_SENT + email);
    }

    @DeleteMapping("/delete/{username}")
//    @PreAuthorize("hasAnyAuthority('user:delete')")
    @PreAuthorize("#username == authentication.name")
    public ResponseEntity<HttpResponse> deleteUser(@PathVariable("username") String username) throws IOException {
        userService.deleteUser(username);
        return response(OK, USER_DELETED_SUCCESFULLY);
    }

    @PostMapping("/updateProfileImage")
    public ResponseEntity<User> updateProfilePicture(@RequestParam("username") String username, @RequestParam(value = "profileImage") MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException {
        User user = userService.updateProfilePicture(username, profileImage);
        return new ResponseEntity<>(user, OK);
    }

    @GetMapping(path = "/image/{username}/{fileName}", produces = IMAGE_PNG_VALUE)
    public byte[] getProfilePicture(@PathVariable("username") String username, @PathVariable("fileName") String fileName) throws IOException {
        return Files.readAllBytes(Paths.get(USER_FOLDER + username + FORWARD_SLASH + fileName));
    }

    @GetMapping(path = "/image/profile/{username}", produces = IMAGE_PNG_VALUE)
    public byte[] getTempProfilePicture(@PathVariable("username") String username) throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        URL url = new URL ("http://127.0.0.1:7860/sdapi/v1/txt2img");
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);
        String jsonString = "{" +
                "   \"prompt\": \"magic the gathering, " + username + "\"," +
                "    \"steps\": 15" +
                "}";
        try(OutputStream os = con.getOutputStream()) {
            byte[] input = jsonString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        StringBuilder response = new StringBuilder();
        try(BufferedReader br = new BufferedReader(
                new InputStreamReader(con.getInputStream(), "utf-8"))) {
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }
        JsonObject jsonObject = new JsonParser().parse(response.toString()).getAsJsonObject();

        byte[] decodedImage = Base64.getDecoder().decode(jsonObject.get("images").getAsString());
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        LOGGER.info(jsonString);
        return decodedImage;
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(),
                message), httpStatus);
    }




    private HttpHeaders getJwtHeader(UserPrincipal user) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(JWT_TOKEN_HEADER, jwtTokenProvider.generateJwtToken(user));
        return headers;
    }

    private void authenticate(String username, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }
}
