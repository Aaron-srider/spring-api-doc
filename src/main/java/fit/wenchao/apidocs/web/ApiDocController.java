package fit.wenchao.apidocs.web;

import com.alibaba.fastjson.JSONObject;
import fit.wenchao.apidocs.*;
import fit.wenchao.apidocs.properties.ApiDocProperties;
import fit.wenchao.apidocs.utils.ApiDocUtils;
import fit.wenchao.apidocs.utils.ClasspathPackageScanner;
import fit.wenchao.apidocs.utils.JsonUtils;
import fit.wenchao.apidocs.utils.VarCaseConvertUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.lang.reflect.Method;
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



    @GetMapping("/modules")
    public JsonResult getModules() throws IOException {
        String controllerPackage = apiDocProperties.getApiBasePackage();
        if (controllerPackage ==  null || "".equals(controllerPackage)) {
            return JsonResult.of(null,
                    "API_BASE_PACKAGE_NOT_SET", "");
        }

        ClasspathPackageScanner scan = new ClasspathPackageScanner(controllerPackage);
        List<String> fullyQualifiedClassNameList = scan.getFullyQualifiedClassNameList();
        // 获取所有标注@ApiModule的Controller类名
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
                    Class<?> controllerClass  =  loadClass(fullClassName);
                    ModuleInfo moduleInfo = new ModuleInfo();
                    ApiModule apiModuleAnno
                            = controllerClass.getAnnotation(ApiModule.class);
                    String moduleName = apiModuleAnno.moduleName();
                    moduleInfo.setName(moduleName);
                    moduleInfo.setId(apiInfoContext.getId());

                    // 获取Controller的所有API
                    List<ApiInfo> apis =
                            new ArrayList<>(getAllApiFromController(controllerClass, apiInfoContext));

                    moduleInfo.setApis(apis);
                    return moduleInfo;
                }

        ).collect(Collectors.toList());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("modules", modules);
        return JsonResult.of(jsonObject,"SUCCESS", "");
    }

    private Class<?> loadClass(String fullClassName) {
        Class<?> controllerClass;
        try {
            controllerClass = Class.forName(fullClassName);
            return controllerClass;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private Collection<? extends ApiInfo> getAllApiFromController(Class<?> controllerClass,ApiInfoContext apiInfoContext) {
        apiInfoContext.resetInfo();
        Method[] methods = controllerClass.getMethods();
        List<ApiInfo> apis = new ArrayList<>();
        for (Method method : methods) {

            String url = ApiDocUtils.getMappingUrlFromMethod(method);
            String httpMethod = ApiDocUtils.getRequestMethod(method);
            String apiName;
            String desc;
            // 只读取标注了ApiDescription的方法
            ApiDescription apiDescriptionAnno = method.getAnnotation(ApiDescription.class);
            if(apiDescriptionAnno == null) {
                continue;
            }
            if ( !apiDescriptionAnno.name().equals("")) {
                apiName = apiDescriptionAnno.name();
            } else {
                apiName = VarCaseConvertUtils.lowerCamel2LowerUnderScore(method.getName());
            }

            if ( !apiDescriptionAnno.name().equals("")) {
                desc = apiDescriptionAnno.detail();
            } else {
                desc = null;
            }

            List<ApiParameterInfo> params = new ArrayList<>();
            ApiParameters apiParameters = method.getAnnotation(ApiParameters.class);
            List<ApiParameter> parameterAnnos = new ArrayList<>();
            if (apiParameters != null) {
                parameterAnnos = Arrays.asList(apiParameters.params());
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

                    Object bodyJsonExample;
                    if(apiParamType.equals(ApiParamType.BODY)) {
                        Class<?> bodyClass = parameterAnno.bodyClass();
                        if(!bodyClass.equals(Void.class)) {
                            bodyJsonExample = JsonUtils.getJsonExample(bodyClass);
                            builder.example(bodyJsonExample.toString());
                        }
                    }

                    ApiParameterInfo apiParameterInfo;
                    apiParameterInfo = builder.build();

                    params.add(apiParameterInfo);
                }
            }

           apiInfoContext.setApiDesc(desc).setApiName(apiName).setMethod(method).setHttpMethod(httpMethod)
                   .setParameterAnnos(parameterAnnos).setUrl(url);
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
        return apis;
    }

}

