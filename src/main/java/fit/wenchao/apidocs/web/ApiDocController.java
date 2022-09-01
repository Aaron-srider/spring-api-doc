package fit.wenchao.apidocs.web;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import fit.wenchao.apidocs.*;
import fit.wenchao.apidocs.properties.ApiDocProperties;
import fit.wenchao.apidocs.utils.ClasspathPackageScanner;
import fit.wenchao.apidocs.utils.VarCaseConvertUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@Slf4j
@Validated
@EnableConfigurationProperties(value = ApiDocProperties.class)
public class ApiDocController {
    @Autowired
    ApiDocProperties apiDocProperties;

    public List<ApiInfoPlugin> apiInfoPlugins;

    @Autowired
    ApplicationContext applicationContext;

    @PostConstruct
    public void findApiInfoPlugins() {
        apiInfoPlugins = new ArrayList<>();
        Map<String, ApiInfoPlugin> beansOfType = applicationContext.getBeansOfType(ApiInfoPlugin.class);
        beansOfType.forEach((key, value) -> {apiInfoPlugins.add(value);});
    }

    private static JSONObject getJsonExample(Class<?> clazz) {
        JSONObject root = getValueJson(clazz);
        return root;
    }

    public static JSONObject getValueJson(Class valueClass)  {
        JSONObject root = new JSONObject();
        Field[] declaredFields = valueClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            declaredField.setAccessible(true);
            JsonProperty jsonPropertyAnno = declaredField.getAnnotation(JsonProperty.class);
            String key = declaredField.getName();
            if(jsonPropertyAnno!=null && !jsonPropertyAnno.value().equals("")) {
                key=jsonPropertyAnno.value();
            }

            Class<?> valueType = declaredField.getType();
            Object value = null;
            // 如果是list
            if (List.class.isAssignableFrom(valueType)) {
                List<Object> valuelist = new ArrayList<>();
                Type genericType = declaredField.getGenericType();
                if (genericType instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType) genericType;
                    // 获取成员变量的泛型类型信息
                    Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                    // 泛型参数默认是Object
                    if (actualTypeArguments.length == 0 || actualTypeArguments.length == 1 && actualTypeArguments[0].equals(Object.class)) {
                        for (int i = 0; i < 3; i++) {
                            value = new Object();
                            valuelist.add(value);
                        }
                        root.put(key, valuelist);
                    } else {
                        // 泛型不是Object，则造3个对象
                        for (Type actualTypeArgument : actualTypeArguments) {
                            Class fieldArgClass = (Class) actualTypeArgument;
                            System.out.println("成员变量的泛型信息：" + fieldArgClass);
                            for (int i = 0; i < 3; i++) {
                                value = getValueJson(fieldArgClass);
                                valuelist.add(value);
                            }
                            root.put(key, valuelist);
                        }
                    }
                }
            } else if (basicType(valueType)) {
                value = getBasicTypeValue(valueType);
                root.put(key, value);
            } else {
                // 普通对象
                value = getValueJson(valueType);
                root.put(key, value);
            }
        }

        return root;
    }

    protected static boolean isInt(Class<?> clazzA) {
        return clazzA == Byte.class || clazzA == Short.class || clazzA == Integer.class || clazzA == Long.class ||
                clazzA == byte.class || clazzA == short.class || clazzA == int.class || clazzA == long.class;
    }

    protected static boolean isDouble(Class<?> clazzA) {
        return clazzA == Double.class || clazzA == Float.class ||
                clazzA == double.class || clazzA == float.class;
    }

    protected static boolean isBoolean(Class<?> clazzA) {
        return clazzA == Boolean.class ||
                clazzA == boolean.class;
    }

    protected static boolean isString(Class<?> clazzA) {
        return clazzA == String.class;
    }

    private static Object getBasicTypeValue(Class<?> valueType) {
        if (isInt(valueType)) {
            return (byte) RandomUtils.nextInt(0, 128);
        } else if (isDouble(valueType)) {
            return (double) new Random().nextDouble();
        } else if (isBoolean(valueType)) {
            int randomInt = RandomUtils.nextInt(0, 2);
            return randomInt == 0 ? false : true;
        } else if (isString(valueType)) {
            return RandomStringUtils.randomAlphabetic(6);
        } else {
            return "null";
        }
    }

    private static boolean basicType(Class<?> valueType) {
        if (valueType.equals(String.class)) {
            return true;
        }
        try {
            return ((Class) valueType.getField("TYPE").get(null)).isPrimitive();
        } catch (Exception e) {
            return false;
        }
    }


    @GetMapping("/modules")
    public JsonResult getModules() throws IOException {
        String controllerPackage = apiDocProperties.getApiBasePackage();
        if (controllerPackage ==  null || "".equals(controllerPackage)) {
            return JsonResult.of(null,
                    "API_BASE_PACKAGE_NOT_SET", "");
        }

        ClasspathPackageScanner scan = new ClasspathPackageScanner(controllerPackage);
        List<String> fullyQualifiedClassNameList = scan.getFullyQualifiedClassNameList();
        Set<String> targetControllerClassName = fullyQualifiedClassNameList.stream().filter((fullClassName) -> {
            Class<?> tClass;
            try {
                tClass = Class.forName(fullClassName);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            ApiModule apiModuleAnno = tClass.getAnnotation(ApiModule.class);
            return apiModuleAnno != null;
        }).collect(Collectors.toSet());

        ApiInfoContext apiInfoContext = new ApiInfoContext();
        List<ModuleInfo> modules = targetControllerClassName.stream().map(
                (fullClassName) -> {
                    Class<?> controllerClassName;

                    try {
                        controllerClassName = Class.forName(fullClassName);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }

                    ModuleInfo moduleInfo = new ModuleInfo();

                    ApiModule apiModuleAnno
                            = controllerClassName.getAnnotation(ApiModule.class);
                    String moduleName = apiModuleAnno.moduleName();
                    moduleInfo.setName(moduleName);
                    moduleInfo.setId(apiInfoContext.getId());

                    List<ApiInfo> apis = new ArrayList<>();
                    Method[] methods = controllerClassName.getDeclaredMethods();
                    for (Method method : methods) {

                        String url = ApiDocUtils.getMappingUrlFromMethod(method);
                        String httpMethod = ApiDocUtils.getRequestMethod(method);
                        String apiName;
                        String desc;
                        ApiDescription apiDescriptionAnno = method.getAnnotation(ApiDescription.class);
                        if (apiDescriptionAnno != null && !apiDescriptionAnno.name().equals("")) {
                            apiName = apiDescriptionAnno.name();
                        } else {
                            apiName = VarCaseConvertUtils.lowerCamel2LowerUnderScore(method.getName());
                        }

                        if (apiDescriptionAnno != null && !apiDescriptionAnno.name().equals("")) {
                            desc = apiDescriptionAnno.detail();
                        } else {
                            desc = null;
                        }

                        List<ApiParameterInfo> params = new ArrayList<>();
                        ApiParameters apiParameters = method.getAnnotation(ApiParameters.class);
                        if (apiParameters != null) {
                            ApiParameter[] parameterAnnos = apiParameters.params();
                            for (ApiParameter parameterAnno : parameterAnnos) {
                                ApiParamDataTypeEnum apiParamDataTypeEnum = parameterAnno.dataType();
                                String detail = parameterAnno.detail();
                                String name = parameterAnno.name();
                                String defaultValue = parameterAnno.defaultValue();
                                boolean required = parameterAnno.required();
                                ApiParamType apiParamType = parameterAnno.paramType();

                                ApiParameterInfo.ApiParameterInfoBuilder builder = ApiParameterInfo.builder().detail(detail)
                                        .defaultValue(defaultValue)
                                        .required(required)
                                        .dataType(apiParamDataTypeEnum.name())
                                        .paramType(apiParamType.name())
                                        .name(name)
                                        .detail(detail)
                                        .id(apiInfoContext.getId());

                                JSONObject bodyJsonExample = new JSONObject();

                                if(apiParamType.equals(ApiParamType.BODY)) {
                                    Class<?> bodyClass = parameterAnno.bodyClass();
                                    if(!bodyClass.equals(Void.class)) {
                                        bodyJsonExample = getJsonExample(bodyClass);
                                        builder.example(bodyJsonExample.toString());
                                    }
                                }

                                ApiParameterInfo apiParameterInfo;
                                apiParameterInfo = builder.build();

                                params.add(apiParameterInfo);
                            }
                        }



                        List<CustomApiInfo> customApiInfos = new ArrayList<>();
                        CustomApiInfo customApiInfo;
                        for (ApiInfoPlugin apiInfoPlugin : apiInfoPlugins) {
                            customApiInfo = apiInfoPlugin.getInfo(method, apiInfoContext);
                            customApiInfos.add(customApiInfo);
                        }

                        ApiInfo apiInfo;
                        apiInfo = ApiInfo.builder()
                                .name(apiName).id(apiInfoContext.getId())
                                .detail(desc).method(httpMethod.toUpperCase())
                                .url(url)
                                .params(params)
                                .build();
                        apiInfo.addCustomInfos(customApiInfos);
                        apis.add(apiInfo);
                    }
                    moduleInfo.setApis(apis);
                    return moduleInfo;
                }

        ).collect(Collectors.toList());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("modules", modules);
        return JsonResult.of(jsonObject,"SUCCESS", "");
    }

}

