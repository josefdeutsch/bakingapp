package com.example.bakingapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.bakingapp.idlingresource.EspressoIdlingResource;
import com.example.bakingapp.ui.ItemDetailIngredientFragment;

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
import static com.example.bakingapp.constant.Constants.RECIPE_INDEX;
import static org.hamcrest.CoreMatchers.equalTo;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4.class)
public class ItemDetailIngredientFragmentTest {

    private IdlingResource mIdlingResource;

    @NotNull
    private Bundle getBundle(int value) {

        Bundle args = new Bundle();
        args.putInt(RECIPE_INDEX, value);

        return args;
    }
    @Test
    public void a_on_FragmentLounch_hasDataSet() throws InterruptedException {

        Bundle args = getBundle(1);

        FragmentScenario<ItemDetailIngredientFragment> fragmentScenario =
                FragmentScenario.launchInContainer(ItemDetailIngredientFragment.class, args);
        fragmentScenario.onFragment(new FragmentScenario.FragmentAction<ItemDetailIngredientFragment>() {
            @Override
            public void perform(@NonNull ItemDetailIngredientFragment fragment) {
                mIdlingResource = fragment.getIdlingResource();
                IdlingRegistry.getInstance().register(mIdlingResource);
            }
        });
        String string = getString3();

        onView(withText(string)).check(matches(isDisplayed()));
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.getIdlingResource());

    }

    @Test
    public void b_on_FragmentLounch_hasIdentfier() {
        Bundle args = getBundle(1);
        FragmentScenario<ItemDetailIngredientFragment> fragmentScenario =
                FragmentScenario.launchInContainer(ItemDetailIngredientFragment.class,args);
        fragmentScenario.onFragment(new FragmentScenario.FragmentAction<ItemDetailIngredientFragment>() {
            @Override
            public void perform(@NonNull ItemDetailIngredientFragment fragment) {
                assertThat(fragment.TAG, equalTo("ItemDetailIngredientFragment"));
            }
        });
    }
    @Test
    public void c_on_FragmentLounch_isArguments(){

        Bundle args = getBundle(1);
        FragmentScenario<ItemDetailIngredientFragment> fragmentScenario =
                FragmentScenario.launchInContainer(ItemDetailIngredientFragment.class,args);
        fragmentScenario.onFragment(new FragmentScenario.FragmentAction<ItemDetailIngredientFragment>() {
            @Override
            public void perform(@NonNull ItemDetailIngredientFragment fragment) {
                assertThat(fragment.getArguments().getInt(RECIPE_INDEX,0),equalTo( 1));
            }
        });
    }

    @Test
    public void d_on_FragmentLounch_isTextViewVisible() {
        Bundle args = getBundle(1);
        FragmentScenario<ItemDetailIngredientFragment> fragmentScenario =
                FragmentScenario.launchInContainer(ItemDetailIngredientFragment.class,args);
        onView(withId(R.id.ingredient_textView)).check(matches(isDisplayed()));
    }
    @Test
    public void e_on_FragmentLounch_isButtonVisible() {
        Bundle args = getBundle(1);
        FragmentScenario<ItemDetailIngredientFragment> fragmentScenario =
                FragmentScenario.launchInContainer(ItemDetailIngredientFragment.class,args);
        onView(withId(R.id.configuration)).check(matches(isDisplayed()));
    }

    @NonNull
    private String getString3(){
        String newline = System.getProperty("line.separator");
        return "Bittersweet chocolate (60-70% cacao) G 350.0" +newline+
                "unsalted butter G 226.0" +newline+
                "granulated sugar G 300.0" +newline+
                "light brown sugar G 100.0" +newline+
                "large eggs UNIT 5.0" +newline+
                "vanilla extract TBLSP 1.0" +newline+
                "all purpose flour G 140.0" +newline+
                "cocoa powder G 40.0" +newline+
                "salt TSP 1.5" +newline+
                "semisweet chocolate chips G 350.0"+newline;
    }
}