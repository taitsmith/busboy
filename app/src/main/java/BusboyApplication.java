import android.app.Application;

import io.realm.Realm;

public class BusboyApplication extends Application {
    Realm realm;

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
    }
}
