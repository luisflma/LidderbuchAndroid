package lu.acel.lidderbuch;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by luis-fleta on 12/01/16.
 */
public class FileHelper {

    public static void Save(File file, String[] data)
    {
        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(file);
        }
        catch (FileNotFoundException e) {e.printStackTrace();}
        try
        {
            try
            {
                for (int i = 0; i<data.length; i++)
                {
                    fos.write(data[i].getBytes());
                    if (i < data.length-1)
                    {
                        fos.write("\n".getBytes());
                    }
                }
            }
            catch (IOException e) {e.printStackTrace();}
        }
        finally
        {
            try
            {
                fos.close();
            }
            catch (IOException e) {e.printStackTrace();}
        }
    }

    public static void writeToFile(String data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(Settings.SONGS_FILE, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public static String[] Load(File file)
    {
        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream(file);
        }
        catch (FileNotFoundException e) {e.printStackTrace();}
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader br = new BufferedReader(isr);

        String test;
        int anzahl=0;
        try
        {
            while ((test=br.readLine()) != null)
            {
                anzahl++;
            }
        }
        catch (IOException e) {e.printStackTrace();}

        try
        {
            fis.getChannel().position(0);
        }
        catch (IOException e) {e.printStackTrace();}

        String[] array = new String[anzahl];

        String line;
        int i = 0;
        try
        {
            while((line=br.readLine())!=null)
            {
                array[i] = line;
                i++;
            }
        }
        catch (IOException e) {e.printStackTrace();}
        return array;
    }

    public static String readFromFile(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput(Settings.SONGS_FILE);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

//    public void readFile() {
//        File yourFile = new File(Environment.getExternalStorageDirectory(), "path/to/the/file/inside_the_sdcard/textarabics.txt");
//        FileInputStream stream = new FileInputStream(yourFile);
//        String jsonStr = null;
//        try {
//            FileChannel fc = stream.getChannel();
//            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
//
//            jsonStr = Charset.defaultCharset().decode(bb).toString();
//        }
//        finally {
//            stream.close();
//        }
//    }

    public void storeSongs(Context context, JSONObject songsJson) {
        // store songs json
        storeKey(context, "songsJson", songsJson.toString());
    }

    public static void storeKey(Context context, String key, String data) {
        SharedPreferences settings;
        settings = context.getSharedPreferences("LIDDERBUCH", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, data);
        editor.commit();
    }

    public static String getKey(Context context, String key) {
        SharedPreferences settings;
        settings = context.getSharedPreferences("LIDDERBUCH", Context.MODE_PRIVATE);
        return settings.getString(key, null);
    }
}
