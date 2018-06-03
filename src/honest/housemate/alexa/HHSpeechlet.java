/**
    Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.

    Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at

        http://aws.amazon.com/apache2.0/

    or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package honest.housemate.alexa;

import honest.housemate.alexa.model.GetUserResponse;
import honest.housemate.alexa.model.User;
import honest.housemate.alexa.network.HHRequests;
import honest.housemate.alexa.model.ListTodosResponse;
import honest.housemate.alexa.model.ListUsersResponse;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.SpeechletV2;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import com.amazon.speech.ui.OutputSpeech;

public class HHSpeechlet implements SpeechletV2 {

    HHRequests requests = new HHRequests();

    @Override
    public void onSessionStarted(SpeechletRequestEnvelope<SessionStartedRequest> requestEnvelope) {
        System.out.println("onSessionStarted requestId=" + requestEnvelope.getRequest().getRequestId() + ", " +
                        "sessionId=" + requestEnvelope.getSession().getSessionId());
    }

    @Override
    public SpeechletResponse onLaunch(SpeechletRequestEnvelope<LaunchRequest> requestEnvelope) {
        System.out.println("onLaunch requestId=" + requestEnvelope.getRequest().getRequestId() + ", " +
                "sessionId=" + requestEnvelope.getSession().getSessionId());
        return getWelcomeResponse();
    }

    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
        IntentRequest request = requestEnvelope.getRequest();
        System.out.println("onIntent requestId=" + requestEnvelope.getRequest().getRequestId() + ", " +
                "sessionId=" + requestEnvelope.getSession().getSessionId());
        String accessToken = requestEnvelope.getSession().getUser().getAccessToken();
        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;

        if ("AMAZON.HelpIntent".equals(intentName)) {
            return getHelpResponse();
        } else if ("AMAZON.StopIntent".equals(intentName)) {
            return SpeechletResponse.newTellResponse(getPlainTextOutputSpeech("Ok"));
        } else if ("AMAZON.CancelIntent".equals(intentName)) {
            return SpeechletResponse.newTellResponse(getPlainTextOutputSpeech("Ok"));
        }

        // Get user
        GetUserResponse userResponse = requests.getUser(accessToken);
        if (userResponse.code != 200) {
            return SpeechletResponse.newTellResponse(getPlainTextOutputSpeech("Network error occurred. Please try again"));
        }
        if ("CreateTodo".equals(intentName)) {
            return getCreateTodoResponse();
        } else if ("ListTodos".equals(intentName)) {
            return getListTodosResponse(accessToken, userResponse.response.getDefaultHouse());
        } else if ("ListResidents".equals(intentName)) {
            return getListResidentsResponse(accessToken, userResponse.response.getDefaultHouse());
        } else if ("GetAssignee".equals(intentName)) {
            return getAssigneeResponse(accessToken, userResponse.response.getDefaultHouse());
        } else {
            return getAskResponse("Honest Housemate", "This is unsupported.  Please try something else.");
        }
    }

    @Override
    public void onSessionEnded(SpeechletRequestEnvelope<SessionEndedRequest> requestEnvelope) {
        System.out.println("onSessionEnded requestId=" + requestEnvelope.getRequest().getRequestId() + ", " +
                "sessionId=" + requestEnvelope.getSession().getSessionId());
    }

    /**
     * Creates and returns a {@code SpeechletResponse} with a welcome message.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getWelcomeResponse() {
        String speechText = "Welcome to the Honest Housemate skill, you can create and list todos, or hear about residents.";
        return getAskResponse("Honest Housemate", speechText);
    }

    /**
     * Creates a {@code SpeechletResponse} for the CreateTodo intent.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getCreateTodoResponse() {
        String speechText = "Create to-do";

        // Create the Simple card content.
        SimpleCard card = getSimpleCard("Honest Housemate", speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = getPlainTextOutputSpeech(speechText);

        return SpeechletResponse.newTellResponse(speech, card);
    }

    /**
     * Creates a {@code SpeechletResponse} for the ListTodos intent.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getListTodosResponse(String accessToken, String uniqueName) {
        ListTodosResponse todosResponse = requests.listTodos(accessToken, uniqueName);
        if (todosResponse.code != 200) {
            return SpeechletResponse.newTellResponse(getPlainTextOutputSpeech("Network error occurred. Please try again"));
        }
        String speechText = "";
        if (todosResponse.todos.size() == 0) {
            speechText += "There are currently no to-dos for your house";
        } else {
            speechText += "Here's a list of house to-dos ";
            int numToSay = 5;
            if (todosResponse.todos.size() < numToSay) {
                numToSay = todosResponse.todos.size();
            }
            for (int i = 0; i < numToSay; i++) {
                speechText += todosResponse.todos.get(i).getTitle() + ",  ";
            }
        }

        // Create the Simple card content.
        SimpleCard card = getSimpleCard("Honest Housemate", speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = getPlainTextOutputSpeech(speechText);

        return SpeechletResponse.newTellResponse(speech, card);
    }

    /**
     * Creates a {@code SpeechletResponse} for the ListResidents intent.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getListResidentsResponse(String accessToken, String uniqueName) {
        ListUsersResponse usersResponse = requests.listResidents(accessToken, uniqueName);
        if (usersResponse.code != 200) {
            return SpeechletResponse.newTellResponse(getPlainTextOutputSpeech("Network error occurred. Please try again"));
        }
        String speechText = "";
        if (usersResponse.users.size() == 0) {
            speechText += "There are currently no users in your house";
        } else {
            speechText += "Here's a list of residents ";
            for (int i = 0; i < usersResponse.users.size(); i++) {
                speechText += usersResponse.users.get(i).getFullName() + ",  ";
            }
        }

        // Create the Simple card content.
        SimpleCard card = getSimpleCard("Honest Housemate", speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = getPlainTextOutputSpeech(speechText);

        return SpeechletResponse.newTellResponse(speech, card);
    }

    /**
     * Creates a {@code SpeechletResponse} for the GetAssignee intent.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getAssigneeResponse(String accessToken, String uniqueName) {
        ListUsersResponse usersResponse = requests.getAssignee(accessToken, uniqueName);
        if (usersResponse.code != 200) {
            return SpeechletResponse.newTellResponse(getPlainTextOutputSpeech("Network error occurred. Please try again"));
        }
        String speechText = "";
        if (usersResponse.users.size() == 0) {
            speechText += "There are currently no users in your house";
        } else {
            speechText = usersResponse.users.get(0).getFullName() + " has been slacking off the most";
        }

        // Create the Simple card content.
        SimpleCard card = getSimpleCard("Honest Housemate", speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = getPlainTextOutputSpeech(speechText);

        return SpeechletResponse.newTellResponse(speech, card);
    }

    /**
     * Creates a {@code SpeechletResponse} for the help intent.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getHelpResponse() {
        String speechText = "You can create and list todos, hear who lives here, or hear who should do the next todo";
        return getAskResponse("Honest Housemate", speechText);
    }

    /**
     * Helper method that creates a card object.
     * @param title title of the card
     * @param content body of the card
     * @return SimpleCard the display card to be sent along with the voice response.
     */
    private SimpleCard getSimpleCard(String title, String content) {
        SimpleCard card = new SimpleCard();
        card.setTitle(title);
        card.setContent(content);

        return card;
    }

    /**
     * Helper method for retrieving an OutputSpeech object when given a string of TTS.
     * @param speechText the text that should be spoken out to the user.
     * @return an instance of SpeechOutput.
     */
    private PlainTextOutputSpeech getPlainTextOutputSpeech(String speechText) {
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        return speech;
    }

    /**
     * Helper method that returns a reprompt object. This is used in Ask responses where you want
     * the user to be able to respond to your speech.
     * @param outputSpeech The OutputSpeech object that will be said once and repeated if necessary.
     * @return Reprompt instance.
     */
    private Reprompt getReprompt(OutputSpeech outputSpeech) {
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(outputSpeech);

        return reprompt;
    }

    /**
     * Helper method for retrieving an Ask response with a simple card and reprompt included.
     * @param cardTitle Title of the card that you want displayed.
     * @param speechText speech text that will be spoken to the user.
     * @return the resulting card and speech text.
     */
    private SpeechletResponse getAskResponse(String cardTitle, String speechText) {
        SimpleCard card = getSimpleCard(cardTitle, speechText);
        PlainTextOutputSpeech speech = getPlainTextOutputSpeech(speechText);
        Reprompt reprompt = getReprompt(speech);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }
}
