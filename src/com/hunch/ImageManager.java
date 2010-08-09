package com.hunch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

public class ImageManager
{
	private final static Map< URL, SoftReference<Drawable> > drawableMap = 
		new HashMap< URL, SoftReference<Drawable> >();
	private static ExecutorService executor;
	
	private static ImageManager instance = null;
	
	// no instantiation
	private ImageManager() {}
	
	public static ImageManager getInstance()
	{
		if ( instance == null ) instance = new ImageManager();
		
		startExecutor();
		
		return instance;
	}
	
	private static void startExecutor()
	{
		// if we already have an executor, theres no need for another one.
		if( executor != null ) return;
		
		if( Const.IMG_FETCH_THREADS > 1 )
			executor = Executors.newFixedThreadPool( Const.IMG_FETCH_THREADS );
		else if ( Const.IMG_FETCH_THREADS == 1 )
			executor = Executors.newSingleThreadExecutor();
		else
			executor = Executors.newCachedThreadPool();
	}

	
	protected void cacheDrawable( Drawable d )
	{
		// TODO: two-level cache
	}
	
	protected String fileNameFromURL( URL url )
	{
		return url.getFile();
	}
	
	protected Drawable getDrawableFromCache( final Context context, final URL url )
	{
		// is the drawable in the level one cache (in memory)
		SoftReference< Drawable > drawableRef = drawableMap.get( url );
		if( drawableRef != null )
		{
			// the key exists
			Drawable drawable = drawableRef.get();
			if( drawable != null )
			{
				// the drawable hasn't been gc'd yet.
				return drawable;
			}
			else
			{
				// the drawable has been gc'd, remove the key
				drawableMap.remove( url );
			}
		}
		
		// the drawable isn't in the level one cache
		// so try the level two cache (internal storage)
		final String fileName = fileNameFromURL( url );
		
		//FileInputStream fIn = null;
		File imageFile = null;
		try
		{
			File catImagesDir = context.getDir( Const.CAT_IMG_DIR,  Context.MODE_WORLD_READABLE );
			// the directory is guaranteed to exist by getDir()
			
			File[] imageFileList = catImagesDir.listFiles( new FilenameFilter()
			{
				
				@Override
				public boolean accept( File dir, String aFilename )
				{
					return fileName.equals( aFilename );
				}
			} );
			
			if( imageFileList == null || imageFileList.length == 0 )
			{
				throw new FileNotFoundException(); 
			}
			
			imageFile = imageFileList[0];
			
			//fIn = context.openFileInput( fileName );
		} catch ( FileNotFoundException e )
		{
			return null;
		}
		
		return Drawable.createFromPath( imageFile.getAbsolutePath() );
	}
	
	protected Runnable getDownloadTask( final URL uri, final Callback callback )
	{
		// hander to deal with the image once this thread has it
		final Handler imgHandler = new Handler()
		{
			@Override
			public void handleMessage( Message msg )
			{
				Drawable d = (Drawable) msg.obj;
				
				// add to the cache and return
				cacheDrawable( d );
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
					Log.v( Const.TAG, String.format( "[%d] fetching image from URI (%s)", 
							Thread.currentThread().getId(), uri.toString() ) );
					InputStream iStream = ImageManager.this.getInputStream( uri );
					final Drawable image = Drawable.createFromStream( iStream, "src" );
					
					final Message msg = imgHandler.obtainMessage();
					
					msg.obj = image;
										
					// aaand we're done
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
		
		return task;
	}
	
	protected InputStream getInputStream( URL url ) throws IOException
	{
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpGet request = new HttpGet( url.toString() );
		HttpResponse response = httpClient.execute( request );
		return response.getEntity().getContent();
	}
	
	/**
	 * Get the image for a Hunch category. These images are cached into internal storage
	 * after the first time they are downloaded. If the image is not present in the cache,
	 * it will be downloaded in a background thread then cached, and a placeholder image
	 * will be displayed it its place.
	 * 
	 * @param context Application context (to get to internal storage dir)
	 * @param image The ImageView that will display the image
	 * @param imgURL The URL to the image.
	 */
	public void getCategoryImage( final Context context, final ImageView image, final String imgURL )
	{
		URL url = stringToURL( imgURL );
		Drawable cachedDrawable = getDrawableFromCache( context, url );
		
		if( cachedDrawable != null )
		{
			// the image is in the cache
			image.setImageDrawable( cachedDrawable );
		}
		else
		{
			// the image is not in the cache, must download
			downloadCategoryDrawable( url, new Callback()
			{
				
				@Override
				public void callComplete( Drawable d )
				{
					image.setImageDrawable( d );		
				}
			} );
		}
		
	}
	
	protected URL stringToURL( String url )
	{
		URL temp = null;
		try
		{
			temp = new URL( url );
		} catch ( MalformedURLException e )
		{
			e.printStackTrace();
			// TODO: better handling of malformed urls?
			
			return null;
		}
		
		return temp;
	}
	
	protected void downloadCategoryDrawable( URL imgURL, Callback callback )
	{
		Runnable task = getDownloadTask( imgURL, callback );
		executor.execute( task );
		
	}
	
	protected void downloadTopicDrawable( URL imgURL, Callback callback )
	{
		
	}
	
	public interface Callback
	{
		public void callComplete( Drawable d );
	}

}
