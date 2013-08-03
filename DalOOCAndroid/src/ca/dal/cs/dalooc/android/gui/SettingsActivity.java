package ca.dal.cs.dalooc.android.gui;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import ca.dal.cs.dalooc.android.R;
import ca.dal.cs.dalooc.model.User;

public class SettingsActivity extends PreferenceActivity {
	
	private User user; 
	
	protected Method mLoadHeaders = null;
    protected Method mHasHeaders = null;
 
    /**
     * Checks to see if using new v11+ way of handling PrefsFragments.
     * @return Returns false pre-v11, else checks to see if using headers.
     */
    public boolean isNewV11Prefs() {
        if (mHasHeaders!=null && mLoadHeaders!=null) {
            try {
                return (Boolean)mHasHeaders.invoke(this);
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            }
        }
        return false;
    }
 
	@SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle aSavedState) {
    	
    	this.user = (User)getIntent().getExtras().getSerializable(LoginActivity.ARG_USER);
    	
		SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {

			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				if (key.equals("pref_sign_in_automatically")) {
					if (sharedPreferences.getBoolean("pref_sign_in_automatically", false)) {
						sharedPreferences.edit().putString("sign_in_automatically_user_id", SettingsActivity.this.user.getId()).commit();
					} else {
						sharedPreferences.edit().remove("sign_in_automatically_user_id").commit();
					}
				}
			}
		};
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(listener);
    	
        //onBuildHeaders() will be called during super.onCreate()
        try {
            mLoadHeaders = getClass().getMethod("loadHeadersFromResource", int.class, List.class );
            mHasHeaders = getClass().getMethod("hasHeaders");
        } catch (NoSuchMethodException e) {
        }
        super.onCreate(aSavedState);
        if (!isNewV11Prefs()) {
            addPreferencesFromResource(R.xml.app_prefs_general);
        }
    }
 
    @Override
    public void onBuildHeaders(List<Header> aTarget) {
        try {
            mLoadHeaders.invoke(this,new Object[]{R.xml.pref_headers, aTarget});
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }  
    }
 
    static public class PrefsGeneralFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle aSavedState) {
            super.onCreate(aSavedState);
            Context anAct = getActivity().getApplicationContext();
            int thePrefRes = anAct.getResources().getIdentifier(getArguments().getString("pref-resource"), "xml",anAct.getPackageName());
            addPreferencesFromResource(thePrefRes);
        }
    }

}
