package org.activiti.config;

import org.h2.tools.Server;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.sql.DataSource;

@Configuration
public class Config {
    /* @Bean
     public DataSource database() {
         return DataSourceBuilder.create()
                 .url("jdbc:mysql://127.0.0.1:3306/record_process_activiti?characterEncoding=UTF-8")
                 .username("root")
                 .password("123456")
                 .driverClassName("com.mysql.jdbc.Driver")
                 .build();
     }*/
 /*   @Bean
    public DataSource database() {
        return DataSourceBuilder.create()
                .url("jdbc:h2:mem:activiti;DB_CLOSE_DELAY=1000")
                .username("sa")
                .password("")
                .driverClassName("org.h2.Driver")
                .build();
    }*/

    @Bean
    org.h2.tools.Server h2Server() {
        Server server = new Server();
        try {
            server.runTool("-tcp");
            server.runTool("-tcpAllowOthers");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return server;

    }
    @Bean(name = "multipartResolver")
    public MultipartResolver multipartResolver() {
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
        return multipartResolver;
    }

}
