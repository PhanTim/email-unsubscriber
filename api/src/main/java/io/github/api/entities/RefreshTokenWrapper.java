package io.github.api;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.OffsetDateTime; 
import lombok.Getter;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "tokens")
public class RefreshTokenWrapper {
    @Getter private String userEmail;
    @Getter private String refreshToken;
    @Getter private OffsetDateTime created;
    @Getter private OffsetDateTime expires; 

    public RefreshTokenWrapper(String userEmail, String refreshToken, OffsetDateTime created, OffsetDateTime expires){
        this.userEmail = userEmail;
        this.refreshToken = refreshToken;
        this.created = created; 
        this.expires = expires;  
    }

}