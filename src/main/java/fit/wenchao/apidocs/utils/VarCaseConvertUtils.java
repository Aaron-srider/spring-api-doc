package fit.wenchao.apidocs.utils;

import com.google.common.base.CaseFormat;

public class VarCaseConvertUtils {

    public static String lowerCamel2LowerUnderScore(String name) {
       return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name);
    }

    public static String lowerUnderScore2LowerCamel(String name) {
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name);
    }

    public static void main(String[] args) {
        System.out.println(lowerCamel2LowerUnderScore("hello_worldMyfriend"));
    }

    public static String lowerHyphen2LowerUnderCase(String name) {
        return CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_UNDERSCORE, name);

    }
}
