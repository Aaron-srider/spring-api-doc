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

import static fit.wenchao.apidocs.utils.ReflectUtils.*;


public class JsonUtils {

    /**
     * 从指定的Class对象生成一个 Json 实例
     *
     * @param clazz 目标Class对象
     * @return 返回Json实例
     */
    public static Object getJsonExample(Class<?> clazz) {
        return walkThroughClassAttrs(clazz, (targetField) -> {
            JsonProperty jsonPropertyAnno = targetField.getAnnotation(JsonProperty.class);
            String key = targetField.getName();
            if (jsonPropertyAnno != null && !jsonPropertyAnno.value().equals("")) {
                key = jsonPropertyAnno.value();
            }
            return key;
        }, (parentField) -> {
            return getBasicTypeValue(parentField.getType());
            //ApiModelMeta apiModelMeta = parentField.getAnnotation(ApiModelMeta.class);
            //return apiModelMeta != null ? apiModelMeta.detail() : "无说明";
        });
    }

    /**
     * 从指定的Class对象生成一个 Json 实例
     *
     * @param clazz 目标Class对象
     * @return 返回Json实例
     */
    public static Object getClassFieldDetail(Class<?> clazz) {
        return walkThroughClassAttrs(clazz, (targetField) -> {
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
    }

    /**
     * 遍历指定Class的所有属性，生成对应的JSONObject，如果有嵌套的对象，则递归生成嵌套的JSONObject
     *
     * @param targetClass        目标Class
     * @param genValue           指定如何从属性生成JSONObject的value
     * @param genKey             指定如何从属性生成JSONObject的key
     * @param typeEncounterTimes 记录递归的过程中遇到过多少次某个非基础类，防止无限递归
     * @param walkField          待walk的属性，递归起始时应该填null，因为此时指定的类并不作为任何类的属性
     */
    private static Object doWalk(Class<?> targetClass,
                                 Function<Field, Object> genValue,
                                 Function<Field, String> genKey,
                                 ClassEncounterTimes typeEncounterTimes,
                                 Field walkField) {

        // targetClass对应的JSONObject对象，每个属性对应JSONObject中的一个键值对
        JSONObject root = new JSONObject();

        // 如果targetClass不是基础类，则遍历其所有属性，并放入root中
        if (!basicType(targetClass)) {
            // 如果过程中遇到3次及以上同一个类型，则判定为递归定义的类型
            Integer times = typeEncounterTimes.classTimeIncrease(targetClass);
            if (times >= 3) {
                root.put("recursive attrs", "...");
                return root;
            }

            Field[] fieldsToWalkThrough = targetClass.getDeclaredFields();
            for (Field field : fieldsToWalkThrough) {
                field.setAccessible(true);
                ClassEncounterTimes typeEncounterTimes1 = typeEncounterTimes.clone();
                // 获取JSONObject的key
                String key = genKey.apply(field);

                // 递归获取JSONObject的value
                Object value = recursivelyGenFieldValue(field.getGenericType(), field, genValue, genKey, typeEncounterTimes1);

                root.put(key, value);
            }
            return root;
        }

        // 如果targetClass是基础类，则直接生成value值返回
        // 递归根节点，从 @ApiModelMeta 中获取名称
        if (walkField == null) {
            ApiModelMeta apiModelMeta = targetClass.getAnnotation(ApiModelMeta.class);
            if (apiModelMeta == null) {
                return targetClass.getSimpleName();
            }
            return apiModelMeta.detail();
        }

        return genValue.apply(walkField);
    }

    /**
     * 为了应对诸如：List&lt;List&lt;List&lt;...&gt;&gt;&gt;这样的嵌套情况，使用此递归生成属性的value
     *
     * @param fieldGenericType   属性的genericType
     * @param targetField        目标属性
     * @param genValue           指定如何从属性生成JSONObject的value
     * @param genKey             指定如何从属性生成JSONObject的key
     * @param typeEncounterTimes 记录递归的过程中遇到过多少次某个非基础类，防止无限递归（此方法中有对doWalk的调用，该
     *                           参数为doWalk准备）
     * @return 指定属性的value
     */
    private static Object recursivelyGenFieldValue(Type fieldGenericType, Field targetField,
                                                   Function<Field, Object> genValue,
                                                   Function<Field, String> genKey,
                                                   ClassEncounterTimes typeEncounterTimes) {
        Object resultValue;

        GenericTypeWrapper genericTypeWrapper = getGenericTypeWrapper(fieldGenericType);

        // 检查属性是否是List
        boolean islist = genericTypeWrapper.getActualClass().equals(List.class);

        // 检查属性是否是Map
        boolean ismap = genericTypeWrapper.getActualClass().equals(Map.class);

        if (islist) {
            List<Object> valuelist = new ArrayList<>();

            if (genericTypeWrapper.isHasTypeParams()) {
                // 参数真实类型
                List<Type> typeParams = genericTypeWrapper.getTypeParams();
                Type actualType = typeParams.get(0);
                Object value1 = recursivelyGenFieldValue(actualType, targetField, genValue, genKey, typeEncounterTimes.clone());
                Object value2 = recursivelyGenFieldValue(actualType, targetField, genValue, genKey, typeEncounterTimes.clone());
                valuelist.add(value1);
                valuelist.add(value2);
            } else {
                valuelist.addAll(Arrays.asList("An Object", "An Object"));
            }
            resultValue = valuelist;
        } else if (ismap) {
            Map<Object, Object> valuemap = new HashMap<>();

            if (fieldGenericType instanceof ParameterizedType) {
                // 参数真实类型
                ParameterizedType parameterizedType = (ParameterizedType) fieldGenericType;
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

                Type keyActualType = actualTypeArguments[0];
                GenericTypeWrapper genericTypeWrapper1 = getGenericTypeWrapper(keyActualType);
                if (genericTypeWrapper1.isHasTypeParams()) {
                    throw new IllegalArgumentException("Map的第一个泛型参数只支持基础类型");
                }

                Type valueActualType = actualTypeArguments[1];
                Object mapkey;
                Object mapvalue;
                //mapkey = recursivelyGenFieldValue(keyActualType, targetField, genValue,genKey, typeEncounterTimes);
                mapkey = getBasicTypeValue(genericTypeWrapper1.getActualClass());
                mapvalue = recursivelyGenFieldValue(valueActualType, targetField, genValue, genKey, typeEncounterTimes);
                valuemap.put(mapkey, mapvalue);
            }
            resultValue = valuemap;
        } else if (basicType((Class<?>) fieldGenericType)) {
            resultValue = genValue.apply(targetField);
        } else {
            // 普通对象
            resultValue = doWalk((Class<?>) fieldGenericType, genValue, genKey, typeEncounterTimes, targetField);
        }
        return resultValue;
    }

    /**
     * doWalk方法的wrapper，详见doWalk
     *
     * @param targetClass 目标Class
     * @param genValue    指定如何从属性生成JSONObject的value
     * @param genKey      指定如何从属性生成JSONObject的key
     */
    private static Object walkThroughClassAttrs(Class targetClass,
                                                Function<Field, String> genKey,
                                                Function<Field, Object> genValue
    ) {
        ClassEncounterTimes typeEncounterTimes = new ClassEncounterTimes();
        return doWalk(targetClass, genValue, genKey, typeEncounterTimes, null);
    }

    public static Object getBasicTypeValue(Class<?> valueType) {
        if (isInt(valueType)) {
            return (byte) RandomUtils.nextInt(0, 128);
        } else if (isDouble(valueType)) {
            return new Random().nextDouble();
        } else if (isBoolean(valueType)) {
            int randomInt = RandomUtils.nextInt(0, 2);
            return randomInt != 0;
        } else if (isString(valueType)) {
            return RandomStringUtils.randomAlphabetic(6);
        } else {
            return "null";
        }
    }

    private static class ClassEncounterTimes {
        Map<Class, Integer> typeEncounterTimes = new HashMap<>();

        ClassEncounterTimes() {
        }

        private ClassEncounterTimes(Map<Class, Integer> typeEncounterTimes) {
            this.typeEncounterTimes = new HashMap<>();
            this.typeEncounterTimes.putAll(typeEncounterTimes);
        }

        public ClassEncounterTimes clone() {
            return new ClassEncounterTimes(this.typeEncounterTimes);
        }

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


}