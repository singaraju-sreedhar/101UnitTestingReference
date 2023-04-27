package io.marketplace.services.contact.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@SuppressWarnings("Duplicates")
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        String apiDesc = "service_name description";
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("Bearer",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer").in(SecurityScheme.In.HEADER)))
                .info(new Info().title("ServiceName API")
                        .description(apiDesc))
                ;
    }

    @Configuration
    public class SwaggerUIConfig extends WebMvcConfigurerAdapter {
        @Override
        public void addResourceHandlers(final ResourceHandlerRegistry registry) {

            registry.addResourceHandler("/**")
                    .addResourceLocations(ResourceUtils.CLASSPATH_URL_PREFIX +
                                    "/META-INF/resources/")
                    .resourceChain(false);

            registry.addResourceHandler("/swagger-ui/**")
                    .addResourceLocations(ResourceUtils.CLASSPATH_URL_PREFIX +
                            "/META-INF/resources/webjars/swagger-ui/3.25.0/")
                    .resourceChain(false);

            super.addResourceHandlers(registry);
        }
    }
}