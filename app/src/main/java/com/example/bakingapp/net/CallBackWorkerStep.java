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

import com.example.bakingapp.model.Recipe;
import com.example.bakingapp.model.Step;
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
import static com.example.bakingapp.constant.Constants.STEP_INDEX;



public class CallBackWorkerStep extends androidx.work.ListenableWorker {

    private ResolvableFuture<Result> mFuture;
    private Gson mGson = new Gson();
    private Context mContext;
    private Integer mRecipeIndex;
    private Integer mStepIndex;

    public CallBackWorkerStep(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
        mContext = appContext;
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        mFuture = ResolvableFuture.create();
        Data data = getInputData();
        mRecipeIndex = data.getInt(RECIPE_INDEX,0);
        mStepIndex = data.getInt(STEP_INDEX,0);

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

    private void executeRetrofitServiceConnection(Call<List<Recipe>> call) {
        Step input = null;
        try {
             input = supplyInput(call);
             String output = mGson.toJson(input);
             Data data = buildData(output);
             mFuture.set(Result.success(data));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private Step supplyInput(Call<List<Recipe>> call) throws IOException {
        Step input;
        input = call.execute().body().get(mRecipeIndex).getSteps().get(mStepIndex);
        return input;
    }

    private Call<List<Recipe>> buildRetrofitServiceConnection(Retrofit retrofit) {
        RetrofitService service = retrofit.create(RetrofitService.class);
        return service.getPosts();
    }

    private Data buildData(String output) {
        return new Data.Builder()
                .putString(KEY_TASK_OUTPUT,output)
                .build();
    }


    @NotNull
    private Retrofit buildRetrofitClient(OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                    .baseUrl(BAKINGRECIPEBASEURL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();
    }

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
            mFuture.set(Result.failure());
        }
        return response;
    }
}
