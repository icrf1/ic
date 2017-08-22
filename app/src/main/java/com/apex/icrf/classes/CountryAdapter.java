/*
 * Copyright (c) 2014-2015 Amberfog.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.apex.icrf.classes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.apex.icrf.R;

import java.util.ArrayList;
import java.util.Locale;

public class CountryAdapter extends BaseAdapter {

    private LayoutInflater mLayoutInflater;

    private ArrayList<Country> data = new ArrayList<>();

    public CountryAdapter(Context context, ArrayList<Country> data) {
        mLayoutInflater = LayoutInflater.from(context);
        this.data = data;
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {

        Country country = data.get(position);

        final ViewHolder holder;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.item_country_drop, parent, false);
            holder = new ViewHolder();
            //holder.mImageView = (ImageView) convertView.findViewById(R.id.image);
            holder.mNameView = (TextView) convertView.findViewById(R.id.country_name);
            holder.mCodeView = (TextView) convertView.findViewById(R.id.country_code);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (country != null) {
            holder.mNameView.setText(country.getName() + "(" + country.getCountryISO().toUpperCase(Locale.ENGLISH) + ")");
            holder.mCodeView.setText(country.getCountryCodeStr());
            //holder.mImageView.setImageResource(country.getResId());
        }
        return convertView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Country country = data.get(position);


        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.item_country, parent, false);
        }
//        ImageView imageView = (ImageView) convertView.findViewById(R.id.image);
//        imageView.setImageResource(country.getResId());

        TextView textView = (TextView) convertView.findViewById(R.id.country_code);
        textView.setText(country.getCountryISO().toUpperCase(Locale.ENGLISH));

        return convertView;
    }

    private static class ViewHolder {
        //public ImageView mImageView;
        public TextView mNameView;
        public TextView mCodeView;
    }
}
