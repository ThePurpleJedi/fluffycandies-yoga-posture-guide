package com.fluffycandies.yogaguide.java;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;

import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.fluffycandies.yogaguide.R;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PoseSelectionActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener {

    private static Pose[] POSES = null;
    private MyArrayAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.pose_chooser);

        populatePosesFromJSON();

        // Set up ListView and Adapter
        ListView listView = findViewById(R.id.test_activity_list_view);
        adapter = new MyArrayAdapter(this, android.R.layout.simple_list_item_2, POSES);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        // Set up SearchView
        SearchView searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false; // No action on submit
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText); // Filter as text changes
                return true;
            }
        });
    }


    private void populatePosesFromJSON() {
        try {
            String jsonString = loadJSONFromAsset();
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray posesArray = jsonObject.getJSONArray("Poses");

            POSES = new Pose[posesArray.length()];

            for (int i = 0; i < posesArray.length(); i++) {
                JSONObject poseObject = posesArray.getJSONObject(i);
                String sanskritName = poseObject.getString("sanskrit_name");
                String englishName = poseObject.getString("english_name");
                POSES[i] = new Pose(sanskritName, englishName);
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
        }
        return json;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Pose clickedPose = adapter.getItem(position);
        Intent intent = new Intent(this, PoseDetailsActivity.class);
        intent.putExtra("sanskrit_name", clickedPose.getSanskritName());
        intent.putExtra("english_name", clickedPose.getEnglishName());
        startActivity(intent);
    }

    public class Pose {
        private final String sanskritName;
        private final String englishName;

        public Pose(String sanskritName, String englishName) {
            this.sanskritName = sanskritName;
            this.englishName = englishName;
        }

        public String getSanskritName() {
            return sanskritName;
        }

        public String getEnglishName() {
            return englishName;
        }

        @Override
        public String toString() {
            return sanskritName + " (" + englishName + ")";
        }
    }

    private static class MyArrayAdapter extends ArrayAdapter<Pose> implements Filterable {

        private final Context context;
        private final Pose[] originalData;
        private List<Pose> filteredData;

        MyArrayAdapter(Context context, int resource, Pose[] objects) {
            super(context, resource, objects);
            this.context = context;
            this.originalData = objects;
            this.filteredData = new ArrayList<>(Arrays.asList(objects));
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

            Pose pose = filteredData.get(position);
            ((TextView) view.findViewById(android.R.id.text1)).setText(pose.getSanskritName());
            ((TextView) view.findViewById(android.R.id.text2)).setText(pose.getEnglishName());

            return view;
        }

        @Override
        public int getCount() {
            return filteredData.size();
        }

        @Override
        public Pose getItem(int position) {
            return filteredData.get(position);
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    List<Pose> suggestions = new ArrayList<>();

                    if (constraint == null || constraint.length() == 0) {
                        suggestions.addAll(Arrays.asList(originalData));
                    } else {
                        String filterPattern = constraint.toString().toLowerCase().trim();
                        for (Pose pose : originalData) {
                            if (pose.getSanskritName().toLowerCase().contains(filterPattern) ||
                                    pose.getEnglishName().toLowerCase().contains(filterPattern)) {
                                suggestions.add(pose);
                            }
                        }
                    }

                    results.values = suggestions;
                    results.count = suggestions.size();
                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    filteredData = (List<Pose>) results.values;
                    notifyDataSetChanged();
                }
            };
        }
    }
}

