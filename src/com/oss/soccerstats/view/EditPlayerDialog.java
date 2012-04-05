/////////////////////////////////////////////////////////////////
// EditPlayerDialog.java
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

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.oss.soccerstats.R;

public class EditPlayerDialog extends Dialog
{
   public interface OnEditTeamEventListener
   {
      public static final int OP_ADD    = 0;
      public static final int OP_UPDATE = 1;
      public static final int OP_DELETE = 2;

      public void onEditPlayerHandler(String aPlayerName, 
                                      String aPlayerJerseyNumber,
                                      String aPlayerId,
                                      int    aOperation,
                                      boolean aAddNewPlayer);
   }

   private OnEditTeamEventListener mOnOkListener;
   
   public EditPlayerDialog(Context context, 
                           OnEditTeamEventListener aOnOkListener)
   {
      super(context);
      setContentView(R.layout.edit_player);
      mOnOkListener = aOnOkListener;
   }
   
   @Override
   public void onCreate(Bundle savedInstanceState)
   {      
      Button ok   = (Button)findViewById(R.id.ok);
      ok.setOnClickListener( new Button.OnClickListener()
      {
         @Override
         public void onClick(View v)
         {
            v = (View) v.getParent().getParent();
            ViewGroup dialog = (ViewGroup) v.getParent().getParent().getParent();
            TextView dialogView = (TextView) dialog.getChildAt(0);
            String dialogTitle = (String) dialogView.getText();
            int operation = OnEditTeamEventListener.OP_ADD;
            //TODO: replace with getString() value
            if ( dialogTitle.equals("Add player") )
            {
               operation = OnEditTeamEventListener.OP_ADD;
            }
            else
            {
               operation = OnEditTeamEventListener.OP_UPDATE;
            }
            EditText nameField = (EditText)v.findViewById(R.id.editPlayerName);
            String playerName = nameField.getText().toString();
            EditText jerseyField = (EditText)v.findViewById(R.id.editJerseyNumber);
            jerseyField.requestFocus();
            String jerseyNumber  = jerseyField.getText().toString();
            EditText idField = (EditText)v.findViewById(R.id.editPlayerId);
            String idValue  = idField.getText().toString();
            mOnOkListener.onEditPlayerHandler(playerName, jerseyNumber, idValue, operation, false);
            dismiss();
         }
      });
      
      Button saveAndAddPlayer = (Button)findViewById(R.id.addNewOrDelete);
      saveAndAddPlayer.setOnClickListener( new Button.OnClickListener()
      {
         @Override
         public void onClick(View v)
         {
            v = (View) v.getParent().getParent();
            ViewGroup dialog = (ViewGroup) v.getParent().getParent().getParent();
            TextView dialogView = (TextView) dialog.getChildAt(0);
            String dialogTitle = (String) dialogView.getText();
            int operation = OnEditTeamEventListener.OP_ADD;
            boolean addAnotherPlayer = false;
            //TODO: replace with getString() value
            if ( dialogTitle.equals("Add player") )
            {
               operation = OnEditTeamEventListener.OP_ADD;
               addAnotherPlayer = true;
            }
            else
            {
               operation = OnEditTeamEventListener.OP_DELETE;
            }
            EditText nameField = (EditText)v.findViewById(R.id.editPlayerName);
            String playerName = nameField.getText().toString();
            EditText jerseyField = (EditText)v.findViewById(R.id.editJerseyNumber);
            jerseyField.requestFocus();
            String jerseyNumber  = jerseyField.getText().toString();
            EditText idField = (EditText)v.findViewById(R.id.editPlayerId);
            String idValue  = idField.getText().toString();
            dismiss();
            mOnOkListener.onEditPlayerHandler(playerName, jerseyNumber, idValue, operation, addAnotherPlayer);
         }
      });


      Button cancel   = (Button)findViewById(R.id.cancel);
      cancel.setOnClickListener( new Button.OnClickListener()
      {
         @Override
         public void onClick(View v)
         {
            dismiss();
         }
      });
   }

}
