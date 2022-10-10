package fit.wenchao.apidocs.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReflectUtils {

    public static GenericTypeWrapper getGenericTypeWrapper(Type type) {
        Class actualClass;
        List<Type> typeParams = new ArrayList<>();
        boolean hasTypeParams = false;
        if (type instanceof ParameterizedType) {
            // 该类型有泛型参数
            actualClass = (Class) ((ParameterizedType) type).getRawType();
            Type[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
            typeParams.addAll(Arrays.asList(actualTypeArguments));
            hasTypeParams = true;
        } else {
            // 该类型无泛型参数（包括Row的泛型类型比如List ）
            actualClass = (Class) type;
        }

        return GenericTypeWrapper.builder().actualClass(actualClass).typeParams(typeParams).hasTypeParams(hasTypeParams)
                .build();
    }


    public static boolean isInt(Class<?> clazzA) {
        return clazzA == Byte.class || clazzA == Short.class || clazzA == Integer.class || clazzA == Long.class ||
                clazzA == byte.class || clazzA == short.class || clazzA == int.class || clazzA == long.class;
    }

    public static boolean isDouble(Class<?> clazzA) {
        return clazzA == Double.class || clazzA == Float.class ||
                clazzA == double.class || clazzA == float.class;
    }

    public static boolean isBoolean(Class<?> clazzA) {
        return clazzA == Boolean.class ||
                clazzA == boolean.class;
    }

    public static boolean isString(Class<?> clazzA) {
        return clazzA == String.class;
    }

    public static boolean basicType(Class<?> valueType) {
        if (valueType.equals(String.class)) {
            return true;
        }
        try {
            return ((Class) valueType.getField("TYPE").get(null)).isPrimitive();
        } catch (Exception e) {
            return false;
        }
    }
}
