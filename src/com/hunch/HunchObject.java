package com.hunch;

import org.json.simple.JSONObject;

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
