package anonymization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.ResponseEntity;

import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import anonymization.component.AnonymizationService;
import anonymization.controller.AnonymizationController;

@SpringBootApplication
@EnableSwagger2
@ComponentScan(basePackageClasses = {
		AnonymizationController.class,
		AnonymizationService.class
	})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    @Bean
    public Docket geocodingApi() { 
        return new Docket(DocumentationType.SWAGGER_2)
          .groupName("excell-anonymization-api")
          .select()
          	//.apis(RequestHandlerSelectors.any()) 
          	//.paths(PathSelectors.any())
          .build()
          .genericModelSubstitutes(ResponseEntity.class)
          //.protocols(Sets.newHashSet("https"))
//          .host("localhost:45555")
          .host("141.64.5.234/excell-anonymization-api")
          .apiInfo(apiInfo())
          ;
    }
    
    private ApiInfo apiInfo() {
        ApiInfo apiInfo = new ApiInfo(
          "ExCELL Track Anonymization API",
          "This API provides anonymization for the ExCELL open data platform.",
          "Version 1.0",
          "Use only for testing",
          "spieper@beuth-hochschule",
          "Apache 2",
          "http://www.apache.org/licenses/LICENSE-2.0");
        return apiInfo;
    }
}