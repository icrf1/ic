package com.apex.icrf.classes;

/**
 * Created by WASPVamsi on 05/09/15.
 */

import android.os.Bundle;

public interface IMainVerifiedPetitionsByMeListener {
    void onVerifiedPetitionsByMeItemClicked(Bundle bundle);
    void onLayoutChangedListener();
    void onSearchFocusChanged(boolean hasFocus);
}
