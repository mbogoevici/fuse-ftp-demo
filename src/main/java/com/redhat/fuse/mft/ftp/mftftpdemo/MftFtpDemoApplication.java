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
                // monitor the FTP server and copy files downstream
                // emit a message when a file has been received
                from("ftp://{{mft.ftp.user}}@{{mft.ftp.host}}/{{mft.ftp.inputPath}}?password={{mft.ftp.password}}&idempotent=true")
                        .to("{{mft.localPath}}/inbox")
                        .transform().simple("${headers['CamelFileNameProduced']}")
                        .to("direct:incomingFiles");

                // Rename and upload received files
                from("direct:incomingFiles")
                        .setHeader("CamelFileName").simple("${headers['CamelFileName']}.upload.renamed.${date:now:yyMMddHHmmssZ}")
                        .to("direct:outgoingFiles");

                // Also send files for upload when a file is copied in the 'outbox' folder locally
                from("{{mft.localPath}}/outbox")
                        .to("direct:outgoingFiles");

                // Route for uploading to FTP
                // The message body contains the path to the file being uploaded
                from("direct:outgoingFiles")
                        .to("ftp://{{mft.ftp.user}}@{{mft.ftp.host}}/{{mft.ftp.outputPath}}?password={{mft.ftp.password}}")
                        .log("Sent ${headers['CamelFileName']}");

            }
        };
    }
}
