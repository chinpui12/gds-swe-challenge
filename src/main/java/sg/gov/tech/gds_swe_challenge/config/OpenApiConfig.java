package sg.gov.tech.gds_swe_challenge.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("GDS Restaurant Challenge API")
                        .version("1.0")
                        .description("Restaurant submission and random selection service"))
                .addServersItem(new Server().url(""));
    }
}
