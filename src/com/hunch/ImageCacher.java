package com.hunch;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.hunch.util.Pair;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ImageCacher
{
	private final static Map< URL, Drawable > drawableMap = new HashMap< URL, Drawable >();
	private final static ExecutorService executor;
	
	static
	{
		if( Const.IMAGE_FETCH_THREADS > 1 )
			executor = Executors.newFixedThreadPool( Const.IMAGE_FETCH_THREADS );
		else if ( Const.IMAGE_FETCH_THREADS == 1 )
			executor = Executors.newSingleThreadExecutor();
		else
			executor = Executors.newCachedThreadPool();
		
	}
	
	// hander to deal with the image once this thread has it
	private final static Handler imgHandler = new Handler()
	{
		@Override
		public void handleMessage( Message msg )
		{
			Pair< Drawable, ImageCacher.Callback > dataPair = 
				(Pair< Drawable, ImageCacher.Callback >) msg.obj;
			Drawable d = dataPair.first;
			
			// get the URL out of cold storage
			URL url = (URL) msg.getData().getSerializable( "url" );
			
			// add to the cache and return
			drawableMap.put( url, d );
			dataPair.second.callComplete( d );
		}
	};
	
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
		
		
		
		// send the job to grab the image off the network
		Runnable task = new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					Log.v( Const.TAG, "fetching image from URL (" + url.toString() + ")" );
					
					final BufferedInputStream input = new BufferedInputStream( url.openStream(), 4096 );
					final Drawable image = Drawable.createFromStream( input, "src" );
					
					final Message msg = imgHandler.obtainMessage();
					
					/*
					 * This is a dirty trick. I'm using a util.Pair to marshall two objects
					 * across in the object field of the message. I'm sending the Drawable itself,
					 * and also the Callback to call from the handler.
					 */
					Pair< Drawable, Callback > dataPair = Pair.create( image, callback );
					msg.obj = dataPair;
					
					// then put the URL in a bundle so we can use it to cache later
					Bundle data = new Bundle();
					data.putSerializable( "url", url );
					msg.setData( data );
					
					// aaand we're off
					imgHandler.sendMessage( msg );
				} catch( FileNotFoundException e )
				{
					Log.e( Const.TAG, "couldn't get image off network (404 error)" );
				}
				catch( IOException e )
				{
					Log.e( Const.TAG, "couldn't get image off network" );
				}
			}
		};
		
		// actually now we're off (kind of)
		executor.execute( task );
		
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
