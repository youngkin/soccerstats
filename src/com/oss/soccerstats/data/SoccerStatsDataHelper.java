/////////////////////////////////////////////////////////////////
// SoccerStatsDataHelper.java
//
// SoccerSoft - Proprietary (Restricted)
// Solely for authorized persons having a need to know
//
// Copyright (c) 2010, 2011 SoccerSoft - All rights reserved.
// THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF SoccerSoft.
// The copyright notice above does not evidence any actual
// or intended publication of such source code.
/////////////////////////////////////////////////////////////////

package com.oss.soccerstats.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Convenience definitions for SoccerStatsProvider
 */
public final class SoccerStatsDataHelper {
   public static final String AUTHORITY = "com.oss.soccerstats.data.SoccerStatsDataHelper";
   
   // Common constants
   public static final int SEASON_ID_DEFAULT = 1;
   public static final int TEAM_ID_DEFAULT   = 1;
   public static final int GAME_ID_DEFAULT   = -1;

   public static final String SEASON_TABLE_NAME        = "season";
   public static final String TEAM_TABLE_NAME          = "team";
   public static final String PLAYER_TABLE_NAME        = "player";
   public static final String PLAYERSTATS_TABLE_NAME   = "playerStats";
   public static final String GAME_TABLE_NAME          = "game";
   
   /**
    *    Season table
    */
    public static final class Season implements BaseColumns {
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI
                = Uri.parse("content://" + AUTHORITY + "/season");

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of players.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.soccerstats.season";

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single player.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.soccerstats.season";

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "name ASC";

        /**
         * Default value for season name if one isn't provided.
         */
        public static final String DEFAULT_SEASON_NAME = "";

        /**
         * The season's name
         * <P>Type: TEXT</P>
         */
        public static final String SEASON_NAME = "season_name";

        /**
         * The timestamp for when the note was created
         * <P>Type: INTEGER (long)</P>
         */
        public static final String CREATED_DATE = "created";

        /**
         * The timestamp for when the note was last modified
         * <P>Type: INTEGER (long)</P>
         */
        public static final String MODIFIED_DATE = "modified";
    }

    /**
     *    Team table
     */
     public static final class Team implements BaseColumns {
         /**
          * The content:// style URL for this table
          */
         public static final Uri CONTENT_URI
                 = Uri.parse("content://" + AUTHORITY + "/team");

         /**
          * The MIME type of {@link #CONTENT_URI} providing a directory of players.
          */
         public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.soccerstats.team";

         /**
          * The MIME type of a {@link #CONTENT_URI} sub-directory of a single player.
          */
         public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.soccerstats.team";

         /**
          * The default sort order for this table
          */
         public static final String DEFAULT_SORT_ORDER = Team.TEAM_NAME + " ASC";
         
         /**
          * Default value for team name if one isn't provided.
          */
         public static final String DEFAULT_TEAM_NAME = "Us";

         /**
          * The Season's unique ID from the Season table
          * <P>Type: INTEGER</P>
          */
         public static final String SEASON_ID = "seasonId";

         /**
          * The team's name
          * <P>Type: TEXT</P>
          */
         public static final String TEAM_NAME = "team_name";

         /**
          * The timestamp for when the note was created
          * <P>Type: INTEGER (long)</P>
          */
         public static final String CREATED_DATE = "created";

         /**
          * The timestamp for when the note was last modified
          * <P>Type: INTEGER (long)</P>
          */
         public static final String MODIFIED_DATE = "modified";
     }

     /**
      *    Game table
      */
      public static final class Game implements BaseColumns {
          /**
           * The content:// style URL for this table
           */
          public static final Uri CONTENT_URI
                  = Uri.parse("content://" + AUTHORITY + "/game");

          /**
           * The MIME type of {@link #CONTENT_URI} providing a directory of games.
           */
          public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.soccerstats.game";

          /**
           * The MIME type of a {@link #CONTENT_URI} sub-directory of a single game.
           */
          public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.soccerstats.game";

