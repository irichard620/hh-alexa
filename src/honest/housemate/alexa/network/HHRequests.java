package honest.housemate.alexa.network;

import honest.housemate.alexa.model.GetUserResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.Gson;

import honest.housemate.alexa.model.ListTodosResponse;
import honest.housemate.alexa.model.ListUsersResponse;

import java.util.concurrent.TimeUnit;

/**
 * Created by ianrichard on 3/21/18.
 */

public class HHRequests {
    private OkHttpClient client;

    private final String baseURL = "https://honesthousemate.herokuapp.com/api/";

    private Response sendGetRequest(String urlExtension, String accessToken, Boolean requiresAuth) {
      Request request;
      if (requiresAuth) {
        request = new Request.Builder()
          .url(baseURL + urlExtension)
          .header("Accept", "application/json")
          .addHeader("Authorization", "Bearer " + accessToken)
          .build();
      } else {
          request = new Request.Builder()
                  .url(baseURL + urlExtension)
                  .header("Accept", "application/json")
                  .build();
      }
      try {
          Response response = getClient().newCall(request).execute();
          return response;
      } catch (Exception e) {
      }
      return null;
    }

    private OkHttpClient getClient() {
      if (client == null) {
        client = new OkHttpClient.Builder()
          .connectTimeout(3, TimeUnit.SECONDS)
          .writeTimeout(3, TimeUnit.SECONDS)
          .readTimeout(3, TimeUnit.SECONDS)
          .build();
      }
      return client;
    }

    public GetUserResponse getUser(String accessToken) {
        String extension = "user/info";
        Response response = sendGetRequest(extension, accessToken, true);
        GetUserResponse userResponse = new Gson().fromJson(response.body().charStream(), GetUserResponse.class);
        userResponse.code = response.code();
        return userResponse;
    }

    public ListTodosResponse listTodos(String accessToken, String uniqueName) {
        String extension = "todos/" + uniqueName + "/incomplete";
        Response response = sendGetRequest(extension, accessToken, true);
        ListTodosResponse todosResponse = new Gson().fromJson(response.body().charStream(), ListTodosResponse.class);
        todosResponse.code = response.code();
        return todosResponse;
    }

    public ListUsersResponse listResidents(String accessToken, String uniqueName) {
      String extension = "house/" + uniqueName + "/users";
      Response response = sendGetRequest(extension, accessToken, true);
      ListUsersResponse residentsResponse = new Gson().fromJson(response.body().charStream(), ListUsersResponse.class);
      residentsResponse.code = response.code();
      return residentsResponse;
    }

    public ListUsersResponse getAssignee(String accessToken, String uniqueName) {
        String extension = "todos/" + uniqueName + "/assignee";
        Response response = sendGetRequest(extension, accessToken, true);
        ListUsersResponse assigneeResponse = new Gson().fromJson(response.body().charStream(), ListUsersResponse.class);
        assigneeResponse.code = response.code();
        return assigneeResponse;
    }
}
