package com.example.bakingapp.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
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
import com.example.bakingapp.model.Recipe;
import com.example.bakingapp.model.Step;
import com.example.bakingapp.net.CallBackWorkerStep;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.ArrayList;

import static com.example.bakingapp.constant.Constants.ARG_ITEM_AMOUNTOFSTEPS;
import static com.example.bakingapp.constant.Constants.ARG_ITEM_ID;
import static com.example.bakingapp.constant.Constants.ARG_ITEM_LAYOUT;
import static com.example.bakingapp.constant.Constants.CURRENT_INDEX;
import static com.example.bakingapp.constant.Constants.KEY_TASK_OUTPUT;
import static com.example.bakingapp.constant.Constants.NUMBER_AMOUNTOFSTEPS;
import static com.example.bakingapp.constant.Constants.RECIPE_INDEX;
import static com.example.bakingapp.constant.Constants.RECIPE_NAME;
import static com.example.bakingapp.constant.Constants.SHAREDPREFERENCES_EDITOR;
import static com.example.bakingapp.constant.Constants.STATE_PLAYER_FULLSCREEN;
import static com.example.bakingapp.constant.Constants.STATE_RESUME_POSITION;
import static com.example.bakingapp.constant.Constants.STATE_RESUME_WINDOW;
import static com.example.bakingapp.constant.Constants.STEP_INDEX;
import static com.example.bakingapp.constant.Constants.WORKREQUEST_STEPFRAGMENT;


public class ItemDetailStepFragment extends Fragment implements Player.EventListener {

    public static final String TAG = "ItemDetailStepFragment";

    private SimpleExoPlayerView mExoPlayerView;
    private boolean mExoPlayerFullscreen = false;
    private FrameLayout mFullScreenButton;
    private ImageView mFullScreenIcon;
    private Dialog mFullScreenDialog;
    private Data data;
    private Constraints constraints;
    private OneTimeWorkRequest download;
    private int mResumeWindow;
    private long mResumePosition;
    private boolean mTwoPane;
    private View rootView;
    private Bitmap posterBitmap;
    private Integer mAmountOfSteps;
    private Integer mCurrent;
    private Integer mRecipeIndex;
    private SharedPreferences mSharedPreferences;

    public ItemDetailStepFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSharedPreferences = getActivity().getSharedPreferences(SHAREDPREFERENCES_EDITOR, Context.MODE_PRIVATE);

        supplyTitle();

        getValues();

        getSavedValuesInstanceState(savedInstanceState);

