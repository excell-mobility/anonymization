package anonymization;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.ResponseEntity;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.Contact;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.service.VendorExtension;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.paths.RelativePathProvider;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.ApiKeyVehicle;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import anonymization.component.StopAnonymizer;
import anonymization.controller.AnonymizationController;

@SpringBootApplication
@EnableSwagger2
@ComponentScan(basePackageClasses = {
		AnonymizationController.class,
		StopAnonymizer.class
	})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    @Bean
    public Docket geocodingApi(ServletContext servletContext) { 
        return new Docket(DocumentationType.SWAGGER_2)
          .groupName("excell-anonymization-api")
          .select()
          .apis(RequestHandlerSelectors.any()) 
          .paths(PathSelectors.regex("/v1/anonymization"))
          .build()
          .genericModelSubstitutes(ResponseEntity.class)
          .protocols(Sets.newHashSet("https"))
//          .host("localhost:45555")
//          .host("141.64.5.234/excell-anonymization-api")
          .host("dlr-integration.minglabs.com")
          .securitySchemes(Lists.newArrayList(apiKey()))
          .securityContexts(Lists.newArrayList(securityContext()))
          .apiInfo(apiInfo())
          .pathProvider(new RelativePathProvider(servletContext) {
              @Override
              public String getApplicationBasePath() {
                  return "/api/v1/service-request/anonymizationservice";
              }
          });
    }
    
	private ApiKey apiKey() {
		return new ApiKey("api_key", "Authorization", "header");
	}
	
    private SecurityContext securityContext() {
        return SecurityContext.builder()
            .securityReferences(defaultAuth())
            .forPaths(PathSelectors.regex("/*.*"))
            .build();
    }

    private List<SecurityReference> defaultAuth() {
    	List<SecurityReference> ls = new ArrayList<>();
    	AuthorizationScope[] authorizationScopes = new AuthorizationScope[0];
    	SecurityReference s = new SecurityReference("api_key", authorizationScopes);
    	ls.add(s);
    	return ls;
    }

	@Bean
	public SecurityConfiguration security() {
		return new SecurityConfiguration(null, null, null, null, "Token", ApiKeyVehicle.HEADER, "Authorization", ",");
	}
    
    private ApiInfo apiInfo() {
        ApiInfo apiInfo = new ApiInfo(
          "ExCELL Track Anonymization API",
          "Diese API ist dafür gedacht datenschutzrechtlich kritische GPS Daten zu anonymisieren. "
          + "Sie wird u.a. intern im Tracking Service verwendet. Der Dienst anonymisiert die Start- und Endpunkte sowie Zwischenhaltestellen der GPS Tracks, "
          + "um keine Rückschlüsse auf persönliche Informationen wie Adressen zu ermöglichen.\n\n"
          + "This API takes individual GPS tracks and removes properties which are critical in terms of user privacy. "
          + "It is also used internally by the ExCELL Tracking Service. "
          + "Start and end points as well as intermediate stops are edited to avoid tracing back personal information such as addresses.\n",
          "Version 1.0",
          "Use only for testing",
          new Contact(
        		  "Beuth Hochschule für Technik Berlin - Labor für Rechner- und Informationssysteme - MAGDa Gruppe",
        		  "https://projekt.beuth-hochschule.de/magda/poeple",
        		  "spieper@beuth-hochschule"),
          "Apache 2",
          "http://www.apache.org/licenses/LICENSE-2.0",
          new ArrayList<VendorExtension>());
        return apiInfo;
    }
   
}