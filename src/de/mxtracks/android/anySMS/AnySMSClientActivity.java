package de.mxtracks.android.anySMS;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParser;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class AnySMSClientActivity extends Activity implements OnClickListener {
	public static final String PREFS_NAME = "MyPrefs";
	private static final int PICK_CONTACT_RES = 1;
	private static final String TAG = "AnySMS";
	private ImageButton btnGetUser;
    private Button btnSend;
    private Button btnReset;
    private TextView tvBalance;
    private TextView tvMessageLength;
    private EditText etEmpfaenger;
    private EditText etMessage;
    private String userID;
    private String userPass;
    private String userAbsender;
    private String userGateway;
    private String userBalance;
    private Integer lastCheck;
    private Integer timeStamp;
    protected ProgressDialog smsSendDialog;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        btnGetUser = (ImageButton) this.findViewById(R.id.btn_getUser);
        btnSend = (Button) this.findViewById(R.id.btnSend);
        btnReset = (Button) this.findViewById(R.id.btnReset);
        
        etEmpfaenger = (EditText) this.findViewById(R.id.et_empfaenger);
        etMessage = (EditText) this.findViewById(R.id.editText1);
        etMessage.addTextChangedListener(smsLengthWatcher);
        
        tvBalance = (TextView) this.findViewById(R.id.tvBalance);
        tvMessageLength = (TextView) this.findViewById(R.id.tvMessageLength);
        
        btnGetUser.setOnClickListener(this);
        btnSend.setOnClickListener(this);
        btnSend.setEnabled(false);
        btnReset.setOnClickListener(this);
        
        this.parseIntent(this.getIntent());
        
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	
    	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        userID = settings.getString("userID", "");
        userPass = settings.getString("userPass", "");
        userAbsender = settings.getString("userAbsender", "");
        userGateway = settings.getString("userGateway", "");
        userBalance = settings.getString("userBalance", "");
        lastCheck = settings.getInt("lastCheck", 0);
        
        timeStamp = (int) (System.currentTimeMillis() / 1000L);
        
        if (userID != "" && lastCheck + 86400 < timeStamp){
        	getBalance chk = new getBalance();
        	chk.execute(getString(R.string.uri_send_sms,userID,userPass,userGateway,"","","","","1"));
        }
        
        if (userBalance != ""){
        	tvBalance.setText(userBalance);
			tvBalance.setTextColor(Color.GREEN);
        }
    }
    
    private class getBalance extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... Uri) {
			StringBuilder builder = new StringBuilder();
			HttpClient client = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(Uri[0]);
			Log.i(TAG,Uri[0]);
			httpGet.setHeader("User-Agent", "Android ");
			String ret = "";
			try {
				HttpResponse response = client.execute(httpGet);
				StatusLine statusLine = response.getStatusLine();
				int statusCode = statusLine.getStatusCode();
				if (statusCode == 200) {
					HttpEntity entity = response.getEntity();
					InputStream content = entity.getContent();
					try {
						XmlPullParser parser = Xml.newPullParser();
						String name = null;
						BufferedReader reader = new BufferedReader(
								new InputStreamReader(content));
						String line;
						while ((line = reader.readLine()) != null) {
							builder.append(line);
						}
						String cont = builder.toString();
						// AnySMS hat so nen blöden header im XML, der muss raus!
						cont = cont.replace("<!DOCTYPE status SYSTEM \"http://gateway.any-sms.de/gateway.dtd\">","");
						cont = cont.replace("<!DOCTYPE status SYSTEM \"http://gateway.any-sms.biz/gateway.dtd\">","");
						Log.i(TAG,cont);
						parser.setInput(new StringReader (cont));
						int eventType = parser.getEventType();
						while (eventType != XmlPullParser.END_DOCUMENT) {
							switch (eventType) {
							case XmlPullParser.START_TAG:
								name = parser.getName().toLowerCase();
								if (name.equalsIgnoreCase("error")) {
									// nextText gibt den Inhalt des XML tags aus
									String error = parser.nextText();
									int errors = Integer.parseInt(error);
									switch (errors){
									case 0:
										// nothing, alles OK
										break;
									default:
										return "ERROR"+error;
									}
									Log.i(TAG, "Fehler: " + error);
								}
								if (name.equalsIgnoreCase("guthaben")) {
									// nextText gibt den Inhalt des XML tags aus
									ret = parser.nextText();
								}
								break;
							}
							eventType = parser.next();
						}
						
					}
					catch (Exception e) {
						e.printStackTrace();
					}
					
				} else {
					Log.e(TAG, "Failed to download file");
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				return "ERROR";
			} catch (IOException e) {
				e.printStackTrace();
				return "ERROR";
			}
			return ret;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		@Override
		protected void onCancelled() {
			super.onCancelled();
		}
		@Override
		protected void onPostExecute(String result) {
			if (result.contains("ERROR")) {
				tvBalance.setText(getString(R.string.not_connected));
				tvBalance.setTextColor(Color.RED);
				SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
			    SharedPreferences.Editor editor = settings.edit();
			    editor.putString("userBalance", "");
			    // Commit the edits!
			    editor.commit();
				try {
					String error = result.replace("ERROR", "");
					int errors = Integer.parseInt(error);
					switch (errors){
					case -1:
						Toast.makeText(AnySMSClientActivity.this, R.string.error1, Toast.LENGTH_LONG).show();
						break;
					case -5:
						Toast.makeText(AnySMSClientActivity.this, R.string.error5, Toast.LENGTH_LONG).show();
						break;
					default:
						Toast.makeText(AnySMSClientActivity.this, "Es trat ein Fehler auf\nFehlernummer: " + result, Toast.LENGTH_LONG).show();
						break;
					}
				}
				catch (Exception e){
					Toast.makeText(AnySMSClientActivity.this, "Es trat ein Fehler auf\nFehlernummer: " + result, Toast.LENGTH_LONG).show();
				}
			}
			else {
				timeStamp = (int) (System.currentTimeMillis() / 1000L);
				SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
			    SharedPreferences.Editor editor = settings.edit();
			    editor.putString("userBalance", result);
			    editor.putInt("lastCheck", timeStamp);
			    // Commit the edits!
			    editor.commit();
				tvBalance.setText(result);
				tvBalance.setTextColor(Color.GREEN);
			}
		}
	}

    private class sendSMS extends AsyncTask<String, Void, String> {
    	private String smsPreis = "";
		@Override
		protected String doInBackground(String... Uri) {
			StringBuilder builder = new StringBuilder();
			HttpClient client = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(Uri[0]);
			Log.i(TAG,Uri[0]);
			httpGet.setHeader("User-Agent", "Android ");
			String ret = "";
			try {
				HttpResponse response = client.execute(httpGet);
				StatusLine statusLine = response.getStatusLine();
				int statusCode = statusLine.getStatusCode();
				if (statusCode == 200) {
					HttpEntity entity = response.getEntity();
					InputStream content = entity.getContent();
					try {
						XmlPullParser parser = Xml.newPullParser();
						String name = null;
						BufferedReader reader = new BufferedReader(
								new InputStreamReader(content));
						String line;
						while ((line = reader.readLine()) != null) {
							builder.append(line);
						}
						String cont = builder.toString();
						// AnySMS hat so nen blöden header im XML, der muss raus!
						cont = cont.replace("<!DOCTYPE status SYSTEM \"http://gateway.any-sms.de/gateway.dtd\">","");
						cont = cont.replace("<!DOCTYPE status SYSTEM \"http://gateway.any-sms.biz/gateway.dtd\">","");
						Log.i(TAG,cont);
						parser.setInput(new StringReader (cont));
						int eventType = parser.getEventType();
						while (eventType != XmlPullParser.END_DOCUMENT) {
							switch (eventType) {
							case XmlPullParser.START_TAG:
								name = parser.getName().toLowerCase();
								if (name.equalsIgnoreCase("error")) {
									// nextText gibt den Inhalt des XML tags aus
									String error = parser.nextText();
									int errors = Integer.parseInt(error);
									switch (errors){
									case 0:
										// nothing, alles OK
										break;
									default:
										return "ERROR"+error;
									}
									Log.i(TAG, "Fehler: " + error);
								}
								if (name.equalsIgnoreCase("preis")) {
									// nextText gibt den Inhalt des XML tags aus
									smsPreis = parser.nextText();
								}
								if (name.equalsIgnoreCase("guthaben")) {
									// nextText gibt den Inhalt des XML tags aus
									ret = parser.nextText();
								}
								break;
							}
							eventType = parser.next();
						}
						
					}
					catch (Exception e) {
						e.printStackTrace();
					}
					
				} else {
					Log.e(TAG, "Failed to download file");
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				return "ERROR";
			} catch (IOException e) {
				e.printStackTrace();
				return "ERROR";
			}
			return ret;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			smsSendDialog = ProgressDialog.show(AnySMSClientActivity.this,"Sende SMS", "Bitte warten...", true);
		}
		@Override
		protected void onCancelled() {
			super.onCancelled();
			smsSendDialog.dismiss();
		}
		@Override
		protected void onPostExecute(String result) {
			smsSendDialog.dismiss();
			if (result.contains("ERROR")) {
				tvBalance.setText(getString(R.string.not_connected));
				tvBalance.setTextColor(Color.RED);
				SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
			    SharedPreferences.Editor editor = settings.edit();
			    editor.putString("userBalance", "");
			    // Commit the edits!
			    editor.commit();
				try {
					String error = result.replace("ERROR", "");
					int errors = Integer.parseInt(error);
					switch (errors){
					case -1:
						Toast.makeText(AnySMSClientActivity.this, R.string.error1, Toast.LENGTH_LONG).show();
						break;
					case -5:
						Toast.makeText(AnySMSClientActivity.this, R.string.error5, Toast.LENGTH_LONG).show();
						break;
					default:
						Toast.makeText(AnySMSClientActivity.this, "Es trat ein Fehler auf\nFehlernummer: " + result, Toast.LENGTH_LONG).show();
						break;
					}
				}
				catch (Exception e){
					Toast.makeText(AnySMSClientActivity.this, "Es trat ein Fehler auf\nFehlernummer: " + result, Toast.LENGTH_LONG).show();
				}
			}
			else {
				timeStamp = (int) (System.currentTimeMillis() / 1000L);
				SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
			    SharedPreferences.Editor editor = settings.edit();
			    editor.putString("userBalance", result);
			    editor.putInt("lastCheck", timeStamp);
			    // Commit the edits!
			    editor.commit();
				tvBalance.setText(result);
				tvBalance.setTextColor(Color.GREEN);
				String message = etMessage.getText().toString();
				String nummer = etEmpfaenger.getText().toString();
				etEmpfaenger.setText("");
				etMessage.setText("");
				btnSend.setEnabled(false);
				
				// SMS ins Sent Folder packen
				ContentValues values = new ContentValues();
				values.put("address", nummer);
				values.put("body", message);
				getContentResolver().insert(Uri.parse("content://sms/sent"), values);
				
				Toast.makeText(AnySMSClientActivity.this, "SMS verschickt.\nPreis: " + smsPreis, Toast.LENGTH_LONG).show();
			}
		}
	}
    
    
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sms_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    /* regiert auf klicks im Menü */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.btnSettings:
			/* ruft die settings activity auf */
			final Intent setIntent = new Intent(this,
					de.mxtracks.android.anySMS.Settings.class);
			startActivity(setIntent);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()){
		case R.id.btn_getUser:
			pickContact();
			break;
		case R.id.btnReset:
			etEmpfaenger.setText("");
			etMessage.setText("");
			btnSend.setEnabled(false);
			break;
		case R.id.btnSend:
			if (etMessage.getText().length() >= 1){
				String message = etMessage.getText().toString();
				try {
					message = URLEncoder.encode(message, "ISO-8859-15");
				} catch (UnsupportedEncodingException uee) { uee.getMessage(); }
				String nummer = etEmpfaenger.getText().toString();
				nummer = nummer.replace("+49","0");
				nummer = nummer.replaceAll(" ", "");
				String sLong = "0";
				if (etMessage.getText().length() > 160){
					sLong = "1";
				}
				sendSMS sms = new sendSMS();
	        	sms.execute(getString(R.string.uri_send_sms,userID,userPass,userGateway,message,nummer,userAbsender,sLong,"0"));
			}
			break;
		}
		
	}
	
	protected void pickContact() {
		try {
	        Intent intent = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
	        startActivityForResult(intent, PICK_CONTACT_RES);
	    } catch (Exception e) {
	            e.printStackTrace();
	      }

    }
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (resultCode == RESULT_OK) {  
	        switch (requestCode) {  
	        case PICK_CONTACT_RES:
	        	Uri result = data.getData();  
	        	Log.i(TAG, "Got a result: "  
	        	    + result.toString());
	        	// get the contact id from the Uri  
	        	String contactId = result.getLastPathSegment();
	        	List<String> allNumbers = new ArrayList<String>();
	        	String phoneNumber = "";
	        	try {
	        		Cursor cursor = getContentResolver().query(result,null, null, null, null);
	        		cursor.moveToFirst();
	        	    Cursor phones = getContentResolver().query(Phone.CONTENT_URI, null, Phone.CONTACT_ID +"=?", new String[]{contactId}, null); 
	        	    while (phones.moveToNext()) {
	        	      phoneNumber = phones.getString(phones.getColumnIndex( ContactsContract.CommonDataKinds.Phone.NUMBER));
	        	      Log.i(TAG,"Fone: " + phoneNumber);
	        	      allNumbers.add(phoneNumber);
	        	    }
	        	    cursor.close();
		        	phones.close(); 
		        	
	        	}
	        	catch(Exception e) {
	        		e.printStackTrace();
	        	}
	        	finally {
	        		final CharSequence[] items = allNumbers.toArray(new String[allNumbers.size()]);
	                AlertDialog.Builder builder = new AlertDialog.Builder(AnySMSClientActivity.this);
	                builder.setTitle("Wähle eine Nummer:");
	                builder.setItems(items, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int item) {
	                        String selectedNumber = items[item].toString();
	                        etEmpfaenger.setText(selectedNumber);
	                        btnSend.setEnabled(true);
	                    }
	                });
	                AlertDialog alert = builder.create();
	                if(allNumbers.size() > 1) {
	                    alert.show();
	                } else {
	                    String selectedNumber = phoneNumber.toString();
	                    etEmpfaenger.setText(selectedNumber);
	                    btnSend.setEnabled(true);
	                }
	                if (phoneNumber.length() == 0) {  
	                	Toast.makeText(AnySMSClientActivity.this, "Der Kontakt hat keine Telefonnummer", Toast.LENGTH_SHORT).show(); 
	                }  

	        	}
	        	
	        	
	            break;  
	        }  
	    } else {
	       //activity result error actions
	    }  
	}

	private final TextWatcher smsLengthWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        	tvMessageLength.setText(String.valueOf(s.length()));
        }
        @Override
        public void afterTextChanged(Editable s) {
        }

	};

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected final void onNewIntent(final Intent intent) {
		super.onNewIntent(intent);
		this.parseIntent(intent);
	}
	
	/**
	 * Parse data pushed by {@link Intent}.
	 * 
	 * @param intent
	 *            {@link Intent}
	 */
	private void parseIntent(final Intent intent) {
		final String action = intent.getAction();
		Log.d(TAG, "Startet with Intent ACTION: " + action);
		if (action == null) {
			return;
		}
		final Uri uri = intent.getData();
		Log.i(TAG, "Intent Uri: " + uri);
		if (uri != null && uri.toString().length() > 0) {
			// launched by clicking a sms icon, number is in URI.
			final String scheme = uri.getScheme();
			if (scheme != null) {
				if (scheme.equals("sms") || scheme.equals("smsto")) {
					final String recipient = uri.getSchemeSpecificPart();
					Log.i(TAG,"Recipient: " + recipient);
					etEmpfaenger.setText(recipient);
					btnSend.setEnabled(true);
				}
			}
			String smsBody = intent.getStringExtra(Intent.EXTRA_TEXT);
			if (smsBody == null) {
				smsBody = intent.getStringExtra("sms_body");
				Log.i(TAG,"sms_body: " + smsBody);
			}
			if (smsBody == null) {
				final Uri stream = (Uri) intent
						.getParcelableExtra(Intent.EXTRA_STREAM);
				if (stream != null) {
					try {
						InputStream is = this.getContentResolver().openInputStream(
								stream);
						final BufferedReader r = new BufferedReader(
								new InputStreamReader(is));
						StringBuffer sb = new StringBuffer();
						String line;
						while ((line = r.readLine()) != null) {
							sb.append(line + "\n");
						}
						smsBody = sb.toString().trim();
					} catch (IOException e) {
						Log.e(TAG, "IO ERROR", e);
					}
	
				}
				Log.i(TAG,"EXTRA_STREAM: " + smsBody);
			}
			if (smsBody != null) {
				etMessage.setText(smsBody);
			}
		}
	}
}