package de.mxtracks.android.anySMS;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.Window;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class Info extends Activity implements OnClickListener {
	private ImageButton btnClose; 
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.info);
		
		btnClose = (ImageButton) this.findViewById(R.id.btnClose);
		btnClose.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
		case R.id.btnClose:
			finish();
			break;
		}
		
	}
	
}
