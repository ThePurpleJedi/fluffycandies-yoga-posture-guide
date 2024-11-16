package com.fluffycandies.yogaguide.java;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.fluffycandies.yogaguide.R;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;

public class PoseSelectionActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener {
    private static String[] POSES = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.pose_chooser);

        populatePosesFromJSON();

        // Set up ListView and Adapter
        ListView listView = findViewById(R.id.test_activity_list_view);

        MyArrayAdapter adapter = new MyArrayAdapter(this, android.R.layout.simple_list_item_2, POSES);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    private void populatePosesFromJSON() {
        try {
            String jsonString = loadJSONFromAsset();
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray posesArray = jsonObject.getJSONArray("Poses");

            POSES = new String[posesArray.length()];

            for (int i = 0; i < posesArray.length(); i++) {
                JSONObject poseObject = posesArray.getJSONObject(i);
                POSES[i] = poseObject.getString("sanskrit_name");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getAssets().open("pose/Poses.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            // fall through
        }
        return json;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String clicked = POSES[position];
        Intent intent = new Intent(this, PoseDetailsActivity.class);
        intent.putExtra("selected_pose", clicked);
        startActivity(intent);
    }

    private static class MyArrayAdapter extends ArrayAdapter<String> {

        private final Context context;
        private final String[] classes;
        MyArrayAdapter(Context context, int resource, String[] objects) {
            super(context, resource, objects);

            this.context = context;
            classes = objects;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            View view = convertView;

            if (convertView == null) {
                LayoutInflater inflater =
                        (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(android.R.layout.simple_list_item_2, null);
            }

            ((TextView) view.findViewById(android.R.id.text1)).setText(classes[position]);

            return view;
        }
    }
}
