package com.hunch.api;

import org.json.JSONObject;

public abstract class HunchObject
{
	public abstract JSONObject getJSON();
	
	static abstract class Builder
	{
		abstract Builder init( JSONObject j );
		abstract void reset();
		abstract HunchObject build();
	}
}
