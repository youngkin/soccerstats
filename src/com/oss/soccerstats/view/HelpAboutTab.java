package com.oss.soccerstats.view;

import android.app.Activity;
import android.content.Intent;
import android.net.MailTo;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.oss.soccerstats.R;

public class HelpAboutTab extends Activity
{
      private WebView mHelpTextWebView;


      public void onCreate(Bundle savedInstanceState)
      {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.help_web_view);

         mHelpTextWebView = (WebView)findViewById(R.id.help_webView);
         
         mHelpTextWebView.getSettings().setJavaScriptEnabled(true);
         mHelpTextWebView.loadUrl("file:///android_asset/helpAbout.html");
         
         mHelpTextWebView.setWebViewClient(new MyWebViewClient(this));

//TODO: Remove?
//         setContentView(R.layout.help);
//
//         TextView helpText = (TextView)findViewById(R.id.helpText);
//         String helpContent = getString(R.string.helpAboutText);
//         String formattedHelpContent = String.format(helpContent, getString(R.string.version));
//         CharSequence styledHelpContent = Html.fromHtml(formattedHelpContent);
//         helpText.setText(styledHelpContent);
//         helpText.setEnabled(false);
//         LayoutParams layoutParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
//         helpText.setLayoutParams(layoutParams);
//         helpText.setAutoLinkMask(Linkify.WEB_URLS);
   }

   @Override
   public boolean onKeyDown(int keyCode, KeyEvent event) 
   {
       if ((keyCode == KeyEvent.KEYCODE_BACK) && mHelpTextWebView.canGoBack()) 
       {
          mHelpTextWebView.goBack();
           return true;
       }
       return super.onKeyDown(keyCode, event);
   }
      
   
   private class MyWebViewClient extends WebViewClient 
   {
      Activity mContext;
      public MyWebViewClient(Activity context)
      {
          this.mContext = context;
      }
      @Override
      public boolean shouldOverrideUrlLoading(WebView view, String url) 
      {     
          if( url.startsWith("mailto:") )
          {
              MailTo mt = MailTo.parse(url);
              Intent emailIntent = new Intent(Intent.ACTION_SEND);
              emailIntent.setType("text/plain");
              emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{mt.getTo()});
              emailIntent.putExtra(Intent.EXTRA_SUBJECT, mt.getSubject());
//              emailIntent.putExtra(Intent.EXTRA_CC, mt.getCc());
//              emailIntent.putExtra(Intent.EXTRA_TEXT, mt.getBody());
              mContext.startActivity(Intent.createChooser(emailIntent, "Select email application"));
//              view.reload();
//              return true;
          }
          else
          {
             Uri uri = Uri.parse(url);
             Intent intent = new Intent(Intent.ACTION_VIEW, uri);
             startActivity(intent);
          }
          return true;
      }
   };
}
