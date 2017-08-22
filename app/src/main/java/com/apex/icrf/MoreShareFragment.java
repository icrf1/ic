package com.apex.icrf;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Created by WASPVamsi on 03/01/16.
 */
public class MoreShareFragment extends Fragment {

    Activity activity;
    Typeface font_roboto_thin, font_robot_regular;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;

        font_roboto_thin = Typeface.createFromAsset(activity.getAssets(),
                "fonts/Roboto-Thin.ttf");

        font_robot_regular = Typeface.createFromAsset(activity.getAssets(),
                "fonts/Roboto-Regular.ttf");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.more_fragment_invite, container, false);

        TextView textView = (TextView) view.findViewById(R.id.textview_share);
        textView.setTypeface(font_robot_regular);

        Button button = (Button) view.findViewById(R.id.button_share);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent shareIntent;
                Bitmap bitmap= BitmapFactory.decodeResource(getResources(),R.drawable.icrf_splash_icrf_logo_square_1);
                //String path = Environment.getExternalStorageDirectory().getAbsolutePath() +"/Icrf/";
                String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                OutputStream out = null;
                File file=new File(path,"logo.png");
                try {
                    out = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                path=file.getPath();
                Uri bmpUri = Uri.parse("file://"+path);
                Log.d("image path",""+bmpUri);
                shareIntent = new Intent(android.content.Intent.ACTION_SEND);
                shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                shareIntent.putExtra(Intent.EXTRA_TEXT,"Share ICRF with your friends " + "https://play.google.com/store/apps/details?id=com.apex.icrf");
                shareIntent.setType("image/png");
                startActivity(Intent.createChooser(shareIntent,"Share with"));

//                Picasso.with(activity).load(R.drawable.icrf_splash_icrf_logo_square_1).into(new Target() {
//                    @Override
//                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//
//                        String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() +
//                                "/images";
//                        File dir = new File(file_path);
//                        if (!dir.exists())
//                            dir.mkdir();
//                        File file = new File(dir, "logo.png");
//                        FileOutputStream fOut;
//                        try {
//                            fOut = new FileOutputStream(file);
//                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
//                            fOut.flush();
//                            fOut.close();
//
//
//                            Uri uri = Uri.fromFile(file);
//                            Intent intent = new Intent();
//                            intent.setAction(Intent.ACTION_SEND);
//                            intent.setType("image/*");
//
//                            intent.putExtra(android.content.Intent.EXTRA_TEXT, "");
//                            intent.putExtra(Intent.EXTRA_STREAM, uri);
//                            intent.putExtra(Intent.EXTRA_SUBJECT, "Share ICRF with your friends");
//                            intent.putExtra(
//                                    Intent.EXTRA_TEXT,
//                                    "https://play.google.com/store/apps/details?id=com.apex.icrf");
//                            startActivity(Intent.createChooser(intent, "Share ICRF"));
//
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//
//                    @Override
//                    public void onBitmapFailed(Drawable errorDrawable) {
//
//                        Intent intent = new Intent(Intent.ACTION_SEND);
//                        intent.setType("text/plain");
//                        intent.putExtra(Intent.EXTRA_SUBJECT, "Share ICRF with your friends");
//                        intent.putExtra(
//                                Intent.EXTRA_TEXT,
//                                "https://play.google.com/store/apps/details?id=com.apex.icrf");
//                        startActivity(Intent.createChooser(intent, "Share ICRF"));
//                    }
//
//                    @Override
//                    public void onPrepareLoad(Drawable placeHolderDrawable) {
//
//                    }
//                });






//                Intent intent = new Intent(Intent.ACTION_SEND);
//                intent.setType("text/plain");
//                intent.putExtra(Intent.EXTRA_SUBJECT, "Share ICRF with your friends");
//                intent.putExtra(
//                        Intent.EXTRA_TEXT,
//                        "https://play.google.com/store/apps/details?id=com.apex.icrf");
//                startActivity(Intent.createChooser(intent, "Share ICRF"));

            }
        });

        return view;
    }
}
