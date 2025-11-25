package com.app.bemyrider.utils;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.stripe.android.PaymentConfiguration;

/**
 * Created by nct33 on 26/10/17.
 */

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        LocaleManager.setLocale(getApplicationContext(), "it");
        // Our nct test
        /* PaymentConfiguration.init(
                getApplicationContext(),
                "pk_test_51IJCahIKCo79n13atXb6zADvI1wsPhhBNBUHZi90qlxoc1KNCYTjKUEAUqZAPK5k6bQJY7GOWWBAsW2hlYbwRTfZ00onHaC0ZW"
        ); */

        // client test
        PaymentConfiguration.init(
                getApplicationContext(),
                "pk_test_51LSl2XJZvpOu0PgrR5R8dmsO4uMdLVJLeuCfH5MHJJnDz80CPKHscsAsaDnY7jlMkBTyuRQJp3d4TXvQdihQtju500Lsd1sY9r"
        );

        // client live
        /*PaymentConfiguration.init(
                getApplicationContext(),
                "pk_live_51LSl2XJZvpOu0Pgr61B8L0FeCIyOoSM9yveiIIJJH4KuUbPMtcli9zknQ0cSX6ZjkWwOzgwYmgAnTFTssdfgV3uP00fyN2HAdo"
        );*/
    }
}
