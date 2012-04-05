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
import com.oss.soccerstats.view.EditPlayerDialog.OnEditTeamEventListener;

public class EditTeamDialog extends Dialog
{
   public interface OnEditTeamEventListener
   {
      public static final int OP_ADD    = 0;
      public static final int OP_UPDATE = 1;
      public static final int OP_DELETE = 2;

      public void onEditTeamHandler(String aTeamName, String aTeamId, int aOperation,
              						boolean aAddNewPlayer);
   }

   private OnEditTeamEventListener mOnOkListener;

   public EditTeamDialog(Context context, 
                         OnEditTeamEventListener aOnOkListener)
   {
      super(context);
      setContentView(R.layout.edit_team);
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
            if ( dialogTitle.equals("Add team") )
            {
               operation = OnEditTeamEventListener.OP_ADD;
            }
            else
            {
               operation = OnEditTeamEventListener.OP_UPDATE;
            }
            EditText nameField = (EditText)v.findViewById(R.id.editTeamName);
            String teamName = nameField.getText().toString();
            EditText idField = (EditText)v.findViewById(R.id.editTeamId);
            String idValue  = idField.getText().toString();
            mOnOkListener.onEditTeamHandler(teamName, idValue, operation, false);
            dismiss();
         }
      });
      
      Button saveAndAddTeam = (Button)findViewById(R.id.addNewOrDelete);
      saveAndAddTeam.setOnClickListener( new Button.OnClickListener()
      {
         @Override
         public void onClick(View v)
         {
            v = (View) v.getParent().getParent();
            ViewGroup dialog = (ViewGroup) v.getParent().getParent().getParent();
            TextView dialogView = (TextView) dialog.getChildAt(0);
            String dialogTitle = (String) dialogView.getText();
            int operation = OnEditTeamEventListener.OP_ADD;
            boolean addAnotherTeam = false;
            //TODO: replace with getString() value
            if ( dialogTitle.equals("Add team") )
            {
               operation = OnEditTeamEventListener.OP_ADD;
               addAnotherTeam = true;
            }
            else
            {
               operation = OnEditTeamEventListener.OP_DELETE;
            }
            EditText nameField = (EditText)v.findViewById(R.id.editTeamName);
            String teamName = nameField.getText().toString();
            EditText idField = (EditText)v.findViewById(R.id.editTeamId);
            String idValue  = idField.getText().toString();
            dismiss();
            mOnOkListener.onEditTeamHandler(teamName, idValue, operation, addAnotherTeam);
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
