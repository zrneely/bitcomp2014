package org.spalabs.bitcomp.bitgive;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

import com.haibison.android.lockpattern.LockPatternActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;
import android.text.InputType;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;

/**
 * Things this activity does:
 * <ol>
 * <li>Prompt the user for some input that can be used to generate an encryption
 * key.</li>
 * <li>Use that key to encrypt an actual encryption key.</li>
 * <li>Create a wallet.</li>
 * <li>Use the actual encryption key to encrypt the wallet information.</li>
 * <li>Store the wallet information.</li>
 * <li>Get user name to be put on donations, store in plaintext.</li>
 * <li>Mark the user as created.</li>
 * </ol>
 * 
 * @author zrneely
 * 
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class UserInitActivity extends Activity {

	public static final int PATTERN_RESULT = 1;
	public static final int QR_CODE_RESULT = 2;

	private static byte[] __key = null;
	private static String __mainAddr = null;
	private static String __patternHash = null;

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// We should put the progress bar in the action bar if possible
		requestWindowFeature(Window.FEATURE_PROGRESS);

		setContentView(R.layout.activity_user_init);
		// Don't show the Up button in the action bar; it would close the app
		// which is not intuitive at all.
		// setupActionBar();

		// Add the progress bar
		final ProgressBar progressBar = new ProgressBar(this, null,
				android.R.attr.progressBarStyleHorizontal);
		progressBar.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				24));
		final FrameLayout decorView = (FrameLayout) getWindow().getDecorView();
		decorView.addView(progressBar);
		// Position it correctly
		ViewTreeObserver observer = progressBar.getViewTreeObserver();
		observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onGlobalLayout() {
				View contentView = decorView.findViewById(android.R.id.content);
				progressBar.setY(contentView.getY() - 10);
				ViewTreeObserver observer = progressBar.getViewTreeObserver();
				if (Build.VERSION.SDK_INT >= 16) {
					observer.removeOnGlobalLayoutListener(this);
				} else {
					observer.removeGlobalOnLayoutListener(this);
				}
			}
		});

		// Start the blockchain synchronization
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
			new BlockChainSync(progressBar).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
		else {
			new BlockChainSync(progressBar).execute();
		}

		// Add action listeners
		final UserInitActivity self = this;
		((Button) findViewById(R.id.patternButton))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Intent i = new Intent(
								LockPatternActivity.ACTION_CREATE_PATTERN,
								null, self, LockPatternActivity.class);
						startActivityForResult(i, PATTERN_RESULT);
					}
				});
		((Button) findViewById(R.id.qrBTCButton))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Intent i = new Intent(
								"com.google.zxing.client.android.SCAN");
						i.putExtra("SCAN_MODE", "QR_CODE_MODE");
						startActivityForResult(i, QR_CODE_RESULT);
					}
				});
		((Button) findViewById(R.id.typeBTCButton))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								self);
						builder.setTitle(R.string.typeBTCTitle);
						final EditText input = new EditText(self);
						input.setInputType(InputType.TYPE_CLASS_TEXT);
						builder.setView(input);
						builder.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO validate
										UserInitActivity.__mainAddr = input
												.getText().toString();
										// Update TextView
										((TextView) findViewById(R.id.addressText)).setText(
												getString(R.string.addressTextPrefix) + 
												UserInitActivity.__mainAddr);
									}
								});
						builder.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.cancel();
									}
								});
						builder.show();
					}
				});
		((Button) findViewById(R.id.finishButton))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						String name = ((EditText) findViewById(R.id.userNameEdit))
								.getText().toString();

						if (UserInitActivity.__key == null) {
							Toast.makeText(self, R.string.errEnterPattern,
									Toast.LENGTH_SHORT).show();
							return;
						}
						if (UserInitActivity.__mainAddr == null) {
							Toast.makeText(self, R.string.errEnterMainAddr,
									Toast.LENGTH_SHORT).show();
							return;
						}
						if (name == "" || name.length() < 3) {
							Toast.makeText(self, R.string.errEnterName,
									Toast.LENGTH_SHORT).show();
							return;
						}
						if (progressBar.getProgress() != 100){
							Toast.makeText(self, R.string.errBlockchain,
									Toast.LENGTH_SHORT).show();
							return;
						}
						// Open the home screen and save values
						SharedPreferences sPrefs = getSharedPreferences(
								HomeScreenActivity.PREFS_NAME, MODE_PRIVATE);
						Editor edit = sPrefs.edit();
						edit.putString(HomeScreenActivity.PREF_USER_NAME, name);
						edit.putString(HomeScreenActivity.PREF_MAIN_BTC_ADDR,
								UserInitActivity.__mainAddr);
						edit.putBoolean(HomeScreenActivity.PREF_USER_EXITS, true);
						edit.commit();
						Intent i = new Intent(self, HomeScreenActivity.class);
						startActivity(i);
						self.finish();
					}
				});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PATTERN_RESULT) {
			if (resultCode == RESULT_OK) {
				try {
					char[] pattern = data
							.getCharArrayExtra(LockPatternActivity.EXTRA_PATTERN);
					UserInitActivity.__patternHash = new String(pattern);
					// Update the TextView
					((TextView) findViewById(R.id.patternText))
							.setText(getString(R.string.patternTextPrefix)
									+ UserInitActivity.__patternHash);
					byte[] pat = UserInitActivity.__patternHash.getBytes();
					byte[] salt = new byte[] { 0x7C, 0x2D, 0x12, 0x25, 0x5F,
							0x14 };
					// Generate key from this data
					byte[] key = new byte[] {};
					MessageDigest digest = MessageDigest.getInstance("SHA-256");
					for (int i = 0; i < 255; i++) {
						digest.reset();
						digest.update(key);
						digest.update(pat);
						digest.update(salt);
						key = digest.digest();
						UserInitActivity.__key = key;
					}
				} catch (NoSuchAlgorithmException ex) {
					ex.printStackTrace();
					Toast.makeText(this, R.string.failure, Toast.LENGTH_SHORT)
							.show();
					return;
				}
			}
		} else if (requestCode == QR_CODE_RESULT) {
			if (resultCode == RESULT_OK) {
				String value = data.getStringExtra("SCAN_RESULT");
				// Store the value in shared preferences
				if (value.startsWith("bitcoin:")) {
					value = value.substring(8); // cut off bitcoin:
				}
				int i;
				while ((i = value.indexOf('?')) != -1) {
					value = value.substring(0, i);
				}
				// Validate address
				if (!Pattern.matches("[a-zA-Z1-9]{27,35}", value)) {
					Toast.makeText(this, R.string.invalidBTCAddress,
							Toast.LENGTH_SHORT).show();
					return;
				}
				UserInitActivity.__mainAddr = value;
				// Update text view
				((TextView) findViewById(R.id.addressText))
						.setText(getString(R.string.addressTextPrefix)
								+ UserInitActivity.__mainAddr);
				Toast.makeText(this, value, Toast.LENGTH_LONG).show();
			}
		}
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.user_init, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
