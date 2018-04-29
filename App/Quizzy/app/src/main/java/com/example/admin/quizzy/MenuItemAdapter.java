package com.example.admin.quizzy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by brianmedina on 3/27/18.
 */

public class MenuItemAdapter extends ArrayAdapter<MenuItem>{
    private static final String TAG = "Quizzy_MenuADebug";

    private MenuItem currentItem;
    private Context context;
    private Activity activity;

    public MenuItemAdapter(Context context, ArrayList<MenuItem> items) {
        super(context, 0, items);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        // Check if an existing view is being reused, otherwise inflate the view
        View v = convertView;
        if (v == null) {
            v = LayoutInflater.from(getContext()).inflate(
                    R.layout.menu_list_item, parent, false);
        }

        // bind everything
        holder = new ViewHolder(v);

        // Get the object located at this position in the list
        currentItem = getItem(position);

        // set name
        holder.titleView.setText(currentItem.getSurveyName());

        // set onclick for edit button
        holder.editButton.setOnClickListener(new View.OnClickListener(){
           public void onClick(View v){
               Intent intent = new Intent(context, CreateSurveyActivity.class);
               intent.putExtra("surveyid", currentItem.getSurveyId());
               intent.putExtra("surveyname", currentItem.getSurveyName());
               context.startActivity(intent);
           }
        });

        holder.deleteButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                // gets the view's position
                View parentRow = (View) v.getParent();
                ListView listView = (ListView) parentRow.getParent();
                final int position = listView.getPositionForView(parentRow);
                // gets the surveyid from the item
                int surveyid = getItem(position).getSurveyId();
                // removes from the list
                MenuItemAdapter.this.remove(getItem(position));
                // removes from the database
                delete(surveyid);
                // updates the listview
                notifyDataSetChanged();
            }
        });

        return v;
    }



    // holds all the views to bind with butterknife
    static class ViewHolder{
        @BindView(R.id.menuItemTitle) TextView titleView;
        @BindView(R.id.menuEditButton) ImageButton editButton;
        @BindView(R.id.menuDeleteButton) ImageButton deleteButton;

        // constructor binds them immediately
        public ViewHolder(View view){
            ButterKnife.bind(this, view);
        }
    }

    void delete(int surveyid){
        Log.d(TAG, "deleting surveyid: " + surveyid);
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("http://quizzybackend.herokuapp.com/quiz/" + surveyid)
                .delete(null)
                .addHeader("Cache-Control", "no-cache")
                .addHeader("Postman-Token", "e63d0adb-0431-4193-a6f9-4ac71cb7b49d")
                .build();

        client.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(final Call call, IOException e) {
                        Log.d(TAG, "Exception: " + e);
                        deleteFailed();

                    }

                    @Override
                    public void onResponse(Call call, final Response response) {
                        Log.d(TAG, "Response recieved");
                        if(response.code() == 200) {
                            Log.d(TAG, "Successful response to delete call");
                            deleteSuccess();
                        } else
                            deleteFailed();

                    }
                });
    }

    void deleteFailed(){
        activity = (Activity) context;
        activity.runOnUiThread(new Runnable() {
            public void run() {
                Log.d(TAG, "Delete failed");
                Toast.makeText(context, "Delete failed", Toast.LENGTH_LONG).show();
            }
        });
    }

    void deleteSuccess(){
        activity = (Activity) context;
        activity.runOnUiThread(new Runnable() {
            public void run() {
                Log.d(TAG, "Delete success");
                Toast.makeText(context, "Delete success", Toast.LENGTH_LONG).show();
            }
        });
    }
}

/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
