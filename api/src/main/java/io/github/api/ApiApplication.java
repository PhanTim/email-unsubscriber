package io.github.api;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.sql.ResultSet;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Autowired;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.springframework.beans.factory.annotation.Value;

@SpringBootApplication
public class ApiApplication implements CommandLineRunner{

	@Autowired
	public static void main(String... args) throws IOException, GeneralSecurityException {
		SpringApplication.run(ApiApplication.class, args);
	}

	@Override 
	public void run(String... args) {
	}
}
