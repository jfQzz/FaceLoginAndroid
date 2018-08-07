/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.fl.exception;

public class FaceError extends Exception{

    public interface ErrorCode{
        // accessToken
        int ACCESS_TOKEN_INVALID_PARAMETER = 100;
        int ACCESS_TOKEN_INVALID_ACCESS_TOKEN = 101;
        int ACCESS_TOKEN_PARSE_ERROR = 110;

        int NETWORK_REQUEST_ERROR = 10000;
        int JSON_PARSE_ERROR = 11000;

        // {"error_msg":"Open api request limit reached","error_code":4}
        int REQUEST_LIMIT_REACHED = 4;

        // common
        int MODULE_CLOSED = 216015;
        int INVALID_PARAMS = 216100;
        int NOT_ENOUGH_PARAMS = 216101;
        int SERVICE_NOT_SUPPORTED = 216102;
        int PARAM_TOO_LONG = 216103;
        int APP_ID_NOT_EXIST = 216110;
        int INVALID_USER_ID = 216111;
        int EMPTY_IMAGE = 216200;
        int INVALID_FORMAT = 216201;
        int INVALID_IMAGE_SIZE = 216202;
        int DATABASE_ERROR = 216300;
        int BACKEND_ERROR = 216400;
        int INTERNAL_ERROR = 216401;
        int UNKNOWN_ERROR = 216500;

        //
        int INVALID_ID_NUMBER_FORMAT = 216600;
        int ID_NUMBER_AND_NAME_MISMATCH = 216601;
        int USER_NOT_EXIST = 216611;
        int USER_NOT_FOUND = 216613;
        int NOT_ENOUGH_IMAGES = 216614;
        int IMAGE_PROCESS_FAILED = 216615;
        int IMAGE_ALREADY_EXIST = 216616;
        int ADD_USER_FAILED = 216617;
        int NO_USER_IN_GROUP = 216618;
        int RECOGNIZE_ERROR = 216630;
        int RECOGNIZE_BANK_CARD_ERROR = 216631;
    }

    public FaceError(int errorCode, String message, Throwable cause) {
        super(message, cause);
        this.cause = cause;
        this.errorCode = errorCode;
    }

    public FaceError(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.errorMessage = message;
    }

    public FaceError(String message){
        super(message);
    }

    public FaceError(){

    }

    public Throwable getCause() {
        return cause;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    private int errorCode;
    private String errorMessage;

    private Throwable cause;

//    @Override
//    public String getMessage() {
//        if(cause != null) {
//
//        }
//        return super.getMessage();
//    }
}
