package fit.wenchao.apidocs;

import java.lang.reflect.Method;

/**
 * 实现该接口，可以为每个API返回额外的自定义信息
 */
public interface ApiInfoPlugin {
    /**
     * <p>为每个API提供额外的信息，额外信息存储在每个 {@code ApiInfo} 对象的 {@code customInfoMap} 属性中。
     * <p>该接口的实现类都会在生成API信息的过程中被调用，以返回自定义的API信息。
     * <p>使用方法：实现该接口，并将实现类加入到 Spring 容器中即可。
     * <p>
     * 一个实现示例如下：
     * <pre>
     * &#64Component
     * class AddApiTagApiInfoPlugin implements ApiInfoPlugin {
     *    CustomApiInfo getInfo(
     *                      Method method,
     *                      ApiInfoContext apiInfoContext
     *                      ) {
     *
     *        String apiTag = apiInfoContext.getHttpMethod()
     *                          + "-"
     *                          + apiInfoContext.getUrl();
     *
     *        return new CustomApiInfo() {
     *            public Object getInfoSignature() {
     *                return "api_tag";
     *            }
     *
     *            public Object getInfoBody() {
     *                return apiTag;
     *            }
     *        };
     * }
     * @param method API对应的Controller方法
     * @param apiInfoContext 该API的一些已经获取的信息，包括api的url，http-method，api名称，api描述等。
     *                       如果在自定义的getInfo中要生成新的实体，可以使用apiInfoContext分配新的id。
     * @return 返回自定义的API信息
     */
    CustomApiInfo getInfo(Method method, ApiInfoContext apiInfoContext);
}
