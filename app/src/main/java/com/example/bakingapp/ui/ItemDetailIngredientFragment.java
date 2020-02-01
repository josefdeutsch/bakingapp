package com.example.bakingapp.ui;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.test.espresso.IdlingResource;
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
import com.example.bakingapp.net.CallBackWorkerIngredient;
import com.example.bakingapp.provider.AppWidgetProvider;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import java.lang.reflect.Type;
import static android.content.Context.MODE_PRIVATE;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.example.bakingapp.constant.Constants.KEY_TASK_OUTPUT;
import static com.example.bakingapp.constant.Constants.RECIPE_INDEX;
import static com.example.bakingapp.constant.Constants.RECIPE_NAME;
import static com.example.bakingapp.constant.Constants.SHAREDPREFERENCES_EDITOR;
import static com.example.bakingapp.constant.Constants.SHOPPINGLIST_TAG;
import static com.example.bakingapp.constant.Constants.WORKREQUEST_INGREDIENTFRAGMENT;

public class ItemDetailIngredientFragment extends Fragment {

    public static final String TAG = "ItemDetailIngredientFragment";

    private View mRootView;
    private Data mData;
    private Constraints mConstraints;
    private OneTimeWorkRequest mDownload;
    private Integer mAdapterPosition;
    private SharedPreferences mSharedPreferences;

    public ItemDetailIngredientFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedPreferences = getActivity().getSharedPreferences(SHAREDPREFERENCES_EDITOR, MODE_PRIVATE);
        supplyTitle();
        getRecipeIndex();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_item_ingredient, container, false);

        setupWorkRequest();

        setupButtonConfiguration();

        EspressoIdlingResource.increment();
        executeWorkRequest();

        initTextView();

        return mRootView;
    }

    private void getRecipeIndex() {
        if (getArguments().containsKey(RECIPE_INDEX)) {
            mAdapterPosition = getArguments().getInt(RECIPE_INDEX);
        }
    }

    private void supplyTitle() {
        Activity activity = this.getActivity();
        CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
        if (appBarLayout != null) {
            appBarLayout.setTitle(mSharedPreferences.getString(RECIPE_NAME, getString(R.string.defaultvalue)));
        }
    }


    private void setupWorkRequest() {
        mData = buildData();
        mConstraints = buildConstraints();
        mDownload = buildOneTimeWorkRequest(mData, mConstraints);
    }

    private void executeWorkRequest() {
        WorkManager.getInstance(getActivity()).beginUniqueWork(WORKREQUEST_INGREDIENTFRAGMENT + mDownload.getId(),
                ExistingWorkPolicy.KEEP, mDownload).enqueue().getState().observe(this, new Observer<Operation.State>() {
            @Override
            public void onChanged(Operation.State state) {
                Toast.makeText(getActivity(), state.toString(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initTextView() {
        WorkManager.getInstance(getActivity()).getWorkInfoByIdLiveData(mDownload.getId())
                .observe(getViewLifecycleOwner(), new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(@Nullable WorkInfo workInfo) {
                        if (workInfo != null) {
                            if (workInfo.getState().isFinished()) {
                                supplyTextView(workInfo);
                                if (!EspressoIdlingResource.getIdlingResource().isIdleNow()) {
                                    EspressoIdlingResource.decrement();
                                }
                            }
                        }
                    }
                });
    }

    private void supplyTextView(@NotNull WorkInfo workInfo) {
        String ingredients = getIngredients(workInfo);
        ((TextView) mRootView.findViewById(R.id.ingredient_textView)).setText(ingredients);
    }

    @Nullable
    private String getIngredients(@NotNull WorkInfo workInfo) {
        Gson gson = new Gson();
        Data data = workInfo.getOutputData();
        String output = data.getString(KEY_TASK_OUTPUT);
        Type token = new TypeToken<String>() {
        }.getType();
        return gson.fromJson(output, token);
    }

    private void setupButtonConfiguration() {

        ((Button) mRootView.findViewById(R.id.configuration)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog();
            }
        });
    }

    private void alertDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.question))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(mSharedPreferences.contains(SHOPPINGLIST_TAG)){
                            supplySharedPreferences();
                            onUpdateAppWidgetProvider();
                        }else{
                            alertDialogfailed();
                        }
                    }
                })
                .setNegativeButton(getString(R.string.DISMISS), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onResume();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void alertDialogfailed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.question2))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        onResume();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void supplySharedPreferences() {
        SharedPreferences.Editor editor = getActivity().getSharedPreferences(SHAREDPREFERENCES_EDITOR, MODE_PRIVATE).edit();
        editor.remove(RECIPE_INDEX);
        editor.putInt(RECIPE_INDEX, mAdapterPosition);
        editor.apply();
    }

    private void onUpdateAppWidgetProvider() {
        Intent intent = new Intent(getActivity(), AppWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(getActivity()).getAppWidgetIds(new ComponentName(getActivity(), AppWidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        getActivity().sendBroadcast(intent);
    }


    @NotNull
    private OneTimeWorkRequest buildOneTimeWorkRequest(Data data, Constraints constraints) {
        return new OneTimeWorkRequest.Builder(CallBackWorkerIngredient.class)
                .setConstraints(constraints)
                .setInputData(data)
                .build();
    }

    @NotNull
    private Constraints buildConstraints() {
        return new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
    }

    @NotNull
    private Data buildData() {
        return new Data.Builder()
                .putInt(RECIPE_INDEX, mAdapterPosition)
                .build();
    }

    @Nullable
    private IdlingResource mIdlingResource;

    @VisibleForTesting
    @NonNull
    public IdlingResource getIdlingResource() {
        if (mIdlingResource == null) {
            mIdlingResource = EspressoIdlingResource.getIdlingResource();
        }
        return mIdlingResource;
    }


}
