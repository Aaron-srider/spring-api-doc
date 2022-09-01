package fit.wenchao.apidocs.springBootStarter;

import fit.wenchao.apidocs.properties.ApiDocProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {
        "fit.wenchao.apidocs.web"
})
@EnableConfigurationProperties(ApiDocProperties.class)
public class ApiDocAutoConfiguration {

}