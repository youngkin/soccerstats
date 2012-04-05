package com.oss.soccerstats.view;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

import com.oss.soccerstats.R;

public class HelpActivity extends TabActivity
{
   public static final String GAME_CLOCK_TAB_ID = "gameClockTabId";
   public static final String WELCOME_TAB_ID    = "welcomeTabId";
   public static final String STATS_TAB_ID      = "statsTabId";
   public static final String GAME_OVER_TAB_ID  = "gameOverTabId";
   public static final String HELP_ABOUT_TAB_ID  = "helpAboutTabId";

   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle savedInstanceState) 
   {
      super.onCreate(savedInstanceState);
      setTitle(R.string.mainTitle);
      setContentView(R.layout.help_view);
      
      Resources res = getResources(); // Resource object to get Drawables
      TabHost tabHost = getTabHost();  // The activity TabHost
      TabHost.TabSpec spec;  // Reusable TabSpec for each tab
      Intent intent;  // Reusable Intent for each tab

      // Create an Intent to launch an Activity for the tab (to be reused)
      intent = new Intent().setClass(this, WelcomeTab.class);
      spec = tabHost.newTabSpec(WELCOME_TAB_ID)
               .setIndicator(res.getText(R.string.welcomeTab), 
                        res.getDrawable(R.drawable.black_white_soccer_ball_icon48))
                    .setContent(intent);
      tabHost.addTab(spec);

      tabHost.setCurrentTab(0);

      // Do the same for the other tabs
      intent = new Intent().setClass(this, GameClockTab.class);
      // Initialize a TabSpec for each tab and add it to the TabHost
      spec = tabHost.newTabSpec(GAME_CLOCK_TAB_ID)
               .setIndicator(res.getText(R.string.clockTab), 
                        res.getDrawable(R.drawable.clock))
               .setContent(intent);
      tabHost.addTab(spec);

      // Do the same for the other tabs
      intent = new Intent().setClass(this, StatsTab.class);
      // Initialize a TabSpec for each tab and add it to the TabHost
      spec = tabHost.newTabSpec(STATS_TAB_ID)
               .setIndicator(res.getText(R.string.statsTab), 
                        res.getDrawable(R.drawable.statistics))
               .setContent(intent);
      tabHost.addTab(spec);

      // Do the same for the other tabs
      intent = new Intent().setClass(this, HelpAboutTab.class);
      // Initialize a TabSpec for each tab and add it to the TabHost
      spec = tabHost.newTabSpec(HELP_ABOUT_TAB_ID)
               .setIndicator(res.getText(R.string.helpAboutTab), 
                        res.getDrawable(R.drawable.call))
               .setContent(intent);
      tabHost.addTab(spec);

      tabHost.setCurrentTab(0);
  }
      

}
