package org.spalabs.bitcomp.bitgive;

import android.os.AsyncTask;
import android.widget.ProgressBar;

/**
 * Synchronizes the blockchain to a local file. TODO look into a cloud-based wallet.
 * @author zrneely
 *
 */
public class BlockChainSync extends AsyncTask<String, Integer, String> {
	
	private ProgressBar progressBar;
	
	public BlockChainSync(ProgressBar p){
		progressBar = p;
	}

	@Override
	protected String doInBackground(String... params) {
		for(int i = 0; i < 100; ++i) {
			try {
				// Dummy task
				Thread.sleep(100);
				publishProgress(i);
			}
			catch(InterruptedException ex) {
				ex.printStackTrace();
			}
		}
		return "done";
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		// Update UI
		progressBar.setProgress(values[0]);
	}
	
	@Override
	protected void onPostExecute(String result) {
		progressBar.setProgress(100);
	}
}
