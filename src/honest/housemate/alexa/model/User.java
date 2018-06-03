package honest.housemate.alexa.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ianrichard on 3/21/18.
 */
public class User {
    // Id of user
    private String id;

    // name of user - serialize
    @SerializedName("full_name")
    private String fullName;

    // Default house - serialize
    @SerializedName("default_house")
    private String defaultHouse;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDefaultHouse() {
        return defaultHouse;
    }

    public void setDefaultHouse(String defaultHouse) {
        this.defaultHouse = defaultHouse;
    }
}
