package com.nikitatomilov.yaprofiiotbackends

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

@Configuration
@EnableSwagger2
class BackendConfig {
  //  @Autowired
  //  private ApplicationArguments applicationArguments;
  //
  //  @Bean
  //  public UDPGateway udpCommunication() {
  //    try {
  //      String gwIp;
  //      if (applicationArguments.getSourceArgs().length != 0) {
  //        gwIp = applicationArguments.getSourceArgs()[0];
  //      } else {
  //        gwIp = "192.168.0.101";
  //      }
  //      return new UDPGateway(InetAddress.getByName(gwIp), 1337);
  //    } catch (Exception ex) {
  //      ex.printStackTrace();
  //      System.exit(-1);
  //      return null;
  //    }
  //  }
  //
  //  @Bean
  //  public MessagesGateway messageGateway(UDPGateway udpGateway) {
  //    return new MessagesGateway(udpGateway);
  //  }
  @Bean
  fun api(): Docket {
    return Docket(DocumentationType.SWAGGER_2)
        .select()
        .apis(RequestHandlerSelectors.any())
        .paths(PathSelectors.any())
        .build()
  }
}