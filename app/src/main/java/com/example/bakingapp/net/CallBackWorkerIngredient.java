package com.example.bakingapp.net;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.concurrent.futures.ResolvableFuture;
import androidx.work.Data;
import androidx.work.WorkerParameters;
import com.example.bakingapp.model.Ingredient;
import com.example.bakingapp.model.Recipe;
import com.example.bakingapp.ui.ErrorActivity;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.bakingapp.constant.Constants.BAKINGRECIPEBASEURL;
import static com.example.bakingapp.constant.Constants.KEY_TASK_OUTPUT;
import static com.example.bakingapp.constant.Constants.RECIPE_INDEX;


/** CallBackWorker design to download data, produce a results and throw errorcallbacks - if occurs **/

public class CallBackWorkerIngredient extends androidx.work.ListenableWorker {

    private ResolvableFuture<Result> mFuture;
    private Gson mGson = new Gson();
    private Context mContext;
    private Integer mRecipeIndex;

    public CallBackWorkerIngredient(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
        mContext = appContext;
    }

    /** process data onBackGroundThread : build OkHttpClient, build RetrofitClient, execute ServiceConnection **/

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        mFuture = ResolvableFuture.create();
        Data data = getInputData();
        mRecipeIndex = data.getInt(RECIPE_INDEX, 0);

        getBackgroundExecutor().execute(new Runnable() {
            @Override
            public void run() {
                OkHttpClient okHttpClient = buildHttpClient();
                Retrofit retrofit = buildRetrofitClient(okHttpClient);
                Call<List<Recipe>> call = buildRetrofitServiceConnection(retrofit);
                executeRetrofitServiceConnection(call);
            }
        });

        return mFuture;
    }

    /** @return supply an output with an callable input **/

    private void executeRetrofitServiceConnection(Call<List<Recipe>> call) {
        String input = "";
        try {
            input = supplyInput(call, input);
            String output = mGson.toJson(input);
            Data data = buildData(output);
            mFuture.set(Result.success(data));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**@param call access and execute retrofit callable resposenody  **/

    private String supplyInput(Call<List<Recipe>> call, String input) throws IOException {
        List<Ingredient> ingredientArrayList = call.execute().body().get(mRecipeIndex).getIngredients();
        String newline = System.getProperty("line.separator");
        for (Ingredient ingredient : ingredientArrayList) {
            input += ingredient.getIngredient() + " " + ingredient.getMeasure() + " " + ingredient.getQuantity() + newline;
        }
        return input;
    }

    /** @param retrofit build a service connection derived retrofitService interface **/

    private Call<List<Recipe>> buildRetrofitServiceConnection(Retrofit retrofit) {
        RetrofitService service = retrofit.create(RetrofitService.class);
        return service.getPosts();
    }

   /** @return Data object with Key passed to Activity/Fragment to be observed **/
    private Data buildData(String output) {
        return new Data.Builder()
                .putString(KEY_TASK_OUTPUT, output)
                .build();
    }

    /** @param okHttpClient build and add an okHttpClient to retrofit
     *  @return retrofit **/

    @NotNull
    private Retrofit buildRetrofitClient(OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .baseUrl(BAKINGRECIPEBASEURL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();
    }

    /** @return a connection timeout 408 request code, if triggered, add interceptor intercept response chain **/
    @NotNull
    private OkHttpClient buildHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS) // produces 408 request.code() - Timeout
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Response response = getResponseCode(chain);
                        return response;
                    }
                })
                .build();
    }

    /** @return query response chain if value >= 301 ErrorActivity is called on Mainthread **/
    @NotNull
    private Response getResponseCode(Interceptor.Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);
        if (response.code() >= 301) {
            new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message message) {
                    mContext.startActivity(new Intent(mContext, ErrorActivity.class));
                }
            };
            mFuture.set(Result.failure()); // nullpointer exception
        }
        return response;
    }
}
