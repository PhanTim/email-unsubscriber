package io.github.api;

import lombok.Getter;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PatchMapping; 
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.List;
import java.util.ArrayList;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.MessagePart;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Base64;
import java.util.HashMap;
import java.util.Collection;
import javax.ws.rs.NotAuthorizedException;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;
import java.lang.InterruptedException;
import org.springframework.http.HttpStatus;
import java.util.stream.Collectors;
import java.time.OffsetDateTime;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// TODO: Hook up PostGresDB

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping(value = "/api/gmail/")
public class GmailController extends BaseController {
    
    // @Autowired
    // private EmailRepository emailRepository;

    //Custom class as Object Mapper attempts to convert Longs into Integers with ListMessagesResponse
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MessageListResponse {
        @Getter private List<Message> messages;
        @Getter private String nextPageToken;
        @Getter private int resultSizeEstimate; 
    }

    @AllArgsConstructor
    @NoArgsConstructor
    public static class HeaderWrapper{
        @Getter private String name;
        @Getter private String value;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    public static class MessagePartBodyWrapper{
        @Getter private String attachmentId;
        @Getter private int size;
        @Getter private String data;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    public static class MessagePartWrapper{
        @Getter private String partId;
        @Getter private String mimeType;
        @Getter private String filename;
        @Getter private HeaderWrapper[] headers;
        @Getter private MessagePartBodyWrapper body;
        @Getter private MessagePartWrapper[] parts;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    public static class MessageWrapper{
        @Getter private String id;
        @Getter private String threadId;
        @Getter private String[] labelIds;
        @Getter private String snippet;
        @Getter private String historyId;
        @Getter private long internalDate;
        @Getter private MessagePartWrapper payload;
        @Getter private int sizeEstimate;
        @Getter private String raw; 
    }

    @PostMapping(produces="application/json")
    public GetUnsubscribeLinksResponse getUnsubscribeLinks(@RequestBody AccessTokenWrapper accessToken) throws Exception{
        ObjectMapper objectMapper = new ObjectMapper();
        BaseResponse<String> emailResponse = getUserEmailFromAccessToken(accessToken.token, objectMapper);
        String userEmail = emailResponse.getData();
        List<UnsubscribeLink> linkList = getUnsubscribeLinksFromDatabase(userEmail);
        List<String> urls = new ArrayList<String>();
        List<String> senders = new ArrayList<String>();
        for (UnsubscribeLink link : linkList){
            urls.add(link.getUrl());
            senders.add(link.getSender());
        }
        GetUnsubscribeLinksResponse response = new GetUnsubscribeLinksResponse("", 200);
        response.setUnsubscribeLinkURLs(urls);
        response.setUnsubscribeLinkSenders(senders);
        if(!emailResponse.getAuthToken().equals("")){
            response.setAuthToken(emailResponse.getAuthToken());
        }
        return response; 
    }

    @PostMapping(value="/refresh", produces="application/json", consumes = MediaType.APPLICATION_JSON_VALUE)
    public GetUnsubscribeLinksResponse refreshUnsubscriptionLinks(@RequestBody AccessTokenWrapper accessToken, HttpServletRequest request, HttpServletResponse response) throws IOException, NotAuthorizedException, InterruptedException, Exception{ //List<Email> GetEmails() {
        long startTime = System.nanoTime();

        //Initialize objects
        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper objectMapper = new ObjectMapper();
        HashMap<String, String> unsubscribeLinksMap = new HashMap<String, String>();
        GetUnsubscribeLinksResponse result = new GetUnsubscribeLinksResponse("", 200);

        //Create Token URL from POST request
        String token = accessToken.token;

        if(accessToken != null){
            //try{
            BaseResponse<List<String>> listResponse = new BaseResponse<List<String>>("", 200, null);
            listResponse = getEmailIds(objectMapper, token, client);
            List<String> messageIdList = listResponse.getData();
            BaseResponse<HashMap<String,String>> mapResponse = new BaseResponse<HashMap<String,String>>("", 200, null);
            mapResponse = getUnsubscribeLinks(token, messageIdList, objectMapper, client);
            unsubscribeLinksMap = mapResponse.getData();
            result.setUnsubscribeLinkURLs(unsubscribeLinksMap.values());
            List<String> unsubscribeLinkSenders = unsubscribeLinksMap.keySet().stream().collect(Collectors.toList());
            result.setUnsubscribeLinkSenders(unsubscribeLinkSenders);
            //}
            // catch(NotAuthorizedException e){
            //     throw new NotAuthorizedException(e);
            // }
            // catch(IOException e){
            //     throw new IOException(e);
            // }
            // catch(InterruptedException e){
            //     throw new InterruptedException(e.getMessage());
            // }
            //TODO: Make batch request option for inboxes smaller than a certain size 
        }
        long endTime = System.nanoTime();
        System.out.println((endTime - startTime)/1000000);
        return result;
    }


//    @GetMapping("")
//    public void GetEmail() {
//
//    }

    @PostMapping(value="/mark", produces="application/json", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void MarkLinksUnsubscribed(@RequestBody String[] linkURLs) {
        for(int i = 0; i< linkURLs.length(); i++){
            updateUnsubscribeLinkByURL(linkURLs[i]);
        }
    }

    private BaseResponse<List<String>> getEmailIds(ObjectMapper objectMapper, String token, HttpClient client) throws ResponseStatusException, IOException, NotAuthorizedException, InterruptedException, Exception{
        //try{
            StringBuffer tokenBuffer = new StringBuffer();
            tokenBuffer.append("Bearer ");
            tokenBuffer.append(token);
            List<String> messageIdList = new ArrayList<String>();
            BaseResponse<List<String>> result = new BaseResponse<List<String>>("", 200, messageIdList);
            StringBuffer urlBuffer = new StringBuffer();
            urlBuffer.append("https://gmail.googleapis.com/gmail/v1/users/me/messages");
            HttpRequest clientRequest = HttpRequest.newBuilder()
                .uri(URI.create(urlBuffer.toString()))
                .setHeader("Authorization", tokenBuffer.toString())
                .build();
            String nextPageToken = "";
            while(nextPageToken != null){
                HttpResponse<String> clientResponse = client.send(clientRequest, HttpResponse.BodyHandlers.ofString());
                if(clientResponse.statusCode() == 401){
                    String authToken = reauthenticate(client, objectMapper);
                    StringBuffer newAuthTokenBuffer = new StringBuffer();
                    newAuthTokenBuffer.append("Bearer ");
                    newAuthTokenBuffer.append(authToken);
                    tokenBuffer = newAuthTokenBuffer;
                    result.setAuthToken(authToken);
                    StringBuffer currentTokenBuffer = new StringBuffer();
                    currentTokenBuffer.append(urlBuffer.toString());
                    currentTokenBuffer.append("?pageToken=");
                    currentTokenBuffer.append(nextPageToken);
                    clientRequest = HttpRequest.newBuilder()
                        .uri(URI.create(currentTokenBuffer.toString()))
                        .setHeader("Authorization", tokenBuffer.toString())
                        .build();
                    clientResponse = client.send(clientRequest, HttpResponse.BodyHandlers.ofString());
                }
                else if((clientResponse.statusCode() < 200 || clientResponse.statusCode() > 299) && clientResponse.statusCode() != 401){
                    throw new ResponseStatusException(HttpStatus.valueOf(clientResponse.statusCode()));
                }
                MessageListResponse messageResponse = objectMapper.readValue(clientResponse.body(), MessageListResponse.class);
                nextPageToken = messageResponse.getNextPageToken();
                List<Message> messages = messageResponse.getMessages();
                for(Message message : messages){
                    messageIdList.add(message.getId());
                }
                StringBuffer nextTokenBuffer = new StringBuffer();
                nextTokenBuffer.append(urlBuffer.toString());
                nextTokenBuffer.append("?pageToken=");
                nextTokenBuffer.append(nextPageToken);
                clientRequest = HttpRequest.newBuilder()
                    .uri(URI.create(nextTokenBuffer.toString()))
                    .setHeader("Authorization", tokenBuffer.toString())
                    .build();

            }
            return result;
        //}
        // catch(NotAuthorizedException e){
        //     throw new NotAuthorizedException(e);
        // }
        // catch(IOException e){
        //     throw new IOException(e);
        // }
        // catch(InterruptedException e){
        //     throw new InterruptedException(e.getMessage());
        // }
    }

    private BaseResponse<HashMap<String, String>> getUnsubscribeLinks(String token, List<String> messageIdList, ObjectMapper objectMapper, HttpClient client) throws IOException, NotAuthorizedException, InterruptedException, Exception{
        HashMap<String, String> unsubscribeLinksMap = new HashMap<String, String>();
        HashMap<String, Long> linkTimeMap = new HashMap<String, Long>();
        BaseResponse<HashMap<String,String>> result = new BaseResponse<HashMap<String,String>>("", 200, unsubscribeLinksMap);
        BaseResponse<String> emailResponse = getUserEmailFromAccessToken(token, objectMapper);
        String userEmail = emailResponse.getData();
        if(!emailResponse.getAuthToken().equals("")){
            result.setAuthToken(emailResponse.getAuthToken());
        }

        StringBuffer tokenBuffer = new StringBuffer();
        tokenBuffer.append("Bearer ");
        tokenBuffer.append(token);
        String tokenURL = tokenBuffer.toString();
        
        try{
            for (int i = 0; i < messageIdList.size(); i++){
                StringBuffer messageBuffer = new StringBuffer();
                messageBuffer.append("https://gmail.googleapis.com/gmail/v1/users/me/messages/");
                messageBuffer.append(messageIdList.get(i));
                messageBuffer.append("?format=full");
                HttpRequest clientRequest = HttpRequest.newBuilder()
                    .uri(URI.create(messageBuffer.toString()))
                    .setHeader("Authorization", tokenURL)
                    .build();
                HttpResponse<String> clientResponse = client.send(clientRequest, HttpResponse.BodyHandlers.ofString());
                if(clientResponse.statusCode() == 401){
                    String authToken = reauthenticate(client, objectMapper);
                    StringBuffer newAuthTokenBuffer = new StringBuffer();
                    newAuthTokenBuffer.append("Bearer ");
                    newAuthTokenBuffer.append(authToken);
                    tokenURL = newAuthTokenBuffer.toString();
                    result.setAuthToken(authToken);
                    clientRequest = HttpRequest.newBuilder()
                        .uri(URI.create(messageBuffer.toString()))
                        .setHeader("Authorization", tokenURL)
                        .build();
                    clientResponse = client.send(clientRequest, HttpResponse.BodyHandlers.ofString());
                }
                else if((clientResponse.statusCode() < 200 || clientResponse.statusCode() > 299) && clientResponse.statusCode() != 401){
                    throw new ResponseStatusException(HttpStatus.valueOf(clientResponse.statusCode()));
                }
                MessageWrapper messageWrapper = objectMapper.readValue(clientResponse.body(), MessageWrapper.class);
                HeaderWrapper[] headers = messageWrapper.getPayload().getHeaders();
                String currentSender = null;
                String currentLink = "";
                for (HeaderWrapper header : headers){
                    if(header.getName().equals("From") || header.getName().equals("from")){
                        if(!header.getValue().equals(""))
                            {
                                String[] splitSenderStringArray = header.getValue().split(" ");
                                if(splitSenderStringArray.length == 1)
                                {
                                    currentSender = splitSenderStringArray[0];
                                    if(currentSender.substring(0,1).equals("<")){
                                        currentSender = currentSender.substring(1,currentSender.length()-1);
                                    }
                                }
                                else{
                                    for(String sender : splitSenderStringArray){
                                        if(sender.length() > 1){
                                            if(sender.substring(0,1).equals("<")){
                                                currentSender = sender.substring(1,sender.length()-1);
                                            }
                                        }
                                    }
                                }
                                if(!unsubscribeLinksMap.containsKey(currentSender))
                                {
                                    linkTimeMap.put(currentSender, messageWrapper.getInternalDate()); 
                                }
                                else{
                                    long emailInternalDate = messageWrapper.getInternalDate();
                                    if(emailInternalDate > linkTimeMap.get(currentSender)){
                                        linkTimeMap.put(currentSender, messageWrapper.getInternalDate()); 
                                    }
                                    else{
                                        currentSender = null;
                                    }
                                }
                        }
                    }
                    if(header.getName().equals("List-Unsubscribe")){
                        currentLink = header.getValue();
                        if(!currentLink.equals("")){
                            String[] currentLinkSplitArray = currentLink.split(", ");
                            if(currentLinkSplitArray[0].substring(0,1).equals("<")){
                                currentLink = currentLinkSplitArray[0].substring(1,currentLinkSplitArray[0].length()-1);
                            }
                            else{
                                currentLink = currentLinkSplitArray[0];
                            }
                            if(currentLink.length() > 1){
                                if(currentLink.substring(currentLink.length()-1).equals(">")){
                                    currentLink = currentLink.substring(0, currentLink.length()-2);
                                }
                            }
                        }
                    }
                }
                if(currentSender != null){
                    unsubscribeLinksMap.put(currentSender, currentLink);
                }
                currentSender = null;
                currentLink = "";
            }
        }
        catch(NotAuthorizedException e){
            throw new NotAuthorizedException(e);
        }
        catch(IOException e){
            throw new IOException(e);
        }
        catch(InterruptedException e){
            throw new InterruptedException(e.getMessage());
        }
        if(!unsubscribeLinksMap.isEmpty()){
            for(HashMap.Entry<String, String> entry : unsubscribeLinksMap.entrySet()){
                UnsubscribeLink link = new UnsubscribeLink(userEmail, entry.getValue(), entry.getKey(), false);
                insertUnsubscribeLink(link);
            }
        }
        return result;
    }

    //TODO: Utilize JPA ORM instead of SQL to obscure SQL commands

    private void insertUnsubscribeLink(UnsubscribeLink unsubscribeLink){
        String SQL = "INSERT INTO unsubscribelinks(useremail, url, sender, isunsubscribed) " 
        + "VALUES(?,?,?,?)";

        try (
            Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(SQL))
        {

            if(getUnsubscribeLinkFromDatabase(unsubscribeLink.getUserEmail(), unsubscribeLink.getSender()) == null){
                pstmt.setString(1, unsubscribeLink.getUserEmail());
                pstmt.setString(2, unsubscribeLink.getUrl());
                pstmt.setObject(3, unsubscribeLink.getSender());
                pstmt.setObject(4, unsubscribeLink.isUnsubscribed());

                int affectedRows = pstmt.executeUpdate();

                if(affectedRows > 0){
                    System.out.println("Inserted unsubscribe link.");
                }
            }
        }
        catch (SQLException e) {
			System.out.println(e.getMessage());
		}
    }

    private UnsubscribeLink getUnsubscribeLinkFromDatabase(String userEmail, String sender){
        String SQL = "SELECT * FROM unsubscribelinks WHERE useremail = ? AND sender = ?";

        try(Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, userEmail);
            pstmt.setString(2, sender);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                UnsubscribeLink unsubscribeLink = new UnsubscribeLink(rs.getString("useremail").toString(), rs.getString("url").toString(), rs.getString("sender"), rs.getBoolean("isunsubscribed"));
                return unsubscribeLink;
            }
        }
        catch (SQLException e) {
			System.out.println(e.getMessage());
		}
        return null;
    }

    private List<UnsubscribeLink> getUnsubscribeLinksFromDatabase(String userEmail){
        List<UnsubscribeLink> result = new ArrayList<UnsubscribeLink>();
        String SQL = "SELECT * FROM  unsubscribelinks WHERE useremail = ?";

        try(Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, userEmail);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                UnsubscribeLink unsubscribeLink = new UnsubscribeLink(rs.getString("useremail").toString(), rs.getString("url").toString(), rs.getString("sender"), rs.getBoolean("isunsubscribed"));
                result.add(unsubscribeLink);
            }
            return result; 
        }
        catch (SQLException e) {
			System.out.println(e.getMessage());
		}  
        return null;
    }

    private void updateUnsubscribeLinkFromDatabase(String linkURL){
        String SQL = "SELECT * FROM  unsubscribelinks WHERE useremail = ?";

        try(Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, userEmail);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                UnsubscribeLink unsubscribeLink = new UnsubscribeLink(rs.getString("useremail").toString(), rs.getString("url").toString(), rs.getString("sender"), rs.getBoolean("isunsubscribed"));
                result.add(unsubscribeLink);
            }
            return result; 
        }
        catch (SQLException e) {
			System.out.println(e.getMessage());
		}  
    }
}