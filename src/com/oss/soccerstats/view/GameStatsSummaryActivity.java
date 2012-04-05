/////////////////////////////////////////////////////////////////
// GameStatsSummaryActivity.java
//
// SoccerSoft - Proprietary (Restricted)
// Solely for authorized persons having a need to know
// pursuant to Company Instructions.
//
// Copyright (c) 2010, 2011 SoccerSoft Inc.  All rights reserved.
// THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF SoccerSoft.
// The copyright notice above does not evidence any actual
// or intended publication of such source code.
/////////////////////////////////////////////////////////////////

package com.oss.soccerstats.view;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.oss.soccerstats.R;
import com.oss.soccerstats.data.SoccerStatsDataHelper;
import com.oss.soccerstats.data.SoccerStatsDataHelper.Game;
import com.oss.soccerstats.data.SoccerStatsDataHelper.Player;
import com.oss.soccerstats.data.SoccerStatsDataHelper.Team;
import com.oss.soccerstats.data.SoccerStatsProvider;
import com.oss.soccerstats.intent.SoccerStatIntent;
import com.oss.soccerstats.widget.PlayerStatsAdapter;

public class GameStatsSummaryActivity extends Activity
{
   /**
    * The columns we are interested in from the Games table
    */
   private static final String[] GAMES_PROJECTION = new String[] {
           SoccerStatsDataHelper.Game._ID,            // 0
           SoccerStatsDataHelper.Game.GAME_DATE,      // 1
           SoccerStatsDataHelper.Game.THEM_TEAM_NAME, // 2
           SoccerStatsDataHelper.Game.US_GOALS,       // 3
           SoccerStatsDataHelper.Game.THEM_GOALS,     // 4
           SoccerStatsDataHelper.Game.US_SOG,         // 5
           SoccerStatsDataHelper.Game.THEM_SOG,       // 6
           SoccerStatsDataHelper.Game.LOCATION,       // 7
           SoccerStatsDataHelper.Team.TEAM_NAME       // 8
   };
   
   /**
    * The columns we are interested in from the PlayerStats table
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

   //-----------[ Lifecycle methods ]-----------------------------------------//
   //
   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle savedInstanceState) 
   {
      super.onCreate(savedInstanceState);
      setTitle(R.string.gameStatusSummaryTitle);
      setContentView(R.layout.game_stats);  
      
      Intent launchIntent = getIntent();
      int teamId = launchIntent.getIntExtra(SoccerStatIntent.TEAM_ID, 
               SoccerStatsDataHelper.TEAM_ID_DEFAULT);
      int gameId = launchIntent.getIntExtra(SoccerStatIntent.GAME_ID, 
               SoccerStatsDataHelper.GAME_ID_DEFAULT);
      
      populateGameSummaryView(gameId, teamId);
      populatePlayerStatsView(gameId);
   }

   private void populateGameSummaryView(int gameId, int teamId)
   {
      Cursor gameStatsCursor = managedQuery(SoccerStatsDataHelper.Game.CONTENT_URI, 
                                            GAMES_PROJECTION, 
                                            SoccerStatsProvider.GAME_TABLE_NAME + "." + Game._ID + 
                                            "=" + gameId + " AND " + 
                                            Game.TEAM_ID + "=" + teamId, 
                                            null,
                                            Game.DEFAULT_SORT_ORDER);

      gameStatsCursor.moveToFirst();
      int gameDateColumnIndex       = 
         gameStatsCursor.getColumnIndex(Game.GAME_DATE);
      int usTeamNameColumnIndex       = 
         gameStatsCursor.getColumnIndex(Team.TEAM_NAME);
      int themTeamNameColumnIndex   = 
         gameStatsCursor.getColumnIndex(Game.THEM_TEAM_NAME);
      int usSogColumnIndex   = 
         gameStatsCursor.getColumnIndex(Game.US_SOG);
      int usGoalsColumnIndex   = 
         gameStatsCursor.getColumnIndex(Game.US_GOALS);
      int themSogColumnIndex   = 
         gameStatsCursor.getColumnIndex(Game.THEM_SOG);
      int themGoalsColumnIndex   = 
         gameStatsCursor.getColumnIndex(Game.THEM_GOALS);
      int locationIndex = 
         gameStatsCursor.getColumnIndex(SoccerStatsDataHelper.Game.LOCATION);
      
      long   gameDate      = gameStatsCursor.getInt(gameDateColumnIndex);
      String usTeamName    = gameStatsCursor.getString(usTeamNameColumnIndex);
      int    usSog         = gameStatsCursor.getInt(usSogColumnIndex);
      int    usGoals       = gameStatsCursor.getInt(usGoalsColumnIndex);
      String themTeamName  = gameStatsCursor.getString(themTeamNameColumnIndex);
      int    themSog       = gameStatsCursor.getInt(themSogColumnIndex);
      int    themGoals     = gameStatsCursor.getInt(themGoalsColumnIndex);
      String locationValue = gameStatsCursor.getString(locationIndex);
     
      gameStatsCursor.close();

      TextView header = (TextView) findViewById(R.id.scoreHeader);
      Date gameDateCal = new Date(gameDate * 1000);
      SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy");
      header.setText(sdf.format(gameDateCal) + " - " + usTeamName + " vs. " + themTeamName);

      TextView score  = (TextView) findViewById(R.id.gameStatsScore);
      score.setText(usGoals + " - " + themGoals);
      TextView sog    = (TextView) findViewById(R.id.gameStatsSOG);
      sog.setText(usSog + " - " + themSog);

      TextView locationField = (TextView) findViewById(R.id.gameLocation);
      locationField.setText(locationValue);

   }

   private void populatePlayerStatsView(int gameId)
   {
      Cursor playerStatsCursor = managedQuery(SoccerStatsDataHelper.PlayerStats.CONTENT_URI, 
                                              PLAYER_STATS_PROJECTION, 
                                              Player.NAME + "!=\'Other\' AND " +
                                              SoccerStatsDataHelper.PlayerStats.GAME_ID +
                                              "=" + gameId, null, Player.DEFAULT_SORT_ORDER);

      // Used to get player rows from the database to views
      PlayerStatsAdapter adapter = 
         new PlayerStatsAdapter(this, R.layout.player_stats_list_item, playerStatsCursor,
              new String[] { Player.JERSEY_NUMBER, Player.NAME, 
                  SoccerStatsDataHelper.PlayerStats.PLAY_TIME,
                  SoccerStatsDataHelper.PlayerStats.GOALS,
                  SoccerStatsDataHelper.PlayerStats.SOGS,
                  SoccerStatsDataHelper.PlayerStats.ASSISTS,
                  SoccerStatsDataHelper.PlayerStats.SAVES,
                  SoccerStatsDataHelper.PlayerStats.AGAINST,
                  Player._ID }, 
              new int[] { R.id.playerJerseyNumber, R.id.playerName, 
                  R.id.playerTimeOnField,
                  R.id.playerGoals,
                  R.id.playerSOGs,
                  R.id.playerAssists,
                  R.id.playerSaves,
                  R.id.playerGoalsAllowed,
                  R.id.playerID });

      ListView playerStatsListView = (ListView)findViewById(R.id.playerStatsView);
      LayoutInflater inflater = getLayoutInflater();
      ViewGroup headerRow = 
         (ViewGroup)inflater.inflate(R.layout.player_stats_header_plain_background, 
                                     playerStatsListView, false);
      playerStatsListView.addHeaderView(headerRow);
      playerStatsListView.setAdapter(adapter);
   }

}
