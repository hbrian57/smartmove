package com.centrale.smartmove;

import android.view.View;

import java.util.Map;

/**
 * @author arochut
 *
 */
public interface DisplayHandler {

    /**
     * Method to get all editable objects in layouts that are managed by a DisplayManager.
     * @return an ArrayList of views that are alterable.
     */
    public Map<Integer,View> getEditableObjects();

}
