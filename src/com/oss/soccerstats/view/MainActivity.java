package com.oss.soccerstats.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.oss.soccerstats.R;
import com.oss.soccerstats.intent.SoccerStatIntent;

public class MainActivity extends Activity
{
   public static final String GAME_CLOCK_TAB_ID = "gameClockTabId";
   public static final String WELCOME_TAB_ID    = "welcomeTabId";
   public static final String STATS_TAB_ID      = "statsTabId";
   public static final String GAME_OVER_TAB_ID  = "gameOverTabId";
   public static final String HELP_ABOUT_TAB_ID  = "helpAboutTabId";

   private Button mStartGameButton;
   private Button mTeamsGamesPlayersButton;
   private Button mHelpButton;
   
   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle savedInstanceState) 
   {
      super.onCreate(savedInstanceState);
      setTitle(R.string.mainTitle);
      setContentView(R.layout.welcome_page);
      
      mStartGameButton  = (Button) findViewById(R.id.startGame);
      mStartGameButton.setOnClickListener(new View.OnClickListener() {

         public void onClick(View view)
         {
            Intent intent = new Intent(SoccerStatIntent.PLAY_GAME);
            intent.addCategory(SoccerStatIntent.DEFAULT);
            startActivity(intent);
         }

      });
      mStartGameButton.setOnTouchListener(new View.OnTouchListener() {
         
         @Override
         public boolean onTouch(View v, MotionEvent event)
         {
            boolean retCode = false;
            if ( event.getAction() == MotionEvent.ACTION_DOWN )
            {
               mStartGameButton.setCompoundDrawablesWithIntrinsicBounds(0, 
                        R.drawable.soccer_field8aaa3d_pressed, 0, 0);
               retCode = false;
            }
            else if ( event.getAction() == MotionEvent.ACTION_UP )
            {
               mStartGameButton.setCompoundDrawablesWithIntrinsicBounds(0, 
                        R.drawable.soccer_field8aaa3d, 0, 0);
               retCode = false;
            }
            return retCode;
         }
      });
      
      mTeamsGamesPlayersButton  = (Button) findViewById(R.id.roster);
      mTeamsGamesPlayersButton.setOnClickListener(new View.OnClickListener() {

         public void onClick(View view)
         {
            Intent intent = new Intent(SoccerStatIntent.TEAMS);
            intent.addCategory(SoccerStatIntent.DEFAULT);
            startActivity(intent);
         }

      });
      
      mHelpButton  = (Button) findViewById(R.id.help);
      mHelpButton.setOnClickListener(new View.OnClickListener() {

         public void onClick(View view)
         {
            Intent intent = new Intent(SoccerStatIntent.HELP);
            intent.addCategory(SoccerStatIntent.DEFAULT);
            startActivity(intent);
         }

      });
      
  }
      
}
