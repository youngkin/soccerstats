package com.oss.soccerstats.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.oss.soccerstats.R;
import com.oss.soccerstats.data.SoccerStatsDataHelper;
import com.oss.soccerstats.data.SoccerStatsDataHelper.Player;
import com.oss.soccerstats.data.SoccerStatsDataHelper.PlayerStats;
import com.oss.soccerstats.intent.SoccerStatIntent;
import com.oss.soccerstats.view.EditPlayerDialog.OnEditTeamEventListener;
import com.oss.soccerstats.widget.AlternatingRowColorAdapter;

public class RosterActivity extends ListActivity
                    implements EditPlayerDialog.OnEditTeamEventListener
{
   // Menu item ids
   public  static final int MENU_ITEM_ADD          = Menu.FIRST;
   /**
    * The columns we are interested in from the database
    */
   private static final String[] PROJECTION = new String[] {
           Player._ID,           // 0
           Player.NAME,          // 1
           Player.JERSEY_NUMBER  // 2
   };

   private   static final int    DIALOG_CONFIRM_DELETE_PLAYER  = 0;
   protected static final int    HEADER_ROW                    = 0;
   private Dialog mDialog;
   private Cursor mCursor;
   private String mDeletePlayerId;
private int mTeamId;

   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle savedInstanceState) 
   {
      super.onCreate(savedInstanceState);
      setTitle(R.string.RosterTitle);

      // Inform the list we provide context menus for items
      getListView().setOnCreateContextMenuListener(this);
      
      Intent launchIntent = getIntent();
      mTeamId = launchIntent.getIntExtra(SoccerStatIntent.TEAM_ID, 
               SoccerStatsDataHelper.TEAM_ID_DEFAULT);
      mCursor = managedQuery(Player.CONTENT_URI, PROJECTION, 
               Player.NAME + "!=\'Other\' AND "
               + Player.TEAM_ID + "=" + mTeamId, null,
               Player.DEFAULT_SORT_ORDER);

      // Used to player rows from the database to views
      AlternatingRowColorAdapter adapter = new AlternatingRowColorAdapter(this, R.layout.roster_list_item, mCursor,
              new String[] { Player.JERSEY_NUMBER, Player.NAME, Player._ID }, 
              new int[] { R.id.playerJerseyNumber, R.id.playerName, R.id.playerID});
            
      mDialog = new EditPlayerDialog(this, this);

      ListView lv = getListView();
      LayoutInflater inflater = getLayoutInflater();
      
      //Header contains hint to press "menu" button. It should only be shown if
      //there are no players defined.
      if ( mCursor.getCount() == 0 )
      {
         ViewGroup headerRow = 
            (ViewGroup)inflater.inflate(R.layout.roster_header, lv, false);
         lv.addHeaderView(headerRow);
      }
      else
      {
         ViewGroup headerRow = 
            (ViewGroup)inflater.inflate(R.layout.roster_list_header, lv, false);
         lv.addHeaderView(headerRow);
      }

      lv.setOnItemClickListener(new OnItemClickListener() 
      {
         public void onItemClick(AdapterView<?> parent, View view,
                  int position, long aId)
         {
            if (position != HEADER_ROW)
            {
               LinearLayout container  = (LinearLayout)view;
               if ( container.getChildCount() > 1)
               {
                  //TODO: open player summary stats view.
               }
               else
               {
                  showAddPlayerDialog();
               }
            }
         }
      });
      
      lv.setOnItemLongClickListener(new OnItemLongClickListener() 
      {
         public boolean onItemLongClick(AdapterView<?> parent, View view,
                  int position, long aId)
         {
            if (position != HEADER_ROW)
            {
               LinearLayout container  = (LinearLayout)view;
               if ( container.getChildCount() > 1)
               {
                  TextView jerseyNum   = (TextView) container.getChildAt(0);
                  TextView playerName  = (TextView) container.getChildAt(1);
                  TextView playerId    = (TextView) container.getChildAt(2);
                  EditText jersey      = (EditText)mDialog.findViewById(R.id.editJerseyNumber);
                  EditText name        = (EditText)mDialog.findViewById(R.id.editPlayerName);
                  EditText id          = (EditText)mDialog.findViewById(R.id.editPlayerId);
                  Button   addOrDelete = (Button)mDialog.findViewById(R.id.addNewOrDelete);
                  
                  jersey.setText(jerseyNum.getText());
                  name.setText(playerName.getText());
                  id.setText(playerId.getText());
                  addOrDelete.setText(R.string.deletePlayer);
                  mDialog.setTitle(R.string.editPlayer);      
                  mDialog.show();
               }
               else
               {
                  showAddPlayerDialog();
               }
            }
            
            return true;
         }
      });
      
      setListAdapter(adapter);
      
  }
   
   @Override
   public boolean onCreateOptionsMenu(Menu aMenu)
   {
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.roster_menu, aMenu);
      return true;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem aMenuItem) 
   {
      // Handle item selection
      switch (aMenuItem.getItemId())
      {
         case R.id.addPlayer:
            showAddPlayerDialog();
           return true;
         default:
            return false;
      }
   }

   private void showAddPlayerDialog()
   {
      EditText jersey = (EditText)mDialog.findViewById(R.id.editJerseyNumber);
      EditText name   = (EditText)mDialog.findViewById(R.id.editPlayerName);
      jersey.setText("");
      name.setText("");
      jersey.setHint(R.string.jerseyNumber);
      jersey.requestFocus();
      name.setHint(R.string.playerName);
      Button   addOrDelete = (Button)mDialog.findViewById(R.id.addNewOrDelete);
      
      addOrDelete.setText(R.string.addPlayer);
      mDialog.setTitle(R.string.addPlayer);      
      mDialog.show();
   }
 
