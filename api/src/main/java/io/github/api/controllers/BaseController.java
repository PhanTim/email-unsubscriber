package io.github.api;

import lombok.Data;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.springframework.beans.factory.annotation.Value;
import java.lang.StringBuilder;
import java.time.OffsetDateTime;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter; 

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public abstract class BaseController {

    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserInfoWrapper{
        @Getter private String id;
        @Getter private String email;
        @Getter private String verified_email;
        @Getter private String name;
        @Getter private String given_name;
        @Getter private String family_name;
        @Getter private String picture;
        @Getter private String locale;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    public static class GoogleReauthenticationResponse {
        @Getter private String access_token;
        @Getter private int expires_in;
        @Getter private String scope;
        @Getter private String token_type;
        @Getter private String id_token;
    }
    
    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String user;

    @Value("${spring.datasource.password}")
    private String password; 

    @Value("${google.clientId}")
    private String clientId;

    @Value("${google.clientSecret}")
    private String clientSecret;

    public Connection connect() {
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url, user, password);
			System.out.println("Connected to the PostgreSQL server successfully.");
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

		return conn; 
	}

    public String reauthenticate(HttpClient client, ObjectMapper objectMapper) throws Exception{
        RefreshTokenWrapper refreshToken = getRefreshTokenFor("me");
        if(OffsetDateTime.now().isBefore(refreshToken.getExpires())){
            StringBuffer newTokenBuffer = new StringBuffer();
            newTokenBuffer.append("https://oauth2.googleapis.com/token");
            newTokenBuffer.append("?client_id=");
            newTokenBuffer.append(clientId);
            newTokenBuffer.append("&client_secret=");
            newTokenBuffer.append(clientSecret);
            newTokenBuffer.append("&refresh_token=");
            newTokenBuffer.append(refreshToken.getRefreshToken());
            newTokenBuffer.append("&grant_type=refresh_token&access_type=offline");
            HttpRequest clientRequest = HttpRequest.newBuilder()
                            .uri(URI.create(newTokenBuffer.toString()))
                            .POST(HttpRequest.BodyPublishers.noBody())
                            .build();
            HttpResponse<String> clientResponse = client.send(clientRequest, HttpResponse.BodyHandlers.ofString());
            GoogleReauthenticationResponse response = objectMapper.readValue(clientResponse.body(), GoogleReauthenticationResponse.class);
            return response.getAccess_token();
        }
        return "";
    }

    public BaseResponse<String> getUserEmailFromAccessToken(String accessToken, ObjectMapper objectMapper) throws Exception{
        BaseResponse<String> result = new BaseResponse<String>("", 200, "");
        StringBuffer userInfoBuffer = new StringBuffer();
        userInfoBuffer.append("https://www.googleapis.com/oauth2/v2/userinfo?access_token=");
        userInfoBuffer.append(accessToken);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest clientRequest = HttpRequest.newBuilder()
            .uri(URI.create(userInfoBuffer.toString()))
            .build();
        HttpResponse<String> clientResponse = client.send(clientRequest, HttpResponse.BodyHandlers.ofString());
        if(clientResponse.statusCode() == 401){
            String authToken = reauthenticate(client, objectMapper);
            StringBuffer newAuthTokenBuffer = new StringBuffer();
            newAuthTokenBuffer.append("Bearer ");
            newAuthTokenBuffer.append(authToken);
            result.setAuthToken(authToken);
            StringBuffer newInfoBuffer = new StringBuffer();
            newInfoBuffer.append("https://www.googleapis.com/oauth2/v2/userinfo?access_token=");
            newInfoBuffer.append(authToken);
            clientRequest = HttpRequest.newBuilder()
                .uri(URI.create(newInfoBuffer.toString()))
                .build();
            clientResponse = client.send(clientRequest, HttpResponse.BodyHandlers.ofString());
        }
        UserInfoWrapper userInfoWrapper = objectMapper.readValue(clientResponse.body(), UserInfoWrapper.class);
        result.setData(userInfoWrapper.getEmail());
        return result;
    }


    private RefreshTokenWrapper getRefreshTokenFor(String userEmail) {
        String SQL = "SELECT * FROM tokens WHERE useremail = ?";

        try(Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, userEmail);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                RefreshTokenWrapper refreshToken = new RefreshTokenWrapper(rs.getString("useremail").toString(), rs.getString("refreshtoken").toString(), rs.getObject("created", OffsetDateTime.class), rs.getObject("expires", OffsetDateTime.class));
                return refreshToken;
            }
        }
        catch (SQLException e) {
			System.out.println(e.getMessage());
		}  
        return null;
    }


    private void displayRefreshToken(ResultSet rs) throws SQLException {
        while (rs.next()) {
            System.out.println(rs.getString("useremail")  + "\t" + rs.getString("refreshtoken") + "\t" + rs.getString("created") + "\t" + rs.getString("expires"));
        }
    }

}