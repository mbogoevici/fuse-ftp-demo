package com.redhat.fuse.mft.ftp.mftftpdemo;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.file.remote.FtpComponent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MftFtpDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(MftFtpDemoApplication.class, args);
    }

    @Bean
    RouteBuilder routeBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("ftp://{{mft.ftp.user}}@{{mft.ftp.host}}/{{mft.ftp.inputPath}}?password={{mft.ftp.password}}&idempotent=true")
                        .to("{{mft.localPath}}/inbox").log("${headers['CamelFileNameProduced']}");
                from("{{mft.localPath}}/outbox")
                        .to("ftp://{{mft.ftp.user}}@{{mft.ftp.host}}/{{mft.ftp.outputPath}}?password={{mft.ftp.password}}");

            }
        };
    }
}
