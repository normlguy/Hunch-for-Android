package com.hunch.api;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HunchQuestion extends HunchObject
{

	private static Builder b;
	private static int THAY_TOPIC_ID = -3;

	static class Builder extends HunchObject.Builder
	{

		private JSONObject val;
		private int __id, __topicId, __questionNumber;
		private String __text, __imageUrl;
		private List< HunchResponse > __responses;

		private Builder()
		{
		}

		@Override
		Builder init( JSONObject jsonVal )
		{
			val = jsonVal;
			return this;
		}

		Builder setId( int id )
		{
			__id = id;
			return this;
		}

		Builder setTopicId( int topicId )
		{
			__topicId = topicId;
			return this;
		}

		Builder setText( String text )
		{
			__text = text;
			return this;
		}

		Builder setImageUrl( String imageUrl )
		{
			__imageUrl = imageUrl;
			return this;
		}

		Builder setResponses( List< HunchResponse > responses )
		{
			__responses = responses;
			return this;
		}

		Builder setQuestionNumber( int questionNumber )
		{
			__questionNumber = questionNumber;
			return this;
		}

		@Override
		void reset()
		{
			val = null;
			__text = null;
			__imageUrl = null;
			__responses = null;
			__questionNumber = __id = __topicId = Integer.MIN_VALUE;
		}

		@Override
		HunchQuestion build()
		{
			assureBuildParams();

			return buildInternal();
		}

		HunchQuestion buildForNextQuestion()
		{
			assureNextQuestionBuildParams();

			return buildInternal();
		}

		private void assureNextQuestionBuildParams()
		{
			if ( val == null || __id == Integer.MIN_VALUE || __text == null
					|| __questionNumber == Integer.MIN_VALUE || __responses == null )
			{
				throw new IllegalStateException(
				"Not all required fields set before building HunchQuestion!" );
			}
		}

		private void assureBuildParams()
		{
			if ( val == null || __text == null || __imageUrl == null || __responses == null
					|| __id == Integer.MIN_VALUE || __topicId == Integer.MIN_VALUE )
			{
				throw new IllegalStateException(
						"Not all required fields set before building HunchQuestion!" );
			}
		}

		private HunchQuestion buildInternal()
		{
			HunchQuestion ret = new HunchQuestion( val, __text, __imageUrl, __id, __topicId,
					__responses );
			reset();

			return ret;
		}
	}

	private final JSONObject json;
	private final String _text, _imageUrl;
	private final int _id, _topicId;
	private final List< HunchResponse > _responses;

	private HunchQuestion( JSONObject jsonObj, String text, String imageUrl, int id,
			int topicId, List< HunchResponse > responseIds )
	{
		json = jsonObj;
		_text = text;
		_imageUrl = imageUrl;
		_id = id;
		_topicId = topicId;
		_responses = responseIds;
	}

	public static Builder getBuilder()
	{
		if ( b == null )
			b = new Builder();

		return b;
	}

	public String getText()
	{
		return _text;
	}

	public String getImageUrl()
	{
		return _imageUrl;
	}

	public int getId()
	{
		return _id;
	}

	public int getTopicId()
	{
		return _topicId;
	}

	public List< HunchResponse > getResponseIds()
	{
		return _responses;
	}

	static HunchQuestion buildFromJSON( JSONObject json )
	{
		final HunchQuestion.Builder builder = getBuilder();
		final HunchResponse.Builder respBuilder = HunchResponse.getBuilder();

		// build the HunchQuestion object
		int id = Integer.MIN_VALUE, topicId = Integer.MIN_VALUE;
		String text = "", imgUrl = "";
		JSONArray jsonResIds;
		List< HunchResponse > responses = new ArrayList< HunchResponse >();

		try
		{
			jsonResIds = json.getJSONArray( "responseIds" );
		} catch ( JSONException e )
		{
			throw new RuntimeException( "Couldn't build HunchQuestion!", e );
		}

		for ( int i = 0; i < jsonResIds.length(); i++ )
		{
			JSONObject obj = new JSONObject();
			try
			{
				obj.put( "responseIds", jsonResIds );
			} catch ( JSONException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			respBuilder.init( obj );

			try
			{
				respBuilder.setId( jsonResIds.getInt( i ) );
			} catch ( JSONException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			responses.add( respBuilder.buildForQuestion() );
		}

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
					throw new RuntimeException( "Couldn't build HunchQuestion!", e );
				}
			}

			id = json.getInt( "id" );
			text = json.getString( "text" );
			imgUrl = json.getString( "imageUrl" );

		} catch ( NumberFormatException e )
		{
			throw new RuntimeException( "Couldn't build HunchQuestion!", e );
		} catch ( JSONException e )
		{
			throw new RuntimeException( "Couldn't build HunchQuestion!", e );
		} finally
		{
			builder.init( json ).setId( id ).setTopicId( topicId ).setText( text )
					.setImageUrl( imgUrl ).setResponses( responses );
		}

		return builder.build();
	}

	@Override
	public JSONObject getJSON()
	{
		return json;
	}

}
