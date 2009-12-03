package com.hunch;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * 
 * 
 * @author Tyler Levine
 * Dec 3, 2009
 *
 */
public class HunchHome extends Activity
{

	@Override
	public void onCreate( Bundle b )
	{
		super.onCreate( b );
		setContentView( R.layout.home );
		Log.d( Const.TAG, "Loading categories..." );
		
		final Button button = (Button) findViewById( R.id.categoriesButton );
		button.setOnClickListener( new OnClickListener()
		{
			
			@Override
			public void onClick( View v )
			{
				Log.d( Const.TAG, "Loading categories..." );
				
			}
		} );
	}
}

/*final Button b = (Button) findViewById( R.id.categoriesButton );
final ListView listView = (ListView) findViewById( R.id.catList );
final HunchAPI api = new HunchAPI();

b.setOnClickListener( new OnClickListener()
{
	@Override
	public void onClick( View v )
	{				

		final HunchAPI.Callback listCallback = new HunchAPI.Callback()
		{

			@SuppressWarnings( "unchecked" )
			@Override
			public void callComplete( HunchObject resp )
			{
				// Log.i( LOG_TAG, resp.getRaw() );

				if( !( resp instanceof HunchList< ? > ) ) return;
				
				HunchList< HunchCategory > list = (HunchList< HunchCategory >) resp;
				final ArrayAdapter< HunchCategory > adapter;
				adapter = new ArrayAdapter< HunchCategory >( HunchSplash.this, android.R.layout.simple_list_item_1, list );
		
				
				listView.setAdapter( adapter );
				listView.setSelection( -1 );
			}
		};
		
		final HunchAPI.Callback printCallback = new HunchAPI.Callback()
		{
			
			@Override
			public void callComplete( HunchObject resp )
			{
				// TODO Auto-generated method stub
				System.out.println( resp );
				
			}
		};
		
		Log.i( LOG_TAG, "fetching categories..." );

		api.listCategories( null, listCallback );
		
		Log.i( LOG_TAG, "beginning comprehensive test..." );
		
		
		
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
	
});*/
