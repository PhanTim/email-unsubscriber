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

@Entity
@Table(name = "unsubscribelinks")
@AllArgsConstructor
public class UnsubscribeLink {

    @Getter private String userEmail;
    @Getter private String url;
    @Getter private String sender;
    @Getter @Setter private boolean isUnsubscribed; 

}