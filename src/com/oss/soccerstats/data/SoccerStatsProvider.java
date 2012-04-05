/////////////////////////////////////////////////////////////////
// SoccerStatsProvider.java
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

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.LiveFolders;
import android.text.TextUtils;
import android.util.Log;

import com.oss.soccerstats.data.SoccerStatsDataHelper.Game;
import com.oss.soccerstats.data.SoccerStatsDataHelper.Player;
import com.oss.soccerstats.data.SoccerStatsDataHelper.PlayerStats;
import com.oss.soccerstats.data.SoccerStatsDataHelper.Season;
import com.oss.soccerstats.data.SoccerStatsDataHelper.Team;

/**
 * Provides access to the team roster. Each player has a name, a jersey number,
 * a creation date, and a modified date.
 */
public class SoccerStatsProvider extends ContentProvider
{

   private enum DbVersions
   {
      V1(1), V2(6), V3(8);

      private int mVersionNum;

      private DbVersions(int versionNum)
      {
         mVersionNum = versionNum;
      }

      public int getVersion()
      {
         return mVersionNum;
      }
   };
   
   private enum Tables
   {
      PLAYER,
      PLAYERSTATS,
      GAME,
      TEAM
   };

   private static final String TAG = "SoccerStatsProvider";

   private static final String DATABASE_NAME            = "soccer_stats.db";
   private static final int    DATABASE_VERSION         = DbVersions.V3.getVersion();
   private static final String SEASON_TABLE_NAME        = SoccerStatsDataHelper.SEASON_TABLE_NAME;
   private static final String TEAM_TABLE_NAME          = SoccerStatsDataHelper.TEAM_TABLE_NAME;
   private static final String PLAYER_TABLE_NAME        = SoccerStatsDataHelper.PLAYER_TABLE_NAME;
   private static final String PLAYERSTATS_TABLE_NAME   = SoccerStatsDataHelper.PLAYERSTATS_TABLE_NAME;
   public  static final String GAME_TABLE_NAME          = SoccerStatsDataHelper.GAME_TABLE_NAME;

   private static HashMap<String, String> sPlayerProjectionMap;
   private static HashMap<String, String> sPlayerStatsProjectionMap;
   private static HashMap<String, String> sGameProjectionMap;
   private static HashMap<String, String> sTeamProjectionMap;
   private static HashMap<String, String> sLiveFolderProjectionMap;

   private static final int PLAYER = 1;
   private static final int PLAYER_ID = 2;
   private static final int LIVE_FOLDER_PLAYER = 3;
   private static final int PLAYERSTATS = 4;
   private static final int PLAYERSTATS_ID = 5;
   private static final int GAME = 6;
   private static final int GAME_ID = 7;
   private static final int TEAM = 8;
   private static final int TEAM_ID = 9;

   private static final UriMatcher sUriMatcher;

   private static final Long MILLIS = 1000L;

   /**
    * This class helps open, create, and upgrade the database file.
    */
   private static class DatabaseHelper extends SQLiteOpenHelper
   {

      DatabaseHelper(Context context)
      {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
      }

