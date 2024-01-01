package io.github.api;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.OffsetDateTime; 
import java.util.ArrayList;
import lombok.Getter;
import lombok.Setter;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Collection;

public class GetUnsubscribeLinksResponse extends BaseResponse {

    @Getter @Setter private Collection<String> unsubscribeLinkURLs = new ArrayList<String>();
    @Getter @Setter private Collection<String> unsubscribeLinkSenders = new ArrayList<String>();
    @Getter @Setter private Collection<Boolean> isUnsubscribedValues = new ArrayList<Boolean>();

    public GetUnsubscribeLinksResponse(String authToken, int statusCode){
        super(authToken, statusCode);
    }

}