class ApiDocUtils {
    public static <T extends Annotation> T getControllerAnnotation(Method method, Class<T> annotationClass) {
        Class<?> declaringClass = method.getDeclaringClass();
        T annotation = declaringClass.getAnnotation(annotationClass);
        return annotation;
    }

    public static String getMappingUrlFromMethod(Method method) {
        Boolean controllerRequestMappingExists = false;
        StringBuilder mappingUrl = new StringBuilder();

        RequestMapping requestMappingAnno = getControllerAnnotation(method, RequestMapping.class);
        if (requestMappingAnno != null && requestMappingAnno.value().length != 0) {
            controllerRequestMappingExists = true;
        }

        String requestMappingFirstUrl = null;
        if (controllerRequestMappingExists) {
            requestMappingFirstUrl = requestMappingAnno.value()[0];
        }

        if (requestMappingFirstUrl != null) {
            mappingUrl.append(requestMappingFirstUrl);
        }

        String mappingUrlFromMethod = doGetMappingUrlFromMethod(method);
        if (mappingUrlFromMethod != null) {
            mappingUrl.append(mappingUrlFromMethod);
        }
        return mappingUrl.toString();
    }


    public static String doGetMappingUrlFromMethod(Method method) {
        RequestMapping requestMappingAnno = method.getAnnotation(RequestMapping.class);
        if (requestMappingAnno != null && requestMappingAnno.value().length != 0 && supportMethod(requestMappingAnno.method())) {
            //只获取第一个url
            return requestMappingAnno.value()[0];
        }

        GetMapping getMappingAnno = method.getAnnotation(GetMapping.class);
        if (getMappingAnno != null && getMappingAnno.value().length != 0) {
            //只获取第一个url
            return getMappingAnno.value()[0];
        }

        PostMapping postMappingAnno = method.getAnnotation(PostMapping.class);
        if (postMappingAnno != null && postMappingAnno.value().length != 0) {
            //只获取第一个url
            return postMappingAnno.value()[0];
        }

        PutMapping putMappingAnno = method.getAnnotation(PutMapping.class);
        if (putMappingAnno != null && putMappingAnno.value().length != 0) {
            //只获取第一个url
            return putMappingAnno.value()[0];
        }

        DeleteMapping deleteMappingAnno = method.getAnnotation(DeleteMapping.class);
        if (deleteMappingAnno != null && deleteMappingAnno.value().length != 0) {
            //只获取第一个url
            return deleteMappingAnno.value()[0];
        }
        return null;
    }

