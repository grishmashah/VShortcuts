package com.whoopeelab.vshortcuts;

public class DataModel {
    private String letter; // selected letter for shortcut
    private String showName; // either app load name or contact name
    private String type; // application type or contact type
    private String activityCallable; // either app package name or contact number
    private String imageUri; // uri for image for app icon or contact icon

    public DataModel(String letter, String showName, String type, String activityCallable, String imageUri) {
        this.letter = letter;
        this.showName = showName;
        this.type = type;
        this.activityCallable = activityCallable;
        this.imageUri = imageUri;
    }

    public DataModel(String serialized) {
        String [] param = serialized.split(";");
        if(param.length == 5) {
            this.letter = param[0];
            this.showName = param[1];
            this.type = param[2];
            this.activityCallable = param[3];
            this.imageUri = param[4];
        }
    }

    public String serialize() {
        return letter + ";" + showName + ";" + type + ";" + activityCallable + ";" + imageUri;
    }

    public String getLetter() {
        return letter;
    }

    public String getShowName() {
        return showName;
    }

    public String getType() {
        return type;
    }

    public String getActivityCallable() {
        return activityCallable;
    }

    public void setLetter(String letter) {
        this.letter = letter;
    }

    public void setShowName(String showName) {
        this.showName = showName;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setActivityCallable(String activityCallable) {
        this.activityCallable = activityCallable;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getImageUri() {
        if("null".equals(imageUri))
            return null;
        return imageUri;
    }

    public boolean isApplicationType() {
        return this.getType().equals("application");
    }

    public boolean isContactType() {
        return this.getType().equals("contact");
    }
}