package com.google.mlkit.vision.demo.java;

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

import com.google.mlkit.vision.demo.R;

public class PoseSelectionActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener {
    private static final String[] POSES = {
            "Navasana",
            "Ardha Navasana",
            "Dhanurasana",
            "Setu Bandha Sarvangasana",
            "Baddha Konasana",
            "Ustrasana",
            "Marjaryasana",
            "Bitilasana",
            "Utkatasana",
            "Balasana",
            "Sivasana",
            "Alanasana",
            "Bakasana",
            "Ardha Pincha Mayurasana",
            "Adho Mukha Svanasana",
            "Garudasana",
            "Utthita Hasta Padangusthasana",
            "Utthita Parsvakonasana",
            "Pincha Mayurasana",
            "Uttanasana",
            "Ardha Chandrasana",
            "Adho Mukha Vrksasana",
            "Anjaneyasana",
            "Supta Kapotasana",
            "Eka Pada Rajakapotasana",
            "Phalakasana",
            "Halasana",
            "Parsvottanasana",
            "Parsva Virabhadrasana",
            "Paschimottanasana",
            "Padmasana",
            "Ardha Matsyendrasana",
            "Salamba Sarvangasana",
            "Vasisthasana",
            "Salamba Bhujangasana",
            "Hanumanasana",
            "Malasana",
            "Uttanasana",
            "Ashta Chandrasana",
            "Upavistha Konasana",
            "Vrksasana",
            "Trikonasana",
            "Urdhva Mukha Svsnssana",
            "Virabhadrasana One",
            "Virabhadrasana Two",
            "Virabhadrasana Three",
            "Urdhva Dhanurasana",
            "Camatkarasana"
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chooser);

        // Set up ListView and Adapter
        ListView listView = findViewById(R.id.test_activity_list_view);

        MyArrayAdapter adapter = new MyArrayAdapter(this, android.R.layout.simple_list_item_2, POSES);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String clicked = POSES[position];
        Intent intent = new Intent(this, CameraXLivePreviewActivity.class);
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
