package com.example.cityexplorersquad;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;

public class DisplayAllTasksActivity extends AppCompatActivity {

    private TasksCardLayout tasksCardLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_all_tasks);

        if (savedInstanceState == null) {
            tasksCardLayout = new TasksCardLayout();
            Bundle bundle = getIntent().getExtras();
            tasksCardLayout.setArguments(bundle);
            getFragmentManager().beginTransaction().add(R.id.container, tasksCardLayout).commit();
        }
    }

}
