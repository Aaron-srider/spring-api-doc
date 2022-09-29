package fit.wenchao.apidocs.utils;

import org.springframework.web.bind.annotation.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class ApiDocUtils {
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