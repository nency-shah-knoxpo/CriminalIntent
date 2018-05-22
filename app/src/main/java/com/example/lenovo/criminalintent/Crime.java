package com.example.lenovo.criminalintent;

import java.util.Date;
import java.util.UUID;

/**
 * Created by LENOVO on 4/4/2018.
 */
public class Crime {

    private UUID mID;
    private String mTitle;
    private Date mDate;
    private boolean mSolved;

    public Crime()

    {
        mID = UUID.randomUUID();
        mDate = new Date();
    }

    //Getters and setters...

    public UUID getID() {
        return mID;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }


    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    public boolean isSolved() {
        return mSolved;
    }

    public void setSolved(boolean solved) {
        mSolved = solved;
    }
}
