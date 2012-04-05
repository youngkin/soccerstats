package com.oss.soccerstats.view;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

import com.oss.soccerstats.R;
import com.oss.soccerstats.data.SoccerStatsDataHelper;
import com.oss.soccerstats.intent.SoccerStatIntent;

public class TeamDetailsActivity extends TabActivity
{
   public static final String PLAYERS_TAB_ID = "playersTabId";
   public static final String GAMES_TAB_ID   = "gamesTabId";

   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle savedInstanceState) 
   {
      super.onCreate(savedInstanceState);

      Intent launchIntent = getIntent();
      int teamId = launchIntent.getIntExtra(SoccerStatIntent.TEAM_ID, 
               SoccerStatsDataHelper.TEAM_ID_DEFAULT);
      String teamName = launchIntent.getStringExtra(SoccerStatIntent.TEAM_NAME);
      setTitle(getString(R.string.teamTitle) + "/"+ teamName);
      setContentView(R.layout.team_details_view);
      
      Resources res = getResources(); // Resource object to get Drawables
      TabHost tabHost = getTabHost();  // The activity TabHost
      TabHost.TabSpec spec;  // Reusable TabSpec for each tab
      Intent intent;  // Reusable Intent for each tab

      // Create an Intent to launch an Activity for the tab (to be reused)
      intent = new Intent().setClass(this, RosterActivity.class);
      intent.putExtra(SoccerStatIntent.TEAM_ID, teamId);
      spec = tabHost.newTabSpec(PLAYERS_TAB_ID)
               .setIndicator(res.getText(R.string.playersTab), 
                        res.getDrawable(R.drawable.roster3_48))
                    .setContent(intent);
      tabHost.addTab(spec);

      tabHost.setCurrentTab(0);

      // Do the same for the other tabs
      intent = new Intent().setClass(this, GamesSummaryActivity.class);
      intent.putExtra(SoccerStatIntent.TEAM_ID, teamId);
      spec = tabHost.newTabSpec(GAMES_TAB_ID)
               .setIndicator(res.getText(R.string.gamesTab), 
                        res.getDrawable(R.drawable.black_white_soccer_ball_icon48))
               .setContent(intent);
      tabHost.addTab(spec);
  }
      

}
