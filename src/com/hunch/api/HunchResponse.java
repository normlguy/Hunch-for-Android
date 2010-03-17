package com.hunch.api;

import org.json.*;

import com.hunch.Const;

import android.util.Log;

/**
 * 
 * 
 * @author Tyler Levine Nov 5, 2009
 * 
 */
public class HunchResponse extends HunchObject
{

	private static Builder b;
	private static int THAY_TOPIC_ID = -3;
	
	public interface Callback
	{
		public void callComplete( HunchResponse h );
	}
	
	public enum Variety
	{
		DEFAULT, QUESTION, NEXT_QUESTION
	}

	static class Builder extends HunchObject.Builder
	{

		private JSONObject val;
		private Integer __id, __order, __questionId, __topicId;
		private String __text, __qaState, __imageUrl;

		private Builder()
		{
		}

		@Override
		Builder init( JSONObject jsonVal )
		{
			val = jsonVal;
			return this;
		}

		Builder setId( Integer id )
		{
			__id = id;
			return this;
		}

		Builder setTopicId( Integer topicId )
		{
			__topicId = topicId;
			return this;
		}

		Builder setText( String text )
		{
			__text = text;
			return this;
		}

		Builder setQuestionId( Integer questionId )
		{
			__questionId = questionId;
			return this;
		}

		Builder setOrder( Integer order )
		{
			__order = order;
			return this;
		}

		Builder setQAState( String qaState )
		{
			__qaState = qaState;
			return this;
		}

		Builder setImageUrl( String imageUrl )
		{
			__imageUrl = imageUrl;
			return this;
		}

		@Override
		void reset()
		{
			val = null;
			__text = null;
			__id = null;
			__topicId = null;
			__order = null;
			__questionId = null;
			__qaState = null;
			__imageUrl = null;
		}

		@Override
		HunchResponse build()
		{
			assureBuildParams();

			return buildInternal( Variety.DEFAULT );
		}

		HunchResponse buildForQuestion()
		{
			assureQuestionBuildParams();

			return buildInternal( Variety.QUESTION );
		}

		HunchResponse buildForNextQuestion()
		{
			assureNextQuestionBuildParams();

			return buildInternal( Variety.NEXT_QUESTION );
		}

		private void assureQuestionBuildParams()
		{
			if ( val == null || __id == Integer.MIN_VALUE )
			{
				throw new IllegalStateException(
						"Not all required fields set before building HunchQuestion!" );
			}
		}

		private void assureNextQuestionBuildParams()
		{
			if ( val == null || __text == null || __qaState == null )
			{
				throw new IllegalStateException(
						"Not all required fields set before building HunchQuestion!" );
			}

			if ( __id == null )
				Log.i(	Const.TAG, "building HunchResponse for NextQuestion " +
						"without ID! (probably a \"skip this question\" response)" );
		}

		private void assureBuildParams()
		{
			if ( val == null || __text == null || __order == null
					|| __questionId == null || __id == null
					|| __topicId == null )
			{
				throw new IllegalStateException(
						"Not all required fields set before building HunchQuestion!" );
			}
		}

		private HunchResponse buildInternal( Variety v )
		{
			HunchResponse ret = new HunchResponse( val, __text, __qaState, __imageUrl,
					__order, __id, __topicId, __questionId, v );
			
			reset();

			return ret;
		}
	}

	private final JSONObject json;
	private final Integer id, order, questionId, topicId;
	private final String text, qaState, imageUrl;
	private final Variety variety;

	private HunchResponse( JSONObject jsonObj, String text, String qaState, String imageUrl,
			Integer order, Integer id, Integer topicId, Integer questionId, Variety v )
	{
		this.json = jsonObj;
		this.text = text;
		this.order = order;
		this.id = id;
		this.topicId = topicId;
		this.questionId = questionId;
		this.qaState = qaState;
		this.imageUrl = imageUrl;
		
		this.variety = v;
	}

	public static Builder getBuilder()
	{
		if ( b == null )
			b = new Builder();

		return b;
	}

	public String getQAState()
	{
		return qaState;
	}

	public String getImageUrl()
	{
		return imageUrl;
	}

	public String getText()
	{
		return text;
	}

	public Integer getOrder()
	{
		return order;
	}

	public Integer getId()
	{
		return id;
	}

	public Integer getTopicId()
	{
		return topicId;
	}

	public Integer getQuestionId()
	{
		return questionId;
	}
	
	public Variety getVariety()
	{
		return variety;
	}

	static HunchResponse buildFromJSON( JSONObject json )
	{
		HunchResponse.Builder builder = getBuilder();

		// build the HunchResponse object
		int id = Integer.MIN_VALUE, topicId = Integer.MIN_VALUE;
		int questionId = Integer.MIN_VALUE, order = Integer.MIN_VALUE;
		String text = "";

		try
		{
			String tId = json.getString( "topicId" );
			try
			{
				topicId = Integer.parseInt( tId );
			} catch ( NumberFormatException e )
			{
				if ( tId.equals( "THAY" ) )
				{
					// Teach Hunch About You!
					topicId = THAY_TOPIC_ID;
				} else
				{
					// some other cause of the NFE
					throw new RuntimeException( "Couldn't build HunchResponse!", e );
				}
			}

			id = Integer.parseInt( json.getString( "id" ) );
			questionId = Integer.parseInt( json.getString( "questionId" ) );
			order = Integer.parseInt( json.getString( "order" ) );
			text = json.getString( "text" );

		} catch ( NumberFormatException e )
		{
			throw new RuntimeException( "Couldn't build HunchResponse!", e );
		} catch ( JSONException e )
		{
			throw new RuntimeException( "Couldn't build HunchResponse!", e );
		} finally
		{
			builder.init( json )
					.setId( id )
					.setTopicId( topicId )
					.setQuestionId( questionId )
					.setOrder( order )
					.setText( text );
		}

		return builder.build();
	}

	@Override
	public JSONObject getJSON()
	{
		return json;
	}

}
