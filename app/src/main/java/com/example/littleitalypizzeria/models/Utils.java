package com.example.littleitalypizzeria.models;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

//Class containing all utility methods
public class Utils {

    //Make first letter of each word in the search string capital
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String formatSearchString(String input) {

        //Split by white spaces into an array
        String[] keywords = input.trim().split("\\s+");

        //Format all words inside array
        for (int i=0; i<keywords.length; i++){
            keywords[i] = keywords[i].substring(0, 1).toUpperCase() + keywords[i].substring(1).toLowerCase();
        }

        /*
        *       Input = HeLLo woRld ->  Output = Hello World
        * */

        //Join words in array with space
        return String.join(" ", keywords);
    }

    //Method to validate emails
    public static boolean isEmailValid(String email) {
        final Pattern EMAIL_REGEX = Pattern.compile("[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?", Pattern.CASE_INSENSITIVE);
        return EMAIL_REGEX.matcher(email).matches();
    }

    //Method to validate contact numbers
    public static boolean isContactNumberValid(String contactNumber) {
        final Pattern EMAIL_REGEX = Pattern.compile("^\\d{10}$");
        return EMAIL_REGEX.matcher(contactNumber).matches();
    }

    //Method to validate if time is a valid pickup time
    public static boolean isPickupTimeValid(int hour, int minute){
        Calendar currentTime = Calendar.getInstance();
        int c_hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int c_minute = currentTime.get(Calendar.MINUTE);

        //Given time in minutes
        int g_mins = hour*60 + minute;

        //Current time in minutes
        int c_mins = c_hour*60 + c_minute;

        if(g_mins-c_mins<30){
            return false;
        }

        return true;
    }

    //Method to validate if time is a valid pickup time
    public static boolean isReservationTimeValid(int hour, int minute){
        Calendar currentTime = Calendar.getInstance();
        int c_hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int c_minute = currentTime.get(Calendar.MINUTE);

        //Given time in minutes
        int g_mins = hour*60 + minute;

        //Current time in minutes
        int c_mins = c_hour*60 + c_minute;

        if(g_mins-c_mins<180){
            return false;
        }

        return true;
    }

    //Get date time string
    public static String getDateTime(){
        String pattern = "MM/dd/yyyy HH:mm:ss";
        DateFormat df = new SimpleDateFormat(pattern);
        Date today = Calendar.getInstance().getTime();
        String todayAsString = df.format(today);
        return todayAsString;
    }

}
