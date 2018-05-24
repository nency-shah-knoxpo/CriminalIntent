package com.example.lenovo.criminalintent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.util.Date;
import java.util.UUID;


public class CrimeFragment extends Fragment {

    private Crime mCrime;
    private EditText mTitleField;
    private Button mDateButton;
    private CheckBox mSolvedCheckBox;
    private static final String DIALOG_DATE = "DialogDate";
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_CONTACT = 1;
    private static final int REQUEST_PHOTO= 2;
    private UUID crimeId;
    private Button mSendReportBtn,mSuspectBtn;
    private ImageButton mPhotoIB;
    private ImageView mPhotoIV;
    private File mPhotoFile;


    private  CallBack mCallBacks;
    public interface CallBack{
        void onCrimeUpdated(Crime crime);
    }

    private void updateCrime() {
        CrimeLab.get(getActivity()).updateCrime(mCrime);
        mCallBacks.onCrimeUpdated(mCrime);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallBacks =(CallBack)getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallBacks=null;
    }
private void updatePhotoView(){

    if (mPhotoFile == null || !mPhotoFile.exists()) {
        mPhotoIV.setImageDrawable(null);
    } else {
        Bitmap bitmap = PictureUtil.getScaledBitmap(
                mPhotoFile.getPath(), getActivity());
       mPhotoIV.setImageBitmap(bitmap);
    }
}

    public static CrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable("Crime_ID", crimeId);
        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onPause() {
        super.onPause();
        CrimeLab.get(getActivity()).updateCrime(mCrime);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        crimeId = (UUID) getArguments().getSerializable("Crime_ID");
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_DATE) {
            Date date = (Date) data
                    .getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            updateCrime();
            updateDate();
/*
            mDateButton.setText(mCrime.getDate().toString());
*/
        }

        else if(requestCode == REQUEST_CONTACT && data != null){
            Uri contactUri = data.getData();
            String[] queryFields = new String[]{ContactsContract.Contacts.DISPLAY_NAME};
            Cursor c=getActivity().getContentResolver().query(contactUri,queryFields,null,null,null);
            try{
                if(c.getCount() == 0){

                    return;
                }
                c.moveToFirst();
                String suspect = c.getString(0);
                mCrime.setSuspect(suspect);
                updateCrime();
                mSuspectBtn.setText(suspect);
            }
            finally{

                c.close();
            }
        }
        else if(requestCode == REQUEST_PHOTO ){
            updateCrime();
            updatePhotoView();
        }
    }

    private void updateDate() {
        mDateButton.setText(mCrime.getDate().toString());
    }

    private String getCrimeReport(){
        String solvedString = null;
        if(mCrime.isSolved()){
            solvedString = getString(R.string.crime_report_solved);
            }
            else{
            solvedString = getString(R.string.crime_report_not_solved);
        }
        String dateFormat = "EEE, MMM dd";
        String DateString = String.format(dateFormat, mCrime.getDate()).toString();
        String suspect = mCrime.getSuspect();
        if(suspect==null){
            suspect = getString(R.string.crime_report_no_suspect);

        }
        else {
            suspect=getString(R.string.crime_report_suspect , suspect);
        }
        String report = getString(R.string.crime_report,mCrime.getTitle(),suspect,solvedString);
        return report;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frgement_crime, container, false);
      final  Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);

        PackageManager packageManager = getActivity().getPackageManager();
        if(packageManager.resolveActivity(pickContact,packageManager.MATCH_DEFAULT_ONLY)==null){
            mSuspectBtn.setEnabled(false);
        }
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);

        mTitleField = (EditText) v.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());

        mDateButton = (Button) v.findViewById(R.id.crime_date);
mDateButton.setText("select date");
        updateDate();
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment
                        .newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                dialog.show(manager, DIALOG_DATE);

            }
        });
        mSolvedCheckBox = (CheckBox) v.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mCrime.setSolved(b);
                updateCrime();
            }
        });

        mSendReportBtn = (Button)v.findViewById(R.id.btn_crime_report);
        mSendReportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT,getCrimeReport());
                i.putExtra(Intent.EXTRA_SUBJECT,getString(R.string.crime_report_subject));
                i.createChooser(i,getString(R.string.send_report));
                startActivity(i);
            }
        });

        mSuspectBtn = (Button)v.findViewById(R.id.btn_crime_suspect);
        mSuspectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(pickContact,REQUEST_CONTACT);
            }
        });

        if(mCrime.getSuspect()!=null){
            mSuspectBtn.setText(mCrime.getSuspect());
        }



        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
// This space intentionally left blank
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
                updateCrime();
            }

            @Override
            public void afterTextChanged(Editable s) {
// This one too
            }
        });


        //Implicit intent for clicking picture using camera
        mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);
        mPhotoIB = (ImageButton)v.findViewById(R.id.btn_crime_photo);
        mPhotoIV = (ImageView)v.findViewById(R.id.img_view_crime);
        updatePhotoView();

        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        boolean canTakePhoto = mPhotoFile != null &&
                captureImage.resolveActivity(packageManager) != null;
        mPhotoIB.setEnabled(canTakePhoto);
        if (canTakePhoto) {
            Uri uri = Uri.fromFile(mPhotoFile);
            captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        }
        mPhotoIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(captureImage, REQUEST_PHOTO);
            }
        });


        return v;
    }
}
