package com.aaa.aaa;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class NapatableActivity extends AppCompatActivity {
    private Button btn105;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_napatable);

        Button btn105 = (Button) findViewById(R.id.btn105);
        btn105.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),NapaActivity.class);
                startActivity(intent);
            }
        });
    }
}