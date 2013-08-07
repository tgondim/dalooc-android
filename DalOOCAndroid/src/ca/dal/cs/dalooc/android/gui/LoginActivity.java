package ca.dal.cs.dalooc.android.gui;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import ca.dal.cs.dalooc.android.R;
import ca.dal.cs.dalooc.android.gui.components.ConfirmDialog;
import ca.dal.cs.dalooc.android.gui.listener.OnConfirmDialogReturnListener;
import ca.dal.cs.dalooc.model.User;
import ca.dal.cs.dalooc.webservice.util.Parser;

import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends FragmentActivity implements OnConfirmDialogReturnListener {
	
	public static final String ARG_USER =  "user";
	
	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;

	// Values for email and password at the time of the login attempt.
	private String mEmail;
	private String mPassword;

	// UI references.
	private EditText mEmailView;
	private EditText mPasswordView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;

	private ConfirmDialog confirmDialog;
	
	@SuppressLint("HandlerLeak")
	private Handler callBackHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case UserLoginTask.USER_NOT_REGISTERED:
				resetFieldErrors();
				showProgress(false);
				getConfirmDialog().show(getSupportFragmentManager(), "fragment_edit_name");
				break;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);
		
		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);
		
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

		String userId = null;
		
		if (pref.getBoolean("pref_sign_in_automatically", false)) {
			userId = pref.getString("sign_in_automatically_user_id", "");
			if (userId != null && !userId.equals("")) {
				// Show a progress spinner, and kick off a background task to
				// perform the user login attempt.
				mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
				showProgress(true);
				mAuthTask = new UserLoginTask();
				mAuthTask.execute(UserLoginTask.AUTOMATICALLY_LOGIN_USER, userId);
			}
		}

		if (userId == null) {
			mLoginFormView.setVisibility(View.VISIBLE);
		}

		// Set up the login form.
//		mEmail = EXTRA_EMAIL;
//		mPassword = PASSWORD;
		mEmailView = (EditText) findViewById(R.id.email);
