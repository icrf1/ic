package com.apex.icrf;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.apex.icrf.utils.OkHttpClientHelper;
import com.viewpagerindicator.CirclePageIndicator;

/**
 * Created by WASPVamsi on 25/03/16.
 */
public class MoreHowItWorksFragment extends Fragment {

    Activity activity;
    ViewPager viewpager;
    CirclePageIndicator indicator;
    int[] slides = {R.drawable.drawable_introduction_slides_new_1, R.drawable.drawable_introduction_slides_new_2,
            R.drawable.drawable_introduction_slides_new_3, R.drawable.drawable_introduction_slides_new_4};
    SharedPreferences prefs;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.more_fragment_how_it_works, container, false);

        prefs = PreferenceManager.getDefaultSharedPreferences(activity);

        viewpager = (ViewPager) view.findViewById(R.id.viewpager);
        viewpager.setOffscreenPageLimit(2);
        viewpager.setAdapter(new CustomPagerAdapter(activity));

        indicator = (CirclePageIndicator) view.findViewById(R.id.indicator);
        indicator.setViewPager(viewpager);

        return view;
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
            image_logo.setVisibility(View.GONE);
            btnNext.setVisibility(View.GONE);

            OkHttpClientHelper.getPicassoBuilder(context).load(slides[position]).fit().into(image);

//            Picasso.with(context)
//                    .load(slides[position])
//                    .fit()
//                    //.centerInside()
//                    .into(image);

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
            return slides.length;
        }
    }
}
