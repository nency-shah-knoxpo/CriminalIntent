package com.example.lenovo.criminalintent;

import android.content.Intent;
import android.support.v4.app.Fragment;

public class CrimeListActivity1 extends SingleFragmentActivity implements CrimeFragment.CallBack,CrimeListFragment.CallBack
{

    @Override
    protected Fragment createFragment() {
        return new CrimeListFragment();
    }

    @Override
    protected int getLayoutResId() {

        return R.layout.activity_master_detail;
    }

    @Override
    public void onCrimeSelected(Crime crime) {
        if (findViewById(R.id.detail_fragement_container) == null) {
            Intent intent = CrimePagerActivity.newIntent(this,crime.getID());
            startActivity(intent);
        } else {
            Fragment newDetail = CrimeFragment.newInstance(crime.getID());
            getSupportFragmentManager().beginTransaction().replace(R.id.detail_fragement_container,newDetail).commit();
    }

    }


    @Override
    public void onCrimeUpdated(Crime crime) {
        CrimeListFragment listFragment = (CrimeListFragment)
                getSupportFragmentManager()
                        .findFragmentById(R.id.fragement_container);
        listFragment.updateUI();
    }
}
