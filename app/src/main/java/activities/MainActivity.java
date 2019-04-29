package activities;

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

import com.taitsmith.busboy.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.exceptions.RealmMigrationNeededException;
import obj.Bus;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import utils.BusAdapter;

import static viewmodels.MainActivityViewModel.createBusList;


public class MainActivity extends AppCompatActivity {
    @BindView(R.id.stopEntryEditText)
    EditText stopEntry;
    @BindView(R.id.searchButton)
    Button searchButton;
    @BindView(R.id.busListView)
    ListView busListView;
    @BindView(R.id.loadingBar)
    ProgressBar loadingBar;

    @BindString(R.string.base_url)
    String baseUrl;
    @BindString(R.string.api_token)
    String apiToken;

    OkHttpClient client;
    Realm realm;

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        client = new OkHttpClient();
        handler = new Handler(Looper.getMainLooper());
        try {
            realm = Realm.getDefaultInstance();
        } catch (RealmMigrationNeededException e) {
            RealmConfiguration configuration = new RealmConfiguration.Builder()
                    .deleteRealmIfMigrationNeeded()
                    .build();
        }

        deleteRealm();
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
    }

    @OnClick(R.id.searchButton) void search() {
        deleteRealm();

        if (stopEntry.getText().toString().matches("")) {
            Toast.makeText(this, "Please enter a stop ID", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = String.format(baseUrl, stopEntry.getText().toString())
                .concat(apiToken);

        hideUi(true);

        try {
            callApi(url);
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.something_wrong_toast), Toast.LENGTH_SHORT).show();
        }
    }


    //talk to AC Transit's API, do some stuff (update the UI with a listview, etc)
    //TODO clean and move
    private void callApi(String url) {
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
                return;
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseString = response.body().string();
                try {
                    JSONArray array = new JSONArray(responseString);
                    createBusList(array);
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    handler.post(() -> {
                        hideUi(false);
                        final BusAdapter adapter = new BusAdapter(realm.where(Bus.class)
                                .equalTo("StopId", "55555")
                                .findAll());
                        busListView.setAdapter(adapter);
                    });
                }
            }
        });
    }

    //hide or show loading indicator
    private void hideUi(Boolean isHidden) {
        if (isHidden) {
            busListView.setVisibility(View.INVISIBLE);
            loadingBar.setVisibility(View.VISIBLE);
        } else {
            busListView.setVisibility(View.VISIBLE);
            loadingBar.setVisibility(View.INVISIBLE);
        }
    }

    public void deleteRealm() {
        RealmResults<Bus> realmResults = realm.where(Bus.class)
                .findAll();

        Realm.Transaction transaction = new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realmResults.deleteAllFromRealm();
            }
        };
    }
}