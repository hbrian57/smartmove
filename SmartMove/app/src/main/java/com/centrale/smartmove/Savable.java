package com.centrale.smartmove;

import org.json.JSONObject;

public interface Savable {

    /**
     * Method
     * @return JSONObject
     */
    public JSONObject getSaveFormat();
    public void loadFromSave(JSONObject saveFormat);
}