//TODO: REMOVE, no longer needed since long click is being handled by an event
//TODO: handler instead of a context menu.
//   @Override
//   public void onCreateContextMenu(ContextMenu menu, View v,
//                                   ContextMenuInfo menuInfo) {
////     super.onCreateContextMenu(menu, v, menuInfo);
////     MenuInflater inflater = getMenuInflater();
////     inflater.inflate(R.menu.roster_context_menu, menu);
//      AdapterView.AdapterContextMenuInfo info;
//      try {
//           info = (AdapterView.AdapterContextMenuInfo) menuInfo;
//      } catch (ClassCastException e) {
//          Log.e("SoccerStats::Roster.onCreateContextMenu", "bad menuInfo", e);
//          return;
//      }
//
//      Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
//      if (cursor == null) {
//          // For some reason the requested item isn't available, do nothing
//          return;
//      }
//
//      // Setup the menu header
//      menu.setHeaderTitle(cursor.getString(COLUMN_INDEX_JERSEY_NUMBER) +
//                          " " +
//                          cursor.getString(COLUMN_INDEX_NAME));
//      menu.setHeaderIcon(android.R.drawable.ic_menu_delete);
//
//      // Add a menu item to delete the note
//      menu.add(R.string.deletePlayer);
//   }
//   
//   @Override
//   public boolean onContextItemSelected(MenuItem aMenuItem) 
//   {
//      AdapterView.AdapterContextMenuInfo info;
//      try {
//           info = (AdapterView.AdapterContextMenuInfo) aMenuItem.getMenuInfo();
//      } catch (ClassCastException e) {
//          Log.e(TAG, "bad menuInfo", e);
//          return false;
//      }
//
//      // Delete the selected Player
//      Uri playerUri = ContentUris.withAppendedId(Player.CONTENT_URI, info.id);
//      getContentResolver().delete(playerUri, null, null);
//      return true;
//   }
   
   @Override
   public void onEditPlayerHandler(String  aPlayerName,
                                   String  aPlayerJerseyNumber,
                                   String  aPlayerId,
                                   int     aOperation,
                                   boolean aAddNewPlayer)
   {
      ContentValues values = new ContentValues();

      values.put(Player.MODIFIED_DATE, System.currentTimeMillis());
      values.put(Player.NAME, aPlayerName);
      values.put(Player.JERSEY_NUMBER, aPlayerJerseyNumber);

      // Commit all of our changes to persistent storage. When the update
      // completes the content provider will notify the cursor of the change, 
      // which will cause the UI to be updated.
      if ( aOperation == OnEditTeamEventListener.OP_UPDATE )
      {
         getContentResolver().update(Player.CONTENT_URI, values, 
                                     Player._ID + "=" + Integer.parseInt(aPlayerId), 
                                     null);
      }
      else if ( aOperation == OnEditTeamEventListener.OP_ADD)
      {
    	  values.put(Player.CREATED_DATE, System.currentTimeMillis());
    	  values.put(Player.TEAM_ID, mTeamId);
    	  getContentResolver().insert(Player.CONTENT_URI, values);
      }
      else if ( aOperation == OnEditTeamEventListener.OP_DELETE)
      {
         mDeletePlayerId = aPlayerId;
         showDialog(DIALOG_CONFIRM_DELETE_PLAYER);
      }
      else
      {
         throw new Error("Invalid operation code: " + aOperation);
      }
      
      if (aAddNewPlayer)   showAddPlayerDialog();
   }

   @Override
   protected Dialog onCreateDialog(int aDialogId)
   {
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      
      builder.setMessage(getString(R.string.confirmDeletePlayer)).setCancelable(false)
               .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int id)
                  {
                     RosterActivity.this.completePlayerDelete();
                  }
               }).setNegativeButton(getString(R.string.no),
                        new DialogInterface.OnClickListener() {
                           public void onClick(DialogInterface dialog, int id)
                           {
                              return;
                           }
                        });
      
      AlertDialog alert = builder.create();
      return alert;
   }

   private void completePlayerDelete()
   {
      Uri playerUri = ContentUris.withAppendedId(Player.CONTENT_URI, 
                                                 Integer.parseInt(mDeletePlayerId) );
      getContentResolver().delete(playerUri, null, null);

      getContentResolver().delete(PlayerStats.CONTENT_URI, 
              PlayerStats.PLAYER_ID + "=" + Integer.parseInt(mDeletePlayerId), 
              null);
     
      mDeletePlayerId = null;
   }
   
}
