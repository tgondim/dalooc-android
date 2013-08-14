package ca.dal.cs.dalooc.android.gui;

import org.bson.types.ObjectId;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import ca.dal.cs.dalooc.android.R;
import ca.dal.cs.dalooc.model.User;
import ca.dal.cs.dalooc.webservice.util.Parser;

public class CreateUserActivity extends Activity {

	public static final String ARG_EMAIL = "email";
	
	private EditText etFirstName;
	
	private EditText etLastName;
	
	private EditText etEmail;
	
	private EditText etPassword;
	
	private EditText etRetypePassword;
	
	private Spinner spnUserType;
	
	private ArrayAdapter<User.UserType> userTypeAdapter;
	
	private View llRegisterStatus;

	private View svRegisterForm;
	
	private TextView tvRegisterStatusMessage;
	
	private Button btnRegister;
	
	private UserRegisterTask userRegisterTask;
	
	private Toast toast;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_user);
		
		this.svRegisterForm = (View)findViewById(R.id.svRegisterForm);
		this.llRegisterStatus = (View)findViewById(R.id.llRegisterStatus);
		this.tvRegisterStatusMessage = (TextView)findViewById(R.id.tvRegisterStatusMessage);
		
		this.etFirstName = (EditText)findViewById(R.id.etFirstName);
		this.etLastName = (EditText)findViewById(R.id.etLastName);
		this.etEmail = (EditText)findViewById(R.id.etEmail);
		this.etPassword = (EditText)findViewById(R.id.etPassword);
		this.etRetypePassword = (EditText)findViewById(R.id.etRetypePassword);
		
		this.spnUserType = (Spinner)findViewById(R.id.spnUserType);
		this.userTypeAdapter = new ArrayAdapter<User.UserType>(this, android.R.layout.simple_spinner_item, User.UserType.values());
		this.userTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		this.spnUserType .setAdapter(this.userTypeAdapter);	
		this.spnUserType.setSelection(0);
		
		String email = (String)getIntent().getExtras().getSerializable(ARG_EMAIL);
		
		if (email != null) {
			this.etEmail.setText(email);
		}
		
		this.btnRegister = (Button)findViewById(R.id.btnRegister);
		this.btnRegister.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (CreateUserActivity.this.userRegisterTask != null) {
					return;
				}

				// Reset errors.
				CreateUserActivity.this.etEmail.setError(null);
				CreateUserActivity.this.etPassword.setError(null);

				// Store values at the time of the login attempt.
				String mFirstName = CreateUserActivity.this.etFirstName.getText().toString();
				String mLastName = CreateUserActivity.this.etLastName.getText().toString();
				String mEmail = CreateUserActivity.this.etEmail.getText().toString();
				String mPassword = CreateUserActivity.this.etPassword.getText().toString();
				String mRetypePassword = CreateUserActivity.this.etRetypePassword.getText().toString();

				boolean cancel = false;
				View focusView = null;

				// Check for a valid firstName
				if (TextUtils.isEmpty(mFirstName)) {
					CreateUserActivity.this.etFirstName.setError(getString(R.string.error_field_required));
					focusView = CreateUserActivity.this.etFirstName;
					cancel = true;
				}
				
				// Check for a valid lastName
				if (TextUtils.isEmpty(mLastName)) {
					CreateUserActivity.this.etLastName.setError(getString(R.string.error_field_required));
					focusView = CreateUserActivity.this.etLastName;
					cancel = true;
				}
				
				// Check for a valid password.
				if (TextUtils.isEmpty(mPassword)) {
					CreateUserActivity.this.etPassword.setError(getString(R.string.error_field_required));
					focusView = CreateUserActivity.this.etPassword;
					cancel = true;
				} else if (mPassword.length() < 4) {
					CreateUserActivity.this.etPassword.setError(getString(R.string.error_invalid_password));
					focusView = CreateUserActivity.this.etPassword;
					cancel = true;
				}
				
				// Check for a valid retypePassword.
				if (TextUtils.isEmpty(mRetypePassword)) {
					CreateUserActivity.this.etRetypePassword.setError(getString(R.string.error_field_required));
					focusView = CreateUserActivity.this.etRetypePassword;
					cancel = true;
				} else if (mRetypePassword.length() < 4) {
					CreateUserActivity.this.etRetypePassword.setError(getString(R.string.error_invalid_password));
					focusView = CreateUserActivity.this.etRetypePassword;
					cancel = true;
				}

				// Check for a valid email address.
				if (TextUtils.isEmpty(mEmail)) {
					CreateUserActivity.this.etEmail.setError(getString(R.string.error_field_required));
					focusView = CreateUserActivity.this.etEmail;
					cancel = true;
				} else if (!mEmail.contains("@")) {
					CreateUserActivity.this.etEmail.setError(getString(R.string.error_invalid_email));
					focusView = CreateUserActivity.this.etEmail;
					cancel = true;
				}
				
				// Check if mPassword and mRetypePassword are equal.
				if (!mPassword.equals(mRetypePassword)) {
					CreateUserActivity.this.etRetypePassword.setError(getString(R.string.error_passwords_mismatch));
					focusView = CreateUserActivity.this.etRetypePassword;
					cancel = true;
				}

				if (cancel) {
					// There was an error; don't attempt send registration and focus the first
					// form field with an error.
					focusView.requestFocus();
				} else {
					// Show a progress spinner, and kick off a background task to
					// perform the user login attempt.
					User newUser = new User(new ObjectId().toString(), 
							CreateUserActivity.this.etFirstName.getText().toString(), 
							CreateUserActivity.this.etLastName.getText().toString(), 
							(User.UserType)CreateUserActivity.this.spnUserType.getSelectedItem(), 
							CreateUserActivity.this.etEmail.getText().toString(),  
							CreateUserActivity.this.etPassword.getText().toString().toCharArray(), 
							false);
					CreateUserActivity.this.tvRegisterStatusMessage.setText(R.string.registering_user);
					showProgress(true);
					CreateUserActivity.this.userRegisterTask = new UserRegisterTask();
					CreateUserActivity.this.userRegisterTask.execute(newUser);
				}
			}
		});
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.create_user, menu);
		return true;
	}
	
	private void showToast(String msg) {
		if (this.toast == null) {
			this.toast = Toast.makeText(this, "", Toast.LENGTH_LONG);
		}
		this.toast.setText(msg);
		this.toast.show();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			this.llRegisterStatus.setVisibility(View.VISIBLE);
			this.llRegisterStatus.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							CreateUserActivity.this.llRegisterStatus.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			this.svRegisterForm.setVisibility(View.VISIBLE);
			this.svRegisterForm.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							CreateUserActivity.this.svRegisterForm.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			this.llRegisterStatus.setVisibility(show ? View.VISIBLE : View.GONE);
			this.svRegisterForm.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}
	
	/**
	 * Represents an asynchronous registration task used to register
	 * a new user.
	 */
	public class UserRegisterTask extends AsyncTask<User, Void, String> {
		
		public static final String ERROR = "ERROR";
		public static final String EMAIL_UNAVAILABLE = "EMAIL_UNAVAILABLE";
		public static final String USER_CREATED = "USER_CREATED";
		
		private User user;
		
		@Override
		protected String doInBackground(User... params) {
			
			this.user = params[0];
			
			SoapObject soap = null;

			// soap.addProperty("chave",
			// ((Activity)this.callBack).getResources().getString(R.string.chave_dalooc_webservice));

			soap = new SoapObject(CreateUserActivity.this.getResources().getString(R.string.namespace_webservice),
					CreateUserActivity.this.getResources().getString(R.string.register_user_webservice_operation));
			soap.addProperty("userString", Parser.getUserDBObject(this.user).toString());	

			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
					SoapEnvelope.VER11);

			envelope.setOutputSoapObject(soap);
			Log.d("UserRegisterTask", "Calling DalOOCWebServices.registerUser");

			HttpTransportSE httpTransport = new HttpTransportSE(getUrlWebService());
			String registerUserResult = ERROR;

			try {
				httpTransport.call("", envelope);
				if (envelope.bodyIn instanceof SoapObject) {
					SoapObject results = (SoapObject) envelope.bodyIn;
					int count = results.getPropertyCount();
					if (count != 0) {
						SoapPrimitive sp = (SoapPrimitive)results.getProperty(0);
						if (sp.toString().equals(USER_CREATED)) {
							registerUserResult = USER_CREATED;
						} else if (sp.toString().equals(EMAIL_UNAVAILABLE)) {
							registerUserResult = EMAIL_UNAVAILABLE;
						}
					}
				} else if (envelope.bodyIn instanceof SoapFault) {
					SoapFault results = (SoapFault) envelope.bodyIn;
					Log.e("UserRegisterTask", results.getLocalizedMessage());
					//TODO deal when registration webservice is not available
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			return registerUserResult;
		}

		@Override
		protected void onPostExecute(final String registerUserResult) {
			CreateUserActivity.this.userRegisterTask = null;
			showProgress(false);

			if (registerUserResult.equals(USER_CREATED)) {
				showToast(getString(R.string.user_registered));
				finish();
			} else if (registerUserResult.equals(EMAIL_UNAVAILABLE)) {
				CreateUserActivity.this.etEmail.setError(getString(R.string.error_email_unavailable));
				CreateUserActivity.this.etEmail.requestFocus();			
			} else if (registerUserResult.equals(ERROR)) {
				showToast(getString(R.string.error_server_not_responding));
			}
		}

		@Override
		protected void onCancelled() {
			CreateUserActivity.this.userRegisterTask = null;
			showProgress(false);
		}
		
		private String getUrlWebService() {
			StringBuilder sb = new StringBuilder();
			sb.append(getResources().getString(R.string.url_webservice));
			sb.append("/");
			sb.append(getResources().getString(R.string.user_repository));
			sb.append("/");
			sb.append(getResources().getString(R.string.register_user_webservice_operation));
			
			return sb.toString(); 
		}
	}
}
