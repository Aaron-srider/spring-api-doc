package fit.wenchao.apidocs.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * spring properties 类，指定要生成的Api文档的包
 */
@Data
@ConfigurationProperties(prefix = "api-doc")
public class ApiDocProperties {
    private String apiBasePackage;
}
