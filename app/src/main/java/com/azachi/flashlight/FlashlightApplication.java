package com.azachi.flashlight;

import android.app.Application;

import io.branch.referral.Branch;

/**
 * Created by Roei.Azachi on 25/04/2017.
 */

public class FlashlightApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Branch.getAutoInstance(this);
    }
}
