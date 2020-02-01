package com.example.bakingapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.bakingapp.R;

import static com.example.bakingapp.constant.Constants.ARG_ITEM_AMOUNTOFSTEPS;
import static com.example.bakingapp.constant.Constants.ARG_ITEM_ID;
import static com.example.bakingapp.constant.Constants.ARG_ITEM_LAYOUT;
import static com.example.bakingapp.constant.Constants.RECIPE_INDEX;


public class ItemDetailStepActivity extends AppCompatActivity {

    private static final String TAG = "ItemDetailStepActivity";
    private Integer mRecipeIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail_step);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        mRecipeIndex = getRecipeIndex();

        setupActionBar();
        setupItemDetailFragment(savedInstanceState);
    }

    private void setupItemDetailFragment(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putString(ARG_ITEM_ID,
                    getIntent().getStringExtra(ARG_ITEM_ID));
            arguments.putBoolean(ARG_ITEM_LAYOUT, false);
            arguments.putInt(ARG_ITEM_AMOUNTOFSTEPS,
                    getIntent().getIntExtra(ARG_ITEM_AMOUNTOFSTEPS, 0));
            arguments.putInt(RECIPE_INDEX, mRecipeIndex);
            ItemDetailStepFragment fragment = new ItemDetailStepFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.item_detail_container, fragment)
                    .commit();
        }
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private int getRecipeIndex() {
        return getIntent().getIntExtra(RECIPE_INDEX, 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            navigateUpTo(new Intent(this, ItemListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
