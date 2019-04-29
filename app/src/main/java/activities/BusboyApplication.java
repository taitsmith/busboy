package activities;

import android.app.Application;

import net.danlew.android.joda.JodaTimeAndroid;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.exceptions.RealmMigrationNeededException;

public class BusboyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            Realm.init(this);
        } catch (RealmMigrationNeededException e) {
            RealmConfiguration config = new RealmConfiguration.Builder()
                    .deleteRealmIfMigrationNeeded()
                    .build();
        }
        JodaTimeAndroid.init(this);
    }
}
