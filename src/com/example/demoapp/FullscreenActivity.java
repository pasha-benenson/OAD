package com.example.demoapp;

import com.example.demoapp.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
//import android.content.Context;
import android.content.Intent;
//import android.content.SharedPreferences;
//import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
//import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
//import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class FullscreenActivity extends Activity {
	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = true;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

	/**
	 * If set, will toggle the system UI visibility upon interaction. Otherwise,
	 * will show the system UI visibility upon interaction.
	 */
	private static final boolean TOGGLE_ON_CLICK = true;

	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;
//	private TextView myText;
	private String text = "Not Authenticated";

	private static String function_name="";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_fullscreen);


		Intent intent = getIntent();
		System.out.println(intent);
		OAuthHelper oh = new OAuthHelper(this);
		
		String action = intent.getAction();
		if (action.equals(Intent.ACTION_VIEW)) {
			String data = intent.getDataString();

			System.out.println(data);
			
			oh.setRefreshToken(data); 
			text = "Refresh Token is \n" + OAuthHelper.RefreshToken;
			if (function_name=="time"){
				function_name="";
				text = oh.callapi("?method=time");
				if (!text.isEmpty())
					text=(OAuthHelper.fromJSONtoTable(text));

			}
			if (function_name=="id"){
				function_name="";
				text = oh.callapi("?method=id");
				if (!text.isEmpty())
					text=(OAuthHelper.fromJSONtoTable(text));

			}
			
			
		}
		{

			//pb final View controlsView = findViewById(R.id.fullscreen_content_controls);
			final View contentView = findViewById(R.id.fullscreen_content);
			TextView t = (TextView) contentView;
			t.setText(text);
			// Set up an instance of SystemUiHider to control the system UI for
			// this activity.
			mSystemUiHider = SystemUiHider.getInstance(this, contentView,
					HIDER_FLAGS);
			mSystemUiHider.setup();
			mSystemUiHider
					.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
						// Cached values.
						int mControlsHeight;
						int mShortAnimTime;

						@Override
						@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
						public void onVisibilityChange(boolean visible) {
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
								// If the ViewPropertyAnimator API is available
								// (Honeycomb MR2 and later), use it to animate
								// the
								// in-layout UI controls at the bottom of the
								// screen.
/*	pb							if (mControlsHeight == 0) {
									mControlsHeight = controlsView.getHeight();
								}*/
								if (mShortAnimTime == 0) {
									mShortAnimTime = getResources()
											.getInteger(
													android.R.integer.config_shortAnimTime);
								}
								/*pbcontrolsView
										.animate()
										.translationY(
												visible ? 0 : mControlsHeight)
										.setDuration(mShortAnimTime);*/
							} else {
								// If the ViewPropertyAnimator APIs aren't
								// available, simply show or hide the in-layout
								// UI
								// controls.
								/*pbcontrolsView
										.setVisibility(visible ? View.VISIBLE
												: View.GONE);*/
							}

							if (visible && AUTO_HIDE) {
								// Schedule a hide().
								delayedHide(AUTO_HIDE_DELAY_MILLIS);
							}
						}
					});

			// Set up the user interaction to manually show or hide the system
			// UI.
			contentView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (TOGGLE_ON_CLICK) {
						mSystemUiHider.toggle();
					} else {
						mSystemUiHider.show();
					}
				}
			});

			// Upon interacting with UI controls, delay any scheduled hide()
			// operations to prevent the jarring behavior of controls going away
			// while interacting with the UI.
			// findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		// pb delayedHide(100);
	}

	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behavior of controls going away
	 * while interacting with activity UI.
	 */
	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
			return false;
		}
	};

	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			mSystemUiHider.hide();
		}
	};

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}

	/** Called when the user clicks the Send button */
	public void click_auth(View view) {
		// Do something in response to button
		OAuthHelper oh = new OAuthHelper(this);
		oh.authenticate();

		// System.out.println(uriUrl);
	}

	public void click_clear(View view) {
		// Do something in response to button
		//OAuthHelper oh = new OAuthHelper(this);
		OAuthHelper.clear();

//		final View controlsView = findViewById(R.id.fullscreen_content_controls);
		final View contentView = findViewById(R.id.fullscreen_content);
		TextView t = (TextView) contentView;
		t.setText("clear");

		// System.out.println(uriUrl);
	}

	public void click_callapi_id(View view) {
		// Do something in response to button
		function_name="id";
//		final View controlsView = findViewById(R.id.fullscreen_content_controls);
		final View contentView = findViewById(R.id.fullscreen_content);
		TextView t = (TextView) contentView;
		
		t.setText("");
		OAuthHelper oh = new OAuthHelper(this);
		String text = oh.callapi("?method=id");
		if (!text.isEmpty())
			t.setText(OAuthHelper.fromJSONtoTable(text));
		// System.out.println(uriUrl);
	}
		public void click_callapi_time(View view) {
			// Do something in response to button
			function_name="time";
//			final View controlsView = findViewById(R.id.fullscreen_content_controls);
			final View contentView = findViewById(R.id.fullscreen_content);
			TextView t = (TextView) contentView;
			
			t.setText("");
			OAuthHelper oh = new OAuthHelper(this);
			String text = oh.callapi("?method=time");
			if (!text.isEmpty())
				t.setText(OAuthHelper.fromJSONtoTable(text));
			// System.out.println(uriUrl);

	}
		public void click_AuthResourceCredentials(View view) {
			// Do something in response to button
			function_name="SetRefreshToken";
//			final View controlsView = findViewById(R.id.fullscreen_content_controls);
			final View contentView = findViewById(R.id.fullscreen_content);
			TextView t = (TextView) contentView;
			
			t.setText("");
			EditText editText = (EditText)findViewById(R.id.userName);
			String Username = editText.getText().toString();
			
			editText = (EditText)findViewById(R.id.userPassword);
			String Password = editText.getText().toString();
			
			OAuthHelper oh = new OAuthHelper(this);
			String text = oh.setRefreshToken(Username,Password);
			if (!text.isEmpty())
				t.setText(OAuthHelper.fromJSONtoTable(text));
			// System.out.println(uriUrl);

	}
}
