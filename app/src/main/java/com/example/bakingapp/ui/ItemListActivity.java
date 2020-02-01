package com.example.bakingapp.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import com.example.bakingapp.model.Step;
import com.example.bakingapp.net.CallBackWorkerItemlist;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.ArrayList;

import static com.example.bakingapp.constant.Constants.KEY_TASK_OUTPUT;
import static com.example.bakingapp.constant.Constants.RECIPE_INDEX;
import static com.example.bakingapp.constant.Constants.RECIPE_NAME;
import static com.example.bakingapp.constant.Constants.SHAREDPREFERENCES_EDITOR;
import static com.example.bakingapp.constant.Constants.WORKREQUEST_ITEMLIST;

public class ItemListActivity extends AppCompatActivity {

    private static final String TAG = "ItemListActivity";
    private boolean mTwoPane;
    private Data mData;
    private Constraints mConstraints;
    private OneTimeWorkRequest mDownload;
    private SharedPreferences mSharedPreferences;
    private Integer mRecipeIndex;
    private String mTitleToolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        mSharedPreferences = getSharedPreferences(SHAREDPREFERENCES_EDITOR, Context.MODE_PRIVATE);

        setupToolbar();

        setupWorkRequest();

        EspressoIdlingResource.increment();

        executeWorkRequest();

        getLayoutDecision();

        buildRecyclerView();

    }

    @Override
    public void onRestart() {
        super.onRestart();

    }


    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mTitleToolBar = mSharedPreferences.getString(RECIPE_NAME, getString(R.string.defaultvalue));
        setSupportActionBar(toolbar);
        toolbar.setTitle(mTitleToolBar);
    }

    private void setupWorkRequest() {

        mData = buildData();
        mConstraints = buildConstraints();
        mDownload = buildOneTimeWorkRequest();

    }

    private void executeWorkRequest() {

        WorkManager.getInstance(getApplicationContext()).beginUniqueWork(WORKREQUEST_ITEMLIST + mRecipeIndex,
                ExistingWorkPolicy.KEEP, mDownload).enqueue().getState().observe(this, new Observer<Operation.State>() {
            @Override
            public void onChanged(Operation.State state) {
                Toast.makeText(getApplicationContext(), state.toString(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void buildRecyclerView() {
        View recyclerView = findViewById(R.id.item_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);
    }

    private void getLayoutDecision() {
        if (findViewById(R.id.item_detail_container) != null) {
            mTwoPane = true;
        }
    }

    private OneTimeWorkRequest buildOneTimeWorkRequest() {
        return new OneTimeWorkRequest.Builder(CallBackWorkerItemlist.class)
                .setConstraints(mConstraints)
                .setInputData(mData)
                .build();
    }

    @NotNull
    private Constraints buildConstraints() {
        return new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
    }

    private Data buildData() {
        mRecipeIndex = mSharedPreferences.getInt(RECIPE_INDEX, 0);
        return new Data.Builder()
                .putInt(RECIPE_INDEX, mRecipeIndex)
                .build();
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {

        ItemListActivityAdapter itemListActivityAdapter = new ItemListActivityAdapter
                (this, this, new ArrayList(1), mRecipeIndex, mTwoPane);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(itemListActivityAdapter);
        initRecyclerView(itemListActivityAdapter);
    }

    private void initRecyclerView(ItemListActivityAdapter itemListActivityAdapter) {
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(mDownload.getId())
                .observe(this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(@Nullable WorkInfo workInfo) {
                        if (workInfo != null) {
                            if (workInfo.getState().isFinished()) {
                                ArrayList<Step> output = getRecipes(workInfo);
                                itemListActivityAdapter.setRecipes(output);
                                if (!EspressoIdlingResource.getIdlingResource().isIdleNow()) {
                                    EspressoIdlingResource.decrement();
                                }
                            }
                        }
                    }
                });
    }

    @Nullable
    private ArrayList<Step> getRecipes(@NotNull WorkInfo workInfo) {
        Gson gson = new Gson();
        Data data = workInfo.getOutputData();
        String output = data.getString(KEY_TASK_OUTPUT);
        Type token = new TypeToken<ArrayList<Step>>() {
        }.getType();
        return gson.fromJson(output, token);
    }
}
