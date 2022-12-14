package fit.wenchao.apidocs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiParameter {
    String name();

    Class<?> bodyClass() default Void.class;

    String defaultValue() default "";

    boolean required();

    ApiParamType paramType();
    ApiParamDataTypeEnum dataType() default ApiParamDataTypeEnum.STRING;

    String detail() default "";
}



