package org.activiti;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.MultipartAutoConfiguration;
/*import org.springframework.context.annotation.Bean;
@EnableAutoConfiguration(exclude = {UserResource.class})*/
@EnableAutoConfiguration(exclude={MultipartAutoConfiguration.class})
@SpringBootApplication
public class MyApp {

    public static void main(String[] args) {
        SpringApplication.run(MyApp.class, args);
    }

}
