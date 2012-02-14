package de.mxtracks.android.anySMS;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.ActionBar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.MenuItem;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;

public class Settings extends FragmentActivity implements OnClickListener,
		OnCheckedChangeListener {
	public static final String PREFS_NAME = "MyPrefs";
	public static final String TAG = "anySMS Settings";
	private EditText etUser, etPassword, etAbsender;
	private Spinner spGateway;
	private Button btnSave;
	private CheckBox cbNotify;
	private ActionBar actionBar;

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
		String userGateway = settings.getString("userGateway", null);
		Boolean userNotify = settings.getBoolean("userNotify", false);

		if (userGateway.equalsIgnoreCase("20")) {
			spGateway.setSelection(1, true);
		}

		btnSave.setOnClickListener(this);
		etUser.setText(userID);
		etPassword.setText(userPass);
		etAbsender.setText(userAbsender);
		cbNotify.setChecked(userNotify);

		actionBar = getSupportActionBar();
		actionBar.setTitle("Zurück");
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);

	}

	@Override
	protected void onStop() {
		super.onStop();
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		String user = (etUser.getText().length() > 3) ? etUser.getText()
				.toString() : null;
		String pass = (etPassword.getText().length() > 3) ? etPassword
				.getText().toString() : null;
		String absender = (etAbsender.getText().length() > 3) ? etAbsender
				.getText().toString() : null;
		editor.putString("userID", user);
		editor.putString("userPass", pass);
		editor.putString("userAbsender", absender);
		editor.putString("userGateway", spGateway.getSelectedItem().toString());
		editor.commit();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnSave:
			Log.i(TAG, "Save settings");
			SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
			SharedPreferences.Editor editor = settings.edit();
			String user = (etUser.getText().length() > 3) ? etUser.getText()
					.toString() : null;
			String pass = (etPassword.getText().length() > 3) ? etPassword
					.getText().toString() : null;
			String absender = (etAbsender.getText().length() > 3) ? etAbsender
					.getText().toString() : null;
			editor.putString("userID", user);
			editor.putString("userPass", pass);
			editor.putString("userAbsender", absender);
			editor.putString("userGateway", spGateway.getSelectedItem()
					.toString());
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

	/* regiert auf klicks im Menü */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// app icon in action bar clicked; go home
			Intent intent = new Intent(this, AnySMSClientActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
