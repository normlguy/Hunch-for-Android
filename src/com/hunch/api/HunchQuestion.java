package com.hunch.api;

import org.json.*;


public class HunchQuestion extends HunchObject
{
	
	private static Builder b;
	private static int THAY_TOPIC_ID = -3;
	
	static class Builder extends HunchObject.Builder
	{
		
		private JSONObject val;
		private int __id, __topicId;
		private String __text, __imageUrl;
		private int[] __responseIds;
		
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
		
		Builder setImageUrl( String imageUrl )
		{
			__imageUrl = imageUrl;
			return this;
		}
		
		Builder setResponseIds( int[] ids )
		{
			this.__responseIds = ids;
			return this;
		}

		@Override
		void reset()
		{
			val = null;
			__text = null;
			__imageUrl = null;
			__responseIds = null;
			__id = __topicId = Integer.MIN_VALUE;
		}
		
		@Override
		HunchQuestion build()
		{
			if( val == null || __text == null || __imageUrl == null || __responseIds == null
					|| __id == Integer.MIN_VALUE || __topicId == Integer.MIN_VALUE )
			{
				throw new IllegalStateException( "Not all required fields set before building HunchQuestion!" );
			}
			
			HunchQuestion ret = new HunchQuestion( val, __text, __imageUrl, __id, __topicId, __responseIds );
			reset();
			
			return ret;
		}
	}
	
	private final JSONObject json;
	private final String _text, _imageUrl;
	private final int _id, _topicId;
	private final int[] _responseIds;
	
	private HunchQuestion( JSONObject jsonObj, String text, String imageUrl,
			int id, int topicId, int[] responseIds )
	{
		json = jsonObj;
		_text = text;
		_imageUrl = imageUrl;
		_id = id;
		_topicId = topicId;
		_responseIds = responseIds;
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
	
	public int[] getResponseIds()
	{
		return _responseIds;
	}
	
	static HunchQuestion buildFromJSON( JSONObject json )
	{
		HunchQuestion.Builder builder = getBuilder();
		
		// build the HunchQuestion object
		int id = Integer.MIN_VALUE, topicId = Integer.MIN_VALUE;
		int[] resIds;
		String text = "", imgUrl = "";
		JSONArray jsonResIds;
		
		try
		{
			jsonResIds = json.getJSONArray( "responseIds" );
		} catch ( JSONException e )
		{
			throw new RuntimeException( "Couldn't build HunchQuestion!", e );
		}
		
		resIds = new int[ jsonResIds.length() ];
		
		
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
					throw new RuntimeException( "Couldn't build HunchQuestion!", e );
				}
			}
			
			id = Integer.parseInt( json.getString( "id" ) );
			for( int i = 0; i < resIds.length; i++ )
			{
				String resId = jsonResIds.getString( i );
				resIds[ i ] = Integer.parseInt( resId );
			}
			
			text = json.getString( "text" );
			imgUrl = json.getString( "imageUrl" );
				
			
		} catch ( NumberFormatException e )
		{
			throw new RuntimeException( "Couldn't build HunchQuestion!", e );
		} catch ( JSONException e )
		{
			throw new RuntimeException( "Couldn't build HunchQuestion!", e );
		}
		finally
		{
			builder.init( json )
			.setId( id )
			.setTopicId( topicId )
			.setText( text )
			.setImageUrl( imgUrl )
			.setResponseIds( resIds );
		}
		
		return builder.build();
	}

	@Override
	public JSONObject getJSON()
	{
		return json;
	}

}
