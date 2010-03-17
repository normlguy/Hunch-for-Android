package com.hunch;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ImageCacher
{
	private final static Map< URL, Drawable > drawableMap = new HashMap< URL, Drawable >();
	
	// no instantiation
	private ImageCacher() {}
	
	public static void fromURL( final String imgUrl, final ImageCacher.Callback callback )
	{
		
		URL tempUrl = null;
		try
		{
			tempUrl = new URL( imgUrl );
		} catch ( MalformedURLException e )
		{
			e.printStackTrace();
		}
		
		final URL url = tempUrl;
		
		// return it from the cache if it's hot
		if( drawableMap.containsKey( url ) )
		{
			callback.callComplete( drawableMap.get( url ) );
			return;
		}
		
		// hander to deal with the image once this thread has it
		final Handler imgHandler = new Handler()
		{
			@Override
			public void handleMessage( Message msg )
			{
				Drawable d = (Drawable) msg.obj;
				
				// add to the cache and return
				drawableMap.put( url, d );
				callback.callComplete( d );
			}
		};
		
		// start a new thread to grab the image off the network
		new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					Log.v( Const.TAG, "fetching image from URL (" + url.toString() + ")" );
					
					final BufferedInputStream input = new BufferedInputStream( url.openStream(), 4096 );
					final Drawable image = Drawable.createFromStream( input, "src" );
					
					final Message msg = imgHandler.obtainMessage( 1, image );
					imgHandler.sendMessage( msg );
				} catch( IOException e )
				{
					e.printStackTrace();
					return;
				}
			}
		}.start();
		
	}
	
	// this is to get around only being able to
	// manipulate final fields in anonymous inner classes
	/*private class DrawableRef
	{
		public Drawable drawable;
		
		private DrawableRef() {}
	}*/
	
	public interface Callback
	{
		public void callComplete( Drawable d );
	}

}
