package fit.wenchao.apidocs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiInfoContext {
    public void resetInfo(){
        this.method = null;
        this.url = null;
        this.httpMethod = null;
        this.apiName = null;
        this.apiDesc = null;
        this.parameterAnnos = new ArrayList<>();
    }
    Method method;
    String url;
    String httpMethod;
    String apiName;
    String apiDesc;
    List<ApiParameter> parameterAnnos;
    Long counter = 0L;
    public Long getId() {
        return counter++;
    }
}
