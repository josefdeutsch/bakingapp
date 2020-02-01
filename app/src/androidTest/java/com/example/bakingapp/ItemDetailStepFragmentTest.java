package com.example.bakingapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.bakingapp.idlingresource.EspressoIdlingResource;
import com.example.bakingapp.ui.ItemDetailStepFragment;

import org.jetbrains.annotations.NotNull;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static com.example.bakingapp.constant.Constants.ARG_ITEM_AMOUNTOFSTEPS;
import static com.example.bakingapp.constant.Constants.ARG_ITEM_ID;
import static com.example.bakingapp.constant.Constants.ARG_ITEM_LAYOUT;
import static com.example.bakingapp.constant.Constants.RECIPE_INDEX;
import static org.hamcrest.CoreMatchers.equalTo;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4.class)
public class ItemDetailStepFragmentTest {

    private IdlingResource mIdlingResource;

    @NotNull
    private Bundle getBundle(int recipeIndex,int stepIndex) {

        Bundle args = new Bundle();
        args.putString(ARG_ITEM_ID,String.valueOf(stepIndex));
        args.putBoolean(ARG_ITEM_LAYOUT,true);
        args.putInt(RECIPE_INDEX, recipeIndex);
        args.putInt(ARG_ITEM_AMOUNTOFSTEPS,10);

        return args;
    }

    @Test
    public void a_onFragmentScenario_hasDataSet() throws InterruptedException {

        Bundle args = getBundle(1,0);
        FragmentScenario<ItemDetailStepFragment> fragmentScenario =
                FragmentScenario.launchInContainer(ItemDetailStepFragment.class,args);
        fragmentScenario.onFragment(new FragmentScenario.FragmentAction<ItemDetailStepFragment>() {
            @Override
            public void perform(@NonNull ItemDetailStepFragment fragment) {
                mIdlingResource = fragment.getIdlingResource();
                IdlingRegistry.getInstance().register(mIdlingResource);
            }
        });
        String fragmentproof = "Recipe Introduction";
        onView(withText(fragmentproof)).check(matches(isDisplayed()));
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.getIdlingResource());
    }

    @Test
    public void b_on_FragmentLounch_isTextViewVisible(){

        Bundle args = getBundle(1,1);
        FragmentScenario<ItemDetailStepFragment> fragmentScenario =
                FragmentScenario.launchInContainer(ItemDetailStepFragment.class,args);
        onView(withId(R.id.step_textView)).check(matches(isDisplayed()));

    }
    @Test
    public void c_on_FragmentLounch_isExoPlayerVisible(){

        Bundle args = getBundle(1,1);
        FragmentScenario<ItemDetailStepFragment> fragmentScenario =
                FragmentScenario.launchInContainer(ItemDetailStepFragment.class,args);
        onView(withId(R.id.exoplayer)).check(matches(isDisplayed()));

    }
    @Test
    public void d_on_FragmentLounch_isNextButtonVisible() throws InterruptedException {

        Bundle args = getBundle(1,1);
        FragmentScenario<ItemDetailStepFragment> fragmentScenario =
                FragmentScenario.launchInContainer(ItemDetailStepFragment.class,args);
        onView(withId(R.id.next)).check(matches(isDisplayed()));

    }

    @Test
    public void e_on_FragmentLounch_isArguments(){

        Bundle args = getBundle(1,1);

        FragmentScenario<ItemDetailStepFragment> fragmentScenario =
                FragmentScenario.launchInContainer(ItemDetailStepFragment.class,args);
        fragmentScenario.onFragment(new FragmentScenario.FragmentAction<ItemDetailStepFragment>() {
            @Override
            public void perform(@NonNull ItemDetailStepFragment fragment) {
                assertThat(fragment.getArguments().getString(ARG_ITEM_ID),equalTo("1"));
                assertThat(fragment.getArguments().getBoolean(ARG_ITEM_LAYOUT),equalTo( true));
                assertThat(fragment.getArguments().getInt(ARG_ITEM_AMOUNTOFSTEPS),equalTo( 10));
                assertThat(fragment.getArguments().getInt(RECIPE_INDEX),equalTo( 1));
            }
        });
    }

    @Test
    public void f_on_FragmentLounch_hasIdentfier() {

        Bundle args = getBundle(1,1);
        FragmentScenario<ItemDetailStepFragment> fragmentScenario =
                FragmentScenario.launchInContainer(ItemDetailStepFragment.class, args);
        fragmentScenario.onFragment(new FragmentScenario.FragmentAction<ItemDetailStepFragment>() {
            @Override
            public void perform(@NonNull ItemDetailStepFragment fragment) {
                assertThat(fragment.TAG, equalTo("ItemDetailStepFragment"));
            }
        });
    }
}