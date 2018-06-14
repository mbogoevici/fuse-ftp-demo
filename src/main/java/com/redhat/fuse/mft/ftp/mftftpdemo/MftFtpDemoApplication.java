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
                from("ftp://{{mft.ftp.incoming.user}}@{{mft.ftp.incoming.host}}/{{mft.ftp.incoming.path}}?password={{mft.ftp.incoming.password}}&idempotent=true")
                        .to("{{mft.local.incoming.path}}")
                        .transform().simple("${headers['CamelFileNameProduced']}")
                        .to("direct:incomingFiles");

                // Rename and upload received files
                from("direct:incomingFiles")
                        .setHeader("CamelFileName").simple("${headers['CamelFileName']}.upload.renamed.${date:now:yyMMddHHmmssZ}")
                        .to("direct:outgoingFiles");

                // Also send files for upload when a file is copied in the 'outbox' folder locally
                from("{{mft.local.outgoing.path}}")
                        .to("direct:outgoingFiles");

                // Route for uploading to FTP
                // The message body contains the path to the file being uploaded
                from("direct:outgoingFiles")
                        .to("ftp://{{mft.ftp.outgoing.user}}@{{mft.ftp.outgoing.host}}/{{mft.ftp.outgoing.path}}?password={{mft.ftp.outgoing.password}}")
                        .log("Sent ${headers['CamelFileName']}");

            }
        };
    }
}
