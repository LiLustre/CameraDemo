package com.zmy.camerademo.permission;

import android.Manifest;

/**
 * Created by lz on 2018/8/13.
 */
public class PermissionGroup {

    public static final int CAMERA_REQUEST_CODE = 1;
    public static final int PHOTO_REQUEST_CODE = 0;
    public static final int LOCATION_REQUES_CODE=2;
    public static final String[] PHOTO_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    public static final String[] LOCATION_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    public static final String[] CAMERA_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /*表 1. 危险权限和权限组

    权限组	权限
    CALENDAR
            READ_CALENDAR
            WRITE_CALENDAR
    CAMERA
            CAMERA

    CONTACTS
            READ_CONTACTS
            WRITE_CONTACTS
            GET_ACCOUNTS

    LOCATION
            ACCESS_FINE_LOCATION
            ACCESS_COARSE_LOCATION

    MICROPHONE
            RECORD_AUDIO

    PHONE
            READ_PHONE_STATE
            CALL_PHONE
            READ_CALL_LOG
            WRITE_CALL_LOG
            ADD_VOICEMAIL
            USE_SIP
            PROCESS_OUTGOING_CALLS

    SENSORS
            BODY_SENSORS

    SMS
            SEND_SMS
            RECEIVE_SMS
            READ_SMS
            RECEIVE_WAP_PUSH
            RECEIVE_MMS

    STORAGE
            READ_EXTERNAL_STORAGE
            WRITE_EXTERNAL_STORAGE*/
}
