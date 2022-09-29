package fit.wenchao.apidocs.utils;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import fit.wenchao.apidocs.ApiModelMeta;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;


public class JsonUtils {
    public static JSONObject getJsonExample(Class<?> clazz) {
        JSONObject root = getValueJson(clazz);
        return root;
    }

    /**
     * 获取类型为 valueClass 的一个JSONObject对象
     *
     * @param valueClass 目标class
     */
    private static JSONObject getValueJson(Class valueClass) {
        JSONObject root = new JSONObject();
        Field[] declaredFields = valueClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            declaredField.setAccessible(true);

            // 获取Json属性名
            JsonProperty jsonPropertyAnno = declaredField.getAnnotation(JsonProperty.class);
            String key = declaredField.getName();
            if (jsonPropertyAnno != null && !jsonPropertyAnno.value().equals("")) {
                key = jsonPropertyAnno.value();
            }

            // 递归获取json属性值
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
                    } else {
                        // 泛型不是Object，则造3个对象
                        for (Type actualTypeArgument : actualTypeArguments) {
                            Class fieldArgClass = (Class) actualTypeArgument;
                            System.out.println("成员变量的泛型信息：" + fieldArgClass);
                            for (int i = 0; i < 3; i++) {
                                value = getValueJson(fieldArgClass);
                                valuelist.add(value);
                            }
                        }
                    }
                }
            } else if (basicType(valueType)) {
                value = getBasicTypeValue(valueType);
            } else {
                // 普通对象
                value = getValueJson(valueType);
            }
            root.put(key, value);
        }

        return root;
    }

    private static class ClassEncounterTimes {
        Map<Class, Integer> typeEncounterTimes = new HashMap<>();

        public Integer classTimeIncrease(Class clazz) {
            Integer times = typeEncounterTimes.get(clazz);
            if (times == null) {
                times = 1;
                typeEncounterTimes.put(clazz, times);
                return times;
            }
            typeEncounterTimes.put(clazz, ++times);
            return times;
        }
    }


    private static Object doWalk(Class<?> targetClass,
                                 Function<Field, Object> genValue,
                                 Function<Field, String> genKey,
                                 ClassEncounterTimes typeEncounterTimes,
                                 Field parentField) {
        JSONObject root = new JSONObject();
        if (!basicType(targetClass)) {
            // 如果过程中遇到3次及以上同一个类型，则判定为递归定义的类型
            Integer times = typeEncounterTimes.classTimeIncrease(targetClass);
            if (times >= 3) {
                root.put("recursive attrs", "...");
                return root;
            }

            Field[] targetFieldsToWalkThrough = targetClass.getDeclaredFields();
            for (Field targetField : targetFieldsToWalkThrough) {
                targetField.setAccessible(true);

                String key = genKey.apply(targetField);
                // 递归获取json属性值
                Object value = null;
                value = recursivelyGenValue(targetField.getGenericType(), targetField, genValue,genKey, typeEncounterTimes);
                root.put(key, value);
            }
            return root;
        }
        Object value = genValue.apply(parentField);
        return value;
    }

    private static Object recursivelyGenValue(Type genericType, Field parentField,
                                              Function<Field, Object> genValue,
                                              Function<Field, String> genKey,
                                              ClassEncounterTimes  typeEncounterTimes) {
        Object value;
        boolean islist = false;
        if (genericType instanceof ParameterizedType) {
            islist = ((ParameterizedType) genericType).getRawType().equals(List.class);
        } else {
            Class k = (Class) genericType;
            islist = k.equals(List.class);
        }
        boolean ismap = false;
        if (genericType instanceof ParameterizedType) {
            ismap = ((ParameterizedType) genericType).getRawType().equals(Map.class);
        } else {
            Class k = (Class) genericType;
            ismap = k.equals(Map.class);
        }

        if (islist) {
            List<Object> valuelist = new ArrayList<>();

            if (genericType instanceof ParameterizedType) {
                // 参数真实类型
                ParameterizedType parameterizedType = (ParameterizedType) genericType;
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                Type actualType = actualTypeArguments[0];
                value = recursivelyGenValue(actualType, null, genValue,genKey, typeEncounterTimes);
                valuelist.add(value);
            }
            value = valuelist;
        } else if (ismap) {
            Map<Object, Object> valuemap = new HashMap<>();

            if (genericType instanceof ParameterizedType) {
                // 参数真实类型
                ParameterizedType parameterizedType = (ParameterizedType) genericType;
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

                Type keyActualType = actualTypeArguments[0];
                Type valueActualType = actualTypeArguments[1];
                Object mapkey;
                Object mapvalue;
                mapkey = recursivelyGenValue(keyActualType, null, genValue,genKey, typeEncounterTimes);
                mapvalue = recursivelyGenValue(valueActualType, null, genValue,genKey, typeEncounterTimes);
                valuemap.put(mapkey, mapvalue);
            }
            value = valuemap;
        } else if (basicType((Class<?>) genericType)) {
            value = genValue.apply(parentField);
        } else {
            // 普通对象
            value = doWalk((Class<?>) genericType, genValue,genKey, typeEncounterTimes, parentField);
        }
        return value;
    }

    private static class User {
        @ApiModelMeta(detail = "姓名")
        String name;
        @ApiModelMeta(detail = "年龄")
        Integer age;
        @ApiModelMeta(detail = "额外信息")
        List<Address> infoMapList;
    }

    private static class Address {
        @ApiModelMeta(detail = "住址详情")
        String detail;

        @ApiModelMeta(detail = "住户")
        User user;
    }

    public static Object walkThroughClassAttrs(Class targetClass,
                                               Function<Field, String> genKey,
                                               Function<Field, Object> genValue
                                               ) {
        ClassEncounterTimes typeEncounterTimes = new ClassEncounterTimes();
        return doWalk(targetClass, genValue,genKey, typeEncounterTimes, null);
    }

    public static void main(String[] args) {
        Object obj = walkThroughClassAttrs(User.class,(targetField) -> {
            JsonProperty jsonPropertyAnno = targetField.getAnnotation(JsonProperty.class);
            String key = targetField.getName();
            if (jsonPropertyAnno != null && !jsonPropertyAnno.value().equals("")) {
                key = jsonPropertyAnno.value();
            }
            return key;
        }, (parentField) -> {
            ApiModelMeta apiModelMeta = parentField.getAnnotation(ApiModelMeta.class);
            return apiModelMeta != null ? apiModelMeta.detail() : "无说明";
        });
        System.out.println(obj);
    }

    private static boolean isInt(Class<?> clazzA) {
        return clazzA == Byte.class || clazzA == Short.class || clazzA == Integer.class || clazzA == Long.class ||
                clazzA == byte.class || clazzA == short.class || clazzA == int.class || clazzA == long.class;
    }

    private static boolean isDouble(Class<?> clazzA) {
        return clazzA == Double.class || clazzA == Float.class ||
                clazzA == double.class || clazzA == float.class;
    }

    private static boolean isBoolean(Class<?> clazzA) {
        return clazzA == Boolean.class ||
                clazzA == boolean.class;
    }

    private static boolean isString(Class<?> clazzA) {
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

}