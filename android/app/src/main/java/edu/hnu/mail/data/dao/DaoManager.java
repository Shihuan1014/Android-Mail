package edu.hnu.mail.data.dao;

import android.content.Context;

public class DaoManager {

    private static DaoManager daoManager;
    private DaoSession daoSession;

    public DaoManager(Context context) {
        daoSession = DaoMaster.newDevSession(context,"mail.db");
    }

    public static DaoManager getInstance(Context context){
        if (daoManager == null){
            synchronized (DaoManager.class){
                if (null ==daoManager){
                    daoManager = new DaoManager(context);
                }
            }
        }
        return daoManager;
    }

    public DaoSession getDaoSession(){
        return daoSession;
    }
}
