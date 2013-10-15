package com.example.kylewbanksblog;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

public class PostsActivity extends Activity {
	
	private static final String TAG = "PostsActivity";
	private List<Post> posts;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_posts);
		
		PostFetcher fetcher = new PostFetcher();
		fetcher.execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.posts, menu);
		return true;
	}
	
	private void handlePostsList(List<Post> posts) {
		this.posts = posts;

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				for(Post post : PostsActivity.this.posts) {
					Toast.makeText(PostsActivity.this, post.title, Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
	
	private void failedLoadingPosts() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(PostsActivity.this, "Failed to load Posts. Have a look at LogCat.", Toast.LENGTH_SHORT).show();
			}
		});
	}

	
	private class PostFetcher extends AsyncTask<Void, Void, String> {
		private static final String TAG = "PostFetcher";
		public static final String SERVER_URL = "http://kylewbanks.com/rest/posts";
		
		@Override
		protected String doInBackground(Void... params) {
			try {
				//Create an HTTP client
				HttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost(SERVER_URL);
				
				//Perform the request and check the status code
				HttpResponse response = client.execute(post);
				StatusLine statusLine = response.getStatusLine();
				if(statusLine.getStatusCode() == 200) {
					HttpEntity entity = response.getEntity();
					InputStream content = entity.getContent();
					
					try {
						//Read the server response and attempt to parse it as JSON
						Reader reader = new InputStreamReader(content);
						
						GsonBuilder gsonBuilder = new GsonBuilder();
						gsonBuilder.setDateFormat("M/d/yy hh:mm a");
						Gson gson = gsonBuilder.create();
						List<Post> posts = Arrays.asList(gson.fromJson(reader, Post[].class));
						content.close();

						handlePostsList(posts);
					} catch (Exception ex) {
						Log.e(TAG, "Failed to parse JSON due to: " + ex);
						failedLoadingPosts();
					}
				} else {
					Log.e(TAG, "Server responded with status code: " + statusLine.getStatusCode());
					failedLoadingPosts();
				}
			} catch(Exception ex) {
				Log.e(TAG, "Failed to send HTTP POST request due to: " + ex);
				failedLoadingPosts();
			}
			return null;
		} 
	}
}
