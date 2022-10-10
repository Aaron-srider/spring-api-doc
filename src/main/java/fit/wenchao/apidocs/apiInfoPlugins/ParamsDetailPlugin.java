package fit.wenchao.apidocs.apiInfoPlugins;

import fit.wenchao.apidocs.*;
import fit.wenchao.apidocs.utils.JsonUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ParamsDetailPlugin implements ApiInfoPlugin {
    @Override
    public CustomApiInfo getInfo(Method method, ApiInfoContext apiInfoContext) {
        List<ApiParameter> parameterAnnos = apiInfoContext.getParameterAnnos();
        Map<String, Object> apiDetailMap = new HashMap<>();
        for (ApiParameter parameterAnno : parameterAnnos) {
            ApiParamType apiParamType = parameterAnno.paramType();
            Object paramDetail;
            if(apiParamType.equals(ApiParamType.BODY)) {
                Class<?> bodyClass = parameterAnno.bodyClass();
                if(!bodyClass.equals(Void.class)) {
                    paramDetail = JsonUtils.getClassFieldDetail(bodyClass);
                    apiDetailMap.put(parameterAnno.name(), paramDetail);
                }
            }

        }

        return new CustomApiInfo() {
            @Override
            public Object getInfoSignature() {
                return "body_detail_map";
            }

            @Override
            public Object getInfoBody() {
                return apiDetailMap;
            }
        };
    }
}
