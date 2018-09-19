package me.light;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import java.math.BigInteger;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class BlockchainJavaApplication {

	public static void main(String[] args) {
		SpringApplication.run(BlockchainJavaApplication.class, args);
	}
}
