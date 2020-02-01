package com.example.bakingapp.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.bakingapp.R;
import com.example.bakingapp.model.Recipe;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.example.bakingapp.constant.Constants.RECIPE_INDEX;
import static com.example.bakingapp.constant.Constants.RECIPE_NAME;
import static com.example.bakingapp.constant.Constants.SHAREDPREFERENCES_EDITOR;


public class MainActivityAdapter extends RecyclerView.Adapter<MainActivityAdapter.ViewHolder> {

    private List<Recipe> mValues;

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Recipe item = (Recipe)view.getTag();
            Context context = view.getContext();
            Intent intent = new Intent(context,ItemListActivity.class);
            supplySharedPreferences(item, context);
            context.startActivity(intent);
        }
    };

    private void supplySharedPreferences(Recipe item, Context context) {
        supplyRecipeIndexSharedPreferences(context,item.getId()-1);
        supplyTitleNameSharedPreferences(context,item.getName());
    }

    MainActivityAdapter(ArrayList<Recipe> items) {
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.main_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mTextView.setText(mValues.get(position).getName());
        holder.itemView.setTag(mValues.get(position));
        holder.itemView.setOnClickListener(mOnClickListener);
        Picasso.get().load(R.drawable.cake).into(holder.imageButton);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public void setRecipes(List<Recipe> arrayList) {
        this.mValues = arrayList;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mTextView;
        public final ImageView imageButton;

        ViewHolder(View view) {
            super(view);
            mTextView = (TextView) view.findViewById(R.id.main_text);
            imageButton = itemView.findViewById(R.id.imageButton);
        }
    }

    private void supplyRecipeIndexSharedPreferences(Context view, int id) {
        SharedPreferences.Editor editor = view.getSharedPreferences(SHAREDPREFERENCES_EDITOR, MODE_PRIVATE).edit();
        editor.remove(RECIPE_INDEX);
        editor.putInt(RECIPE_INDEX, id);
        editor.apply();
    }

    private void supplyTitleNameSharedPreferences(Context view,String name) {
        SharedPreferences.Editor editor = view.getSharedPreferences(SHAREDPREFERENCES_EDITOR, MODE_PRIVATE).edit();
        editor.remove(RECIPE_NAME);
        editor.putString(RECIPE_NAME, name);
        editor.apply();
    }
}