          /**
           * The default sort order for this table
           */
          public static final String DEFAULT_SORT_ORDER = Game.GAME_DATE + " ASC";
          
          /**
           * Default location name if one isn't provided
           */
          public static final String DEFAULT_LOCATION_NAME = "";
          
          /**
           * Default Them team name if one isn't provided.
           */
          public static final String DEFAULT_THEM_TEAM_NAME = "Them";

          /**
           * The Season's unique ID from the Season table
           * <P>Type: INTEGER</P>
           */
          public static final String SEASON_ID = "seasonId";

          /**
           * The team's ID
           * <P>Type: TEXT</P>
           */
          public static final String TEAM_ID = "teamId";

          /**
           * The game location
           * <P>Type: TEXT</P>
           */
          public static final String LOCATION = "location";

          /**
           * Opposing team name
           * <P>Type: TEXT</P>
           */
          public static final String THEM_TEAM_NAME = "themTeamName";

          /**
           * The goals scored by "us"
           * <P>Type: INTEGER</P>
           */
          public static final String US_GOALS = "usGoals";

          /**
           * The goals scored by "them"
           * <P>Type: INTEGER</P>
           */
          public static final String THEM_GOALS = "themGoals";

          /**
           * The shots-on-goal by "us"
           * <P>Type: INTEGER</P>
           */
          public static final String US_SOG = "usSog";

          /**
           * The shots-on-goal by "them"
           * <P>Type: INTEGER</P>
           */
          public static final String THEM_SOG = "themSog";

          /**
           * The date of the game
           * <P>Type: INTEGER (long)</P>
           */
          public static final String GAME_DATE = "gameDate";

          /**
           * The timestamp for when the game was last modified
           * <P>Type: INTEGER (long)</P>
           */
          public static final String MODIFIED_DATE = "modified";

          public static final int SEASON_ID_DEFAULT = SoccerStatsDataHelper.SEASON_ID_DEFAULT;
          public static final int TEAM_ID_DEFAULT = SoccerStatsDataHelper.TEAM_ID_DEFAULT;
      }

    /**
     *    Player table
     */
     public static final class Player implements BaseColumns {
         /**
          * The content:// style URL for this table
          */
         public static final Uri CONTENT_URI
                 = Uri.parse("content://" + AUTHORITY + "/player");

         /**
          * The MIME type of {@link #CONTENT_URI} providing a directory of players.
          */
         public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.soccerstats.player";

         /**
          * The MIME type of a {@link #CONTENT_URI} sub-directory of a single player.
          */
         public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.soccerstats.player";

         /**
          * Name for the "Other" player. This is the player used when a game event isn't 
          * attributed to a tracked player.
          */
         public static final String OTHER_PLAYER_NAME          = "Other";
         public static final String OTHER_PLAYER_JERSEY_NUMBER = "000";
         
         /**
          * The default sort order for this table
          */
         public static final String DEFAULT_SORT_ORDER = "jerseyNumber ASC";

         /**
          * The unique ID for the default Season
          * <P>Type: INTEGER</P>
          */
         public static final int SEASON_ID_DEFAULT = SoccerStatsDataHelper.SEASON_ID_DEFAULT;

         /**
          * The unique ID for the default Team 
          * <P>Type: INTEGER</P>
          */
         public static final int TEAM_ID_DEFAULT = SoccerStatsDataHelper.TEAM_ID_DEFAULT;

         /**
          * The Season's unique ID from the Season table
          * <P>Type: INTEGER</P>
          */
         public static final String SEASON_ID = "seasonId";

         /**
          * The Team's unique ID from the Team table
          * <P>Type: INTEGER</P>
          */
         public static final String TEAM_ID = "teamId";
         /**
          * The player's name
          * <P>Type: TEXT</P>
          */
         public static final String NAME = "name";

