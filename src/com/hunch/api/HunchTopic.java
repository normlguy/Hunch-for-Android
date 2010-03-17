package com.hunch.api;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * 
 * 
 * @author Tyler Levine Oct 25, 2009
 * 
 */
public class HunchTopic extends HunchObject implements IHunchTopic
{

	private static Builder b = new Builder();
	
	public interface Callback
	{
		public void callComplete( IHunchTopic h );
	}
	
	public interface ListCallback
	{
		public void callComplete( List< IHunchTopic > h );
	}
	
	// what is with the long names in this app???
	// it makes sense (to me at least)
//	public interface LazyDeserializingListCallback
//	{
//		public void callComplete( )
//	}
	
	public enum Variety
	{
		DEFAULT, SEARCH, NEXT_QUESTION
	}

	static class Builder extends HunchObject.Builder
	{
		private JSONObject _val;
		private Integer buildId;
		private String buildDecision, buildImageUrl, buildResultType,
				buildUrlName, buildShortName, buildHunchUrl;
		private Boolean buildIsEitherOr;
		private HunchCategory buildCategory;
		private Double buildScore;

		// instance control
		private Builder()
		{
		}

		@Override
		Builder init( JSONObject val )
		{
			this._val = val;
			return this;
		}

		Builder setId( Integer id )
		{
			this.buildId = id;
			return this;
		}

		Builder setDecision( String decision )
		{
			this.buildDecision = decision;
			return this;
		}

		Builder setImageUrl( String imageUrl )
		{
			this.buildImageUrl = imageUrl;
			return this;
		}

		Builder setResultType( String resultType )
		{
			this.buildResultType = resultType;
			return this;

		}

		Builder setUrlName( String urlName )
		{
			this.buildUrlName = urlName;
			return this;
		}

		Builder setShortName( String shortName )
		{
			this.buildShortName = shortName;
			return this;
		}

		Builder setIsEitherOr( Boolean isEitherOr )
		{
			this.buildIsEitherOr = isEitherOr;
			return this;
		}

		Builder setHunchUrl( String hunchUrl )
		{
			this.buildHunchUrl = hunchUrl;
			return this;
		}

		Builder setCategory( HunchCategory category )
		{
			this.buildCategory = category;
			return this;
		}

		Builder setScore( Double score )
		{
			this.buildScore = score;
			return this;
		}

		@Override
		void reset()
		{
			_val = null;
			buildId = null;
			buildDecision = null;
			buildImageUrl = null;
			buildResultType = null;
			buildUrlName = null;
			buildShortName = null;
			buildHunchUrl = null;
			buildIsEitherOr = false;
			buildCategory = null;
			buildScore = null;
		}

		@Override
		HunchTopic build()
		{
			assureBuildParams();

			return buildInternal( Variety.DEFAULT );

		}

		HunchTopic buildForSearch()
		{
			assureSearchBuildParams();

			return buildInternal( Variety.SEARCH );
		}
		
		IHunchTopic buildForNextQuestion()
		{
			assureNextQuestionBuildParams();
			
			return buildInternal( Variety.NEXT_QUESTION );
		}

		private HunchTopic buildInternal( Variety v )
		{
			HunchTopic ht = new HunchTopic( _val, buildId, buildDecision,
					buildImageUrl, buildResultType, buildUrlName,
					buildShortName, buildIsEitherOr, buildHunchUrl,
					buildCategory, buildScore, v );
			reset();

			return ht;
		}
		
		private void assureNextQuestionBuildParams()
		{
			if ( _val == null || buildId == null || buildHunchUrl == null
					|| buildDecision == null || buildImageUrl == null
					|| buildCategory == null )
			{
				throw new IllegalStateException(
						"Not all required fields set before building HunchTopic!" );
			}
		}

		private void assureBuildParams()
		{
			if ( _val == null || buildId == null
					|| buildDecision == null || buildImageUrl == null
					|| buildResultType == null || buildUrlName == null
					|| buildShortName == null )
			{
				throw new IllegalStateException(
						"Not all required fields set before building HunchTopic!" );
			}
		}

		private void assureSearchBuildParams()
		{
			if ( _val == null || buildId == null
					|| buildDecision == null || buildUrlName == null
					|| buildScore == null )
			{
				throw new IllegalStateException(
						"Not all required fields set before building HunchTopic!" );
			}
		}

	}

