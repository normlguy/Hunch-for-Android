package com.hunch.api;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hunch.Const;

import android.util.Log;

/**
 * This class is a representation of the JSON object returned by the
 * nextQuestion call of the HunchAPI.
 * 
 * @author Tyler Levine
 * @since Dec 8, 2009
 * 
 */
public class HunchNextQuestion extends HunchObject
{

	private static Builder b;
	
	/*
	 * The various varieties of this object returned by the API.
	 * 
	 * Different varieties have different capabilities. Runtime checking
	 * of variety should help reduce possible NPE's
	 */
	public enum Variety
	{
		DEFAULT, RESULTS
	}

	static class Builder extends HunchObject.Builder
	{

		private JSONObject val;
		private HunchQuestion buildNextQuestion;
		private String buildPrevQAState, buildRankedResultResponses;
		private HunchTopic buildTopic;

		private Builder()
		{
		}

		@Override
		Builder init( JSONObject jsonVal )
		{
			val = jsonVal;
			return this;
		}

		Builder setNextQuestion( HunchQuestion nextQuestion )
		{
			buildNextQuestion = nextQuestion;
			return this;
		}

		Builder setPrevQAState( String prevQAState )
		{
			buildPrevQAState = prevQAState;
			return this;
		}

		Builder setRankedResultResponses( String rankedResultResponses )
		{
			buildRankedResultResponses = rankedResultResponses;
			return this;
		}