    public static String getRequestMethod(Method method) {
        RequestMapping requestMappingAnno = method.getAnnotation(RequestMapping.class);
        if (requestMappingAnno != null && supportMethod(requestMappingAnno.method())) {
            return requestMappingAnno.method()[0].toString().toLowerCase();
        }

        GetMapping getMappingAnno = method.getAnnotation(GetMapping.class);
        if (getMappingAnno != null) {
            return RequestMethod.GET.toString().toLowerCase();
        }

        PostMapping postMappingAnno = method.getAnnotation(PostMapping.class);
        if (postMappingAnno != null) {
            return RequestMethod.POST.toString().toLowerCase();
        }

        PutMapping putMappingAnno = method.getAnnotation(PutMapping.class);
        if (putMappingAnno != null) {
            return RequestMethod.PUT.toString().toLowerCase();
        }

        DeleteMapping deleteMappingAnno = method.getAnnotation(DeleteMapping.class);
        if (deleteMappingAnno != null) {
            return RequestMethod.DELETE.toString().toLowerCase();
        }

        //默认使用GET请求
        return RequestMethod.GET.toString().toLowerCase();
    }

    private static boolean supportMethod(RequestMethod[] method) {
        if (method.length > 1) {
            return false;
        }
        if (method.length < 1) {
            return false;
        }
        if (method[0].equals(RequestMethod.GET) ||
                method[0].equals(RequestMethod.POST) ||
                method[0].equals(RequestMethod.PUT) ||
                method[0].equals(RequestMethod.DELETE)) {
            return true;
        }
        return false;
    }
}
