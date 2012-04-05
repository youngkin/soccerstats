/////////////////////////////////////////////////////////////////
// EditGameDialog.java
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.oss.soccerstats.R;

public class EditGameDialog extends Dialog
{
   public interface OnEditGameEventListener
   {
      public void onEditGameHandler(String aOurTeamName, 
                                    String aTheirTeamName,
                                    String aGameLocation,
                                    String aGameId);
   }

   private OnEditGameEventListener mOnOkListener;

   public EditGameDialog(Context context, 
                         OnEditGameEventListener aOnOkListener)
   {
      super(context);
      setContentView(R.layout.edit_game_details);
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
            EditText usNameField = (EditText)v.findViewById(R.id.editUs);
            String usName = usNameField.getText().toString();
            EditText themNameField = (EditText)v.findViewById(R.id.editThem);
            String themName  = themNameField.getText().toString();
            EditText locationField = (EditText)v.findViewById(R.id.gameLocation);
            String locationValue  = locationField.getText().toString();
            TextView gameIdField = (TextView)v.findViewById(R.id.gameID);
            String gameIdValue  = gameIdField.getText().toString();
            mOnOkListener.onEditGameHandler(usName, themName, locationValue, gameIdValue);
            dismiss();
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
