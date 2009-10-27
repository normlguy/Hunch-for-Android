package com.hunch;

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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import android.os.Handler;
import android.util.Log;

public class HunchAPI
{

	public static final String API_KEY = "public_devKey";
	public static final int RESPONSE_TIMEOUT = 5000;

	protected static final Map< String, String > requiredRequestParameters;

	static
	{
		// these parameters MUST be present in each API request
		Map< String, String > tempParams = new HashMap< String, String >();

		tempParams.put( "devKey", API_KEY );

		// nobody should be messing with these parameters
		requiredRequestParameters = Collections.unmodifiableMap( tempParams );

	}

	// protected static HttpURLConnection hunchConnection;

	/**
	 * Wraps a request to the hunch API.
	 * 
	 * Allows setting GET parameters in the URL before a connection is made, for
	 * login.
	 * 
	 * @author Tyler Levine
	 * 
	 */
	class Request
	{
		// protected HttpURLConnection connection;
		protected Map< String, String > paramMap;
		protected String _apiCall;
		protected boolean callPrepared = false;
		protected String url;

		Request()
		{

		}

		Request( String apiCall, Map< String, String > params )
		{
			this._apiCall = apiCall;

			addParams( params );
		}

		HunchAPI.Response execute() throws IOException
		{
			HttpURLConnection con = initCall();
			con.connect();

			Response apiResponse = new Response();

			apiResponse.buildFromOpenHttp( con );

			return apiResponse;

		}

		void addParam( String key, String value )
		{
			paramMap = assureMap( paramMap );

			paramMap.put( key, value );
		}

		void addParams( Map< String, String > params )
		{
			if ( params == null )
				return;
			paramMap = assureMap( paramMap );

			paramMap.putAll( params );
		}

		void setAPICall( String call )
		{
			_apiCall = call;
		}

		private Map< String, String > assureMap( Map< String, String > m )
		{
			if ( m == null )
				m = new LinkedHashMap< String, String >();

			return m;
		}

		protected HttpURLConnection initCall()
		{
			URL address;
			HttpURLConnection connection;
			try
			{
				addDefaultParams();
				address = new URL( buildURL() );
				connection = (HttpURLConnection) address.openConnection();
				connection.setRequestMethod( "GET" );
				connection.setReadTimeout( RESPONSE_TIMEOUT );

				callPrepared = true;

				return connection;
			} catch ( MalformedURLException e )
			{
				// if we're here something went wrong
				throw new RuntimeException( "Could not connect for API call!",
						e );
			} catch ( IOException e )
			{
				// if we're here something went wrong
				throw new RuntimeException( "Could not connect for API call!",
						e );
			}

			// if we're here something went wrong
			// throw new RuntimeException( "Could not connect for API call!" );

		}

		protected String buildURL()
		{
			if ( _apiCall == null )
				throw new NullPointerException(
						"tried to build URL, but no api call set!" );

			StringBuilder sb = new StringBuilder();

			// add the URL to the api root
			sb.append( "http://" );
			sb.append( ConnectionInfo.host );
			sb.append( "/" );
			sb.append( ConnectionInfo.api_extention );
			sb.append( "/" );

			sb.append( _apiCall );
			sb.append( "/?" );

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

			url = sb.toString();
			return url;

		}

		String getURL()
		{
			// build URL on every get call? how long does this operation take?
			// no action taken, would be premature optimization
			buildURL();

			return url;
		}

		protected void addDefaultParams()
		{
			paramMap = assureMap( paramMap );

			paramMap.putAll( requiredRequestParameters );
		}

	}

	public class Response
	{

		private String responseText;
		private boolean responseReceived = false;

		public Response()
		{

		}

		public Response( String responseStr )
		{
			responseText = responseStr;
		}

		/**
		 * Builds this Response from an open HttpURLConnection.
		 * 
		 * @param con
		 *            An already-opened and connected connection to the Request
		 *            URL.
		 */
		public void buildFromOpenHttp( HttpURLConnection con )
				throws IOException
		{
			if ( con == null )
				throw new NullPointerException(
						"Can not build Response from null connection!" );

			BufferedReader bufIn = new BufferedReader( new InputStreamReader(
					con.getInputStream() ) );

			StringBuilder sb = new StringBuilder();

			String temp;
			while ( ( temp = bufIn.readLine() ) != null )
			{
				sb.append( temp );
			}

			responseText = sb.toString();

			responseReceived = true;

		}

