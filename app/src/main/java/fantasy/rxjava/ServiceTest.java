package fantasy.rxjava;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by Michael Smith on 2016/7/21.
 */

public class ServiceTest {
    public static void main(String[] args) throws Exception {
/*        String auth = "Basic dG1qMDAxOjEyMzQ1Ng==";

        CompositeSubscription mSubscriptions = new CompositeSubscription();
        ServiceApi mApiClient = ServiceFactory.createRetrofitService(ServiceApi.class);

        mSubscriptions.add(mApiClient.login(auth)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Token>() {
                    @Override
                    public void onCompleted() {
                        System.out.print("onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        System.out.print("onError");
                    }

                    @Override
                    public void onNext(Token token) {
                        System.out.print("onNext");
                    }
                })

        );*/
    }


}
