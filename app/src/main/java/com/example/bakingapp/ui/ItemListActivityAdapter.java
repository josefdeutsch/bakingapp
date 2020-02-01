package com.example.bakingapp.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bakingapp.R;
import com.example.bakingapp.model.Step;
import com.squareup.picasso.Picasso;

import java.util.List;

import static com.example.bakingapp.constant.Constants.ARG_ITEM_AMOUNTOFSTEPS;
import static com.example.bakingapp.constant.Constants.ARG_ITEM_ID;
import static com.example.bakingapp.constant.Constants.ARG_ITEM_LAYOUT;
import static com.example.bakingapp.constant.Constants.RECIPE_INDEX;


public class ItemListActivityAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static int TYPE_INGREDIENT = 1;
    private static int TYPE_STEP = 2;

    private final ItemListActivity mParentActivity;
    private List<Step> mValues;
    private final boolean mTwoPane;
    private final Context mContext;
    private Integer mAdapterposition;

    public ItemListActivityAdapter(Context context, ItemListActivity parent,
                                   List<Step> items, Integer adapterPosition,
                                   boolean twoPane) {
        mValues = items;
        mParentActivity = parent;
        mTwoPane = twoPane;
        mContext = context;
        mAdapterposition = adapterPosition;

        onAdapterCreateDefaultFragment();

    }

    private void onAdapterCreateDefaultFragment() {
        if (mTwoPane) {
            Bundle arguments = new Bundle();
            ItemDetailIngredientFragment fragment = new ItemDetailIngredientFragment();
            fragment.setArguments(arguments);
            arguments.putInt(RECIPE_INDEX,mAdapterposition);
            mParentActivity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.item_detail_container, fragment)
                    .commit();
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view;
        if (viewType == TYPE_INGREDIENT) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_ingredient_card, viewGroup, false);
            return new IngredientViewHolder(view);
        } else {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_step_card, viewGroup, false);
            return new StepViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_INGREDIENT;
        } else {
            return TYPE_STEP;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (getItemViewType(position) == TYPE_INGREDIENT) {
            ((IngredientViewHolder) viewHolder).setIngredientDetails(mValues.get(position));
        } else {
            ((StepViewHolder) viewHolder).setStepViewDetails(mValues.get(position));
        }
    }

     void setRecipes(List<Step> arrayList) {
        this.mValues = arrayList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    class IngredientViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private View mView;
        private TextView txtName;
        private ImageView imageView;

        IngredientViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            txtName = itemView.findViewById(R.id.txtName);
            imageView = itemView.findViewById(R.id.imageButton);
            itemView.setOnClickListener(this);
        }

        void setIngredientDetails(Step item) {
            txtName.setText(item.getShortDescription());
            itemView.setTag(item);
            Picasso.get().load(R.drawable.cake).into(imageView);
        }

        @Override
        public void onClick(View view) {
            Step ingredient = (Step) view.getTag();
            if (mTwoPane) {
                Bundle arguments = new Bundle();
                ItemDetailIngredientFragment fragment = new ItemDetailIngredientFragment();
                fragment.setArguments(arguments);
                arguments.putInt(RECIPE_INDEX,mAdapterposition);
                mParentActivity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.item_detail_container, fragment)
                        .commit();
            } else {
                Context context = view.getContext();
                Intent intent = new Intent(context, ItemDetailIngredientActivity.class);
                intent.putExtra(RECIPE_INDEX,mAdapterposition);
                context.startActivity(intent);
            }
        }
    }

    class StepViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private View mView;
        private TextView txtName;
        private ImageView imageView;

        StepViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            txtName = itemView.findViewById(R.id.txtName);
            imageView = itemView.findViewById(R.id.imageButton);
            itemView.setOnClickListener(this);
        }

        void setStepViewDetails(Step item) {
            txtName.setText(item.getShortDescription());
            itemView.setTag(item);
            Picasso.get().load(R.drawable.cake).into(imageView);
        }

        @Override
        public void onClick(View view) {
            Step item = (Step) view.getTag();
            if (mTwoPane) {
                Bundle arguments = new Bundle();
                arguments.putString(ARG_ITEM_ID, String.valueOf(item.getStepId()));
                arguments.putBoolean(ARG_ITEM_LAYOUT,true);
                arguments.putInt(ARG_ITEM_AMOUNTOFSTEPS,mValues.size());
                arguments.putInt(RECIPE_INDEX,mAdapterposition);
                ItemDetailStepFragment fragment = new ItemDetailStepFragment();
                fragment.setArguments(arguments);
                mParentActivity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.item_detail_container, fragment)
                        .commit();
            } else {
                Context context = view.getContext();
                Intent intent = new Intent(context, ItemDetailStepActivity.class);
                intent.putExtra(RECIPE_INDEX,mAdapterposition);
                intent.putExtra(ARG_ITEM_ID, String.valueOf(item.getStepId()));
                intent.putExtra(ARG_ITEM_AMOUNTOFSTEPS,mValues.size());
                context.startActivity(intent);
            }
        }
    }

}

