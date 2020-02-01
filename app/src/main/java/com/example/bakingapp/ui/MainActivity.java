package com.example.bakingapp.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Operation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.example.bakingapp.R;
import com.example.bakingapp.idlingresource.EspressoIdlingResource;
import com.example.bakingapp.model.Recipe;
import com.example.bakingapp.net.CallBackWorkerMain;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.UUID;

import static com.example.bakingapp.constant.Constants.KEY_TASK_OUTPUT;
import static com.example.bakingapp.constant.Constants.WORKREQUEST_MAIN;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    private RecyclerView.LayoutManager mLayoutManager;
    private Constraints mConstraints;
    private OneTimeWorkRequest mDownload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getLayoutDecision();

        setupWorkRequest();

        EspressoIdlingResource.increment();

        executeWorkRequest();

        setupRecyclerView();

    }

    private void initRecyclerView(MainActivityAdapter adapter) {
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(UUID.fromString(String.valueOf(mDownload.getId())))
                .observe(this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(@Nullable WorkInfo workInfo) {
                        if (workInfo != null) {
                            if (workInfo.getState().isFinished()) {
                                supplyRecyclerView(workInfo, adapter);
                                if (!EspressoIdlingResource.getIdlingResource().isIdleNow()) {
                                    EspressoIdlingResource.decrement();
                                }
                            }
                        }
                    }
                });
    }

    @NotNull
    private void setupRecyclerView() {
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        final MainActivityAdapter simpleAdapter = new MainActivityAdapter(new ArrayList<Recipe>());
        mRecyclerView.setAdapter(simpleAdapter);
        initRecyclerView(simpleAdapter);
    }

    private void executeWorkRequest() {
        WorkManager.getInstance(getApplicationContext()).beginUniqueWork(WORKREQUEST_MAIN,
                ExistingWorkPolicy.KEEP, mDownload).enqueue().getState().observe(this, new Observer<Operation.State>() {
            @Override
            public void onChanged(Operation.State state) {
                Toast.makeText(getApplicationContext(), state.toString(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupWorkRequest() {
        mConstraints = buildConstraints();
        mDownload = buildOneTimeWorkRequest(mConstraints);
    }

    @NotNull
    private OneTimeWorkRequest buildOneTimeWorkRequest(Constraints constraints) {
        return new OneTimeWorkRequest.Builder(CallBackWorkerMain.class)
                .setConstraints(constraints)
                .build();
    }

    @NotNull
    private Constraints buildConstraints() {
        return new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
    }

    private void getLayoutDecision() {
        if (findViewById(R.id.ingredient_textView) != null) {
            mLayoutManager = new GridAutofitLayoutManager(this, 600);
        } else {
            mLayoutManager = new LinearLayoutManager(this);
        }
    }

    private void supplyRecyclerView(@NotNull WorkInfo workInfo, MainActivityAdapter adapter) {
        ArrayList<Recipe> output = getRecipes(workInfo);
        adapter.setRecipes(output);
    }

    @Nullable
    private ArrayList<Recipe> getRecipes(@NotNull WorkInfo workInfo) {
        Gson gson = new Gson();
        Data data = workInfo.getOutputData();
        String output = data.getString(KEY_TASK_OUTPUT);
        Type token = new TypeToken<ArrayList<Recipe>>() {
        }.getType();
        ArrayList<Recipe> recipes = gson.fromJson(output, token);
        return recipes;
    }

}

