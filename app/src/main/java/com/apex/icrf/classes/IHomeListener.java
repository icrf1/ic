package com.apex.icrf.classes;

import android.os.Bundle;

/**
 * Created by WASPVamsi on 11/09/15.
 */
public interface IHomeListener {

    void onPopularButtonClicked(Bundle bundle);
    void onNewButtonClicked(Bundle bundle);
    void onVictoryButtonClicked(Bundle bundle);
    void onMyTotalPointsClicked(Bundle bundle);
    void onPostAPetitionClicked();

    void onNotificationsClicked();
    void onFavouritesClicked();
    void onProfileClicked();
}
