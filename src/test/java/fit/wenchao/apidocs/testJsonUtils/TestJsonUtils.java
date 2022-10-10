package fit.wenchao.apidocs.testJsonUtils;

import com.fasterxml.jackson.annotation.JsonProperty;
import fit.wenchao.apidocs.ApiModelMeta;
import fit.wenchao.apidocs.utils.GenericTypeWrapper;
import fit.wenchao.apidocs.utils.JsonUtils;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static fit.wenchao.apidocs.utils.JsonUtils.getBasicTypeValue;
import static fit.wenchao.apidocs.utils.JsonUtils.getJsonExample;
import static fit.wenchao.apidocs.utils.ReflectUtils.getGenericTypeWrapper;

/**
 * for test
 */
@ApiModelMeta(detail = "用户信息")
class User {
    //List rowList;
    //@ApiModelMeta(detail = "姓名")
    //String name;
    //@ApiModelMeta(detail = "年龄")
    //Integer age;
    @ApiModelMeta(detail = "额外信息")
    List<Map<String, Address>> infoMapList;
    Map<String, Address> map;
}

/**
 * for test
 */
class Address {
    @ApiModelMeta(detail = "住址详情")
    String detail;

    @ApiModelMeta(detail = "住户")
    User user;
}

public class TestJsonUtils {

    @Test
    public void testGetGenericTypeWrapper() throws NoSuchFieldException {
        Field infoMapList = User.class.getDeclaredField("infoMapList");
        Type genericType = infoMapList.getGenericType();
        GenericTypeWrapper genericTypeWrapper = getGenericTypeWrapper(genericType);
        System.out.println(genericTypeWrapper.getActualClass().equals(List.class));
        System.out.println(genericTypeWrapper.getTypeParams());
        System.out.println(genericTypeWrapper.isHasTypeParams());
    }

    @Test
    public void testGetJsonExample() {
        Object jsonExample = getJsonExample(User.class);
        System.out.println(jsonExample);
    }

    @Test
    public void testWalkThroughClassAttrs() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        Function<Field, Object> getKey = (targetField) -> {
            JsonProperty jsonPropertyAnno = targetField.getAnnotation(JsonProperty.class);
            String key = targetField.getName();
            if (jsonPropertyAnno != null && !jsonPropertyAnno.value().equals("")) {
                key = jsonPropertyAnno.value();
            }
            return key;
        };

        Function<Field, Object> getValue = (parentField) -> {
            ApiModelMeta apiModelMeta = parentField.getAnnotation(ApiModelMeta.class);
            return apiModelMeta != null ? apiModelMeta.detail() : "无说明";
        };


        Object obj = ReflectionTestUtils.invokeMethod(JsonUtils.class, "walkThroughClassAttrs", User.class,
                getKey, getValue);
        System.out.println(obj);

        getKey = (targetField) -> {
            JsonProperty jsonPropertyAnno = targetField.getAnnotation(JsonProperty.class);
            String key = targetField.getName();
            if (jsonPropertyAnno != null && !jsonPropertyAnno.value().equals("")) {
                key = jsonPropertyAnno.value();
            }
            return key;
        };
        getValue = (parentField) -> getBasicTypeValue(parentField.getType());
        obj = ReflectionTestUtils.invokeMethod(JsonUtils.class, "walkThroughClassAttrs", User.class,
                getKey, getValue);
        System.out.println(obj);
    }
}
