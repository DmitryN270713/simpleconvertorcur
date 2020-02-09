package com.exchanger.exchanger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

public final class XMLParser 
{
	private XmlPullParserFactory factory;
	private Context context;
	private XmlPullParser parser;

	private final static String CUBE = "Cube";
	
	public XMLParser(Context context)
	{
		this.context = context;
	}
	
	public void ParseXML(final File file)
	{
		new ParseXMLAsync().execute(file);
	}
	
	private class ParseXMLAsync extends AsyncTask<File, Void, ArrayList<CurrencyUnit>>
	{
		private ProgressDialog progress;
		
		@Override
		protected void onPreExecute()
		{
			this.progress = ProgressDialog.show(XMLParser.this.context, "", "Loading. Please, wait...");
			this.progress.setCancelable(false);
		}

		@Override
		protected ArrayList<CurrencyUnit> doInBackground(File... filesToParse) 
		{
			try
			{
				FileInputStream fis = new FileInputStream(filesToParse[0]);
				InputStream input = new BufferedInputStream(fis);
				
				return XMLParser.this.parseXmlHelper(input);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				return null;
			}
		}
		
		@Override
		protected void onPostExecute(ArrayList<CurrencyUnit> result)
		{
			if(result != null)
			{
				Intent intent = new Intent(ExchangerActivity.CURRENCY_SENT);
				intent.putParcelableArrayListExtra(ExchangerActivity.CURRENCY_SENT_KEY, result);
				XMLParser.this.context.sendBroadcast(intent);
				
				this.progress.dismiss();
			}
		}
	}

	public ArrayList<CurrencyUnit> parseXmlHelper(InputStream input) 
	{
		ArrayList<CurrencyUnit> ls = new ArrayList<CurrencyUnit>();

		try 
		{
			this.factory = XmlPullParserFactory.newInstance();
			this.factory.setNamespaceAware(true);
			this.parser = this.factory.newPullParser();
			this.parser.setInput(input, "UTF-8");
			
			int eventType = this.parser.getEventType();
			
			while(eventType != XmlPullParser.END_DOCUMENT)
			{
				if(eventType == XmlPullParser.START_TAG)
				{
					String name = this.parser.getName();
					if(name.equals(CUBE))
					{
						if(this.parser.getAttributeCount() == 2)
						{
							ls.add(new CurrencyUnit(this.parser.getAttributeValue(0), Float.valueOf(this.parser.getAttributeValue(1))));
						}
					}
				}
				
	            eventType = parser.next();
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		return ls;
	}
}
