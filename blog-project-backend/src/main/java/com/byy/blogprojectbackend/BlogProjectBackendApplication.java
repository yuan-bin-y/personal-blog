package com.byy.blogprojectbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
//byybyy
/** BinSpace 后端应用入口。 */
@SpringBootApplication
@EnableScheduling
public class BlogProjectBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlogProjectBackendApplication.class, args);
    }

}
