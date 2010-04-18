package com.hunch;

import android.graphics.Color;

public class Const
{
	// debug tag
	public static final String	TAG						= "HUNCH";

	// update constants
	public static final int		HOME_CATEGORY_LIST_INIT	= 0;
	public static final int		HOME_TOPIC_LIST_INIT	= 1;
	public static final int		TOPIC_PLAY_INIT			= 2;
	public static final int		TOPIC_PLAY_RESPONSE		= 3;
	public static final int		TOPIC_PLAY_TRANSITION	= 4;
	public static final int		TOPIC_PLAY_END			= 5;
	
	// sizes for the images we pull from the Hunch API
	public static final String TOPIC_IMAGE_SIZE = "64x64";
	public static final String TOPIC_LIST_IMAGE_SIZE = "32x32";
	public static final String RESULT_IMAGE_SIZE = "48x48";
	public static final String RESULT_DETAILS_IMAGE_SIZE = "64x64";
	public static final String CATEGORY_IMAGE_SIZE = "32x32";
	
	// not sure that any of these icons are being displayed, but I'll define them anyway
	public static final String QUESTION_IMAGE_SIZE = "48x48";
	public static final String RESPONSE_IMAGE_SIZE = "48x48";
	
	// color constants
	public static final int		HOME_TAB_FOCUSED_COLOR	= Color.argb( 198, 57, 25, 125 );
	
	// thread number constants (0 for cached thread pool)
	public static final int IMAGE_FETCH_THREADS = 3;
	public static final int API_CALLING_THREADS = 0;
	
	// topic play menu item identifiers
	public static final int MENU_RESTART_TOPIC = 0;
	
	// select topic menu item identifiers
	public static final int MENU_SEARCH = 0;
	public static final int MENU_SETTINGS = 1;
}
