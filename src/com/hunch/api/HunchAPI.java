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

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.hunch.Const;

public class HunchAPI
{

	public static final String API_KEY = "public_devKey";

	final class ConnectionInfo
	{

		// no instantiation please
		private ConnectionInfo()
		{
		}

		public static final String host = "api.hunch.com";
		public static final String api_extention = "api";
	}

	/*
	 * The lone instance of HunchAPI
	 */
	private static HunchAPI instance = null;

	/*
	 * Private constructor - only one instance of the Hunch API should exist at
	 * any one point in time. The Hunch API is static by nature, so each call
	 * could be implemented by a single static method. Each client of the API
	 * can therefore safely share a single instance.
	 */
	private HunchAPI()
	{
	}

	/*
	 * The only way to get the lone instance of the Hunch API.
	 */
	public static synchronized HunchAPI getInstance()
	{
		if ( instance == null )
			instance = new HunchAPI();

		return instance;
	}

	public void getResult( String resultId, String resultImgSize,
			final HunchResult.Callback completedCallback )
	{
		if ( resultId == null )
			throw new IllegalArgumentException( "must set resultId!" );

		HunchAPIRequest resultRequest = new HunchAPIRequest( "getResult" );

		resultRequest.addParam( "resultId", resultId );
		resultRequest.addParam( "resultImgSize", resultImgSize );

		resultRequest.execute( new HunchAPIResponseCallback()
		{

			@Override
			public void callComplete( JSONObject h )
			{
				JSONObject result;
				try
				{
					result = h.getJSONObject( "result" );
				} catch ( JSONException e )
				{
					throw new RuntimeException( "Couldn't execute a result request!", e );
				}

				HunchResult ret = HunchResult.buildFromJSON( result );

				completedCallback.callComplete( ret );

			}
		} );

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
	public void getResponse( String responseId, String responseImgSize,
			final HunchResponse.Callback completedCallback )
	{
		if ( responseId == null )
			throw new IllegalArgumentException( "must set responseId!" );

		HunchAPIRequest responseRequest = new HunchAPIRequest( "getResponse" );

		responseRequest.addParam( "responseId", responseId );
		responseRequest.addParam( "responseImgSize", responseImgSize );

		responseRequest.execute( new HunchAPIResponseCallback()
		{

			@Override
			public void callComplete( JSONObject j )
			{
				JSONObject response;
				try
				{
					response = j.getJSONObject( "response" );
				} catch ( JSONException e )
				{
					throw new RuntimeException( "could not execute getResponse!", e );
				}

				HunchResponse ret = HunchResponse.buildFromJSON( response );

				completedCallback.callComplete( ret );
			}
		} );

	}

	/**
	 * Retrieves a specific question.
	 * 
	 * @param questionId
	 *            The id of the Hunch question
	 * @param questionImgSize
	 *            The size of the icon returned, in the form "32x32"
	 * @param completedCallback
	 *            Callback to call upon completion of this request. Passes a
	 *            {@link HunchQuestion} as the sole argument.
	 * @throws RuntimeException
	 *             Upon failure to complete the call.
	 */
	public void getQuestion( String questionId, String questionImgSize,
			final HunchQuestion.Callback completedCallback )
	{
		if ( questionId == null )
			throw new IllegalArgumentException( "must set questionId!" );

		// send the response
		HunchAPIRequest questionRequest = new HunchAPIRequest( "getQuestion" );

		questionRequest.addParam( "questionId", questionId );
		questionRequest.addParam( "questionImgSize", questionImgSize );

		questionRequest.execute( new HunchAPIResponseCallback()
		{

			@Override
			public void callComplete( JSONObject j )
			{
				// get the question data, build the object, and call back to the
				// client
				JSONObject question;
				try
				{
					question = j.getJSONObject( "question" );
				} catch ( JSONException e )
				{
					throw new RuntimeException( "could not execute getQuestion!", e );
				}

				HunchQuestion ret = HunchQuestion.buildFromJSON( question );

				completedCallback.callComplete( ret );
			}
		} );

	}

	/**
	 * Retrieves a specific Hunch topic.
	 * 
	 * Specify either a topicId, or a urlName, but not both.
	 * 
	 * @param topicId
	 *            The id of the topic to return
	 * @param urlName
	 *            The url textual identifier for the topic, which can be found
	 *            in the Hunch URL for the topic
	 * @param topicImgSize
	 *            The size of the image to return for this topic.
	 */
	public void getTopic( String topicId, String urlName, String topicImgSize,
			final HunchTopic.Callback completedCallback )
	{
		if ( topicId == null && urlName == null )
			throw new IllegalArgumentException( "must set topicId or urlName!" );

		if ( topicId != null && urlName != null )
			throw new IllegalArgumentException( "can not set both topicId and urlName!" );

		HunchAPIRequest getTopicRequest = new HunchAPIRequest( "getTopic" );

		getTopicRequest.addParam( "topicId", topicId );
		getTopicRequest.addParam( "urlName", urlName );
		getTopicRequest.addParam( "topicImgSize", topicImgSize );

		getTopicRequest.execute( new HunchAPIResponseCallback()
		{

			@Override
			public void callComplete( JSONObject j )
			{
				JSONObject topic;
				try
				{
					topic = j.getJSONObject( "topic" );
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

				IHunchTopic hTopic;
				HunchTopic.Builder b = HunchTopic.getBuilder();

				b.init( j );
				try
				{
					b.setId( String.valueOf( id ) )
							.setDecision( topic.getString( "decision" ) )
							.setUrlName( topic.getString( "urlName" ) )
							.setShortName( topic.getString( "shortName" ) )
							.setHunchUrl( topic.getString( "hunchUrl" ) )
							.setImageUrl( topic.getString( "imageUrl" ) )
							.setResultType( topic.getString( "resultType" ) )
							.setIsEitherOr( eitherOr == 1 );

					JSONObject category = topic.getJSONObject( "category" );

					HunchCategory hCategory = HunchCategory.getBuilder().init( category )
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
		} );

	}

	public void searchForTopic( String query,
			final HunchTopic.ListCallback completedCallback )
	{
		if ( query == null )
			throw new IllegalArgumentException( "must set query!" );

		HunchAPIRequest searchForTopicRequest = new HunchAPIRequest( "searchForTopic" );

		searchForTopicRequest.addParam( "query", query );

		searchForTopicRequest.execute( new HunchAPIResponseCallback()
		{

			@Override
			public void callComplete( JSONObject j )
			{
				JSONArray topics;
				List< IHunchTopic > resultList = new ArrayList< IHunchTopic >();

				try
				{
					topics = j.getJSONArray( "topics" );
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
							.setId( String.valueOf( id ) )
							.setDecision( decision )
							.setUrlName( urlName )
							.setScore( score );

					hTopic = b.buildForSearch();
					resultList.add( hTopic );

				}

				completedCallback.callComplete( resultList );
			}
		} );

	}

	public void searchForResult( String query, String topicId,
			HunchResult.ListCallback completedCallback )
	{

	}

	public void searchForQuestion( String query, boolean extended,
			HunchQuestion.ListCallback completedCallback )
	{

	}

	/**
	 * Lists all Hunch Topics in a given category.
	 * 
	 * @param categoryUrlName
	 *            The textual identifier for the category.
	 * @param topicImgSize
	 *            The size of the image to return.
	 * @param limit
	 *            The number of topics to deserialize at once.
	 * @param completedCallback
	 *            Callback to call upon completion of this request. Passes a
	 *            {@link HunchList} of {@link HunchTopic}s as the sole argument.
	 * @throws RuntimeException
	 *             Upon failure to complete the call.
	 */
	public void listTopics( String categoryUrlName, String topicImgSize, 
			final HunchTopic.ListCallback completedCallback )
	{
		if ( categoryUrlName == null )
			throw new IllegalArgumentException( "must set categoryUrlName" );

		HunchAPIRequest listTopicsRequest = new HunchAPIRequest( "listTopics" );

		listTopicsRequest.addParam( "categoryUrlName", categoryUrlName );
		listTopicsRequest.addParam( "topicImgSize", topicImgSize );

		listTopicsRequest.execute( new HunchAPIResponseCallback()
		{

			@Override
			public void callComplete( JSONObject j )
			{
				List< IHunchTopic > hunchTopics = new ArrayList< IHunchTopic >();

				try
				{
					JSONArray topics = j.getJSONArray( "topics" );

//					if ( limit != null )
//					{
//						Log.d( Const.TAG, "only returning " + limit
//								+ " of " + topics.length() + " HunchTopics from listTopics()" );
//
//						// don't auto-unbox on every iteration please
//						final int intLimit = limit;
//						for ( int i = 0; i < intLimit; i++ )
//						{
//							JSONObject topic = topics.getJSONObject( i );
//							HunchTopic h = HunchTopic.buildFromJSON( topic );
//							hunchTopics.add( h );
//						}
//					} else
//					{
					for ( int i = 0; i < topics.length(); i++ )
					{
						JSONObject topic = topics.getJSONObject( i );
						IHunchTopic h = new LazyHunchTopic( topic );
						
						hunchTopics.add( h );
					}
//					}

				} catch ( JSONException e )
				{
					throw new RuntimeException( "could not execute listTopics!", e );
				}

				completedCallback.callComplete( hunchTopics );
			}
		} );

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
	public void listCategories( final HunchCategory.ListCallback completedCallback )
	{
		// list categories requires no parameters

		HunchAPIRequest listCategoriesRequest = new HunchAPIRequest( "listCategories" );
		
		listCategoriesRequest.execute( new HunchAPIResponseCallback()
		{
			
			@Override
			public void callComplete( JSONObject j )
			{
				List< HunchCategory > catList = new ArrayList< HunchCategory >();

				try
				{
					JSONArray cats = j.getJSONArray( "categories" );

					for ( int i = 0; i < cats.length(); i++ )
					{
						JSONObject category = cats.getJSONObject( i );
						HunchCategory cat = HunchCategory.buildFromJSON( category );

						catList.add( cat );
					}

				} catch ( JSONException e )
				{
					throw new RuntimeException( "could not execute listCategories!", e );
				}
				
				Log.v( Const.TAG, "listCategories call complete, calling callback" );

				completedCallback.callComplete( catList );
				
			}
		} );

	}
	
	public void recentTopics( int numToReturn, String topicImgSize,
			final HunchTopic.ListCallback callback )
	{
		
	}
	
	public void recommendedTopics( int numToReturn, String authToken, String topicImgSize, 
			final HunchTopic.ListCallback callback )
	{
		
	}

	/*
	public void responseStats( String responseId,
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

	*/
	
	public void nextQuestion( String topicId, String qaState, String authToken, String questionImgSize,
			String responseImgSize, String topicImgSize, final HunchNextQuestion.Callback completedCallback )
	{
		if( topicId == null )
			throw new IllegalArgumentException( "must set topicId" );

		HunchAPIRequest nextQuestionRequest = new HunchAPIRequest( "nextQuestion" );
		
		nextQuestionRequest.addParam( "topicId", topicId );
		nextQuestionRequest.addParam( "qaState", qaState );
		nextQuestionRequest.addParam( "authToken", authToken );
		nextQuestionRequest.addParam( "questionImgSize", questionImgSize );
		nextQuestionRequest.addParam( "responseImgSize", responseImgSize );
		nextQuestionRequest.addParam( "topicImgSize", topicImgSize );
		
		nextQuestionRequest.execute( new HunchAPIResponseCallback()
		{
			
			@Override
			public void callComplete( JSONObject j )
			{
				HunchNextQuestion nextQuestion = HunchNextQuestion.buildFromJSON( j );

				completedCallback.callComplete( nextQuestion );			
			}
		} );

	}

	public void rankedResults( String topicId, String responses, String authToken,
			final HunchRankedResults.Callback completedCallback )
	{
		if( topicId == null )
			throw new IllegalArgumentException( "must set topicId!" );

		HunchAPIRequest rankedResultsRequest = new HunchAPIRequest( "rankedResults" );
		
		rankedResultsRequest.addParam( "topicId", topicId );
		rankedResultsRequest.addParam( "responses", responses );
		rankedResultsRequest.addParam( "authToken", authToken );
		
		rankedResultsRequest.execute( new HunchAPIResponseCallback()
		{
			
			@Override
			public void callComplete( JSONObject j )
			{
				HunchRankedResults results = HunchRankedResults.buildFromJSON( j );

				completedCallback.callComplete( results );
				
			}
		} );

	}

}
