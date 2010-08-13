/*
 * Copyright 2009, 2010 Tyler Levine
 * 
 * This file is part of Hunch for Android.
 *
 * Hunch for Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Hunch for Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Hunch for Android.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.hunch.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.hunch.Const;
import com.hunch.api.HunchAPI.ConnectionInfo;


/**
 * @author  Tyler Levine Jan 12, 2010
 */
public class HunchAPIRequest
{
	
	protected static final int RESPONSE_TIMEOUT = 5000;
	
	protected static final Map< String, String > requiredRequestParameters;
	static
	{
		// these parameters MUST be present in each API request
		Map< String, String > tempParams = new HashMap< String, String >();

		tempParams.put( "devKey", HunchAPI.API_KEY );

		// nobody should be messing with these parameters
		requiredRequestParameters = Collections.unmodifiableMap( tempParams );

	}

	protected static final ExecutorService executor = Executors.newCachedThreadPool();
	
	protected final class AsyncAPIRequest
	{
		protected HunchAPIResponseCallback c;
		
		protected final int MESSAGE_POST_RESULT = 0x0;
		
		
		protected Handler h = new Handler()
		{
			@Override
			public void handleMessage( Message msg )
			{
				switch( msg.what )
				{
					case MESSAGE_POST_RESULT:
						onPostExecute( (JSONObject) msg.obj );
						
				}
			}
		};

		protected void execute( final URL param )
		{
			// callable that does the work on the network 
			Callable< JSONObject > worker = new Callable< JSONObject >()
			{
				public JSONObject call()
				{
					URL address = param;
					HttpURLConnection con;
					
					try
					{
						con = (HttpURLConnection) address.openConnection();
						con.setReadTimeout( RESPONSE_TIMEOUT );
					} catch ( IOException e )
					{
						// if we're here something went wrong
						throw new RuntimeException( "Could not connect for API call!", e );
					}
					
					if ( con == null )
						throw new NullPointerException(	"Can not make request on null connection!" );
					
					String resp;
					try
					{
						// connect to the URL and read the response
						con.connect();

						BufferedReader bufIn = new BufferedReader( 
											new InputStreamReader(
													con.getInputStream() ),
													4096 );

						StringBuilder sb = new StringBuilder();

						String temp;
						while ( ( temp = bufIn.readLine() ) != null )
						{
							sb.append( temp );
						}
						
						resp = sb.toString();
						
					}
					catch ( IOException  e )
					{
						throw new RuntimeException( "couldn't connect for API request!", e );
					}
					
					JSONObject ret;
					try
					{
						ret = new JSONObject( resp );
					} catch ( JSONException e )
					{
						throw new RuntimeException( "couldn't build JSONObject!", e );
					}
					
					return ret;
				}
			};
			
			// the task wrapping the work done by the callable
			FutureTask< JSONObject > task = new FutureTask< JSONObject >( worker )
			{
				// hook the done event to send a message back to the UI thread
				@Override
				protected void done()
				{
					Message m;
					JSONObject result = null;
					
					try
					{
						result = get();
					}
					catch ( InterruptedException e )
					{
						Log.w( Const.TAG, "interrupted while getting API result!" );
					}
					catch ( ExecutionException e )
					{
						throw new RuntimeException( "Couldn't perform API request", e );
					}
					
					m = h.obtainMessage( MESSAGE_POST_RESULT, result );
					m.sendToTarget();
				}
			};
			
			// send the task out for execution
			executor.execute( task );
		}
		
		protected void onPostExecute( JSONObject s )
		{
			// we're done here, and back in the UI thread.
			// call back to the last layer
			c.callComplete( s );
		}
		
		AsyncAPIRequest setCallback( HunchAPIResponseCallback c )
		{
			this.c = c;
			return this;
		}
		
	}
	
	// data needed for every request
	protected Map< String, String > paramMap;
	protected String apiCall;
	protected boolean callPrepared;
	
	HunchAPIRequest( String apiCall )
	{
		this.apiCall = apiCall;
	}

	HunchAPIRequest( String apiCall, Map< String, String > params )
	{
		this.apiCall = apiCall;

		paramMap = new LinkedHashMap< String, String >();
		
		paramMap.putAll( params );
	}
	
	void execute( HunchAPIResponseCallback c )
	{		
		
		// add the required default parameters
		if( paramMap == null)
			paramMap = new LinkedHashMap< String, String >();
		
		paramMap.putAll( requiredRequestParameters );
		
		URL address;
		String url = buildURL();
		
		try
		{
			address = new URL( url );
		} catch ( MalformedURLException e )
		{
			throw new RuntimeException( String.format( "tried to build malformed URL! (%s)", url ), e );
		}
		
		Log.i( Const.TAG, "performing API call (" + address.toString() + ")" );
		
		new AsyncAPIRequest().setCallback( c ).execute( address );
		
	}

	void addParam( String key, String value )
	{
		// make sure our maps not null
		if( paramMap == null )
			paramMap = new LinkedHashMap< String, String >();
		
		// some values may be null, don't include them
		if( value == null )
			return;

		paramMap.put( key, value );
	}

	void setAPICall( String call )
	{
		apiCall = call;
	}

	protected String buildURL()
	{
		if ( apiCall == null )
			throw new NullPointerException(
					"tried to build URL, but no api call set!" );

		StringBuilder sb = new StringBuilder();

		// add the URL to the api root
		sb.append( "http://" );
		sb.append( ConnectionInfo.host );
		sb.append( "/" );
		sb.append( ConnectionInfo.api_extention );
		sb.append( "/" );

		sb.append( apiCall );
		sb.append( "/?" );
		
		if( paramMap == null || paramMap.isEmpty() )
			return sb.toString();

		// now all URL parameters
		Set< Entry< String, String > > GETentrys = paramMap.entrySet();

		boolean first = true;

		for ( Entry< String, String > entry : GETentrys )
		{
			if ( !first )
				sb.append( "&" );

			String key = entry.getKey(), value = entry.getValue();

			if ( key == null )
			{
				sb.append( value );
			} else if ( value == null )
			{
				sb.append( key );
			} else
			{
				// key and value was set
				sb.append( key );
				sb.append( "=" );
				sb.append( value );
			}

			if ( first )
				first = false;
		}

		return sb.toString();
	
	}
}