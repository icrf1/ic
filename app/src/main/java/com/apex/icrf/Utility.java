package com.apex.icrf;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.squareup.picasso.Request;

import org.apache.http.client.methods.RequestBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;

/**
 * Created by Apex on 9/5/2017.
 */

public class Utility {

    static Context context;
    public Utility(Context context) {
        this.context = context;
    }

    public void toast(String message)
    {
        Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
    }

    public void requestFocus(View editText, Activity a)
    {
        try {
            editText.requestFocus();
            InputMethodManager imm = (InputMethodManager) a.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
            a.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void hideKeyBoard(EditText editText)
    {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    static class CheckAppUpdates extends AsyncTask<Void,Void,Void>
    {

        Context contextAPP;
        Handler handler;
         OkHttpClient okHttpClient;



        public CheckAppUpdates(Context context) {
            this.contextAPP = context;
            handler=new Handler();
            okHttpClient = new OkHttpClient();

        }

        String check_app_update_url = Const.FINAL_URL + Const.URLs.CHECK_APP_VERSION_UPDATES;
        String response = "";
        @Override
        protected Void doInBackground(Void... params) {

            final okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(check_app_update_url)
                    .build();
            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    try {
                        JSONArray jsonArray= new JSONArray(response.body().string());

                        if (response.code() == 200)
                        {
                        if (jsonArray.length() > 0)
                        {
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            Log.d("CheckAppUpdates",""+jsonObject.toString());
                            int vcode = Integer.parseInt(jsonObject.getString("app_ver_code"));
                            String vname = jsonObject.getString("app_ver");
                            String message = jsonObject.getString("display_msg");
                            Log.d("CheckAppUpdates",BuildConfig.VERSION_CODE +" --- "+vcode);
                            if (BuildConfig.VERSION_CODE < vcode)
                            {
                                Log.d("CheckAppUpdates",""+vcode);
                                displayVersionUpdateAlert(true,message);
                            }

                        }

                        }



                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                @Override
                public void onFailure(Call call, IOException e) {

                    Toast.makeText(contextAPP," error : "+e.getMessage(),Toast.LENGTH_SHORT).show();
                }


            });
//            okHttpClient = new OkHttpClient();
//            handler.post(new Runnable() {
//                @Override
//                public void run() {
//
//                }
//            });

            return null;
        }

        private void displayVersionUpdateAlert(boolean canSkip, String msg) {

            Log.d("CheckAppUpdates","displayVersionUpdateAlert called");
            AlertDialog.Builder alert = new AlertDialog.Builder(contextAPP);
            AlertDialog dialog;

            alert.setTitle("New Version Available");

//        String message = "We have a new version available on Play Store " +
//                "and its highly recommended that you update it immediately. ";

            alert.setMessage(msg);
            alert.setPositiveButton("UPDATE",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            try {
                                Uri uri = Uri.parse("market://details?id="
                                        + contextAPP.getPackageName());
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY
                                        | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET
                                        | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

                                contextAPP.startActivity(intent);
                            } catch (ActivityNotFoundException e) {

                                Uri uri = Uri
                                        .parse("http://play.google.com/store/apps/details?id="
                                                + context.getPackageName());
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY
                                        | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET
                                        | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

                                contextAPP.startActivity(intent);
                            }
                        }
                    });

            if (canSkip) {
                alert.setNegativeButton("SKIP", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });
            }


            dialog = alert.create();
            dialog.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            dialog.show();
        }


    }
}
