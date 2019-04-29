package viewmodels;

import androidx.lifecycle.ViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.exceptions.RealmMigrationNeededException;
import obj.Bus;

public class MainActivityViewModel extends ViewModel {


    public static void createBusList(JSONArray busArray) throws JSONException {
        try {
            Realm realm = Realm.getDefaultInstance();
            RealmResults<Bus> results = realm.where(Bus.class).findAll();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    results.deleteAllFromRealm();
                }
            });

        } catch (RealmMigrationNeededException e) {
            RealmConfiguration configuration = new RealmConfiguration.Builder()
                    .deleteRealmIfMigrationNeeded()
                    .build();
        }

        for (int i = 0; i < busArray.length(); i++) {
            JSONObject bus = busArray.getJSONObject(i);

            Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.createObjectFromJson(Bus.class, bus);
                }
            });
        }
    }
}