      @Override
      public void onCreate(SQLiteDatabase db)
      {
         db.execSQL("CREATE TABLE " + SEASON_TABLE_NAME + " (" + Season._ID
                  + " INTEGER PRIMARY KEY," + Season.SEASON_NAME
                  + " TEXT," + Season.CREATED_DATE + " INTEGER,"
                  + Season.MODIFIED_DATE + " INTEGER" + ");");

         db.execSQL("INSERT INTO " + SEASON_TABLE_NAME + " ("
                  + Season.SEASON_NAME + ") VALUES ('');");
         Cursor c = db.rawQuery("SELECT " + Season._ID + " FROM "
                  + SEASON_TABLE_NAME + " WHERE " + Season.SEASON_NAME
                  + "=''", null);
         c.moveToFirst();
         int seasonId = c.getInt(0);
         c.close();

         db.execSQL("CREATE TABLE " + TEAM_TABLE_NAME + " (" + Team._ID
                  + " INTEGER PRIMARY KEY," + Team.SEASON_ID
                  + " INTEGER NOT NULL," + Team.TEAM_NAME + " TEXT,"
                  + Team.CREATED_DATE + " INTEGER," + Team.MODIFIED_DATE
                  + " INTEGER" + ");");

         String sql = "INSERT INTO " + TEAM_TABLE_NAME + " ("
         + Team.SEASON_ID + ", " + Team.TEAM_NAME + ") VALUES ("
         + seasonId + ",'" + SoccerStatsDataHelper.Team.DEFAULT_TEAM_NAME + "');";
         db.execSQL(sql);
         Cursor tc = db.rawQuery("SELECT " + Team._ID + " FROM "
                  + TEAM_TABLE_NAME + " WHERE " + Team.TEAM_NAME
                  + "='" + SoccerStatsDataHelper.Team.DEFAULT_TEAM_NAME + "'", null);
         tc.moveToFirst();
         int teamId = tc.getInt(0);
         tc.close();

         db.execSQL("CREATE TABLE " + PLAYER_TABLE_NAME + " (" + Player._ID
                  + " INTEGER PRIMARY KEY," + Player.SEASON_ID
                  + " INTEGER NOT NULL," + Player.TEAM_ID
                  + " INTEGER NOT NULL," + Player.NAME + " TEXT,"
                  + Player.JERSEY_NUMBER + " TEXT," + Player.CREATED_DATE
                  + " INTEGER," + Player.MODIFIED_DATE + " INTEGER"
                  + ");");

         db.execSQL("INSERT INTO " + PLAYER_TABLE_NAME + " ("
                  + Player.SEASON_ID + "," + Player.TEAM_ID + ","
                  + Player.NAME + ", " + Player.JERSEY_NUMBER
                  + ") VALUES (" + seasonId + "," + teamId + ","
                  + "'" + Player.OTHER_PLAYER_NAME + "'," 
                  + "'" +Player.OTHER_PLAYER_JERSEY_NUMBER + "');");

         db.execSQL("CREATE TABLE " + GAME_TABLE_NAME + " (" + Game._ID
                  + " INTEGER PRIMARY KEY," + Game.SEASON_ID
                  + " INTEGER NOT NULL," + Game.TEAM_ID
                  + " INTEGER NOT NULL," + Game.LOCATION + " TEXT,"
                  + Game.THEM_TEAM_NAME + " TEXT," + Game.US_GOALS
                  + " INTEGER," + Game.THEM_GOALS + " INTEGER,"
                  + Game.US_SOG + " INTEGER," + Game.THEM_SOG
                  + " INTEGER," + Game.GAME_DATE + " INTEGER,"
                  + Player.MODIFIED_DATE + " INTEGER" + ");");

         db.execSQL("CREATE TABLE " + PLAYERSTATS_TABLE_NAME + " ("
                  + PlayerStats._ID + " INTEGER PRIMARY KEY,"
                  + PlayerStats.SEASON_ID + " INTEGER NOT NULL,"
                  + PlayerStats.TEAM_ID + " INTEGER NOT NULL,"
                  + PlayerStats.GAME_ID + " INTEGER NOT NULL,"
                  + PlayerStats.PLAYER_ID + " INTEGER NOT NULL,"
                  + PlayerStats.GOALS + " INTEGER," + PlayerStats.SOGS
                  + " INTEGER," + PlayerStats.ASSISTS + " INTEGER,"
                  + PlayerStats.SAVES + " INTEGER," + PlayerStats.AGAINST
                  + " INTEGER," + PlayerStats.PLAY_TIME + " INTEGER,"
                  + PlayerStats.BENCH_TIME + " INTEGER,"
                  + PlayerStats.CREATED_DATE + " INTEGER,"
                  + PlayerStats.MODIFIED_DATE + " INTEGER" + ");");

      }

