package com.gsdp.opsecurity;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.text.InputFilter;
import android.text.Spanned;
import android.widget.EditText;

public class UserSettings extends PreferenceActivity 
implements OnSharedPreferenceChangeListener {

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		for(int i=0; i<getPreferenceScreen().getPreferenceCount();i++){
			initSummary(getPreferenceScreen().getPreference(i));
		}
		
		final EditText edit = ((EditTextPreference)findPreference("pref_siteName")).getEditText();
		InputFilter filter = new InputFilter(){
			public CharSequence filter(CharSequence source, int start, int end,
					Spanned dest, int dstart, int dend){
				for (int i = start; i < end; i++){
					if ((i == start)&&(Character.isSpaceChar(source.charAt(i)))){
						edit.setError("You cannot start with a space character");
						return " ";
					}
					if(!((Character.isLetterOrDigit(source.charAt(i))) || (Character.isSpaceChar(source.charAt(i))))){
						edit.setError("Character is invalid. Use only letters, numbers and spaces");
						return "";	
					}
				}
				return null;
			}
		};
		InputFilter[] filterArray = new InputFilter[2];
		filterArray[0] = new InputFilter.LengthFilter(18); //Max length =18
		filterArray[1] = filter; // Characters can only be A-z and 0-9 and " "
		edit.setFilters(filterArray);
	}
	
    @Override
    protected void onResume() {
        super.onResume();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {
        updatePrefSummary(findPreference(key));
    }

    private void initSummary(Preference p) {
        if (p instanceof PreferenceCategory) {
            PreferenceCategory pCat = (PreferenceCategory) p;
            for (int i = 0; i < pCat.getPreferenceCount(); i++) {
                initSummary(pCat.getPreference(i));
            }
        } else {
            updatePrefSummary(p);
        }
    }

    private void updatePrefSummary(Preference p) {
        if (p instanceof ListPreference) {
            ListPreference listPref = (ListPreference) p;
            p.setSummary(listPref.getEntry());
        }
        if (p instanceof EditTextPreference) {
            EditTextPreference editTextPref = (EditTextPreference) p;
            p.setSummary(editTextPref.getText());
        }
    }
	
}