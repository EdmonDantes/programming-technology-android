package ru.loginov.chemistryapplication;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kotlin.Pair;
import ru.loginov.chemistryapplication.api.ChemistryApi;
import ru.loginov.chemistryapplication.api.PairDeserializer;
import ru.loginov.chemistryapplication.util.FileSystemUtils;

public class ChemistryApplication extends Application {

    public static final String TAG = "Chemistry";
    public static final ObjectMapper MAPPER = createObjectMapper();
    public static ChemistryApi API;
    public static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    public static final int DEFAULT_BUFFER_LENGTH = 4096;

    public static final SharedPreferences.OnSharedPreferenceChangeListener LISTENER = (sharedPreferences, key) -> updateApi(sharedPreferences);

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        updateApi(preferences);

        FileSystemUtils.clearCache(this);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    @Override
    public void onTerminate() {
        FileSystemUtils.clearCache(this);
        super.onTerminate();
    }

    public static void updateApi(SharedPreferences preferences) {
        String host = preferences.getString("server_host", "localhost");
        int port = Integer.parseInt(preferences.getString("server_port", "8080"));
        int timeout =  preferences.getInt("timeout", 1000);

        Log.i(TAG, "updateApi: Update chemistry api with host = " + host + ":" + port + " and timeout = " + timeout);

        API = new ChemistryApi(host, port, timeout);
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        SimpleModule module = new SimpleModule();

        module.addDeserializer(Pair.class, new PairDeserializer());
        objectMapper.registerModule(module);

        return objectMapper;
    }
}
