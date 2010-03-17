package com.hunch.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.hunch.Const;
import com.hunch.util.Pair;

/**
 * 
 * 
 * @author Tyler Levine
 * Dec 30, 2009
 *
 */
public class HunchRankedResults extends HunchObject
{
	
	private static final Builder b = new Builder();
	
	public interface Callback
	{
		public void callComplete( HunchRankedResults h );
	}
	
	private static class Builder extends HunchObject.Builder
	{
		private List< Pair< String, String > > results = new ArrayList< Pair< String, String > >();
		private String wildcardId;
		private String allResultsHunchUrl;
		private JSONObject val;
		
		private Builder() {}

		/* (non-Javadoc)
		 * @see com.hunch.api.HunchObject.Builder#init(org.json.JSONObject)
		 */
		@Override
		Builder init( JSONObject j )
		{
			val = j;
			return this;
		}
		
		Builder addResultsPair( Pair< String, String> p )
		{
			results.add( p );
			return this;
		}
		
		Builder setWildcardId( String id )
		{
			wildcardId = id;
			return this;
		}
		
		Builder setAllResultsHunchUrl( String url )
		{
			allResultsHunchUrl = url;
			return this;
		}
		
		/* (non-Javadoc)
		 * @see com.hunch.api.HunchObject.Builder#build()
		 */
		@Override
		HunchRankedResults build()
		{
			if( val == null )
			{
				throw new IllegalStateException( "Not all required fields set" +
						" before building HunchRankedResult! (val is null)" );
			}	
			else if( results.isEmpty() )
			{
				throw new IllegalStateException( "Not all required fields set" +
				" before building HunchRankedResult! (results is empty)" );
			}
			else if( wildcardId == null )
			{
				/*
				 * From the Hunch API spec:
				 * 
				 * Sometimes-included identifier for the "wildcard" result.
				 */
				//throw new IllegalStateException( "Not all required fields set" +
				//" before building HunchRankedResult! (wildcardId is null)" );
				Log.d( Const.TAG, "got null wildCardId when building HunchRankedResults!" +
						" (missing from response?)" );
			}
			else if( allResultsHunchUrl == null )
			{
				throw new IllegalStateException( "Not all required fields set" +
				" before building HunchRankedResult! (allResultsHunchUrl is null)" );
			}
			
			HunchRankedResults ret = new HunchRankedResults( val, results, wildcardId, allResultsHunchUrl );
			
			reset();
			
			return ret;
			
		}
		
		/* (non-Javadoc)
		 * @see com.hunch.api.HunchObject.Builder#reset()
		 */
		@Override
		void reset()
		{
			results = new ArrayList< Pair< String, String > >();
			val = null;
			wildcardId = null;
			allResultsHunchUrl = null;
		}
		
	}
	
	private final List< ResultStub > results;
	private final String wildcardId;
	private final String allResultsHunchUrl;
	private final JSONObject val; 

	private HunchRankedResults( JSONObject val, List< Pair< String, String > > results,
			String wildcardId, String allResultsHunchUrl )
	{
		this.val = val;
		this.wildcardId = wildcardId;
		this.allResultsHunchUrl = allResultsHunchUrl;
		this.results = new ArrayList< ResultStub >( results.size() );
		
		for( Pair< String, String > p : results )
		{
			this.results.add( new ResultStub( p.first, p.second ) );
		}
	}
	
	public String getAllResultsHunchUrl()
	{
		assert allResultsHunchUrl != null;
		
		return allResultsHunchUrl;
	}
	
	public boolean hasWildCard()
	{
		return wildcardId != null;
	}
	
	public String getWildcardId()
	{
		assert hasWildCard();
		
		return wildcardId;
	}
	
	public List< ResultStub > getAllResults()
	{
		assert results != null;
		
		// return a defensive copy of our mutable list
		List< ResultStub > outList = new ArrayList< ResultStub >( results.size() );
		
		outList.addAll( results );
		
		return outList;
	}
	
	public ResultStub getResult( int idx )
	{
		assert results != null;
		
		return results.get( idx );
	}
	
	/* (non-Javadoc)
	 * @see com.hunch.api.HunchObject#getJSON()
	 */
	@Override
	public JSONObject getJSON()
	{
		assert val != null;
		
		return val;
	}
	
	public static HunchRankedResults buildFromJSON( JSONObject val )
	{
		try
		{
			b.init( val )
			.setAllResultsHunchUrl( val.getString( "hunchUrl" ) );
			
			// bugfix: wildcard id is sometimes not included with the results
			try
			{
				b.setWildcardId( val.getString( "wildCardResultId" ) );
			} catch ( JSONException e )
			{
				Log.d( Const.TAG, "building HunchRankedResults without wildCardResultId!" +
						" (this is probably OK)" );
			}
			
			JSONArray ids = null, pcts = null;
			ids = val.getJSONArray( "rankedResultIds" );
			
			if( val.has( "eitherOrPct" ) )
			{
				pcts = val.getJSONArray( "eitherOrPct" );
			}
			
			if( pcts == null )
			{
				for( int i = 0; i < ids.length(); i++ )
				{
					// this is dumb, null is an Object by default?
					// so you have to explicitly downcast
					b.addResultsPair( Pair.create( ids.getString( i ), (String) null ) );
				}
			}
			else
			{
				for( int i = 0; i < ids.length(); i++ )
				{
					b.addResultsPair( Pair.create( ids.getString( i ), pcts.getString( i ) ) );
				}
			}
						
		} catch ( JSONException e )
		{
			throw new RuntimeException( "Couldn't build HunchRankedResults!", e );
		}
		
		return b.build();
	}
	
	public static Builder getBuilder()
	{
		return b;
	}
	
	public static class ResultStub
	{
		private final String id;
		private final String eitherOrPct;
		
		private ResultStub( String id, String eitherOrPct )
		{
			this.id = id;
			this.eitherOrPct = eitherOrPct;
		}
		
		private ResultStub( Integer id, Integer eitherOrPct )
		{
			this( String.valueOf( id ), String.valueOf( eitherOrPct ) );
		}
		
		public String getId()
		{
			return id;
		}
		
		public String getEitherOrPct()
		{
			return eitherOrPct;
		}
		
		public boolean hasEitherOrPct()
		{
			return eitherOrPct != null;
		}
		
		@Override
		public String toString()
		{
			return "ResultStub ID#" + id;
		}
		
		@Override
		public int hashCode()
		{
			int result = 17;
			
			result = 31 * result + id.hashCode();
			
			// eitherOrPct may be null
			if( eitherOrPct == null )
				return result;
			
			result = 31 * result + eitherOrPct.hashCode();
			
			return result;
		}
	}

}
