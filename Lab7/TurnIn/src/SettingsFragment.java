package michaelpeterson.lab7;


import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        // Restore preferences to reflect their stored value
        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        Resources resources = getResources();

        onSharedPreferenceChanged(sharedPreferences, resources.getString(R.string.preferenceKey_springCoilCount));
        onSharedPreferenceChanged(sharedPreferences, resources.getString(R.string.preferenceKey_springInitDisp));
        onSharedPreferenceChanged(sharedPreferences, resources.getString(R.string.preferenceKey_springStiffness));
        onSharedPreferenceChanged(sharedPreferences, resources.getString(R.string.preferenceKey_massShape));
    }

    @Override
    public void onResume(){
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause(){
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(sharedPreferences == null || key == null)
            return;
        try {
            Object preferenceValue = SharedPreferencesHelper.getPreferenceValue(getActivity(), key);
            if (preferenceValue != null)
                findPreference(key).setSummary("" + preferenceValue.toString());
        }
        catch(Throwable ex){
            Log.d("michaelpeterson.lab7", "onSharedPreferenceChanged()", ex);
        }
    }
}
