package io.github.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.util.MultiValueMap;
import org.springframework.http.MediaType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.StringBuffer;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.auth.oauth2.AuthorizationCodeResponseUrl;
import com.google.api.client.auth.oauth2.AuthorizationCodeTokenRequest;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import org.springframework.http.HttpCookie; 
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.OffsetDateTime;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.net.URI;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/")
public class LoginController extends BaseController{

    @Value("${google.clientId}")
    private String clientId;

    @Value("${google.clientSecret}")
    private String clientSecret;

    @PostMapping(value = "/auth", consumes = MediaType.APPLICATION_JSON_VALUE)
    public HttpCookie authenticate(@RequestBody AuthorizationCodeWrapper authorizationCode, HttpServletRequest request, HttpServletResponse response) throws Exception{
        StringBuffer urlBuffer = request.getRequestURL();
        if(authorizationCode != null){
            urlBuffer.append("?code=").append(authorizationCode.code);
        }
        AuthorizationCodeResponseUrl authResponse = new AuthorizationCodeResponseUrl(urlBuffer.toString());
        String authCode = authResponse.getCode();
        try {
            TokenResponse tokenResponse = new AuthorizationCodeTokenRequest(new NetHttpTransport(), new GsonFactory(), new GenericUrl("https://oauth2.googleapis.com/token"), authCode).setRedirectUri("http://localhost:3000").setCode(authCode).setClientAuthentication(new ClientParametersAuthentication(clientId,clientSecret)).execute();

            OffsetDateTime currentTime = OffsetDateTime.now();
            OffsetDateTime expirationTime = currentTime.plusDays(1);

            RefreshTokenWrapper refreshToken = new RefreshTokenWrapper("me", tokenResponse.getRefreshToken(), currentTime, expirationTime);
            insertRefreshToken(refreshToken);
            return new HttpCookie("token", tokenResponse.getAccessToken());
        }
        catch(Exception e){
            throw new Exception(e);
        }
    }

    //TODO: Error handling
    @PostMapping(value ="/revoke")  
    public void revoke(@RequestBody AccessTokenWrapper accessToken, HttpServletRequest request, HttpServletResponse response) throws Exception{
        String token = accessToken.token;
        if(token != null){
            deleteRefreshToken();
            StringBuffer urlBuffer = new StringBuffer();
            urlBuffer.append("https://oauth2.googleapis.com/revoke?token=");
            urlBuffer.append(token);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest clientRequest = HttpRequest.newBuilder()
                .uri(URI.create(urlBuffer.toString()))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
            HttpResponse<String> clientResponse = client.send(clientRequest, HttpResponse.BodyHandlers.ofString());
        }
    }

    private void insertRefreshToken(RefreshTokenWrapper refreshToken){
        String SQL = "INSERT INTO tokens(useremail, refreshtoken, created, expires) " 
        + "VALUES(?,?,?,?)";

        try (
            Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(SQL))
        {
            pstmt.setString(1, refreshToken.getUserEmail());
            pstmt.setString(2, refreshToken.getRefreshToken());
            pstmt.setObject(3, refreshToken.getCreated());
            pstmt.setObject(4, refreshToken.getExpires());

            int affectedRows = pstmt.executeUpdate();
            if(affectedRows > 0){
                System.out.println("Inserted refresh token.");
            }
        }
        catch (SQLException e) {
			System.out.println(e.getMessage());
		}
    }

    private void deleteRefreshToken() {
        String SQL = "DELETE FROM tokens WHERE useremail = ?";

        int affectedRows = 0;
        try(Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, "me");

            affectedRows = pstmt.executeUpdate();
            if(affectedRows > 0){
                System.out.println("Deleted refresh token.");
            }
        }
        catch (SQLException e) {
			System.out.println(e.getMessage());
		}
    }


}