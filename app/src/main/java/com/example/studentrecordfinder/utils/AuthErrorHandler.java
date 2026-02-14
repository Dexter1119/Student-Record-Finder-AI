package com.example.studentrecordfinder.utils;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.*;

public class AuthErrorHandler {

    public static String getMessage(Exception e) {

        if (e instanceof FirebaseAuthInvalidUserException) {
            return "Account does not exist.";
        }
        if (e instanceof FirebaseAuthInvalidCredentialsException) {
            return "Incorrect email or password.";
        }
        if (e instanceof FirebaseAuthUserCollisionException) {
            return "Email already registered.";
        }
        if (e instanceof FirebaseNetworkException) {
            return "No internet connection.";
        }

        return "Something went wrong. Please try again.";
    }
}
