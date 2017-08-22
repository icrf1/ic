package com.apex.icrf;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.viewpagerindicator.CirclePageIndicator;

/**
 * Created by WASPVamsi on 07/01/16.
 */
public class IntroductionActivity extends AppCompatActivity {

    ViewPager viewpager;
    CirclePageIndicator indicator;
    //int[] slides = {R.drawable.icrf_page_1, R.drawable.icrf_page_2, R.drawable.icrf_page_3};
    int[] slides = {R.drawable.drawable_introduction_slides_new_1, R.drawable.drawable_introduction_slides_new_2,
            R.drawable.drawable_introduction_slides_new_3, R.drawable.drawable_introduction_slides_new_4};
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_introduction);

//        if (Build.VERSION.SDK_INT >= 21) {
//            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
//                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        }

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        viewpager = (ViewPager) findViewById(R.id.viewpager);
        viewpager.setOffscreenPageLimit(2);
        viewpager.setAdapter(new CustomPagerAdapter(this));

        indicator = (CirclePageIndicator) findViewById(R.id.indicator);
        indicator.setViewPager(viewpager);
    }

    public class CustomPagerAdapter extends PagerAdapter {

        Context context;
        LayoutInflater inflater;


        public CustomPagerAdapter(Context context) {
            this.context = context;
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = inflater.inflate(R.layout.item_viewpager_introduction,
                    container, false);

            ImageView image = (ImageView) view.findViewById(R.id.imageView_slide);
            ImageView image_logo = (ImageView) view.findViewById(R.id.imageView_logo);
            Button btnNext = (Button) view.findViewById(R.id.button_next);

            if(position == slides.length) {

                image.setVisibility(View.GONE);
                image_logo.setVisibility(View.VISIBLE);
                btnNext.setVisibility(View.VISIBLE);

                btnNext.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        prefs.edit().putBoolean(Const.Prefs.IS_INTRO_SHOWN, true).apply();
                        //startActivity(new Intent(context, MainActivity.class));
                        startActivity(new Intent(context, MainTabbedActivity.class));
                        IntroductionActivity.this.finish();
                    }
                });

            } else {

                image.setVisibility(View.VISIBLE);
                image_logo.setVisibility(View.GONE);
                btnNext.setVisibility(View.GONE);

                Picasso.with(context)
                        .load(slides[position])
                        .fit()
                        //.centerInside()
                        .into(image);

            }




//            if (position == slides.length - 1)
//                btnNext.setVisibility(View.VISIBLE);
//            else
//                btnNext.setVisibility(View.GONE);
//
//            btnNext.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//
//                    prefs.edit().putBoolean(Const.Prefs.IS_INTRO_SHOWN, true).commit();
//                    //startActivity(new Intent(context, MainActivity.class));
//                    startActivity(new Intent(context, MainTabbedActivity.class));
//                    IntroductionActivity.this.finish();
//                }
//            });

            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public int getCount() {
            return slides.length + 1;
        }
    }
}
