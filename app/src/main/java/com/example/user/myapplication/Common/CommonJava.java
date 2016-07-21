package com.example.user.myapplication.Common;

import android.util.Log;

/**
 * Created by USER on 2016-07-15.
 * 공용 함수 집합
 */
public class CommonJava {
    public static class Loging {
        public static Boolean logingCheck = false;


        public static void i(String className, String strContent) {
            if (logingCheck) {
                Log.i(className, strContent);
            }
        }


        public static void d(String className, String strContent) {
            if (logingCheck) {
                Log.d(className, strContent);
            }
        }


        public static void e(String className, String strContent) {
            if (logingCheck) {
                Log.e(className, strContent);
            }
        }

        public static void w(String className, String strContent) {
            if (logingCheck) {
                Log.w(className, strContent);
            }
        }
    }

}
