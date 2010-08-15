package com.tarunrs.movies;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class EditPreferences extends PreferenceActivity{
	public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	addPreferencesFromResource(R.xml.preferences);
	}
}
