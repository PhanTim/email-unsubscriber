package io.github.api;
import javax.persistence.Entity;

@Entity
public class AccessTokenWrapper {

    public long id; 
    public String token; 

    public AccessTokenWrapper(long id, String token){
        this.id = id;
        this.token = token; 
    }

}