        initWorkManager();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_item_step, container, false);
        mExoPlayerView = (SimpleExoPlayerView) rootView.findViewById(R.id.exoplayer);
        onNextFragment();

        EspressoIdlingResource.increment();
        executeWorkmanager();

        if (!mTwoPane) {
            initFullscreenDialog();
            initFullscreenButton();
        }

        initExoPlayer();

        queryWorkmanager();

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putInt(NUMBER_AMOUNTOFSTEPS, mAmountOfSteps);
        outState.putInt(CURRENT_INDEX, mCurrent);
        outState.putInt(STATE_RESUME_WINDOW, mResumeWindow);
        outState.putLong(STATE_RESUME_POSITION, mResumePosition);
        outState.putBoolean(STATE_PLAYER_FULLSCREEN, mExoPlayerFullscreen);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Util.SDK_INT <= Build.VERSION_CODES.M) {
            matchesExoPlayerFullScreenConfig();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= Build.VERSION_CODES.M) {
            withdrawExoPlayer();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > Build.VERSION_CODES.M) {
            matchesExoPlayerFullScreenConfig();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > Build.VERSION_CODES.M) {
            withdrawExoPlayer();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mExoPlayerView.getPlayer() != null)
            mExoPlayerView.getPlayer().release();
    }

    private void getValues() {
        getCurrentItemId();
        getRecipeIndex();
        getLayoutDecision();
        getAmountofSteps();
    }

    private void getSavedValuesInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mCurrent = savedInstanceState.getInt(CURRENT_INDEX);
            mResumeWindow = savedInstanceState.getInt(STATE_RESUME_WINDOW);
            mResumePosition = savedInstanceState.getLong(STATE_RESUME_POSITION);
            mExoPlayerFullscreen = savedInstanceState.getBoolean(STATE_PLAYER_FULLSCREEN);
        }
    }

    private void getCurrentItemId() {
        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mCurrent = Integer.parseInt(getArguments().getString(ARG_ITEM_ID));
        }
    }

    private void getAmountofSteps() {
        if (getArguments().containsKey(ARG_ITEM_AMOUNTOFSTEPS)) {
            mAmountOfSteps = getArguments().getInt(ARG_ITEM_AMOUNTOFSTEPS);
        }
    }

    private void getLayoutDecision() {
        if (getArguments().containsKey(ARG_ITEM_LAYOUT)) {
            mTwoPane = getArguments().getBoolean(ARG_ITEM_LAYOUT);
        }
    }

    private void getRecipeIndex() {
        if (getArguments().containsKey(RECIPE_INDEX)) {
            mRecipeIndex = getArguments().getInt(RECIPE_INDEX);
        }
    }

    private void supplyTitle() {
        Activity activity = this.getActivity();
        CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
        if (appBarLayout != null) {
            appBarLayout.setTitle(mSharedPreferences.getString(RECIPE_NAME, getString(R.string.defaultvalue)));
        }
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == Player.STATE_READY && playWhenReady) {


        } else if (playbackState == Player.STATE_READY) {


        } else if (playbackState == Player.STATE_ENDED) {
            mExoPlayerView.getPlayer().seekTo(0);
            mExoPlayerView.getPlayer().setPlayWhenReady(false);
        }

    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        getActivity().finish();
    }

    @Override
    public void onPositionDiscontinuity() {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
    }

    private void onNextFragment() {
        ((Button) rootView.findViewById(R.id.next)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeFragmentManager();
            }
        });
    }

    private void executeFragmentManager() {

        Bundle arguments = new Bundle();
        Integer index = getIndex();

        arguments.putString(ARG_ITEM_ID, String.valueOf(index));
        arguments.putBoolean(ARG_ITEM_LAYOUT, mTwoPane);
        arguments.putInt(ARG_ITEM_AMOUNTOFSTEPS, mAmountOfSteps);
        arguments.putInt(RECIPE_INDEX, mRecipeIndex);

        ItemDetailStepFragment fragment = new ItemDetailStepFragment();
        fragment.setArguments(arguments);
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.item_detail_container, fragment)
                .commit();
    }

    @NotNull
    private Integer getIndex() {
        Integer index = null;
        if (mCurrent <= mAmountOfSteps - 1) {
            index = mCurrent;
            index++;
        } else {
            index = 0;
        }
        return index;
    }

    private void initWorkManager() {

        data = new Data.Builder()
                .putInt(RECIPE_INDEX, mRecipeIndex)
                .putInt(STEP_INDEX, mCurrent)
                .build();
        constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        download = new OneTimeWorkRequest.Builder(CallBackWorkerStep.class)
                .setConstraints(constraints)
                .setInputData(data)
                .build();
    }

    private void executeWorkmanager() {

        WorkManager.getInstance(getActivity()).beginUniqueWork(WORKREQUEST_STEPFRAGMENT + download.getId(),
                ExistingWorkPolicy.KEEP, download).enqueue().getState().observe(this, new Observer<Operation.State>() {
            @Override
            public void onChanged(Operation.State state) {
                Toast.makeText(getActivity(), state.toString(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initFullscreenDialog() {

        mFullScreenDialog = new Dialog(getActivity(), android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
            public void onBackPressed() {
                if (mExoPlayerFullscreen)
                    closeFullscreenDialog();
                super.onBackPressed();
            }
        };
    }

    private void openFullscreenDialog() {

        ((ViewGroup) mExoPlayerView.getParent()).removeView(mExoPlayerView);
        mFullScreenDialog.addContentView(mExoPlayerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_fullscreen_skrink));
        mExoPlayerFullscreen = true;
        mFullScreenDialog.show();
    }

    private void closeFullscreenDialog() {

        ((ViewGroup) mExoPlayerView.getParent()).removeView(mExoPlayerView);
        ((FrameLayout) rootView.findViewById(R.id.main_media_frame)).addView(mExoPlayerView);
        mExoPlayerFullscreen = false;
        mFullScreenDialog.dismiss();
        mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_fullscreen_expand));
    }

    private void initFullscreenButton() {

        PlaybackControlView controlView = mExoPlayerView.findViewById(R.id.exo_controller);
        mFullScreenIcon = controlView.findViewById(R.id.exo_fullscreen_icon);
        mFullScreenButton = controlView.findViewById(R.id.exo_fullscreen_button);
        mFullScreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mExoPlayerFullscreen)
                    openFullscreenDialog();
                else
                    closeFullscreenDialog();
            }
        });

    }

    private void initExoPlayer() {

        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        LoadControl loadControl = new DefaultLoadControl();
        SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(getActivity()), trackSelector, loadControl);
        mExoPlayerView.setPlayer(player);
        boolean haveResumePosition = mResumeWindow != C.INDEX_UNSET;
        if (haveResumePosition) {
            mExoPlayerView.getPlayer().seekTo(mResumeWindow, mResumePosition);
        }
        mExoPlayerView.getPlayer().addListener(this);
    }

    private void queryWorkmanager() {
        WorkManager.getInstance(getActivity()).getWorkInfoByIdLiveData(download.getId())
                .observe(getViewLifecycleOwner(), new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(@Nullable WorkInfo workInfo) {
                        if (workInfo != null) {
                            if (workInfo.getState().isFinished()) {

                                Step step = getRecipes(workInfo);
                                String videoURL = step.getVideoUrl();
                                String thumbnail = step.getThumbnailUrl();
                                supplyExoPlayer(videoURL, thumbnail);
                                supplyTextView(step);
                                if (!EspressoIdlingResource.getIdlingResource().isIdleNow()) {
                                    EspressoIdlingResource.decrement();
                                }
                            }
                        }
                    }
                });
    }

    private void supplyExoPlayer(String videoURL, String thumbnail) {

        String output;
        if (!videoURL.isEmpty()) {
            output = videoURL;
            MediaSource videoSource = buildMediaSource(output);
            mExoPlayerView.getPlayer().prepare(videoSource);
            mExoPlayerView.getPlayer().setPlayWhenReady(true);

        } else if (!thumbnail.isEmpty()) {
            output = thumbnail;
            MediaSource videoSource = buildMediaSource(output);
            mExoPlayerView.getPlayer().prepare(videoSource);
            mExoPlayerView.getPlayer().setPlayWhenReady(true);

        } else if (thumbnail.isEmpty() && videoURL.isEmpty()) {
            mExoPlayerView.setDefaultArtwork(BitmapFactory.decodeResource
                    (getResources(), R.drawable.question_mark));
        }
    }

    private void supplyTextView(Step step) {
        ((TextView) rootView.findViewById(R.id.step_textView)).setText(step.getDescription());
    }

    @NotNull
    private MediaSource buildMediaSource(String videoURL) {
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(getActivity(), Util.getUserAgent(getActivity(), "ExoPlayer"));
        final ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        return new ExtractorMediaSource(Uri.parse(videoURL), dataSourceFactory, extractorsFactory, null, null);
    }

    @Nullable
    private Step getRecipes(@NotNull WorkInfo workInfo) {
        Gson gson = new Gson();
        Data data = workInfo.getOutputData();
        String output = data.getString(KEY_TASK_OUTPUT);
        Type token = new TypeToken<Step>() {
        }.getType();
        return gson.fromJson(output, token);
    }

    private void postThumbnailIntoExoplayer(ArrayList<Recipe> recipes) {
        String thumbnailURL = recipes.get(0).getmSteps().get(0).getThumbnailUrl();
        if (!thumbnailURL.isEmpty()) {
            Picasso.get().load(thumbnailURL).resize(250, 250).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    if (bitmap != null) {
                        posterBitmap = bitmap;
                    }
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                    if (e.getCause() != null) {
                        Log.d(TAG, "onBitmapFailed: " + e.toString());
                    }
                    if (errorDrawable.getCallback() != null) {
                        Log.d(TAG, "onBitmapFailed: " + e.toString());
                    }
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                }
            });
            mExoPlayerView.setDefaultArtwork(posterBitmap);
        }

    }

    private void matchesExoPlayerFullScreenConfig() {
        if (mExoPlayerFullscreen) {
            ((ViewGroup) mExoPlayerView.getParent()).removeView(mExoPlayerView);
            mFullScreenDialog.addContentView(mExoPlayerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_fullscreen_skrink));
            mFullScreenDialog.show();
        }
    }

    private void withdrawExoPlayer() {
        mExoPlayerView.getPlayer().setPlayWhenReady(false);
        if (mExoPlayerView != null && mExoPlayerView.getPlayer() != null) {
            mResumeWindow = mExoPlayerView.getPlayer().getCurrentWindowIndex();
            mResumePosition = Math.max(0, mExoPlayerView.getPlayer().getContentPosition());
            mExoPlayerView.getPlayer().release();
        }
        if (mFullScreenDialog != null) {
            mFullScreenDialog.dismiss();
        }
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


