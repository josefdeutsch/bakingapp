package com.example.bakingapp.provider;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.bakingapp.R;
import com.example.bakingapp.model.Ingredient;
import com.example.bakingapp.net.AsyncDownloadWidget;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class WidgetService extends RemoteViewsService {


    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        final ExampleWidgetItemFactory itemFactory =  new ExampleWidgetItemFactory(getApplicationContext(), intent);
                itemFactory.
        context = getApplicationContext();
        return itemFactory;
    }

    static class ExampleWidgetItemFactory implements RemoteViewsFactory {

        private Context context;

        private int recipeIndex;

        List<Ingredient> recipes;

        ExampleWidgetItemFactory(Context context, Intent intent) {
            this.context = context;
            this.recipeIndex = intent.getIntExtra(AppWidgetManager.EXTRA_CUSTOM_EXTRAS,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        @Override
        public void onCreate() {
        }
        @Override
        public void onDataSetChanged() {
            if (hasInternet(context)) {
                executeRetrofitServiceConnection();
            }
        }

        @Override
        public void onDestroy() {
            recipes.clear();
        }

        @Override
        public int getCount() {
            if(recipes==null)recipes = new ArrayList<>(1);
            return recipes.size();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_item);

            String ingredient = recipes.get(position).getIngredient();
            String measure    = recipes.get(position).getMeasure();
            String quantity   = String.valueOf(recipes.get(position).getQuantity());
            String txt = ingredient+" "+measure+" "+quantity;

            views.setTextViewText(R.id.example_widget_item_text, txt);
            return views;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        private void executeRetrofitServiceConnection() {
            AsyncDownloadWidget asyncDownloadWidget = new AsyncDownloadWidget(context,recipeIndex);
            try {
                recipes = asyncDownloadWidget.execute().get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public boolean hasInternet(Context context) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
                return true;
            } else {
                return false;
            }
        }
    }

}