		protected String getRaw()
		{
			// make sure we have a response
			if ( !responseReceived )
				throw new IllegalStateException(
						"Response has not yet been received." );

			return responseText;
		}

		public JSONObject getJSON()
		{
			return (JSONObject) JSONValue.parse( getRaw() );

		}

	}

	static final class ConnectionInfo
	{

		// no instantiation please
		private ConnectionInfo()
		{
		}

		public static final String host = "api.hunch.com";
		public static final String api_extention = "api";
	}

	/**
	 * The interface used to call back after an API call has been completed.
	 * 
	 * @param resp
	 *            The HunchObject returned by the API call.
	 * @author Tyler Levine
	 * 
	 */
	public interface Callback
	{
		public void callComplete( HunchObject resp );
	}

	public void authenticateUser( String email_or_username, String password )
	{

	}

	public void getQuestion( Map< String, String > params,
			HunchAPI.Callback completedCallback )
	{
		// get question requests must set the question ID
		assureParams( params, "questionId" );

		Request questionRequest = new Request();

		questionRequest.setAPICall( "getQuestion" );
		questionRequest.addParams( params );

		Response r;
		try
		{
			r = questionRequest.execute();
		} catch ( IOException e )
		{
			throw new RuntimeException( "Couldn't execute a question request!",
					e );
		}

		//completedCallback.callComplete( r );

	}

	public void listTopics( Map< String, String > params,
			HunchAPI.Callback completedCallback )
	{
		// list topics request must set the categoryUrlName
		assureParams( params, "categoryUrlName" );

		Request listTopicsRequest = new Request( "listTopics", params );

		Response r;
		try
		{
			r = listTopicsRequest.execute();
		} catch ( IOException e )
		{
			throw new RuntimeException(
					"Couldn't execute a listTopics request!", e );
		}
		
		JSONArray topics = (JSONArray) r.getJSON().get( "topics" );
		HunchList< HunchTopic > hunchTopics = new HunchList< HunchTopic >();
		
		for( Object objTopic : topics )
		{
			JSONObject topic = (JSONObject) objTopic;
			HunchTopic h = HunchTopic.buildFromJSON( topic );
			hunchTopics.add( h );	
		}

		completedCallback.callComplete( hunchTopics );
	}

	/**
	 * Lists all Hunch question categories.
	 * 
	 * @param params Map of GET parameters
	 * @param completedCallback Callback to call upon completion.
	 *  Passes a {@link HunchList} of {@link HunchCategory}s as the sole argument.
	 * @throws RuntimeException Upon failure to complete the call.
	 */
	public void listCategories( Map< String, String > params,
			HunchAPI.Callback completedCallback )
	{
		// list categories requires no parameters
		//Handler h = new Handler();

		Request listCategoriesRequest = new Request( "listCategories", params );

		Response r;
		try
		{
			r = listCategoriesRequest.execute();
		} catch ( IOException e )
		{
			throw new RuntimeException(
					"Couldn't execute a listCategories request!", e );
		}

		JSONArray cats = (JSONArray) r.getJSON().get( "categories" );
		HunchList< HunchCategory > catList = new HunchList< HunchCategory >();
		
		for( Object objCat : cats )
		{
			JSONObject category = (JSONObject) objCat;
			HunchCategory cat = HunchCategory.buildFromJSON( category );
			
			catList.add( cat );
		}
		
		Log.d( HunchSplash.LOG_TAG, "listCategories call complete, calling callback" );
		
		completedCallback.callComplete( catList );
		

	}

	private static void assureParams( Map< String, String > m, String first,
			String... rest )
	{
		if ( m == null )
			throw new IllegalArgumentException(
					"Can not assure parameters on a null map!" );

		if ( !m.containsKey( first ) || m.get( first ) == null )
			throw new IllegalArgumentException( "the " + first
					+ " parameter must be present and has not been found!" );

		for ( String s : rest )
		{
			if ( !m.containsKey( s ) || m.get( s ) == null )
				throw new IllegalArgumentException( "the " + s
						+ " parameter must be present and has not been found!" );
		}
	}

}