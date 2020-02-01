package com.example.bakingapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.bakingapp.R;

import static com.example.bakingapp.constant.Constants.RECIPE_INDEX;

public class ItemDetailIngredientActivity extends AppCompatActivity {

    private static final String TAG = "ItemDetailIngredientAct";
    private Integer mAdapterPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail_ingredient);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);
        mAdapterPosition = getIntent().getIntExtra(RECIPE_INDEX,0);

        setupActionBar();
        setupItemDetailFragment(savedInstanceState);
    }

    private void setupItemDetailFragment(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putInt(RECIPE_INDEX,mAdapterPosition);
            ItemDetailIngredientFragment fragment = new ItemDetailIngredientFragment();
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

