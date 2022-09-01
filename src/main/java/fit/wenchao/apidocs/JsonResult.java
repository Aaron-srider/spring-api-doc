package fit.wenchao.apidocs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JsonResult {

    private Object data;

    private String code;
    private String msg;

    public static JsonResult of(Object data, String code, String msg) {
        JsonResult jsonResult = new JsonResult();
        jsonResult.data = data;
        jsonResult.code = code;
        jsonResult.msg = msg;
        return jsonResult;
    }
}
