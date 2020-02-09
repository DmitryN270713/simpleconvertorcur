package com.exchanger.exchanger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

public final class HttpWorker 
{
	private final static String URL_REQ = "http://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";
	private final static String CACHE_DIR = ".valute_convertor_cache";
	private final static String CACHE_FILE = "currency_exchange_courses.xml";
	
	private File cacheDir;
	private Context context;
	private XMLParser parser;
	
	public HttpWorker(Context context)
	{
		this.context = context;
		this.CreateCacheDir();
		this.CheckNetworkConnection();
	}
	
	private void CreateCacheDir()
	{
		if(android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
		{
			this.cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), CACHE_DIR);
		}
		else
		{
			this.cacheDir = this.context.getCacheDir();
		}
		
		if(!this.cacheDir.exists())
		{
			this.cacheDir.mkdir();
		}
	}
	
	private void CheckNetworkConnection()
	{
		ConnectivityManager connectivity = (ConnectivityManager) 
				this.context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo information = connectivity.getActiveNetworkInfo();
		try
		{
			if(information == null || !information.isConnected())
			{
				throw new Exception("No connection available");
			}
		}
		catch(Exception e)
		{
			this.ShowDialog(e.getLocalizedMessage());
		}
	}
	
	public void RequestToGetInfo()
	{
		this.CheckNetworkConnection();
		File file = new File(this.cacheDir, CACHE_FILE);
		this.parser = new XMLParser(this.context);
		
		if(!file.exists())
		{
			new ExchangeCourseLoaderAsyncTask().execute(URL_REQ);
		}
		else
		{
			this.parser.ParseXML(this.GetCurrencyFile());
		}
	}
	
	private class ExchangeCourseLoaderAsyncTask extends AsyncTask<String, Void, Void>
	{
		private ProgressDialog progrDlg;
		
		private final static int OK_RESP = 200;

		@Override
		protected void onPreExecute()
		{
			this.progrDlg = ProgressDialog.show(HttpWorker.this.context, "Information", "Please, wait...");
			this.progrDlg.setCancelable(false);
		}
		
		@Override
		protected Void doInBackground(String... url_str) 
		{
			HttpURLConnection connection = null;
			InputStream input = null;
			
			try
			{
				URL url = new URL(url_str[0]);
				connection = (HttpURLConnection) url.openConnection();
				connection.setReadTimeout(10000);
				connection.setRequestMethod("GET");
				connection.setDoInput(true);
				connection.connect();Log.d("TEST", "HERE");
				
				int resp = connection.getResponseCode();
				if(resp != OK_RESP)
				{
					connection.disconnect();
					throw new Exception("Bad response " + resp);
				}
				
				input = connection.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(input));
				StringBuilder result = new StringBuilder();
				String line = null;
				
				while((line = reader.readLine()) != null)
				{
					result.append(line);
				}
				
				input.close();
				connection.disconnect();
				
				if(!HttpWorker.this.cacheDir.canWrite())
				{
					throw new Exception("Cache directory cannot be written");
				}
				
				FileOutputStream fos = new FileOutputStream(new File(HttpWorker.this.cacheDir, HttpWorker.CACHE_FILE));
				OutputStreamWriter output = new OutputStreamWriter(fos);
				output.write(result.toString());
				output.flush();
				output.close();
				
				result = null;
			}
			catch(Exception e)
			{
				HttpWorker.this.ShowDialog(e.getLocalizedMessage());
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Void res)
		{
			this.progrDlg.dismiss();
			HttpWorker.this.parser.ParseXML(HttpWorker.this.GetCurrencyFile());
		}
		
	}
	
	private File GetCurrencyFile()
	{
		return new File(this.cacheDir, CACHE_FILE);
	}
	
	public void RemoveCacheDirectory()
    {
    	File[] files = this.cacheDir.listFiles();
    	String parent = this.cacheDir.getParent();
    	for(File file : files)
    	{
    		file.delete();
    	}
    	this.cacheDir.delete();
    	new File(parent).delete();    	
    }
	
	private void ShowDialog(String msg)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
		builder.setTitle("Error").setMessage(msg).setIcon(R.drawable.dialog_warning)
		.setNegativeButton("OK", new OnClickListener() 
		{			
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				dialog.dismiss();
				((ExchangerActivity) HttpWorker.this.context).finish();				
			}
		})
		.setCancelable(false);
		
		AlertDialog alert = builder.create();
		alert.show();
	}
}
