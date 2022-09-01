package fit.wenchao.apidocs;

public class ApiInfoContext {
    Long counter = 0L;
    public Long getId() {
        return counter++;
    }
}
