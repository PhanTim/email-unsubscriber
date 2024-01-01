package io.github.api;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.OffsetDateTime; 
import lombok.Getter;
import lombok.Setter;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

public class BaseResponse<T> {
    @Getter @Setter private String authToken;
    @Getter @Setter private int statusCode; 
    @Getter @Setter private T data; 

    public BaseResponse(String authToken, int statusCode){
        this.authToken = authToken;
        this.statusCode = statusCode;
    }

    public BaseResponse(String authToken, int statusCode, T data){
        this.authToken = authToken;
        this.statusCode = statusCode;
        this.data = data;
    }

}