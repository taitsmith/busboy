package activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.taitsmith.busboy.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import im.delight.android.location.SimpleLocation;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.exceptions.RealmMigrationNeededException;
import obj.Bus;
import obj.Prediction;
import obj.Stop;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import utils.HelpfulUtils;
import utils.NearbyAdapter;
import utils.PredictionAdapter;

import static viewmodels.MainActivityViewModel.createNearbyStopList;
import static viewmodels.MainActivityViewModel.createPredictionList;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.stopEntryEditText)
    EditText stopEntry;
    @BindView(R.id.searchIdButton)
    Button searchButton;
    @BindView(R.id.searchNearbyButton)
    Button searchNearbyButton;
    @BindView(R.id.busListView)
    ListView listView;
    @BindView(R.id.loadingBar)
    ProgressBar loadingBar;

    @BindString(R.string.url_prediction)
    String predictionUrl;
    @BindString(R.string.url_nearby)
    String nearbyUrl;
    @BindString(R.string.api_token)
    String apiToken;

    OkHttpClient client;
    Realm realm;
    String[] coords;

    public static SimpleLocation location;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        client = new OkHttpClient();
        handler = new Handler(Looper.getMainLooper());
        location = new SimpleLocation(this);

        setupLocation();

        try {
            realm = Realm.getDefaultInstance();
        } catch (RealmMigrationNeededException e) {
            RealmConfiguration configuration = new RealmConfiguration.Builder()
                    .deleteRealmIfMigrationNeeded()
                    .build();
        }
    }

    //disable the nearby button until we have a good location
    //the simplelocation library seems weird about not finding a location right away (pixel xl android 10)
    private void setupLocation() {
        searchNearbyButton.setEnabled(false);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    69); //TODO change this
        }
        if (!location.hasLocationEnabled()) {
            SimpleLocation.openSettings(this);
            Toast.makeText(this, "Please enable location", Toast.LENGTH_SHORT).show();
        }

        location.beginUpdates();
        location.setListener(() -> searchNearbyButton.setEnabled(true));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        final RealmResults<Bus> results = realm.where(Bus.class).findAll();

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                results.deleteAllFromRealm();
                Toast.makeText(MainActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
            }
        });
        realm.close();
        location.endUpdates();
    }

    @OnClick(R.id.searchIdButton) void searchById() {
        deleteRealm();

        if (stopEntry.getText().toString().matches("")) {
            Toast.makeText(this, "Please enter a stop ID", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = String.format(predictionUrl, stopEntry.getText().toString())
                .concat(apiToken);

        Log.d("URL: ", url);

        hideUi(true);

        try {
            callApi(url, true);
        } catch (Exception e) { //TODO good exception catching
            Toast.makeText(this, getString(R.string.toast_something_wrong), Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.searchNearbyButton) void searchNearby() {
        deleteRealm();

        coords = HelpfulUtils.getCoords();

        String url = String.format(nearbyUrl, coords[0], coords[1], Integer.toString(500))
                .concat(apiToken);

        callApi(url, false);
    }


    //talk to AC Transit's API, do some stuff (update the UI with a listview, etc)
    //in the interest of just having one api method we take a boolean to
    //decide whether it's a nearby call or a stopid call
    //TODO make this neater
    private void callApi(String url, boolean isByStopId) {
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("ERROR! ", e.toString());
                handler.post(() -> {
                    Toast.makeText(MainActivity.this, "Something went wrong...",
                            Toast.LENGTH_SHORT).show();
                    hideUi(false);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseString = response.body().string();

                try {
                    if (isByStopId) {
                        JSONObject object = new JSONObject(responseString);
                        object = object.getJSONObject("bustime-response");
                        createPredictionList(object.getJSONArray("prd")); //the name of the array in ACT's json response
                    } else {
                        JSONArray array = new JSONArray(responseString);
                        createNearbyStopList(array);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {

                    if (isByStopId) {
                        handler.post(() -> {
                            hideUi(false);
                            final PredictionAdapter adapter = new PredictionAdapter(realm.where(Prediction.class)
                                    .equalTo("stpid", stopEntry.getText().toString())
                                    .findAll());
                            listView.setAdapter(adapter);
                        });
                    } else {
                        handler.post(()-> {
                            hideUi(false);
                            final NearbyAdapter adapter = new NearbyAdapter(realm.where(Stop.class)
                            .findAll());
                            listView.setAdapter(adapter);
                        });
                    }
                }
            }
        });
    }

    //hide or show loading indicator
    private void hideUi(Boolean isHidden) {
        if (isHidden) {
            listView.setVisibility(View.INVISIBLE);
            loadingBar.setVisibility(View.VISIBLE);
        } else {
            listView.setVisibility(View.VISIBLE);
            loadingBar.setVisibility(View.INVISIBLE);
        }
    }

    public void deleteRealm() {
        RealmResults<Prediction> realmResults = realm.where(Prediction.class)
                .findAll();

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realmResults.deleteAllFromRealm();
            }
        });
    }
}
