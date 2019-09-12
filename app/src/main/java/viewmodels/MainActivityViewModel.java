package viewmodels;

import android.content.pm.PackageManager;

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
import obj.Stop;

public class MainActivityViewModel extends ViewModel {

    //get the jsonarray from AC Transit's API and turn each object into a bus
    public static void createPredictionList(JSONArray busArray) throws JSONException {
        Realm realm = Realm.getDefaultInstance();
        try {
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

            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.createObjectFromJson(Prediction.class, prediction);
                }
            });
        }

        realm.close();
    }

    public static void createNearbyStopList(JSONArray nearbyArray) throws JSONException {
        Realm realm = Realm.getDefaultInstance();
        try {
            RealmResults<Stop> results = realm.where(Stop.class).findAll();

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

        for (int i = 0; i < nearbyArray.length(); i++) {
            JSONObject nearbyStop = nearbyArray.getJSONObject(i);

            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.createObjectFromJson(Stop.class, nearbyStop);
                }
            });
        }

        realm.close();
    }
}
