package com.oss.soccerstats.view;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.oss.soccerstats.R;

public class GameOverTab extends Activity
{
      public void onCreate(Bundle savedInstanceState)
      {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.help);

         TextView helpText = (TextView)findViewById(R.id.helpText);
         String helpContent = 
            "Finally the game can be ended, during the second half, by pressing the"
            +" <i>Game Over</i> button.  A game can also be ended by pressing the "
            + "device's 'back' button."
            + "<br/>"
            + "<br/>"
            + "At this time the statistics are final. "
            + "Clicking on the 'Player statistics' button will display the "
            + "individual player stats for the game."
            ;
         Spanned htmlText = Html.fromHtml(helpContent);
         helpText.setText(htmlText);
         helpText.setEnabled(false);
         LayoutParams layoutParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
         helpText.setLayoutParams(layoutParams);
   }

}
