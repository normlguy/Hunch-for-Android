package com.hunch.api;

import org.json.JSONObject;

import com.hunch.api.HunchTopic.Variety;

/**
 * 
 * 
 * @author Tyler Levine
 * Jan 15, 2010
 *
 */
public class LazyHunchTopic extends HunchObject implements IHunchTopic
{
	private final JSONObject json;
	private IHunchTopic lazyTopic = null;
	
	LazyHunchTopic( JSONObject json )
	{
		this.json = json;
	}
	
	private void lazyBuild()
	{
		if( lazyTopic == null )
		{
			//Log.d( Const.TAG, "BUILDING LAZYHUNCHTOPIC!" );
			lazyTopic = HunchTopic.buildFromJSON( json );
		}
	}

	/* (non-Javadoc)
	 * @see com.hunch.api.HunchObject#getJSON()
	 */
	@Override
	public JSONObject getJSON()
	{
		return json;
	}

	/* (non-Javadoc)
	 * @see com.hunch.api.IHunchTopic#getCategory()
	 */
	@Override
	public HunchCategory getCategory()
	{
		lazyBuild();
		
		return lazyTopic.getCategory();
	}

	/* (non-Javadoc)
	 * @see com.hunch.api.IHunchTopic#getDecision()
	 */
	@Override
	public String getDecision()
	{
		lazyBuild();
		
		return lazyTopic.getDecision();
	}

	/* (non-Javadoc)
	 * @see com.hunch.api.IHunchTopic#getHunchUrl()
	 */
	@Override
	public String getHunchUrl()
	{
		lazyBuild();
		
		return lazyTopic.getHunchUrl();
	}

	/* (non-Javadoc)
	 * @see com.hunch.api.IHunchTopic#getId()
	 */
	@Override
	public Integer getId()
	{
		lazyBuild();
		
		return lazyTopic.getId();
	}

	/* (non-Javadoc)
	 * @see com.hunch.api.IHunchTopic#getImageUrl()
	 */
	@Override
	public String getImageUrl()
	{
		lazyBuild();
		
		return lazyTopic.getImageUrl();
	}

	/* (non-Javadoc)
	 * @see com.hunch.api.IHunchTopic#getResultType()
	 */
	@Override
	public String getResultType()
	{
		lazyBuild();
		
		return lazyTopic.getResultType();
	}

	/* (non-Javadoc)
	 * @see com.hunch.api.IHunchTopic#getScore()
	 */
	@Override
	public Double getScore()
	{
		lazyBuild();
		
		return lazyTopic.getScore();
	}

	/* (non-Javadoc)
	 * @see com.hunch.api.IHunchTopic#getShortName()
	 */
	@Override
	public String getShortName()
	{
		lazyBuild();
		
		return lazyTopic.getShortName();
	}

	/* (non-Javadoc)
	 * @see com.hunch.api.IHunchTopic#getUrlName()
	 */
	@Override
	public String getUrlName()
	{
		lazyBuild();
		
		return lazyTopic.getUrlName();
	}

	/* (non-Javadoc)
	 * @see com.hunch.api.IHunchTopic#getVariety()
	 */
	@Override
	public Variety getVariety()
	{
		lazyBuild();
		
		return lazyTopic.getVariety();
	}

	/* (non-Javadoc)
	 * @see com.hunch.api.IHunchTopic#isEitherOr()
	 */
	@Override
	public Boolean isEitherOr()
	{
		lazyBuild();
		
		return lazyTopic.isEitherOr();
	}
	
	@Override
	public String toString()
	{
		lazyBuild();
		
		return lazyTopic.toString();
	}

}
