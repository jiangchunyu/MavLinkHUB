package com.paku.mavlinkhub.fragments;

import com.paku.mavlinkhub.R;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class FragmentSettings extends PreferenceFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);
	}

}