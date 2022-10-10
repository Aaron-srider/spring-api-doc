package fit.wenchao.apidocs.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.lang.reflect.Type;
import java.util.List;

@Data
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public
class GenericTypeWrapper {
    boolean hasTypeParams;
    Class actualClass;
    List<Type> typeParams;
}