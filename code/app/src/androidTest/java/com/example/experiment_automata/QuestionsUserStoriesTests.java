package com.example.experiment_automata;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.experiment_automata.ui.LinkView;
import com.example.experiment_automata.ui.NavigationActivity;
import com.example.experiment_automata.ui.Screen;
import com.robotium.solo.Solo;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Test functionality the deals with
 *  1. us.02.01.01
 *  2. us.02.02.01
 *  3. us.02.03.01
 *
 * Known Issues:
 *  1. Not yet dealing with the owner/experimenter access values
 *
 */
public class QuestionsUserStoriesTests {
    private Solo solo;
    private NavigationActivity currentTestingActivity;

    // The needed views
    private View addExperimentButton;
    private View descriptionEdit;
    private View countTrialsEdit;
    private View location;
    private View acceptNewResults;

    //Needed Objects


    @Rule
    public ActivityTestRule<NavigationActivity> rule =
            new ActivityTestRule<>(NavigationActivity.class, true, true);


    @Before
    public void setup() {
        solo = new Solo(InstrumentationRegistry.getInstrumentation(), rule.getActivity());
        currentTestingActivity = (NavigationActivity) solo.getCurrentActivity();
        //Finding the buttons we need to press
        addExperimentButton = currentTestingActivity.findViewById(R.id.fab_button);
        makeExperiment("GUI Test Experiment");
    }

    private void makeExperiment(String des) {
        //Click from the home screen the + button to make an experiment
        solo.clickOnView(addExperimentButton);
        solo.waitForDialogToOpen();
        descriptionEdit = solo.getView(R.id.create_experiment_description_editText);
        assertNotEquals("Can't find description box", null, descriptionEdit);

        //We need to fill the form to add the experiment
        //Writing description
        solo.clickOnView(descriptionEdit);
        solo.enterText((EditText) descriptionEdit, des);

        //Writing min num trials
        countTrialsEdit = solo.getView(R.id.experiment_min_trials_editText);
        assertNotEquals("Can't find description box", null, countTrialsEdit);
        solo.clickOnView(countTrialsEdit);
        solo.enterText((EditText) countTrialsEdit, "3");

        //Setting the boxes
        location = solo.getView(R.id.experiment_require_location_switch);
        acceptNewResults = solo.getView(R.id.experiment_accept_new_results_switch);
        solo.clickOnView(location);
        if(des != "One")
            solo.clickOnView(acceptNewResults);
        solo.clickOnText("Ok");
    }

    /**
     * Testing user story - us.02.01.01
     * Here we're testing if the we're able to add questions to an experiment
     * and have them be displayed.
     */
    @Test
    public void testingAddingQuestionsExperiment() {
        View questionButton = null;

        int beforeAddingQuestionCount = currentTestingActivity.questionManager.getAllQuestions().size();
        solo.clickOnText("GUI Test Experiment");
        questionButton = solo.getView(R.id.nav_fragment_experiment_detail_view_qa_button);
        solo.clickOnView(questionButton);
        solo.clickOnView(addExperimentButton);
        View questionBox = solo.getView(R.id.frag_add_edit_question_input_box_diolog);
        solo.enterText((EditText)questionBox,"Test Question");
        solo.clickOnText("Ok");

        int afterAddingQuestionCount = currentTestingActivity.questionManager.getAllQuestions().size();
        assertEquals("Question not added make sure model works", false,
                beforeAddingQuestionCount > afterAddingQuestionCount);
    }

    /**
     * testing user story -- us.02.02.01
     * Adding a reply to a question that the user has already entered
     */
    @Test
    public void testingAddingReplies() {
        View replyButton = null;
        View questionButton = null;

        solo.clickOnText("GUI Test Experiment");
        questionButton = solo.getView(R.id.nav_fragment_experiment_detail_view_qa_button);
        solo.clickOnView(questionButton);
        solo.clickOnView(addExperimentButton);
        View questionBox = solo.getView(R.id.frag_add_edit_question_input_box_diolog);
        solo.enterText((EditText)questionBox,"Test Question");
        solo.clickOnText("Ok");

        replyButton = solo.getView(R.id.main_question_display_reply_button);
        solo.clickOnView(replyButton);
        solo.enterText((EditText)questionBox,"Test Question");
        solo.clickOnText("Ok");

        Object[] x = (currentTestingActivity.questionManager.getAllQuestions()).toArray();
        int size = ((ArrayList)(x[0])).size();

        assertEquals("Failed tp add reply to question", 1, size);
    }

