package com.oss.soccerstats.view;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

import com.oss.soccerstats.R;

public class WelcomeTab extends Activity
{
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.help_web_view);

      WebView helpTextWebView = (WebView)findViewById(R.id.help_webView);
      
      helpTextWebView.getSettings().setJavaScriptEnabled(false);
      helpTextWebView.loadUrl("file:///android_asset/helpWelcome.html");
//TODO: Remove?
//      LayoutParams layoutParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
//      helpTextWebView.setLayoutParams(layoutParams);
      
   }

}
