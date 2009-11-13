package com.hunch;

import org.json.*;

/**
 * 
 * 
 * @author Tyler Levine
 * Nov 5, 2009
 *
 */
public class HunchResponse extends HunchObject
{

	private static Builder b;
	private static int THAY_TOPIC_ID = -3;
	
	static class Builder extends HunchObject.Builder
	{
		
		private JSONObject val;
		private int __id, __order, __questionId, __topicId;
		private String __text;
		
		private Builder() {}
		
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
		
		Builder setQuestionId( int questionId )
		{
			__questionId = questionId;
			return this;
		}
		
		Builder setOrder( int order )
		{
			__order = order;
			return this;
		}

		@Override
		void reset()
		{
			val = null;
			__text = null;
			__id = __topicId = __order = __questionId = Integer.MIN_VALUE;
		}
		
		@Override
		HunchResponse build()
		{
			if( val == null || __text == null || __order == Integer.MIN_VALUE || __questionId == Integer.MIN_VALUE
					|| __id == Integer.MIN_VALUE || __topicId == Integer.MIN_VALUE )
			{
				throw new IllegalStateException( "Not all required fields set before building HunchQuestion!" );
			}
			
			HunchResponse ret = new HunchResponse( val, __text, __order, __id, __topicId, __questionId );
			reset();
			
			return ret;
		}
	}
	
	private final JSONObject json;
	private final int _id, _order, _questionId, _topicId;
	private final String _text;
	
	private HunchResponse( JSONObject jsonObj, String text, int order,
			int id, int topicId, int questionId )
	{
		json = jsonObj;
		_text = text;
		_order = order;
		_id = id;
		_topicId = topicId;
		_questionId = questionId;
		
	}
	
	public static Builder getBuilder()
	{
		if( b == null ) b = new Builder();
		
		return b;
	}
	
	public String getText()
	{
		return _text;
	}
	
	public int getOrder()
	{
		return _order;
	}
	
	public int getId()
	{
		return _id;
	}
	
	public int getTopicId()
	{
		return _topicId;
	}
	
	public int getQuestionId()
	{
		return _questionId;
	}
	
	static HunchResponse buildFromJSON( JSONObject json )
	{
		HunchResponse.Builder builder = getBuilder();
		
		// build the HunchResponse object
		int id = Integer.MIN_VALUE, topicId = Integer.MIN_VALUE,
			questionId = Integer.MIN_VALUE, order = Integer.MIN_VALUE;
		String text = "";
		
		try
		{
			String tId = json.getString( "topicId" );
			try
			{
				topicId = Integer.parseInt( tId );
			} catch ( NumberFormatException e )
			{
				if( tId.equals( "THAY" ) )
				{
					// Teach Hunch About You!
					topicId = THAY_TOPIC_ID;
				}
				else
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
		}
		finally
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
