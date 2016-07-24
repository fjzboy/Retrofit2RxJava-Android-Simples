package internet.rxjava.lnterface;

/**
 * Created by Michael Smith on 2016/7/24.
 */

public interface Observer<T> {
    public void register(T t);

    public void unregister(T t);
}
