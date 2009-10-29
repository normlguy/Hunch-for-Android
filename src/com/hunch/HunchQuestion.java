package com.hunch;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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
		
		protected Builder() {}
		
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
	
	protected HunchQuestion( JSONObject jsonObj, String text, String imageUrl,
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
		JSONArray jsonResIds = (JSONArray) json.get( "responseIds" );
		int[] resIds = new int[ jsonResIds.size() ];
		try
		{
			try
			{
				topicId = Integer.parseInt( json.get( "topicId" ).toString() );
			} catch ( NumberFormatException ex )
			{
				String topicIdVal = json.get( "topicId" ).toString();
				if( topicIdVal.equals( "THAY" ) )
				{
					// Teach Hunch About You!
					topicId = THAY_TOPIC_ID;
				}
				else
				{
					// some other cause of the NFE
					ex.printStackTrace();
				}
			}
			
			id = Integer.parseInt( json.get( "id" ).toString() );
			for( int i = 0; i < resIds.length; i++ )
			{
				Object resId = jsonResIds.get( i );
				resIds[ i ] = Integer.parseInt( resId.toString() );
			}
				
			
		} catch ( NumberFormatException e )
		{
			e.printStackTrace();
		}
		finally
		{
			builder.init( json ).setId( id ).setTopicId( topicId );
			builder.setText( json.get( "text" ).toString() );
			builder.setImageUrl( json.get( "imageUrl").toString() );
			builder.setResponseIds( resIds );
		}
		
		return builder.build();
	}

	@Override
	public JSONObject getJSON()
	{
		return json;
	}

}
