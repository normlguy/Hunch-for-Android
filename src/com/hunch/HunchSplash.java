package com.hunch;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class HunchSplash extends Activity
{
	public static final String LOG_TAG = "HUNCH";

	/** Called when the activity is first created. */
	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.main );

		final Button b = (Button) findViewById( R.id.categoriesButton );
		final ListView listView = (ListView) findViewById( R.id.catList );
		final HunchAPI api = new HunchAPI();

		b.setOnClickListener( new OnClickListener()
		{
			@Override
			public void onClick( View v )
			{				

				final HunchAPI.Callback listCallback = new HunchAPI.Callback()
				{

					@Override
					public void callComplete( HunchObject resp )
					{
						// Log.i( LOG_TAG, resp.getRaw() );

						HunchList< HunchCategory > list = (HunchList< HunchCategory >) resp;
						final ArrayAdapter< HunchCategory > adapter;
						adapter = new ArrayAdapter< HunchCategory >( HunchSplash.this, android.R.layout.simple_list_item_1, list );
				
						
						listView.setAdapter( adapter );
						listView.setSelection( -1 );
					}
				};
				
				Log.i( LOG_TAG, "fetching categories..." );

				api.listCategories( null, listCallback );
				
				Log.i( LOG_TAG, "onClick complete." );

			}
		} );
		
		listView.setOnItemClickListener( new OnItemClickListener()
		{

			@Override
			public void onItemClick( AdapterView< ? > parent, View v, int position, long id )
			{
				
				// adding some content for SVN test
				
				
			}
			
		});
	}
}