//		mEmailView.setText(mEmail);
		
		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
						@Override
						public boolean onEditorAction(TextView textView, int id,
								KeyEvent keyEvent) {
							if (id == R.id.login || id == EditorInfo.IME_NULL) {
								attemptLogin();
								return true;
							}
							return false;
						}
					});
		
		findViewById(R.id.sign_in_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptLogin();
					}
				});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		
		if (mAuthTask != null) {
			return;
		}

		resetFieldErrors();

		// Store values at the time of the login attempt.
		mEmail = mEmailView.getText().toString();
		mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		} else if (mPassword.length() < 4) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(mEmail)) {
			mEmailView.setError(getString(R.string.error_field_required));
			focusView = mEmailView;
			cancel = true;
		} else if (!mEmail.contains("@")) {
			mEmailView.setError(getString(R.string.error_invalid_email));
			focusView = mEmailView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			showProgress(true);
			mAuthTask = new UserLoginTask();
			mAuthTask.execute(UserLoginTask.VALIDATE_USER, this.mEmail, this.mPassword);
		}
	}

	private void resetFieldErrors() {
		// Reset errors.
		mEmailView.setError(null);
		mPasswordView.setError(null);
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}
	
	private ConfirmDialog getConfirmDialog() {
		if (this.confirmDialog == null) {	
			Bundle args = new Bundle();
			args.putString(ConfirmDialog.ARG_MESSAGE, getResources().getString(R.string.want_to_do_registration));
			args.putBoolean(ConfirmDialog.ARG_CANCEL_BUTTON, false);
			
			this.confirmDialog = new ConfirmDialog();
			this.confirmDialog.setArguments(args);
			this.confirmDialog.setOnConfirmDialogResultListener(this);
		}
		return this.confirmDialog;
	}

	@Override
	public void onConfirmDialogReturn(boolean confirm, int returnCode) {
		if (confirm) {
			Intent createUserIntent = new Intent(LoginActivity.this, CreateUserActivity.class);
			createUserIntent.putExtra(CreateUserActivity.ARG_EMAIL, this.mEmailView.getText().toString());
			startActivity(createUserIntent);
		}
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserLoginTask extends AsyncTask<String, Void, User> {
		
		public static final String AUTOMATICALLY_LOGIN_USER = "get_user";

		public static final String VALIDATE_USER = "validate_user";
		
		public static final int USER_NOT_REGISTERED = 1;
		
		private User user;
		
		private String userId;
		
		private String email;
		
		private String password;

		private String requestType;
		
		@Override
		protected User doInBackground(String... params) {
			
			this.requestType = params[0];
			
			SoapObject soap = null;

			// soap.addProperty("chave",
			// ((Activity)this.callBack).getResources().getString(R.string.chave_dalooc_webservice));

			if (this.requestType.equals(VALIDATE_USER)) {
				this.email = params[1];
				this.password = params[2];
				soap = new SoapObject(LoginActivity.this.getResources().getString(R.string.namespace_webservice),
						LoginActivity.this.getResources().getString(R.string.validate_user_webservice_operation));
				soap.addProperty("email", this.email);	
			} else if (this.requestType.equals(AUTOMATICALLY_LOGIN_USER)) {
				this.userId = params[1];
				soap = new SoapObject(LoginActivity.this.getResources().getString(R.string.namespace_webservice),
						LoginActivity.this.getResources().getString(R.string.get_user_webservice_operation));
				soap.addProperty("userId", this.userId);	
			}

			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
					SoapEnvelope.VER11);

			envelope.setOutputSoapObject(soap);
			String urlWebService = getUrlWebService(this.requestType);
			Log.d("UserLoginTask", "Calling DalOOCWebServices.validateUser at: " + urlWebService);

			HttpTransportSE httpTransport = new HttpTransportSE(urlWebService);
			this.user = null;

			try {
				httpTransport.call("", envelope);
				if (envelope.bodyIn instanceof SoapObject) {
					SoapObject results = (SoapObject) envelope.bodyIn;
					int count = results.getPropertyCount();
					if (count != 0) {
						SoapPrimitive sp = (SoapPrimitive)results.getProperty(0);
						if (!sp.toString().equals("{}")) {
							this.user = Parser.getUserObject((BasicDBObject)JSON.parse(sp.toString()));
						}
					}
				} else if (envelope.bodyIn instanceof SoapFault) {
					SoapFault results = (SoapFault) envelope.bodyIn;
					Log.e("UserLoginTask", results.getLocalizedMessage());
					//TODO deal when validation webservice is not available
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			return this.user;
		}

		@Override
		protected void onPostExecute(final User user) {
			mAuthTask = null;
			
			if (this.requestType.equals(VALIDATE_USER)) {
				boolean success = false;
				
				if (user != null) {
					if (String.valueOf(user.getPassword()).equals(this.password)) {
						success = true;
					}
				} else {
					Message msg = new Message();
					msg.what = UserLoginTask.USER_NOT_REGISTERED;
					msg.obj = this.email;
					callBackHandler.sendMessage(msg);
					return;
				}
				
				showProgress(false);
	
				if (success) {
					this.user = user;
					if (this.user.isEmailValid()) {
						callMainActivity();
					} else {
						LoginActivity.this.mEmailView.setError(getString(R.string.error_email_not_validated));
						LoginActivity.this.mEmailView.requestFocus();
					}
				} else {
					mPasswordView.setError(getString(R.string.error_incorrect_password));
					mPasswordView.requestFocus();
				}
			} else if (this.requestType.equals(AUTOMATICALLY_LOGIN_USER)) {
				if (user != null) {
					this.user = user;
					callMainActivity();
				} else {
					showProgress(false);
				}
			}
		}

		private void callMainActivity() {
			Intent intent = new Intent(LoginActivity.this, MainActivity.class);
			intent.putExtra(ARG_USER, this.user);
		
			startActivity(intent);
			finish();
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
		
		public String getUrlWebService(String requestType) {
			StringBuilder sb = new StringBuilder();
			sb.append(getResources().getString(R.string.url_webservice));
			sb.append("/");
			sb.append(getResources().getString(R.string.user_repository));
			sb.append("/");
			
			if (requestType.equals(UserLoginTask.VALIDATE_USER)) {
				sb.append(getResources().getString(R.string.validate_user_webservice_operation));
			} else if (requestType.equals(UserLoginTask.VALIDATE_USER)) {
				sb.append(getResources().getString(R.string.get_user_webservice_operation));
			}
			
			return sb.toString(); 
		}
	}
}
