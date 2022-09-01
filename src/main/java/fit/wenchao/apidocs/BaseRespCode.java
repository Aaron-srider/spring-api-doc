package fit.wenchao.apidocs;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class BaseRespCode {

    public BaseRespCode() {
        autoGenValue();
    }

    public List<String> codes() {
        List<String> result = new ArrayList<>();
        Class thisclass = this.getClass();
        Field[] declaredFields = thisclass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            declaredField.setAccessible(true);
            Class<?> type = declaredField.getType();
            if(!type.isAssignableFrom(String.class)) {
                throw new RuntimeException("RespCode 的所有子类属性只能是String");
            }

            try {
                String value = (String) declaredField.get(this);
                result.add(value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    private void autoGenValue() {
        Class thisclass = this.getClass();
        Field[] declaredFields = thisclass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            declaredField.setAccessible(true);
            Class<?> type = declaredField.getType();
            if(!type.isAssignableFrom(String.class)) {
                throw new RuntimeException("RespCode 的所有子类属性只能是String");
            }

            try {
                declaredField.set(this, declaredField.getName());
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
