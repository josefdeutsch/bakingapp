package com.example.bakingapp;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import com.example.bakingapp.idlingresource.EspressoIdlingResource;
import com.example.bakingapp.ui.ItemListActivity;

import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4.class)
public class ItemListActivityTest {

    @Rule
    public final ActivityTestRule<ItemListActivity> mActivityRule =
            new ActivityTestRule<>(ItemListActivity.class, true, true);

    @Test
    public void a_testSelectedItem_hasCorrectDataSet() throws InterruptedException {
        int defaultPosition = 0;

        IdlingRegistry.getInstance().register(EspressoIdlingResource.getIdlingResource());
        onView(ViewMatchers.withId(R.id.item_list))
                .perform(RecyclerViewActions.actionOnItemAtPosition(defaultPosition, scrollTo()));
        String itemElementText = "Recipe Introduction";
        onView(withText(itemElementText)).check(matches(isDisplayed()));
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.getIdlingResource());

    }
    @Test
    public void b_testSelectedItem_isStepDetailFragmentVisible() throws InterruptedException {
        int defaultPosition = 1;

        IdlingRegistry.getInstance().register(EspressoIdlingResource.getIdlingResource());
        onView(ViewMatchers.withId(R.id.item_list))
                .perform(RecyclerViewActions.actionOnItemAtPosition(defaultPosition, click()));
        onView(withId(R.id.step_textView)).check(matches(isDisplayed()));
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.getIdlingResource());

    }
    @Test
    public void c_testSelectedItem_isIngredientDetailFragmentVisible() throws InterruptedException {
        int defaultPosition = 0;
        IdlingRegistry.getInstance().register(EspressoIdlingResource.getIdlingResource());
        onView(ViewMatchers.withId(R.id.item_list))
                .perform(RecyclerViewActions.actionOnItemAtPosition(defaultPosition, click()));
        onView(withId(R.id.ingredient_textView)).check(matches(isDisplayed()));
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.getIdlingResource());
    }

    @Test
    public void d_isRecyclerViewVisible_onActivityLaunch(){
        try (ActivityScenario<ItemListActivity> scenario = ActivityScenario.launch(ItemListActivity.class)) {
            onView(withId(R.id.item_list)).check(matches(isDisplayed()));
        }
    }
}