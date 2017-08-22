package com.apex.icrf.classes;

/**
 * Created by WASPVamsi on 05/09/15.
 */

import android.os.Bundle;

public interface IMainAllPetitionsListener {

    void onViewButtonClicked(Bundle bundle);
    void onLayoutChangedListener();
    void onSearchFocusChanged(boolean hasFocus);
}