	protected final Integer _id;
	protected final String _decision, _imageUrl, _resultType, _urlName,
			_shortName;
	protected final Boolean _isEitherOr;
	protected final JSONObject json;
	protected final String _hunchUrl; // optional
	protected final HunchCategory _category;
	protected final Double _score;
	protected final Variety variety;

	protected HunchTopic( JSONObject val, Integer id, String decision,
			String imageUrl, String resultType, String urlName,
			String shortName, Boolean isEitherOr, String hunchUrl,
			HunchCategory category, Double score, Variety v )
	{
		json = val;
		_id = id;
		_decision = decision;
		_imageUrl = imageUrl;
		_resultType = resultType;
		_urlName = urlName;
		_shortName = shortName;
		_isEitherOr = isEitherOr;
		_hunchUrl = hunchUrl;
		_category = category;
		_score = score;
		
		this.variety = v;
	}

	public static Builder getBuilder()
	{
		return b;
	}

	/* (non-Javadoc)
	 * @see com.hunch.api.IHunchTopic#getId()
	 */
	public Integer getId()
	{
		return _id;
	}

	/* (non-Javadoc)
	 * @see com.hunch.api.IHunchTopic#getDecision()
	 */
	public String getDecision()
	{
		return _decision;
	}

	/* (non-Javadoc)
	 * @see com.hunch.api.IHunchTopic#getImageUrl()
	 */
	public String getImageUrl()
	{
		return _imageUrl;
	}
	
	/* (non-Javadoc)
	 * @see com.hunch.api.IHunchTopic#getResultType()
	 */
	public String getResultType()
	{
		return _resultType;
	}

	/* (non-Javadoc)
	 * @see com.hunch.api.IHunchTopic#getUrlName()
	 */
	public String getUrlName()
	{
		return _urlName;
	}

	/* (non-Javadoc)
	 * @see com.hunch.api.IHunchTopic#getShortName()
	 */
	public String getShortName()
	{
		return _shortName;
	}

	/* (non-Javadoc)
	 * @see com.hunch.api.IHunchTopic#getHunchUrl()
	 */
	public String getHunchUrl()
	{
		return _hunchUrl;
	}

	/* (non-Javadoc)
	 * @see com.hunch.api.IHunchTopic#isEitherOr()
	 */
	public Boolean isEitherOr()
	{
		return _isEitherOr;
	}

	/* (non-Javadoc)
	 * @see com.hunch.api.IHunchTopic#getCategory()
	 */
	public HunchCategory getCategory()
	{
		return _category;
	}

	/* (non-Javadoc)
	 * @see com.hunch.api.IHunchTopic#getScore()
	 */
	public Double getScore()
	{
		return _score;
	}
	
	/* (non-Javadoc)
	 * @see com.hunch.api.IHunchTopic#getVariety()
	 */
	public Variety getVariety()
	{
		return variety;
	}

	@Override
	public JSONObject getJSON()
	{
		return json;
	}

	/* (non-Javadoc)
	 * @see com.hunch.api.IHunchTopic#toString()
	 */
	@Override
	public String toString()
	{
		return getShortName();
	}

	static IHunchTopic buildFromJSON( JSONObject topic )
	{

		HunchTopic.Builder builder = getBuilder();

		// build the HunchTopic object
		int id = Integer.MIN_VALUE;
		int eitherOr = Integer.MIN_VALUE;
		
		builder.init( topic );

		try
		{
			id = Integer.parseInt( topic.getString( "id" ) );
			eitherOr = Integer.parseInt( topic.getString( "eitherOrTopic" ) );

			
			builder.setId( id )
			.setDecision( topic.getString( "decision" ) )
			.setImageUrl( topic.getString( "imageUrl" ) )
			.setResultType( topic.getString( "resultType" ) )
			.setUrlName( topic.getString( "urlName" ) )
			.setShortName( topic.getString( "shortName" ) )
			.setIsEitherOr(	eitherOr == 1 );
		} catch ( NumberFormatException e )
		{
			throw new RuntimeException( "could not build HunchTopic!", e );
		} catch ( JSONException e )
		{
			throw new RuntimeException( "could not build HunchTopic", e );
		}

		return builder.build();
	}

}
