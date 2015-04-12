package sg.edu.nus.helper.persistence;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import sg.edu.nus.POJOs.MidiPOJO;
import sg.edu.nus.helper.Constant;

public class PersistenceHelper {

    public static List<MidiPOJO> getMidiList(Context context) {
        SharedPreferences midiPreferences = context.getSharedPreferences(Constant.MIDI_PREFS_NAME,
                Context.MODE_PRIVATE);
        String serializedDataFromPreferences = midiPreferences.getString(Constant.MIDI_PREFS_KEY, null);
        if (serializedDataFromPreferences == null) {
            System.out.print("Cannot find the midi list. System will return an empty list");
            return new ArrayList<MidiPOJO>();
        }
        Type midiListType = new TypeToken<List<MidiPOJO>>(){}.getType();
        List<MidiPOJO> midiList = new Gson().fromJson(serializedDataFromPreferences, midiListType);
        if (midiList == null) {
            throw new NullPointerException("The persistence data is not in correct format");
        }
        return midiList;
    }

    public static void saveMidiList(Context context, List<MidiPOJO> midiList) {
        SharedPreferences midiPreferences = context.getSharedPreferences(Constant.MIDI_PREFS_NAME,
                Context.MODE_PRIVATE);
        String json = new Gson().toJson(midiList);
        midiPreferences.edit().putString(Constant.MIDI_PREFS_KEY, json).apply();
    }

    public static String getFacebookUserId(Context context) {
        SharedPreferences facebookPreferences = context.getSharedPreferences(Constant.FACEBOOK_PREFS_NAME,
                Context.MODE_PRIVATE);
        String facebookUserId = facebookPreferences.getString(Constant.FACEBOOK_PREFS_USER_ID, null);
        if (facebookUserId == null) {
            throw new NullPointerException("Facebook User ID does not exist");
        }
        return facebookUserId;
    }

    public static void saveFacebookUserId(Context context, String facebookUserId) {
        SharedPreferences facebookPreferences = context.getSharedPreferences(Constant.FACEBOOK_PREFS_NAME,
                Context.MODE_PRIVATE);
        facebookPreferences.edit().putString(Constant.FACEBOOK_PREFS_USER_ID, facebookUserId).apply();
    }

    public static String getFacebookUserName(Context context) {
        SharedPreferences facebookPreferences = context.getSharedPreferences(Constant.FACEBOOK_PREFS_NAME,
                Context.MODE_PRIVATE);
        String facebookUserId = facebookPreferences.getString(Constant.FACEBOOK_PREFS_USER_NAME, null);
        if (facebookUserId == null) {
            throw new NullPointerException("Facebook User Name does not exist");
        }
        return facebookUserId;
    }

    public static void saveFacebookUserName(Context context, String facebookUserName) {
        SharedPreferences facebookPreferences = context.getSharedPreferences(Constant.FACEBOOK_PREFS_NAME,
                Context.MODE_PRIVATE);
        facebookPreferences.edit().putString(Constant.FACEBOOK_PREFS_USER_NAME, facebookUserName).apply();
    }

    public static String getFacebookToken(Context context) {
        SharedPreferences facebookPreferences = context.getSharedPreferences(Constant.FACEBOOK_PREFS_NAME,
                Context.MODE_PRIVATE);
        return facebookPreferences.getString(Constant.FACEBOOK_PREFS_TOKEN, null);
    }

    public static void saveFacebookToken(Context context, String facebookToken) {
        SharedPreferences facebookPreferences = context.getSharedPreferences(Constant.FACEBOOK_PREFS_NAME,
                Context.MODE_PRIVATE);
        facebookPreferences.edit().putString(Constant.FACEBOOK_PREFS_TOKEN, facebookToken).apply();
    }

    public static void saveImage(String imageName, Bitmap finalBitmap) {

        String filePath = Constant.BASE_FILE_DIR + imageName + ".jpg";
        File file = new File (filePath);
        if (file.exists()) {
            file.delete ();
        }

        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
