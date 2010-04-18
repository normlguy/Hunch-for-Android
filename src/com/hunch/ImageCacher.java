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

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ImageCacher
{
	private final static Map< URL, Drawable > drawableMap = new HashMap< URL, Drawable >();
	private static ExecutorService executor;
	
	// no instantiation
	private ImageCacher() {}
	
	private static void startExecutor()
	{
		// if we already have an executor, theres no need for another one.
		if( executor != null ) return;
		
		if( Const.IMAGE_FETCH_THREADS > 1 )
			executor = Executors.newFixedThreadPool( Const.IMAGE_FETCH_THREADS );
		else if ( Const.IMAGE_FETCH_THREADS == 1 )
			executor = Executors.newSingleThreadExecutor();
		else
			executor = Executors.newCachedThreadPool();
	}
	
	public static void fromURL( final String imgUrl, final ImageCacher.Callback callback )
	{
		
		startExecutor();
		
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
		
		
		// send the job to grab the image off the network
		Runnable task = new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					Log.v( Const.TAG, String.format( "[%d] fetching image from URL (%s)", 
							Thread.currentThread().getId(), url.toString() ) );
					
					final BufferedInputStream input = new BufferedInputStream( url.openStream(), 4096 );
					final Drawable image = Drawable.createFromStream( input, "src" );
					
					final Message msg = imgHandler.obtainMessage();
					
					msg.obj = image;
										
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