		Builder setTopic( HunchTopic topic )
		{
			buildTopic = topic;
			return this;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.hunch.api.HunchObject.Builder#build()
		 */
		@Override
		HunchNextQuestion build()
		{
			if ( val == null )
			{
				throw new IllegalStateException(
				"Not all required fields set before building HunchNextQuestion! (JSON value null)" );
			}
			else if( buildNextQuestion == null )
			{
				throw new IllegalStateException(
				"Not all required fields set before building HunchNextQuestion! (nextQuestion null)" );
			}
			else if( buildTopic == null )
			{
				throw new IllegalStateException(
				"Not all required fields set before building HunchNextQuestion! (buildTopic null)" );
			}

			/*
			 * if ( buildPrevQAState == null ) buildPrevQAState = "";
			 * 
			 * if ( buildRankedResultResponses == null )
			 * buildRankedResultResponses = "";
			 */

			HunchNextQuestion ret = new HunchNextQuestion( val, buildNextQuestion, buildTopic,
					buildPrevQAState, buildRankedResultResponses, Variety.DEFAULT );

			reset();

			return ret;
		}
		
		HunchNextQuestion buildForResults()
		{
			if ( val == null )
			{
				throw new IllegalStateException(
				"Not all required fields set before building HunchNextQuestion! (JSON value null)" );
			} 
			else if ( buildRankedResultResponses == null )
			{
				throw new IllegalStateException(
				"Not all required fields set before building HunchNextQuestion! (RankedResults value null)" );
			}
			
			HunchNextQuestion ret = new HunchNextQuestion( val, null, null, null,
					buildRankedResultResponses, Variety.RESULTS );
			
			reset();
			
			return ret;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.hunch.api.HunchObject.Builder#reset()
		 */
		@Override
		void reset()
		{
			val = null;
			buildNextQuestion = null;
			buildPrevQAState = null;
			buildRankedResultResponses = null;
			buildTopic = null;
		}

	}

	private final JSONObject json;
	private final HunchQuestion nextQuestion;
	private final HunchTopic topic;
	private final String prevQAState, rankedResultResponses;
	private final Variety variety;

	private HunchNextQuestion( JSONObject json, HunchQuestion nextQuestion, HunchTopic topic,
			String prevQAState, String rankedResultResponses, Variety v )
	{
		this.json = json;
		this.nextQuestion = nextQuestion;
		this.topic = topic;
		this.prevQAState = prevQAState;
		this.rankedResultResponses = rankedResultResponses;
		this.variety = v;
	}

	public HunchQuestion getNextQuestion()
	{
		assert nextQuestion != null;
		
		return nextQuestion;
	}

	public HunchTopic getTopic()
	{
		assert topic != null;
		
		return topic;
	}

	public String getPrevQAState()
	{
		assert prevQAState != null;
		
		return prevQAState;
	}

	public String getRankedResultResponses()
	{
		assert rankedResultResponses != null;
		
		return rankedResultResponses;
	}
	
	public Variety getVariety()
	{
		assert variety != null;
		
		return variety;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hunch.api.HunchObject#getJSON()
	 */
	@Override
	public JSONObject getJSON()
	{
		assert json != null;
		
		return json;
	}

	public static HunchNextQuestion.Builder getBuilder()
	{
		if ( b == null )
			b = new HunchNextQuestion.Builder();

		return b;
	}

	public static HunchNextQuestion buildFromJSON( final JSONObject response )
	{
		final HunchNextQuestion.Builder nqBuilder = getBuilder();
		
		if ( response.has( "rankedResultResponses" ) && response.length() == 1 )
		{
			// this is the last question in the topic
			// it contains only the list of results to be passed
			// to rankedResponses
			try
			{
				
				nqBuilder.init( response )
				.setRankedResultResponses( response.getString( "rankedResultResponses" ) );
			} catch ( JSONException e )
			{
				throw new RuntimeException( "Couldn't build HunchNextQuestion!", e );
			}
			
			return nqBuilder.buildForResults();
		}
		
		final HunchQuestion.Builder qBuilder = HunchQuestion.getBuilder();
		final HunchTopic.Builder tBuilder = HunchTopic.getBuilder();
		final HunchResponse.Builder respBuilder = HunchResponse.getBuilder();
		final HunchCategory.Builder catBuilder = HunchCategory.getBuilder();

		try
		{
			final JSONObject jsonNextQuestion = response.getJSONObject( "nextQuestion" );

			List< HunchResponse > responses = new ArrayList< HunchResponse >();

			JSONArray jsonResponses = jsonNextQuestion.getJSONArray( "responses" );

			for ( int i = 0; i < jsonResponses.length(); i++ )
			{
				JSONObject jsonResponse = jsonResponses.getJSONObject( i );
				
				/*
				 * 	image url can be not set on some				 
				 *  response objects returned by the Hunch API
				 *  --
				 *  in order to avoid jumping to the catch block
				 *  upon missing imageUrl, we need to handle it
				 *  in it's own try block
				 */
				
				String respImgUrl = null;
				try
				{
					respImgUrl = jsonResponse.getString( "imageUrl" );
				} catch ( JSONException e )
				{
					Log.d( Const.TAG, "got response object with no imageUrl " +
							"in HunchNextQuestion.buildFromJSON()" );
				}
				
				/*
				 * 	Same deal here, the responseID is not set for 
				 *  "skip this question" responses, handle it separately
				 */
				
				Integer respID = null;
				try
				{
					respID = jsonResponse.getInt( "responseId" );
				} catch ( JSONException e )
				{
					//Log.d( Const.TAG, "couldn't find ID for HunchResponse! (probably a " +
					//		"\"skip this question\" response)" );
				}
				
				Log.d( Const.TAG, "building hunchResponse...\n" + jsonResponse.toString( 4 ) );
				
				respBuilder.init( jsonResponse )
						.setId( respID )
						.setText( jsonResponse.getString( "responseText" ) )
						.setQAState( jsonResponse.getString( "qaState" ) )
						.setImageUrl( respImgUrl );

				HunchResponse resp = respBuilder.buildForNextQuestion();

				responses.add( resp );
			}
			
			Log.d( Const.TAG, "building HunchQuestion...\n" + jsonNextQuestion.toString( 4 ) );
			
			/*
			 * Again, imageUrl can be null sometimes when the
			 * API returns a hunchQuestion as part of a
			 * nextQuestion call, so we have to handle it 
			 * in a separate try block 
			 */
			String questionImageUrl = null;
			try
			{
				questionImageUrl = jsonNextQuestion.getString( "imageUrl" );
			} catch ( JSONException e )
			{
				Log.d( Const.TAG, "got question object with no imageUrl " +
				"in HunchNextQuestion.buildFromJSON()" );
			}

			// build the question
			qBuilder.init( jsonNextQuestion )
					.setId( jsonNextQuestion.getInt( "questionId" ) )
					.setText( jsonNextQuestion.getString( "questionText" ) )
					.setQuestionNumber( jsonNextQuestion.getInt( "questionNumber" ) )
					.setImageUrl( questionImageUrl )
					.setResponses( responses );

			final HunchQuestion nextQuestion = qBuilder.buildForNextQuestion();

			JSONObject jsonTopic = response.getJSONObject( "topic" );

			JSONObject jsonCategory = jsonTopic.getJSONObject( "category" );
			
			Log.d( Const.TAG, "building HunchCategory...\n" + jsonCategory.toString( 4 ) );

			catBuilder.init( jsonCategory )
					.setUrlName( jsonCategory.getString( "categoryUrlName" ) )
					.setName( jsonCategory.getString( "categoryName" ) )
					.setImageUrl( jsonCategory.getString( "categoryImageUrl" ) );
			
			Log.d( Const.TAG, "building HunchTopic...\n" + jsonTopic.toString( 4 ) );
			
			tBuilder.init( jsonTopic )
			.setId( jsonTopic.getInt( "topicId" ) )
			.setDecision( jsonTopic.getString( "topicDecision" ) )
			.setHunchUrl( jsonTopic.getString( "hunchUrl" ) )
			.setImageUrl( jsonTopic.getString( "imageUrl" ) )
			.setCategory( catBuilder.build() );
			
			final HunchTopic topic = tBuilder.buildForNextQuestion();
			
			/*
			 * PrevQaState is left unset by the API on the first question.
			 * Again, gotta handle it separately.
			 */
			
			String prevQaState = null;
			try
			{
				prevQaState = response.getString( "prevQaState" );
			} catch ( JSONException e )
			{
				Log.d( Const.TAG, "got nextQuestion object with no prevQaState " +
				"in HunchNextQuestion.buildFromJSON() (probably first question)" );
			}
			
			nqBuilder.init( jsonNextQuestion )
			.setNextQuestion( nextQuestion )
			.setTopic( topic )
			.setPrevQAState( prevQaState )
			.setRankedResultResponses( response.getString( "rankedResultResponses" ) );

		} catch ( JSONException e )
		{
			throw new RuntimeException( "Couldn't build HunchNextQuestion!", e );
		}
		
		return nqBuilder.build();
	}

}
