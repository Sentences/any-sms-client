package de.mxtracks.android.anySMS;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;

public class Settings extends Activity implements OnClickListener,
		OnCheckedChangeListener {
	public static final String PREFS_NAME = "MyPrefs";
	public static final String TAG = "anySMS Settings";
	private EditText etUser;
	private EditText etPassword;
	private EditText etAbsender;
	private Spinner spGateway;
	private Button btnSave;
	private CheckBox cbNotify;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);

		etUser = (EditText) this.findViewById(R.id.etUser);
		etPassword = (EditText) this.findViewById(R.id.etPassword);
		etAbsender = (EditText) this.findViewById(R.id.etAbsender);
		spGateway = (Spinner) this.findViewById(R.id.spinner1);
		btnSave = (Button) this.findViewById(R.id.btnSave);
		cbNotify = (CheckBox) this.findViewById(R.id.cbNotify);

		cbNotify.setOnCheckedChangeListener(this);

		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		String userID = settings.getString("userID", null);
		String userPass = settings.getString("userPass", null);
		String userAbsender = settings.getString("userAbsender", null);
		Boolean userNotify = settings.getBoolean("userNotify", false);

		btnSave.setOnClickListener(this);
		etUser.setText(userID);
		etPassword.setText(userPass);
		etAbsender.setText(userAbsender);
		cbNotify.setChecked(userNotify);
	}

	@Override
	protected void onPause() {
		super.onStop();

		// We need an Editor object to make preference changes.
		// All objects are from android.context.Context
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("userID", etUser.getText().toString());
		editor.putString("userPass", etPassword.getText().toString());
		editor.putString("userAbsender", etAbsender.getText().toString());
		editor.putString("userGateway", spGateway.getSelectedItem().toString());
		// Commit the edits!
		editor.commit();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnSave:
			Log.i(TAG, "Save settings");
			// We need an Editor object to make preference changes.
			// All objects are from android.context.Context
			SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString("userID", etUser.getText().toString());
			editor.putString("userPass", etPassword.getText().toString());
			editor.putString("userAbsender", etAbsender.getText().toString());
			editor.putString("userGateway", spGateway.getSelectedItem()
					.toString());
			// Commit the edits!
			editor.commit();
			break;

		}

	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		switch (buttonView.getId()) {
		case R.id.cbNotify:
			SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean("userNotify", isChecked);
			editor.commit();
			break;

		}
	}

}
