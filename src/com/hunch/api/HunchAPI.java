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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.hunch.Const;

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

			return new Response( con );

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

				Log.d( Const.TAG, "performing API call (" + address.toString() + ")" );

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

		/**
		 * Builds this Response from an open HttpURLConnection.
		 * 
		 * @param con
		 *            An already-opened and connected connection to the Request
		 *            URL.
		 */
		public Response( HttpURLConnection con ) throws IOException
		{
			buildFromOpenHttp( con );
		}

		public Response( String responseStr )
		{
			responseText = responseStr;
		}

		private void buildFromOpenHttp( HttpURLConnection con )
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
			JSONObject ret;
			try
			{
				ret = new JSONObject( getRaw() );
			} catch ( JSONException e )
			{
				throw new RuntimeException( "couldn't build JSONObject!", e );
			}

			return ret;

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

	public void getResult( Map< String, String > params,
			HunchAPI.Callback completedCallback )
	{
		assureParams( params, "resultId" );

		Request resultRequest = new Request( "getResult", params );

		Response r;
		try
		{
			r = resultRequest.execute();
		} catch ( IOException e )
		{
			throw new RuntimeException( "Couldn't execute a result request!", e );
		}

		JSONObject result;
		try
		{
			result = r.getJSON().getJSONObject( "result" );
		} catch ( JSONException e )
		{
			throw new RuntimeException( "Couldn't execute a result request!", e );
		}

		HunchResult ret = HunchResult.buildFromJSON( result );

		completedCallback.callComplete( ret );

	}

	/**
	 * Retrieves a specific question.
	 * 
	 * @param params
	 *            Params to pass in the URL of the request
	 * @param completedCallback
	 *            Callback to call upon completion of this request. Passes a
	 *            {@link HunchQuestion} as the sole argument.
	 * @throws RuntimeException
	 *             Upon failure to complete the call.
	 */
	public void getResponse( Map< String, String > params,
			HunchAPI.Callback completedCallback )
	{
		// get response must set the response ID
		assureParams( params, "responseId" );

		Request responseRequest = new Request( "getResponse", params );

		Response r;
		try
		{
			r = responseRequest.execute();
		} catch ( IOException e )
		{
			throw new RuntimeException( "Couldn't execute a response request!",
					e );
		}

		JSONObject response;
		try
		{
			response = r.getJSON().getJSONObject( "response" );
		} catch ( JSONException e )
		{
			throw new RuntimeException( "could not execute getResponse!", e );
		}

		HunchResponse ret = HunchResponse.buildFromJSON( response );

		completedCallback.callComplete( ret );
	}

	/**
	 * Retrieves a specific question.
	 * 
	 * @param params
	 *            Params to pass in the URL of the request
	 * @param completedCallback
	 *            Callback to call upon completion of this request. Passes a
	 *            {@link HunchQuestion} as the sole argument.
	 * @throws RuntimeException
	 *             Upon failure to complete the call.
	 */
	public void getQuestion( Map< String, String > params,
			HunchAPI.Callback completedCallback )
	{
		// get question requests must set the question ID
		assureParams( params, "questionId" );

		// send the response
		Request questionRequest = new Request( "getQuestion", params );

		Response r;
		try
		{
			r = questionRequest.execute();
		} catch ( IOException e )
		{
			throw new RuntimeException( "Couldn't execute a question request!",
					e );
		}

		// get the question data, build the object, and call back to the client
		JSONObject question;
		try
		{
			question = r.getJSON().getJSONObject( "question" );
		} catch ( JSONException e )
		{
			throw new RuntimeException( "could not execute getQuestion!", e );
		}

		HunchQuestion ret = HunchQuestion.buildFromJSON( question );

		completedCallback.callComplete( ret );

	}

	public void getTopic( Map< String, String > params,
			HunchAPI.Callback completedCallback )
	{

		// throw exception unless we have one or the other
		assureOneOf( params, "topicId", "urlName" );

		Request getTopicRequest = new Request( "getTopic", params );

		Response r;
		try
		{
			r = getTopicRequest.execute();
		} catch ( IOException e )
		{
			throw new RuntimeException( "couldn't execute a getTopicRequest!", e );
		}

		JSONObject topic;
		try
		{
			topic = r.getJSON().getJSONObject( "topic" );
		} catch ( JSONException e )
		{
			throw new RuntimeException( "couldn't execute a getTopicRequest!", e );
		}

		int id = Integer.MIN_VALUE, eitherOr = Integer.MIN_VALUE;
		try
		{
			id = topic.getInt( "id" );
			eitherOr = topic.getInt( "eitherOrTopic" );
		} catch ( JSONException e )
		{
			throw new RuntimeException( "couldn't execute a getTopicRequest!", e );
		}

		HunchTopic hTopic;
		HunchTopic.Builder b = HunchTopic.getBuilder();

		b.init( r.getJSON() );
		try
		{
			b.setId( id )
					.setDecision( topic.getString( "decision" ) )
					.setUrlName( topic.getString( "urlName" ) )
					.setShortName( topic.getString( "shortName" ) )
					.setHunchUrl( topic.getString( "hunchUrl" ) )
					.setImageUrl( topic.getString( "imageUrl" ) )
					.setResultType( topic.getString( "resultType" ) )
					.setIsEitherOr( eitherOr == 1 );

			JSONObject category = topic.getJSONObject( "category" );

			HunchCategory hCategory = HunchCategory.getBuilder()
					.setUrlName( category.getString( "categoryUrlName" ) )
					.setName( category.getString( "categoryName" ) )
					.setImageUrl( category.getString( "categoryImageUrl" ) )
					.build();

			b.setCategory( hCategory );

			hTopic = b.build();

		} catch ( JSONException e )
		{
			throw new RuntimeException( "couldn't build HunchTopic object!", e );
		}

		completedCallback.callComplete( hTopic );

	}

	public void searchForTopic( Map< String, String > params,
			HunchAPI.Callback completedCallback )
	{
		assureParams( params, "query" );

		Request searchForTopicRequest = new Request( "searchForTopic", params );
		Response r;

		try
		{
			r = searchForTopicRequest.execute();
		} catch ( IOException e )
		{
			throw new RuntimeException( "could not execute searchForTopic!", e );
		}

		JSONArray topics;
		HunchList< HunchTopic > resultList = new HunchList< HunchTopic >();

		try
		{
			topics = r.getJSON().getJSONArray( "topics" );
		} catch ( JSONException e )
		{
			throw new RuntimeException( "could not execute searchForTopic!", e );
		}

		for ( int i = 0; i < topics.length(); i++ )
		{
			JSONObject topic;

			try
			{
				topic = topics.getJSONObject( i );
			} catch ( JSONException e )
			{
				throw new RuntimeException( "couldn't build HunchTopic!", e );
			}

			if ( topic == null )
				break;

			HunchTopic hTopic;
			HunchTopic.Builder b = HunchTopic.getBuilder();

			int id;
			double score;
			String decision, urlName;

			try
			{
				id = topic.getInt( "id" );
				score = topic.getDouble( "score" );
				decision = topic.getString( "decision" );
				urlName = topic.getString( "urlName" );
			} catch ( JSONException e )
			{
				throw new RuntimeException( "couldn't build HunchTopic!", e );
			}

			b.init( topic )
					.setId( id )
					.setDecision( decision )
					.setUrlName( urlName )
					.setScore( score );

			hTopic = b.buildForSearch();
			resultList.add( hTopic );

		}

		completedCallback.callComplete( resultList );

	}

	public void searchForResult( Map< String, String > params,
			HunchAPI.Callback completedCallback )
	{

	}

	public void searchForQuestion( Map< String, String > params,
			HunchAPI.Callback completedCallback )
	{

	}

	/**
	 * Lists all Hunch Topics in a given category.
	 * 
	 * @param params
	 *            Parameters to pass in the URL request.
	 * @param completedCallback
	 *            Callback to call upon completion of this request. Passes a
	 *            {@link HunchList} of {@link HunchTopic}s as the sole argument.
	 * @throws RuntimeException
	 *             Upon failure to complete the call.
	 */
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

		HunchList< HunchTopic > hunchTopics = new HunchList< HunchTopic >();

		try
		{
			JSONArray topics = r.getJSON().getJSONArray( "topics" );

			if ( params.containsKey( "limit" ) )
			{
				Integer limit = new Integer( params.get( "limit" ) );
				for ( int i = 0; i < limit; i++ )
				{
					JSONObject topic = topics.getJSONObject( i );
					HunchTopic h = HunchTopic.buildFromJSON( topic );
					hunchTopics.add( h );
				}

				params.remove( "limit" );
			} else
			{
				for ( int i = 0; i < topics.length(); i++ )
				{
					JSONObject topic = topics.getJSONObject( i );
					HunchTopic h = HunchTopic.buildFromJSON( topic );
					hunchTopics.add( h );
				}
			}
			
			for ( int i = 0; i < topics.length(); i++ )
			{
				JSONObject topic = topics.getJSONObject( i );
				HunchTopic h = HunchTopic.buildFromJSON( topic );
				hunchTopics.add( h );
			}

		} catch ( JSONException e )
		{
			throw new RuntimeException( "could not execute listTopics!", e );
		}

		completedCallback.callComplete( hunchTopics );
	}

	/**
	 * Lists all Hunch question categories.
	 * 
	 * @param params
	 *            Map of GET parameters
	 * @param completedCallback
	 *            Callback to call upon completion. Passes a {@link HunchList}
	 *            of {@link HunchCategory}s as the sole argument.
	 * @throws RuntimeException
	 *             Upon failure to complete the call.
	 */
	public void listCategories( Map< String, String > params,
			HunchAPI.Callback completedCallback )
	{
		// list categories requires no parameters
		// Handler h = new Handler();

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

		HunchList< HunchCategory > catList = new HunchList< HunchCategory >();

		try
		{
			JSONArray cats = r.getJSON().getJSONArray( "categories" );

			for ( int i = 0; i < cats.length(); i++ )
			{
				JSONObject category = cats.getJSONObject( i );
				HunchCategory cat = HunchCategory.buildFromJSON( category );

				catList.add( cat );
			}

			Log.d( Const.TAG,
					"listCategories call complete, calling callback" );
		} catch ( JSONException e )
		{
			throw new RuntimeException( "could not execute listCategories!", e );
		}

		completedCallback.callComplete( catList );

	}

	public void responseStats( Map< String, String > params,
			HunchAPI.Callback completedCallback )
	{

	}

	public void responsePairStats( Map< String, String > params,
			HunchAPI.Callback completedCallback )
	{

	}

	public void responsePositiveCorrelations( Map< String, String > params,
			HunchAPI.Callback completedCallback )
	{

	}

	public void responseNegativeCorrelations( Map< String, String > params,
			HunchAPI.Callback completedCallback )
	{

	}

	public void nextQuestion( Map< String, String > params,
			HunchAPI.Callback completedCallback )
	{
		assureParams( params, "topicId" );

		Request nextQuestionRequest = new Request( "nextQuestion", params );

		Response r;
		try
		{
			r = nextQuestionRequest.execute();
		} catch ( IOException e )
		{
			throw new RuntimeException( "Couldn't execute nextQuestion request!", e );
		}

		HunchNextQuestion nextQuestion = HunchNextQuestion.buildFromJSON( r.getJSON() );

		completedCallback.callComplete( nextQuestion );

	}

	public void rankedResults( Map< String, String > params,
			HunchAPI.Callback completedCallback )
	{

	}

	private static void assureParams( Map< String, String > m, String first,
			String... rest )
	{
		if ( m == null )
			throw new IllegalArgumentException(
					"Can not assure parameters on a null map!" );

		if ( !m.containsKey( first ) || m.get( first ) == null )
			throw new AssertionError( "the " + first
					+ " parameter must be present and has not been found!" );

		for ( String s : rest )
		{
			if ( !m.containsKey( s ) || m.get( s ) == null )
				throw new AssertionError( "the " + s
						+ " parameter must be present and has not been found!" );
		}
	}

	private static void assureOneOf( Map< String, String > m, String first, String... rest )
	{
		if ( m == null )
			throw new IllegalArgumentException(
					"Can not assure parameters on a null map!" );

		boolean found = false;

		if ( m.containsKey( first ) && m.get( first ) != null )
			found = true;

		for ( String s : rest )
		{
			if ( m.containsKey( s ) && m.get( s ) != null )
			{
				if ( found )
				{
					throw new AssertionError( "only one parameter allowed!" );
				} else
				{
					found = true;
				}
			}
		}

		if ( !found )
			throw new AssertionError( "did not find any of the parameters!" );
	}

}
