package io.github.api;
import javax.persistence.Entity;

@Entity
public class AuthorizationCodeWrapper {

    public long id; 
    public String code; 

    public AuthorizationCodeWrapper(long id, String code){
        this.id = id;
        this.code = code; 
    }

}