    /**
     * Testing us.02.03.01
     * Testing if the user can views the questions and replies that have been given.
     * This test is working under the assumption that the user can only reply to the question
     * once and each question can have more replies.
     *
     * For part 4: This will change to allow the nesting of questions such that a question can
     * have more than one reply.
     */
    @Test
    public void testingUserCanSeeQuestionAndReplies() {
        View replyButton = null;
        View questionButton = null;

        solo.clickOnText("GUI Test Experiment");
        questionButton = solo.getView(R.id.nav_fragment_experiment_detail_view_qa_button);
        solo.clickOnView(questionButton);
        solo.clickOnView(addExperimentButton);
        View questionBox = solo.getView(R.id.frag_add_edit_question_input_box_diolog);
        solo.enterText((EditText)questionBox,"Test Question");
        solo.clickOnText("Ok");

        replyButton = solo.getView(R.id.main_question_display_reply_button);
        solo.clickOnView(replyButton);
        questionBox = solo.getView(R.id.frag_add_edit_question_input_box_diolog);
        solo.enterText((EditText)questionBox,"Test reply");
        solo.clickOnText("Ok");

        boolean replyQuestionOnScreen = solo.searchText("Test Question") && solo.searchText("Test reply");
        assertEquals("Failed to display the questions and reply onto the screen",
                true,
                replyQuestionOnScreen);
    }

    /**
     * Testing if the username of the question post links to the user profile.
     */
//    @Test
//    public void testingQuestionLinksToProfile() {
//        View replyButton = null;
//        View questionButton = null;
//
//        solo.clickOnText("GUI Test Experiment");
//        questionButton = solo.getView(R.id.nav_fragment_experiment_detail_view_qa_button);
//        solo.clickOnView(questionButton);
//        solo.clickOnView(addExperimentButton);
//        View questionBox = solo.getView(R.id.frag_add_edit_question_input_box_diolog);
//        solo.enterText((EditText)questionBox,"Test Question");
//        solo.clickOnText("Ok");
//
//        replyButton = solo.getView(R.id.main_question_display_reply_button);
//        solo.clickOnView(replyButton);
//        questionBox = solo.getView(R.id.frag_add_edit_question_input_box_diolog);
//        solo.enterText((EditText)questionBox,"Test reply");
//        solo.clickOnText("Ok");
//
//        // check linking to profile
//        View questionUsername = solo.getView(R.id.main_question_display_user);
//        solo.clickOnView(questionUsername);
//        solo.waitForFragmentById(R.id.profile_screen);
//        assertEquals(Screen.Profile, currentTestingActivity.getCurrentScreen());
//        assertEquals(solo.getString(R.string.profile_contact),
//                ((TextView) solo.getView(R.id.profile_contact)).getText().toString());
//    }

    /**
     * Testing if the username of the reply post links to the user profile.
     */
    @Test
    public void testingReplyLinksToProfile() {
        View replyButton = null;
        View questionButton = null;

        solo.clickOnText("GUI Test Experiment");
        questionButton = solo.getView(R.id.nav_fragment_experiment_detail_view_qa_button);
        solo.clickOnView(questionButton);
        solo.clickOnView(addExperimentButton);
        View questionBox = solo.getView(R.id.frag_add_edit_question_input_box_diolog);
        solo.enterText((EditText)questionBox,"Test Question");
        solo.clickOnText("Ok");

        replyButton = solo.getView(R.id.main_question_display_reply_button);
        solo.clickOnView(replyButton);
        questionBox = solo.getView(R.id.frag_add_edit_question_input_box_diolog);
        solo.enterText((EditText)questionBox,"Test reply");
        solo.clickOnText("Ok");

        // check linking to profile
        LinkView replyUsername = (LinkView) solo.clickInList(0).get(0);
        solo.clickOnView(replyUsername);
        solo.waitForFragmentById(R.id.profile_screen);
        assertEquals(Screen.Profile, currentTestingActivity.getCurrentScreen());
        assertEquals(solo.getString(R.string.profile_contact),
                ((TextView) solo.getView(R.id.profile_contact)).getText().toString());
    }
}