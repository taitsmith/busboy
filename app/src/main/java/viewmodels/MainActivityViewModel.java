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
import obj.Prediction;

public class MainActivityViewModel extends ViewModel {


    //get the jsonarray from AC Transit's API and turn each object into a bus
    public static void createPredictionList(JSONArray busArray) throws JSONException {
        try {
            Realm realm = Realm.getDefaultInstance();
            RealmResults<Bus> results = realm.where(Bus.class).findAll();
            if (!results.isEmpty()) {
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        results.deleteAllFromRealm();
                    }
                });
            }

        } catch (RealmMigrationNeededException | NullPointerException e) {
            RealmConfiguration configuration = new RealmConfiguration.Builder()
                    .deleteRealmIfMigrationNeeded()
                    .build();
        }

        for (int i = 0; i < busArray.length(); i++) {
            JSONObject prediction = busArray.getJSONObject(i);

            Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.createObjectFromJson(Prediction.class, prediction);
                }
            });
        }
    }
}
