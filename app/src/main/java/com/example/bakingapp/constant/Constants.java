package com.example.bakingapp.constant;


public class Constants {


    /** pointer to actual json */

    public static final String BAKINGRECIPEBASEURL = "https://d17h27t6h515a5.cloudfront.net/";

    /**
     * @Constant recipe_name and recipe_index, name and object of current object processed.
     *
     * one Object contains of :index,name,array<steps>,array<ingredient>
     */

    public static final String RECIPE_NAME = "recipe_name";
    public static final String RECIPE_INDEX = "recipe_index";

    /** pointer to current step index processed. */

    public static final String STEP_INDEX = "step_index";

    /** CallBackWorkers requestids of current worker processed */

    public static final String WORKREQUEST_MAIN = "main_request";
    public static final String WORKREQUEST_ITEMLIST = "itemlist_request";
    public static final String WORKREQUEST_STEPFRAGMENT = "step_request";
    public static final String WORKREQUEST_INGREDIENTFRAGMENT = "main_request";

    /** Arguments of Bundle passed to fragments **/

    public static final String ARG_ITEM_ID = "item_id";
    public static final String ARG_ITEM_LAYOUT = "item_layout";
    public static final String ARG_ITEM_AMOUNTOFSTEPS = "item_amount";

    /** exoPlayer mediaFrame utility **/

    public static final String STATE_RESUME_WINDOW = "resumeWindow";
    public static final String STATE_RESUME_POSITION = "resumePosition";
    public static final String STATE_PLAYER_FULLSCREEN = "playerFullscreen";

    /** current index of stepFragment used to add ++; if onNext is pressed **/

    public static final String CURRENT_INDEX = "current_index";

    /** the total amount of steps in array<steps> **/

    public static final String NUMBER_AMOUNTOFSTEPS = "amount_of_steps";

    /** one key task of CallbackWorker, tag current data Object to be passed **/

    public static final String KEY_TASK_OUTPUT ="key_task_output";

    /** @AppWidgetConfig text tag, tag current data to be saved **/

    public static final String KEY_BUTTON_TEXT = "keyButtonText";

    /** an editor to savely deposit values **/

    public static final String SHAREDPREFERENCES_EDITOR = "sharedpreferences_editor";

    /** @IngredientFragment text tag, tag current data to be saved**/

    public static final String SHOPPINGLIST_TAG = "shoppinglist_tag";

}