      @Override
      public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
      {
         Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                  + newVersion + ".");
         db.execSQL("DROP TABLE IF EXISTS " + PLAYER_TABLE_NAME);
         db.execSQL("DROP TABLE IF EXISTS " + PLAYERSTATS_TABLE_NAME);
         onCreate(db);
      }
   }

   private DatabaseHelper mOpenHelper;

   @Override
   public boolean onCreate()
   {
      mOpenHelper = new DatabaseHelper(getContext());
      if (DATABASE_VERSION == DbVersions.V2.getVersion())
      {
         delete(SoccerStatsDataHelper.Player.CONTENT_URI, Player.NAME
                  + "=\'None\' AND " + Player.JERSEY_NUMBER + " IS NULL",
                  null);
      }
      return true;
   }

   @Override
   public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder)
   {
      SQLiteQueryBuilder playerQb = new SQLiteQueryBuilder();
      playerQb.setTables(PLAYER_TABLE_NAME);

      SQLiteQueryBuilder playerStatsQb = new SQLiteQueryBuilder();
      playerStatsQb.setTables(PLAYERSTATS_TABLE_NAME + " INNER JOIN "
               + PLAYER_TABLE_NAME + " ON " + PLAYERSTATS_TABLE_NAME + "."
               + PlayerStats.PLAYER_ID + "=" + PLAYER_TABLE_NAME + "."
               + Player._ID);

      SQLiteQueryBuilder gameQb = new SQLiteQueryBuilder();
      gameQb.setTables(GAME_TABLE_NAME + " INNER JOIN "
               + TEAM_TABLE_NAME + " ON " + GAME_TABLE_NAME + "."
               + Game.TEAM_ID + "=" + TEAM_TABLE_NAME + "."
               + Team._ID);

      SQLiteQueryBuilder teamQb = new SQLiteQueryBuilder();
      teamQb.setTables(TEAM_TABLE_NAME + " INNER JOIN "
               + SEASON_TABLE_NAME + " ON " + TEAM_TABLE_NAME + "."
               + Team.SEASON_ID + "=" + SEASON_TABLE_NAME + "."
               + Season._ID);

      Tables rqstType = Tables.PLAYER;

      switch (sUriMatcher.match(uri))
      {
         case GAME:
            gameQb.setProjectionMap(sGameProjectionMap);
            rqstType = Tables.GAME;
            break;

         case GAME_ID:
            gameQb.setProjectionMap(sGameProjectionMap);
            gameQb.appendWhere(Game._ID + "="
                     + uri.getPathSegments().get(1));
            rqstType = Tables.GAME;
            break;

         case PLAYER:
            playerQb.setProjectionMap(sPlayerProjectionMap);
            rqstType = Tables.PLAYER;
            break;

         case PLAYER_ID:
            playerQb.setProjectionMap(sPlayerProjectionMap);
            playerQb.appendWhere(Player._ID + "="
                     + uri.getPathSegments().get(1));
            rqstType = Tables.PLAYER;
            break;

         case LIVE_FOLDER_PLAYER:
            playerQb.setProjectionMap(sLiveFolderProjectionMap);
            rqstType = Tables.PLAYER;
            break;

         case PLAYERSTATS:
            playerStatsQb.setProjectionMap(sPlayerStatsProjectionMap);
            rqstType = Tables.PLAYERSTATS;
            break;

         case PLAYERSTATS_ID:
            playerStatsQb.setProjectionMap(sPlayerStatsProjectionMap);
            playerStatsQb.appendWhere(PlayerStats._ID + "="
                     + uri.getPathSegments().get(1));
            rqstType = Tables.PLAYERSTATS;
            break;

         case TEAM:
            teamQb.setProjectionMap(sTeamProjectionMap);
            rqstType = Tables.TEAM;
            break;

         case TEAM_ID:
            teamQb.setProjectionMap(sTeamProjectionMap);
            teamQb.appendWhere(Team._ID + "="
                     + uri.getPathSegments().get(1));
            rqstType = Tables.TEAM;
            break;

         default:
            throw new IllegalArgumentException("Unknown URI " + uri);
      }

      // If no sort order is specified use the default
      String orderBy;

      // Get the database and run the query
      SQLiteDatabase db = mOpenHelper.getReadableDatabase();
      Cursor c = null;
      switch (rqstType)
      {
         case PLAYER:
            if (TextUtils.isEmpty(sortOrder))
            {
               orderBy = SoccerStatsDataHelper.Player.DEFAULT_SORT_ORDER;
            }
            else
            {
               orderBy = sortOrder;
            }
            c = playerQb.query(db, projection, selection, selectionArgs, null,
                               null, orderBy);            
            break;
         case PLAYERSTATS:
            if (TextUtils.isEmpty(sortOrder))
            {
               orderBy = SoccerStatsDataHelper.PlayerStats.DEFAULT_SORT_ORDER;
            }
            else
            {
               orderBy = sortOrder;
            }
            c = playerStatsQb.query(db, projection, selection, selectionArgs,
                                    null, null, orderBy);
            break;
         case GAME:
            if (TextUtils.isEmpty(sortOrder))
            {
               orderBy = SoccerStatsDataHelper.Game.DEFAULT_SORT_ORDER;
            }
            else
            {
               orderBy = sortOrder;
            }
            c = gameQb.query(db, projection, selection, selectionArgs,
                                    null, null, orderBy);
            break;
         case TEAM:
            if (TextUtils.isEmpty(sortOrder))
            {
               orderBy = SoccerStatsDataHelper.Team.DEFAULT_SORT_ORDER;
            }
            else
            {
               orderBy = sortOrder;
            }
            c = teamQb.query(db, projection, selection, selectionArgs,
                                    null, null, orderBy);
            break;
         default:
            throw new IllegalArgumentException("Unknown SoccerStatsProvider.Tables" 
                                               + rqstType);
      }

      // Tell the cursor what uri to watch, so it knows when its source data
      // changes
      c.setNotificationUri(getContext().getContentResolver(), uri);
      return c;
   }

   @Override
   public String getType(Uri uri)
   {
      switch (sUriMatcher.match(uri))
      {
         case PLAYER:
         case LIVE_FOLDER_PLAYER:
            return Player.CONTENT_TYPE;

         case PLAYER_ID:
            return Player.CONTENT_ITEM_TYPE;

         case PLAYERSTATS:
            return PlayerStats.CONTENT_TYPE;

         case PLAYERSTATS_ID:
            return PlayerStats.CONTENT_ITEM_TYPE;

         case GAME:
            return Game.CONTENT_TYPE;

         case GAME_ID:
            return Game.CONTENT_ITEM_TYPE;

         default:
            throw new IllegalArgumentException("Unknown URI " + uri);
      }
   }

   @Override
   public Uri insert(Uri uri, ContentValues initialValues)
   {
      Uri retUri;

      switch (sUriMatcher.match(uri))
      {
         case PLAYER:
            retUri = insertPlayerRow(uri, initialValues);
            break;
         case PLAYERSTATS:
            retUri = insertPlayerStatsRow(uri, initialValues);
            break;
         case GAME:
             retUri = insertGameRow(uri, initialValues);
             break;

         case TEAM:
             retUri = insertTeamRow(uri, initialValues);
             break;

         default:
            throw new IllegalArgumentException("Unknown or unexpected URI "
                     + uri);
      }

      return retUri;
   }

   private Uri insertGameRow(Uri uri, ContentValues initialValues)
   {
      ContentValues values;
      if (initialValues != null)
      {
         values = new ContentValues(initialValues);
      }
      else
      {
         values = new ContentValues();
      }

      Long now = Long.valueOf(System.currentTimeMillis()) / MILLIS;

      // Make sure that the fields are all set
      if (values.containsKey(SoccerStatsDataHelper.Game.GAME_DATE) == false)
      {
         values.put(SoccerStatsDataHelper.Game.GAME_DATE, now);
      }
      if (values.containsKey(SoccerStatsDataHelper.Game.MODIFIED_DATE) == false)
      {
         values.put(SoccerStatsDataHelper.Game.MODIFIED_DATE, now);
      }
      if (values.containsKey(SoccerStatsDataHelper.Game.TEAM_ID) == false)
      {
         values.put(SoccerStatsDataHelper.Game.TEAM_ID, SoccerStatsDataHelper.TEAM_ID_DEFAULT);
      }
      if (values.containsKey(SoccerStatsDataHelper.Game.THEM_TEAM_NAME) == false)
      {
         values.put(SoccerStatsDataHelper.Game.THEM_TEAM_NAME, 
                    SoccerStatsDataHelper.Game.DEFAULT_THEM_TEAM_NAME);
      }
      if (values.containsKey(SoccerStatsDataHelper.Game.LOCATION) == false)
      {
         values.put(SoccerStatsDataHelper.Game.LOCATION, 
                    SoccerStatsDataHelper.Game.DEFAULT_LOCATION_NAME);
      }
      if (values.containsKey(SoccerStatsDataHelper.Game.SEASON_ID) == false)
      {
         values.put(SoccerStatsDataHelper.Game.SEASON_ID, 
                    SoccerStatsDataHelper.Game.SEASON_ID_DEFAULT);
      }

      SQLiteDatabase db = mOpenHelper.getWritableDatabase();
      long rowId = db.insert(GAME_TABLE_NAME, Game.LOCATION, values);
      if (rowId > 0)
      {
         Uri gameUri = ContentUris.withAppendedId(
                  SoccerStatsDataHelper.Game.CONTENT_URI, rowId);
         getContext().getContentResolver().notifyChange(gameUri, null);
         return gameUri;
      }

      throw new SQLException("Failed to insert row into " + uri);
   }

   private Uri insertTeamRow(Uri uri, ContentValues initialValues)
   {
      ContentValues values;
      if (initialValues != null)
      {
         values = new ContentValues(initialValues);
      }
      else
      {
         values = new ContentValues();
      }

      Long now = Long.valueOf(System.currentTimeMillis()) / MILLIS;

      // Make sure that the fields are all set
      if (values.containsKey(SoccerStatsDataHelper.Team.MODIFIED_DATE) == false)
      {
         values.put(SoccerStatsDataHelper.Team.MODIFIED_DATE, now);
      }
      if (values.containsKey(SoccerStatsDataHelper.Player.CREATED_DATE) == false)
      {
         values.put(SoccerStatsDataHelper.Player.CREATED_DATE, now);
      }
      if (values.containsKey(SoccerStatsDataHelper.Team.TEAM_NAME) == false)
      {
         values.put(SoccerStatsDataHelper.Team.TEAM_NAME, 
                    SoccerStatsDataHelper.Team.DEFAULT_TEAM_NAME);
      }
      if (values.containsKey(SoccerStatsDataHelper.Game.SEASON_ID) == false)
      {
         values.put(SoccerStatsDataHelper.Game.SEASON_ID, 
                    SoccerStatsDataHelper.Game.SEASON_ID_DEFAULT);
      }

      SQLiteDatabase db = mOpenHelper.getWritableDatabase();
      long rowId = db.insert(TEAM_TABLE_NAME, null, values);
      if (rowId > 0)
      {
         Uri teamUri = ContentUris.withAppendedId(
                  SoccerStatsDataHelper.Team.CONTENT_URI, rowId);
         getContext().getContentResolver().notifyChange(teamUri, null);
         return teamUri;
      }

      throw new SQLException("Failed to insert row into " + uri);
   }

   private Uri insertPlayerStatsRow(Uri uri, ContentValues initialValues)
   {
      ContentValues values;
      if (initialValues != null)
      {
         values = new ContentValues(initialValues);
      }
      else
      {
         values = new ContentValues();
      }

      Long now = Long.valueOf(System.currentTimeMillis()) / MILLIS;

      // Make sure that the fields are all set
      if (values.containsKey(SoccerStatsDataHelper.Player.CREATED_DATE) == false)
      {
         values.put(SoccerStatsDataHelper.Player.CREATED_DATE, now);
      }

      if (values.containsKey(SoccerStatsDataHelper.Player.MODIFIED_DATE) == false)
      {
         values.put(SoccerStatsDataHelper.Player.MODIFIED_DATE, now);
      }

      SQLiteDatabase db = mOpenHelper.getWritableDatabase();
      long rowId = db.insert(PLAYERSTATS_TABLE_NAME, PlayerStats.ASSISTS,
               values);
      if (rowId > 0)
      {
         Uri playerStatsUri = ContentUris.withAppendedId(
                  SoccerStatsDataHelper.PlayerStats.CONTENT_URI, rowId);
         getContext().getContentResolver()
            .notifyChange(playerStatsUri, null);
         return playerStatsUri;
      }

      throw new SQLException("Failed to insert row into " + uri);
   }

   private Uri insertPlayerRow(Uri uri, ContentValues initialValues)
   {
      ContentValues values;
      if (initialValues != null)
      {
         values = new ContentValues(initialValues);
      }
      else
      {
         values = new ContentValues();
      }

      Long now = Long.valueOf(System.currentTimeMillis()) / MILLIS;

      // Make sure that the fields are all set
      if (values.containsKey(SoccerStatsDataHelper.Player.CREATED_DATE) == false)
      {
         values.put(SoccerStatsDataHelper.Player.CREATED_DATE, now);
      }

      if (values.containsKey(SoccerStatsDataHelper.Player.MODIFIED_DATE) == false)
      {
         values.put(SoccerStatsDataHelper.Player.MODIFIED_DATE, now);
      }

      if (values.containsKey(SoccerStatsDataHelper.Player.NAME) == false)
      {
         Resources r = Resources.getSystem();
         values.put(SoccerStatsDataHelper.Player.NAME,
                  r.getString(android.R.string.unknownName));
      }

      if (values.containsKey(SoccerStatsDataHelper.Player.JERSEY_NUMBER) == false)
      {
         values.put(SoccerStatsDataHelper.Player.JERSEY_NUMBER, "");
      }

      if (values.containsKey(SoccerStatsDataHelper.Player.SEASON_ID) == false)
      {
         values.put(SoccerStatsDataHelper.Player.SEASON_ID,
                  SoccerStatsDataHelper.Player.SEASON_ID_DEFAULT);
      }

      if (values.containsKey(SoccerStatsDataHelper.Player.TEAM_ID) == false)
      {
         values.put(SoccerStatsDataHelper.Player.TEAM_ID,
                  SoccerStatsDataHelper.Player.TEAM_ID_DEFAULT);
      }

      SQLiteDatabase db = mOpenHelper.getWritableDatabase();
      long rowId = db.insert(PLAYER_TABLE_NAME, Player.NAME, values);
      if (rowId > 0)
      {
         Uri noteUri = ContentUris.withAppendedId(
                  SoccerStatsDataHelper.Player.CONTENT_URI, rowId);
         getContext().getContentResolver().notifyChange(noteUri, null);
         return noteUri;
      }

      throw new SQLException("Failed to insert row into " + uri);
   }

   @Override
   public int delete(Uri uri, String where, String[] whereArgs)
   {
      SQLiteDatabase db = mOpenHelper.getWritableDatabase();
      int count;
      switch (sUriMatcher.match(uri))
      {
         case GAME:
            count = db.delete(GAME_TABLE_NAME, where, whereArgs);
            break;

         case GAME_ID:
             String gameId = uri.getPathSegments().get(1);
             count = db.delete(GAME_TABLE_NAME, Game._ID
                      + "="
                      + gameId
                      + (!TextUtils.isEmpty(where) ? " AND (" + where
                               + ')' : ""), whereArgs);
             break;

         case TEAM:
             count = db.delete(TEAM_TABLE_NAME, where, whereArgs);
             break;

         case TEAM_ID:
             String teamId = uri.getPathSegments().get(1);
             count = db.delete(TEAM_TABLE_NAME, Team._ID
                      + "=" + teamId
                      + (!TextUtils.isEmpty(where) ? " AND (" + where
                               + ')' : ""), whereArgs);
             break;

         case PLAYER:
            count = db.delete(PLAYER_TABLE_NAME, where, whereArgs);
            break;

         case PLAYER_ID:
            String playerId = uri.getPathSegments().get(1);
            count = db.delete(PLAYER_TABLE_NAME, Player._ID
                     + "="
                     + playerId
                     + (!TextUtils.isEmpty(where) ? " AND (" + where
                              + ')' : ""), whereArgs);
            break;

         case PLAYERSTATS:
            count = db.delete(PLAYERSTATS_TABLE_NAME, where, whereArgs);
            break;

         case PLAYERSTATS_ID:
            String playerStatsId = uri.getPathSegments().get(1);
            count = db.delete(PLAYERSTATS_TABLE_NAME, PlayerStats._ID
                     + "="
                     + playerStatsId
                     + (!TextUtils.isEmpty(where) ? " AND (" + where
                              + ')' : ""), whereArgs);
            break;

         default:
            throw new IllegalArgumentException("Unknown URI " + uri);
      }

      getContext().getContentResolver().notifyChange(uri, null);
      return count;
   }

   @Override
   public int update(Uri uri, ContentValues values, String where,
            String[] whereArgs)
   {
      SQLiteDatabase db = mOpenHelper.getWritableDatabase();
      int count;
      switch (sUriMatcher.match(uri))
      {
         case GAME:
            count = db.update(GAME_TABLE_NAME, values, where, whereArgs);
            break;

         case GAME_ID:
            String gameId = uri.getPathSegments().get(1);
            count = db.update(GAME_TABLE_NAME, values, Game._ID
                     + "="
                     + gameId
                     + (!TextUtils.isEmpty(where) ? " AND (" + where
                              + ')' : ""), whereArgs);
            break;

         case PLAYER:
            count = db.update(PLAYER_TABLE_NAME, values, where, whereArgs);
            break;

         case PLAYER_ID:
            String playerId = uri.getPathSegments().get(1);
            count = db.update(PLAYER_TABLE_NAME, values, Player._ID
                     + "="
                     + playerId
                     + (!TextUtils.isEmpty(where) ? " AND (" + where
                              + ')' : ""), whereArgs);
            break;

         case PLAYERSTATS:
            count = db.update(PLAYERSTATS_TABLE_NAME, values, where,
                     whereArgs);
            break;

         case PLAYERSTATS_ID:
            String playerStatsId = uri.getPathSegments().get(1);
            count = db.update(
                     PLAYERSTATS_TABLE_NAME,
                     values,
                     PlayerStats._ID
                     + "="
                     + playerStatsId
                     + (!TextUtils.isEmpty(where) ? " AND ("
                              + where + ')' : ""),
                              whereArgs);
            break;

         case TEAM:
            count = db.update(TEAM_TABLE_NAME, values, where,
                     whereArgs);
            break;

         case TEAM_ID:
            String teamId = uri.getPathSegments().get(1);
            count = db.update(
                     TEAM_TABLE_NAME,
                     values,
                     PlayerStats._ID
                     + "="
                     + teamId
                     + (!TextUtils.isEmpty(where) ? " AND ("
                              + where + ')' : ""),
                              whereArgs);
            break;

         default:
            throw new IllegalArgumentException("Unknown URI " + uri);
      }

      getContext().getContentResolver().notifyChange(uri, null);
      return count;
   }

   static
   {
      sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
      sUriMatcher.addURI(SoccerStatsDataHelper.AUTHORITY, "player", PLAYER);
      sUriMatcher.addURI(SoccerStatsDataHelper.AUTHORITY, "player/#",
               PLAYER_ID);
      sUriMatcher.addURI(SoccerStatsDataHelper.AUTHORITY,
               "live_folders/player", LIVE_FOLDER_PLAYER);
      sUriMatcher.addURI(SoccerStatsDataHelper.AUTHORITY, "playerStats",
               PLAYERSTATS);
      sUriMatcher.addURI(SoccerStatsDataHelper.AUTHORITY, "playerStats/#",
               PLAYERSTATS_ID);
      sUriMatcher.addURI(SoccerStatsDataHelper.AUTHORITY, "game", GAME);
      sUriMatcher.addURI(SoccerStatsDataHelper.AUTHORITY, "game/#", GAME_ID);
      sUriMatcher.addURI(SoccerStatsDataHelper.AUTHORITY, "team", TEAM);
      sUriMatcher.addURI(SoccerStatsDataHelper.AUTHORITY, "team/#", TEAM_ID);

      sGameProjectionMap = new HashMap<String, String>();
      sGameProjectionMap.put(Game._ID, GAME_TABLE_NAME + "." + Game._ID);
      sGameProjectionMap.put(Game.LOCATION, Game.LOCATION);
      sGameProjectionMap.put(Team.TEAM_NAME, Team.TEAM_NAME);
      sGameProjectionMap.put(Game.THEM_TEAM_NAME, Game.THEM_TEAM_NAME);
      sGameProjectionMap.put(Game.THEM_GOALS, Game.THEM_GOALS);
      sGameProjectionMap.put(Game.THEM_SOG, Game.THEM_SOG);
      sGameProjectionMap.put(Game.US_GOALS, Game.US_GOALS);
      sGameProjectionMap.put(Game.US_SOG, Game.US_SOG);
      sGameProjectionMap.put(Game.GAME_DATE, Game.GAME_DATE);
      sGameProjectionMap.put(Game.MODIFIED_DATE, Game.MODIFIED_DATE);

      sPlayerProjectionMap = new HashMap<String, String>();
      sPlayerProjectionMap.put(Player._ID, Player._ID);
      sPlayerProjectionMap.put(Player.JERSEY_NUMBER, Player.JERSEY_NUMBER);
      sPlayerProjectionMap.put(Player.NAME, Player.NAME);
      sPlayerProjectionMap.put(Player.CREATED_DATE, Player.CREATED_DATE);
      sPlayerProjectionMap.put(Player.MODIFIED_DATE, Player.MODIFIED_DATE);

      sPlayerStatsProjectionMap = new HashMap<String, String>();
      sPlayerStatsProjectionMap.put(PlayerStats._ID, PLAYERSTATS_TABLE_NAME
               + "." + PlayerStats._ID);
      sPlayerStatsProjectionMap.put(PlayerStats.PLAYER_ID,
               PLAYERSTATS_TABLE_NAME + "." + PlayerStats.PLAYER_ID);
      sPlayerStatsProjectionMap.put(Player.JERSEY_NUMBER, PLAYER_TABLE_NAME
               + "." + Player.JERSEY_NUMBER);
      sPlayerStatsProjectionMap.put(Player.NAME, PLAYER_TABLE_NAME + "."
               + Player.NAME);
      sPlayerStatsProjectionMap.put(PlayerStats.ASSISTS,
               PLAYERSTATS_TABLE_NAME + "." + PlayerStats.ASSISTS);
      sPlayerStatsProjectionMap.put(PlayerStats.GOALS, PLAYERSTATS_TABLE_NAME
               + "." + PlayerStats.GOALS);
      sPlayerStatsProjectionMap.put(PlayerStats.SOGS, PLAYERSTATS_TABLE_NAME
               + "." + PlayerStats.SOGS);
      sPlayerStatsProjectionMap.put(PlayerStats.SAVES, PLAYERSTATS_TABLE_NAME
               + "." + PlayerStats.SAVES);
      sPlayerStatsProjectionMap.put(PlayerStats.AGAINST,
               PLAYERSTATS_TABLE_NAME + "." + PlayerStats.AGAINST);
      sPlayerStatsProjectionMap.put(PlayerStats.BENCH_TIME,
               PLAYERSTATS_TABLE_NAME + "." + PlayerStats.BENCH_TIME);
      sPlayerStatsProjectionMap.put(PlayerStats.PLAY_TIME,
               PLAYERSTATS_TABLE_NAME + "." + PlayerStats.PLAY_TIME);
      sPlayerStatsProjectionMap.put(PlayerStats.CREATED_DATE,
               PLAYERSTATS_TABLE_NAME + "." + PlayerStats.CREATED_DATE);
      sPlayerStatsProjectionMap.put(PlayerStats.MODIFIED_DATE,
               PLAYERSTATS_TABLE_NAME + "." + PlayerStats.MODIFIED_DATE);

      sTeamProjectionMap = new HashMap<String, String>();
      sTeamProjectionMap.put(Team._ID,             TEAM_TABLE_NAME   + "." + Team._ID);
      sTeamProjectionMap.put(Team.SEASON_ID,       TEAM_TABLE_NAME   + "." + Team.SEASON_ID);
      sTeamProjectionMap.put(Season.SEASON_NAME,   SEASON_TABLE_NAME + "." + Season.SEASON_NAME);
      sTeamProjectionMap.put(Team.TEAM_NAME,       TEAM_TABLE_NAME   + "." + Team.TEAM_NAME);
      sTeamProjectionMap.put(Team.CREATED_DATE,    TEAM_TABLE_NAME   + "." + Team.CREATED_DATE);
      sTeamProjectionMap.put(Team.MODIFIED_DATE,   TEAM_TABLE_NAME   + "." + Team.MODIFIED_DATE);

      // Support for Live Folders.
      sLiveFolderProjectionMap = new HashMap<String, String>();
      sLiveFolderProjectionMap.put(LiveFolders._ID, Player._ID + " AS "
               + LiveFolders._ID);
      sLiveFolderProjectionMap.put(LiveFolders.NAME, Player.NAME + " AS "
               + LiveFolders.NAME);
      // Add more columns here for more robust Live Folders.
   }
}
