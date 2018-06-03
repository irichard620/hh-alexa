package honest.housemate.alexa.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ianrichard on 3/21/18.
 */
public class House {
    @SerializedName("display_name")
    private String displayName;

    @SerializedName("unique_name")
    private String uniqueName;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public void setUniqueName(String uniqueName) {
        this.uniqueName = uniqueName;
    }
}
