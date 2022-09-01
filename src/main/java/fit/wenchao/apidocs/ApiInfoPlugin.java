package fit.wenchao.apidocs;

import java.lang.reflect.Method;

public interface ApiInfoPlugin {

    CustomApiInfo getInfo(Method method, ApiInfoContext apiInfoContext) ;
}
