package fit.wenchao.apidocs;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Accessors(chain = true)
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

    /**
     * 给API或Module分配的自增id
     */
    Long counter = 0L;

    public Long getId() {
        return counter++;
    }
}
