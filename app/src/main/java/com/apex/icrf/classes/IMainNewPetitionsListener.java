package com.apex.icrf.classes;

import android.os.Bundle;

/**
 * Created by WASPVamsi on 11/09/15.
 */
public interface IMainNewPetitionsListener {

    void onNewItemClicked(Bundle bundle);
    void onLayoutChangedListener();

    void onSearchFocusChanged(boolean hasFocus);
}