         /**
          * The player's jersey number
          * <P>Type: TEXT</P>
          */
         public static final String JERSEY_NUMBER = "jerseyNumber";

         /**
          * The timestamp for when the note was created
          * <P>Type: INTEGER (long)</P>
          */
         public static final String CREATED_DATE = "created";

         /**
          * The timestamp for when the note was last modified
          * <P>Type: INTEGER (long)</P>
          */
         public static final String MODIFIED_DATE = "modified";
     }

    /**
     *    PlayerStats table
     */
     public static final class PlayerStats implements BaseColumns {
         /**
          * The content:// style URL for this table
          */
         public static final Uri CONTENT_URI
                 = Uri.parse("content://" + AUTHORITY + "/playerStats");

         /**
          * The MIME type of {@link #CONTENT_URI} providing a directory of playerStats.
          */
         public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.soccerstats.playerStats";

         /**
          * The MIME type of a {@link #CONTENT_URI} sub-directory of a single playerStat.
          */
         public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.soccerstats.playerStats";

         /**
          * The default sort order for this table
          */
         public static final String DEFAULT_SORT_ORDER = "playerId ASC";

         /**
          * The unique ID for the default Season
          * <P>Type: INTEGER</P>
          */
         public static final int SEASON_ID_DEFAULT = SoccerStatsDataHelper.SEASON_ID_DEFAULT;

         /**
          * The unique ID for the default Team 
          * <P>Type: INTEGER</P>
          */
         public static final int TEAM_ID_DEFAULT = SoccerStatsDataHelper.TEAM_ID_DEFAULT;

         /**
          * The unique ID for the default Game 
          * <P>Type: INTEGER</P>
          */
         public static final int GAME_ID_DEFAULT = SoccerStatsDataHelper.GAME_ID_DEFAULT;

         /**
          * The Season's unique ID from the Season table
          * <P>Type: INTEGER</P>
          */
         public static final String SEASON_ID = "seasonId";

         /**
          * The Team's unique ID from the Team table
          * <P>Type: INTEGER</P>
          */
         public static final String TEAM_ID = "teamId";

         /**
          * The Games's unique ID from the Game table
          * <P>Type: INTEGER</P>
          */
         public static final String GAME_ID = "gameId";

         /**
          * The player's unique ID from the Player table
          * <P>Type: INTEGER</P>
          */
         public static final String PLAYER_ID = "playerId";

         /**
          * The player's time in game (in seconds)
          * <P>Type: INTEGER</P>
          */
         public static final String PLAY_TIME = "playTime";

         /**
          * The player's time on the bench (in seconds)
          * <P>Type: INTEGER</P>
          */
         public static final String BENCH_TIME = "benchTime";

         /**
          * The number of goals scored by the player
          * <P>Type: INTEGER</P>
          */
         public static final String GOALS = "goals";

         /**
          * The number of shots on goal by the player
          * <P>Type: INTEGER</P>
          */
         public static final String SOGS = "sogs";

         /**
          * The number of goal assists by the player
          * <P>Type: INTEGER</P>
          */
         public static final String ASSISTS = "assists";

         /**
          * The number of saves by the player
          * <P>Type: INTEGER</P>
          */
         public static final String SAVES = "saves";

         /**
          * The number of goals allowed by the keeper
          * <P>Type: INTEGER</P>
          */
         public static final String AGAINST = "against";

         /**
          * The player's jersey number
          * <P>Type: STRING</P>
          */
         public static final String JERSEY_NUMBER = Player.JERSEY_NUMBER;

         /**
          * The player's jersey number
          * <P>Type: STRING</P>
          */
         public static final String PLAYER_NAME = Player.NAME;

         /**
          * The timestamp for when the note was created
          * <P>Type: INTEGER (long)</P>
          */
         public static final String CREATED_DATE = "created";

         /**
          * The timestamp for when the note was last modified
          * <P>Type: INTEGER (long)</P>
          */
         public static final String MODIFIED_DATE = "modified";
     }

}

