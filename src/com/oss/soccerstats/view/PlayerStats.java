/////////////////////////////////////////////////////////////////
// PlayerStats.java
//
// SoccerSoft - Proprietary (Restricted)
// Solely for authorized persons having a need to know
//
// Copyright (c) 2010, 2011 SoccerSoft - All rights reserved.
// THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF SoccerSoft.
// The copyright notice above does not evidence any actual
// or intended publication of such source code.
/////////////////////////////////////////////////////////////////

package com.oss.soccerstats.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ListView;

import com.oss.soccerstats.R;
import com.oss.soccerstats.data.SoccerStatsDataHelper;
import com.oss.soccerstats.data.SoccerStatsDataHelper.Player;
import com.oss.soccerstats.intent.SoccerStatIntent;
import com.oss.soccerstats.widget.PlayerStatsAdapter;

public class PlayerStats extends ListActivity
{
   /**
    * The columns we are interested in from the database
    */
   private static final String[] PLAYER_STATS_PROJECTION = new String[] {
           SoccerStatsDataHelper.PlayerStats._ID,           // 0
           SoccerStatsDataHelper.PlayerStats.PLAYER_NAME,   // 1
           SoccerStatsDataHelper.PlayerStats.JERSEY_NUMBER, // 2
           SoccerStatsDataHelper.PlayerStats.GOALS,         // 3
           SoccerStatsDataHelper.PlayerStats.ASSISTS,       // 4
           SoccerStatsDataHelper.PlayerStats.SOGS,          // 5
           SoccerStatsDataHelper.PlayerStats.SAVES,         // 6
           SoccerStatsDataHelper.PlayerStats.AGAINST,       // 7
           SoccerStatsDataHelper.PlayerStats.PLAY_TIME      // 8
   };

   private static final int DIALOG_HELP_STATS = 0;
   private Cursor mCursor;

   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle savedInstanceState) 
   {
      super.onCreate(savedInstanceState);
      setTitle(R.string.playerStatsTitle);
      
      Intent intent = getIntent();
      int gameId = intent.getIntExtra(SoccerStatIntent.GAME_ID, 
                                      SoccerStatsDataHelper.GAME_ID_DEFAULT);
      
      mCursor = managedQuery(SoccerStatsDataHelper.PlayerStats.CONTENT_URI, 
                             PLAYER_STATS_PROJECTION, 
                             Player.NAME + "!=\'Other\' AND " +
                             SoccerStatsDataHelper.PlayerStats.GAME_ID +
                             "=" + gameId, null,
                             Player.DEFAULT_SORT_ORDER);

      // Used to map playerstats table entries to this view
      PlayerStatsAdapter adapter = new PlayerStatsAdapter(
              this, R.layout.player_stats_list_item, mCursor,
              new String[] { 
                       Player.JERSEY_NUMBER, Player.NAME, 
                       SoccerStatsDataHelper.PlayerStats.PLAY_TIME,
                       SoccerStatsDataHelper.PlayerStats.GOALS,
                       SoccerStatsDataHelper.PlayerStats.SOGS,
                       SoccerStatsDataHelper.PlayerStats.ASSISTS,
                       SoccerStatsDataHelper.PlayerStats.SAVES,
                       SoccerStatsDataHelper.PlayerStats.AGAINST,
                       Player._ID }, 
              new int[] { 
                       R.id.playerJerseyNumber, R.id.playerName, 
                       R.id.playerTimeOnField,
                       R.id.playerGoals,
                       R.id.playerSOGs,
                       R.id.playerAssists,
                       R.id.playerSaves,
                       R.id.playerGoalsAllowed,
                       R.id.playerID});
      
//      adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
//         
//         @Override
//         public boolean setViewValue(View view, Cursor cursor, int columnIndex)
//         {
//            boolean retCode = false;
//            
//            int timeIndex = 
//               cursor.getColumnIndex(PlayerStatsDataHelper.PlayerStats.PLAY_TIME);
//            if (columnIndex == timeIndex)
//            {
//               int playTime = cursor.getInt(columnIndex);
//               //convert to seconds
//               playTime = playTime / 1000; // milliseconds to seconds
//               long minutes = playTime / 60;
//               long remainder = playTime % 60;
//               long seconds   = remainder;
//               String strTime =  ( (minutes < 10 ? "0":"") + minutes
//                                    + ":"
//                                    + (seconds < 10 ? "0":"") + seconds );
//               ((TextView) view).setText(strTime);
//               retCode = true;
//            }
//            
//            return retCode;
//         }
//      });
      
      ListView lv = getListView();
      LayoutInflater inflater = getLayoutInflater();
      ViewGroup headerRow = (ViewGroup)inflater.inflate(R.layout.player_stats_header, 
                                                        lv, false);
      lv.addHeaderView(headerRow);

      setListAdapter(adapter);
   }
      
   @Override
   public boolean onCreateOptionsMenu(Menu aMenu)
   {
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.stats_menu, aMenu);
      return true;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem aMenuItem) 
   {
      // Handle item selection
      switch (aMenuItem.getItemId())
      {
         case R.id.help:
         {
           showDialog(DIALOG_HELP_STATS);
           return true;
         }
         default:
            return false;
      }
   }

   @Override
   protected Dialog onCreateDialog(int aDialogId)
   {
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      if (DIALOG_HELP_STATS == aDialogId)
      {
         builder.setTitle(R.string.help)
            .setNeutralButton(getString(R.string.dismiss),
                           new DialogInterface.OnClickListener() {
                              public void onClick(DialogInterface dialog, int id)
                              {
                                 return;
                              }
                           })
            .setMessage(getString(R.string.playerStatsText))
            .setCancelable(true);
      }
      
      AlertDialog alert = builder.create();

      return alert;
   }
   
}
