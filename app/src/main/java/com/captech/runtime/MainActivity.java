package com.captech.runtime;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;

import java.util.ArrayList;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends Activity implements View.OnClickListener {
    private final static int COARSE_LOCATION_RESULT = 100;
    private final static int FINE_LOCATION_RESULT = 101;
    private final static int CALL_PHONE_RESULT = 102;
    private final static int CAMERA_RESULT = 103;
    private final static int READ_CONTACTS_RESULT = 104;
    private final static int WRITE_EXTERNAL_RESULT = 105;
    private final static int RECORD_AUDIO_RESULT = 106;
    private final static int ALL_PERMISSIONS_RESULT = 107;


    private SharedPreferences sharedPreferences;
    private Button btnLocationFine, btnLocationCoarse,
            btnCamera, btnContacts, btnMicrophone,
            btnPhone, btnStorageWrite, btnRequestAll;
    private FrameLayout permissionSuccess;

    private ArrayList<String> permissionsToRequest;
    private ArrayList<String>  permissionsRejected;
    private View coordinatorLayoutView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //for determining if we have asked the questions..
        sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);
        //assign views
        coordinatorLayoutView = findViewById(R.id.snackbarPosition);
        permissionSuccess = (FrameLayout)findViewById(R.id.permissionSuccess);
        btnLocationFine = (Button)findViewById(R.id.btnLocationFine);
        btnLocationCoarse = (Button)findViewById(R.id.btnLocationCoarse);
        btnCamera = (Button)findViewById(R.id.btnCamera);
        btnContacts = (Button)findViewById(R.id.btnContacts);
        btnMicrophone = (Button)findViewById(R.id.btnMicrophone);
        btnPhone = (Button)findViewById(R.id.btnPhone);
        btnStorageWrite = (Button)findViewById(R.id.btnStorageWrite);
        btnRequestAll = (Button)findViewById(R.id.btnRequestAll);
        //set click listeners
        btnLocationFine.setOnClickListener(this);
        btnLocationCoarse.setOnClickListener(this);
        btnCamera.setOnClickListener(this);
        btnContacts.setOnClickListener(this);
        btnMicrophone.setOnClickListener(this);
        btnPhone.setOnClickListener(this);
        btnStorageWrite.setOnClickListener(this);
        btnRequestAll.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        permissionSuccess.setVisibility(View.GONE);
        ArrayList<String> permissions = new ArrayList<>();
        int resultCode = 0;
        switch(v.getId()){
            case R.id.btnLocationFine:
                permissions.add(ACCESS_FINE_LOCATION);
                resultCode = FINE_LOCATION_RESULT;
                break;
            case R.id.btnLocationCoarse:
                permissions.add(ACCESS_COARSE_LOCATION);
                resultCode = COARSE_LOCATION_RESULT;
                break;
            case R.id.btnCamera:
                permissions.add(CAMERA);
                resultCode = CAMERA_RESULT;
                break;
            case R.id.btnContacts:
                permissions.add(READ_CONTACTS);
                resultCode = READ_CONTACTS_RESULT;
                break;
            case R.id.btnMicrophone:
                permissions.add(RECORD_AUDIO);
                resultCode = RECORD_AUDIO_RESULT;
                break;
            case R.id.btnPhone:
                permissions.add(CALL_PHONE);
                resultCode = CALL_PHONE_RESULT;
                break;
            case R.id.btnStorageWrite:
                permissions.add(WRITE_EXTERNAL_STORAGE);
                resultCode = WRITE_EXTERNAL_RESULT;
                break;
            case R.id.btnRequestAll:
                permissions.add(ACCESS_FINE_LOCATION);
                permissions.add(ACCESS_COARSE_LOCATION);
                permissions.add(CAMERA);
                permissions.add(READ_CONTACTS);
                permissions.add(RECORD_AUDIO);
                permissions.add(CALL_PHONE);
                permissions.add(WRITE_EXTERNAL_STORAGE);
                resultCode = ALL_PERMISSIONS_RESULT;
                break;
        }

        //filter out the permissions we have already accepted
        permissionsToRequest = findUnAskedPermissions(permissions);
        //get the permissions we have asked for before but are not granted..
        //we will store this in a global list to access later.
        permissionsRejected = findRejectedPermissions(permissions);

        if(permissionsToRequest.size()>0){//we need to ask for permissions
            //but have we already asked for them?
            requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), resultCode);
            //mark all these as asked..
            for(String perm : permissionsToRequest){
                markAsAsked(perm);
            }
        }else{
            //show the success banner
            if(permissionsRejected.size()<permissions.size()){
                //this means we can show success because some were already accepted.
                permissionSuccess.setVisibility(View.VISIBLE);
            }

            if(permissionsRejected.size()>0){
                //we have none to request but some previously rejected..tell the user.
                //It may be better to show a dialog here in a prod application
                Snackbar
                        .make(coordinatorLayoutView, String.valueOf(permissionsRejected.size()) + " permission(s) were previously rejected", Snackbar.LENGTH_LONG)
                        .setAction("Allow to Ask Again", new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                for(String perm: permissionsRejected){
                                    clearMarkAsAsked(perm);
                                }
                            }
                        })
                        .show();
            }
        }

    }


    /**
     * This is the method that is hit after the user accepts/declines the
     * permission you requested. For the purpose of this example I am showing a "success" header
     * when the user accepts the permission and a snackbar when the user declines it.  In your application
     * you will want to handle the accept/decline in a way that makes sense.
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode){
            case FINE_LOCATION_RESULT:
                if(hasPermission(ACCESS_FINE_LOCATION)){
                    permissionSuccess.setVisibility(View.VISIBLE);
                }else{
                    permissionsRejected.add(ACCESS_FINE_LOCATION);
                    makePostRequestSnack();
                }
                break;
            case COARSE_LOCATION_RESULT:
                if(hasPermission(ACCESS_COARSE_LOCATION)){
                    permissionSuccess.setVisibility(View.VISIBLE);
                }else{
                    permissionsRejected.add(ACCESS_COARSE_LOCATION);
                    makePostRequestSnack();
                }
                break;
            case CALL_PHONE_RESULT:
                if(hasPermission(CALL_PHONE)){
                    permissionSuccess.setVisibility(View.VISIBLE);
                }else{
                    permissionsRejected.add(CALL_PHONE);
                    makePostRequestSnack();
                }
                break;
            case CAMERA_RESULT:
                if(hasPermission(CAMERA)){
                    permissionSuccess.setVisibility(View.VISIBLE);
                }else{
                    permissionsRejected.add(CAMERA);
                    makePostRequestSnack();
                }
                break;
            case READ_CONTACTS_RESULT:
                if(hasPermission(READ_CONTACTS)){
                    permissionSuccess.setVisibility(View.VISIBLE);
                }else{
                    permissionsRejected.add(READ_CONTACTS);
                    makePostRequestSnack();
                }
                break;
            case WRITE_EXTERNAL_RESULT:
                if(hasPermission(WRITE_EXTERNAL_STORAGE)){
                    permissionSuccess.setVisibility(View.VISIBLE);
                }else{
                    permissionsRejected.add(WRITE_EXTERNAL_STORAGE);
                    makePostRequestSnack();
                }
                break;
            case RECORD_AUDIO_RESULT:
                if(hasPermission(RECORD_AUDIO)){
                    permissionSuccess.setVisibility(View.VISIBLE);
                }else{
                    permissionsRejected.add(RECORD_AUDIO);
                    makePostRequestSnack();
                }
                break;
            case ALL_PERMISSIONS_RESULT:
                boolean someAccepted = false;
                boolean someRejected = false;
                for(String perms : permissionsToRequest){
                    if(hasPermission(perms)){
                        someAccepted = true;
                    }else{
                        someRejected = true;
                        permissionsRejected.add(perms);
                    }
                }

                if(permissionsRejected.size()>0){
                    someRejected = true;
                }

                if(someAccepted){
                    permissionSuccess.setVisibility(View.VISIBLE);
                }
                if(someRejected){
                    makePostRequestSnack();
                }
                break;
        }

    }

    /**
     * a method that will centralize the showing of a snackbar
     */
    private void makePostRequestSnack(){
        Snackbar
                .make(coordinatorLayoutView, String.valueOf(permissionsRejected.size()) + " permission(s) were rejected", Snackbar.LENGTH_LONG)
                .setAction("Allow to Ask Again", new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for(String perm: permissionsRejected){
                            clearMarkAsAsked(perm);
                        }
                    }
                })
                .show();
    }

    /**
     * method that will return whether the permission is accepted. By default it is true if the user is using a device below
     * version 23
     * @param permission
     * @return
     */
    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            return(checkSelfPermission(permission)== PackageManager.PERMISSION_GRANTED);
        }
        return true;
    }

    /**
     * method to determine whether we have asked
     * for this permission before.. if we have, we do not want to ask again.
     * They either rejected us or later removed the permission.
     * @param permission
     * @return
     */
    private boolean shouldWeAsk(String permission) {
        return(sharedPreferences.getBoolean(permission, true));
    }

    /**
     * we will save that we have already asked the user
     * @param permission
     */
    private void markAsAsked(String permission) {
        sharedPreferences.edit().putBoolean(permission, false).apply();
    }

    /**
     * We may want to ask the user again at their request.. Let's clear the
     * marked as seen preference for that permission.
     * @param permission
     */
    private void clearMarkAsAsked(String permission) {
        sharedPreferences.edit().putBoolean(permission, true).apply();
    }


    /**
     * This method is used to determine the permissions we do not have accepted yet and ones that we have not already
     * bugged the user about.  This comes in handle when you are asking for multiple permissions at once.
     * @param wanted
     * @return
     */
    private ArrayList<String> findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList<String> result = new ArrayList<String>();

        for (String perm : wanted) {
            if (!hasPermission(perm) && shouldWeAsk(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    /**
     * this will return us all the permissions we have previously asked for but
     * currently do not have permission to use. This may be because they declined us
     * or later revoked our permission. This becomes useful when you want to tell the user
     * what permissions they declined and why they cannot use a feature.
     * @param wanted
     * @return
     */
    private ArrayList<String> findRejectedPermissions(ArrayList<String> wanted) {
        ArrayList<String> result = new ArrayList<String>();

        for (String perm : wanted) {
            if (!hasPermission(perm) && !shouldWeAsk(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    /**
     * Just a check to see if we have marshmallows (version 23)
     * @return
     */
    private boolean canMakeSmores() {
        return(Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP_MR1);
    }

}
