package fit.wenchao.apidocs;


import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ApiInfo {
    Long id;
    String name;
    String method;
    String url;
    String detail;
    List<ApiParameterInfo> params;

    Map<Object, Object> customInfoMap;

    public void addCustomInfos(List<CustomApiInfo> customInfos) {
        if(customInfoMap == null) {
            customInfoMap = new HashMap<>();
        }
        for (CustomApiInfo customInfo : customInfos) {
            customInfoMap.put(customInfo.getInfoSignature(), customInfo.getInfoBody());
        }
    }
}
