package com.example.sherif.smssender;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class MainActivity extends AppCompatActivity {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    String smsMessage;
    LocationManager lManager;
    LocationListener listener;
    TextView smsView;
    SharedPreferences pref;
    String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        smsView = (TextView) findViewById(R.id.smsView);
        phoneNumber= getPhoneNumber();

        pref =getSharedPreferences("com.example.sherif.smssender", MODE_PRIVATE);
        smsView.setText("SMS Sender");
       /* if (pref.getBoolean("firstrun", true)) {
            getPhoneNumber();
        }*/

        String smsMessage = readSMS();
        int smsSendRate;

            if(isNumeric(smsMessage)){
                smsSendRate = Integer.parseInt(smsMessage);
                //smsRateTask(smsSendRate);
                findLocation(smsSendRate*1000);
            }
            else{
                stopHandle();
            }


        //Below checks are carried for permissions which need to be granted to allow the application to send and read sms messages

    }
    /*public void authenticationStep() throws IOException {
        System.out.println("ENTERING AUTHENTICATION STEP!!!");
        String sms=readSMS();
        String[]idLine=sms.split(" ");
        String phoneNumber=idLine[2];
        System.out.println(phoneNumber+" !!!");
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File("PhoneNumber.txt")));
        writer.write(phoneNumber);
        System.out.println("Phone number stored as: "+phoneNumber);
        Toast.makeText(this, "User number set to: "+phoneNumber, Toast.LENGTH_SHORT).show();
        pref.edit().putBoolean("firstrun", false).commit();
        resetActivity();
    }*/



    /*public String getPhoneNumber() throws IOException {
        BufferedReader reader= new BufferedReader(new FileReader("PhoneNumber.txt"));
        String phoneNumber=reader.readLine();
        System.out.println(phoneNumber+" !!!");
        return phoneNumber;

    }*/

    public String getPhoneNumber(){
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_SMS)) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_SMS}, 1);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_SMS}, 1);
            }
        } else {

        }

        //StringBuilder smsBuilder = new StringBuilder();
        String phone_number="";
        final String SMS_URI_INBOX = "content://sms/inbox";
        final String SMS_URI_ALL = "content://sms/";



        try {
            Uri uri = Uri.parse(SMS_URI_ALL);
            String[] projection = new String[]{"_id", "address", "person", "body", "date", "type"};
            Cursor cur = getContentResolver().query(uri, projection,null, null, "date desc");


            if (cur.moveToFirst()) {
                int index_Address = cur.getColumnIndex("address");
                int index_Person = cur.getColumnIndex("person");
                int index_Body = cur.getColumnIndex("body");
                int index_Date = cur.getColumnIndex("date");
                int index_Type = cur.getColumnIndex("type");
                do {
                    String strAddress = cur.getString(index_Address);
                    String strbody = cur.getString(index_Body);
                    int intPerson = cur.getInt(index_Person);
                    if(strbody.equals("stop")||strbody.equals("Number authenticated")||isNumeric(strbody)){
                        phone_number = strAddress;
                        break;
                    }

                }
                while (cur.moveToNext());

                if (!cur.isClosed()) {
                    cur.close();
                    cur = null;
                }

            } else {
               // smsBuilder.append("no result!"); do nothing
            } // end if


        } catch (SQLiteException ex) {
            Log.d("SQLiteException", ex.getMessage());
        }/* catch (ParseException e) {
            e.printStackTrace();
        }*/ catch (Exception e) {
            e.printStackTrace();


        }
        Toast.makeText(this, "Phone number set "+phone_number, Toast.LENGTH_SHORT).show();
        System.out.println("SMS: " + phone_number+" !!!");
        return phone_number;
    }

    public boolean isNumeric(String sms){
        boolean numeric=true;
        try{
            Integer.parseInt(sms);
        }
        catch(Exception e){
            numeric=false;
        }
        return numeric;
    }

    /*public String convertPhone(String phoneNumber){
        if(phoneNumber.startsWith("+44")){
            phoneNumber="0"+phoneNumber.substring(3);
        }
        return phoneNumber;
    }*/

   /* @Override
    protected void onStop() {
        super.onStop();                                 //This method will make the app launch even when application closed
        startActivity(getIntent());
    }*/

    /* public void writePhoneNumber(String phone_number){

        try {
            FileOutputStream file = new FileOutputStream(new File(getFilesDir(), "phoneNumber"));
            byte[] numberBytes = phone_number.getBytes();
            file.write(numberBytes);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        /*BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(new File("PhoneNumber.txt")));
            writer.write(phone_number);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Phone number stored as: "+phone_number);*/


   /* public String getPhoneNumber(){
        String phone_number="";
        By

        System.out.println(phone_number+" !!!");
        return phone_number;

    }*/

    public void sendSMS(String[]coordinates) throws IOException {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.SEND_SMS)) { // Check for permissions
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.SEND_SMS}, 1);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.SEND_SMS}, 1);
            }
        } else {

        }
       // String[]coordinates=formatCoordinates(0.0,0.0);
        String number = phoneNumber;//Phone number to be sent to
        String sentMessage = "The vehicle has been located. Find it at: \n" + "Latitude:"+coordinates[0]+"\nLongitude:"+coordinates[1];//Contents of SMS message
        try {
            SmsManager manager = SmsManager.getDefault();
            manager.sendTextMessage(number, null, sentMessage, null, null);
            Toast.makeText(this, "Message sent ", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            //System.out.println("Message could not be sent");
        }

    }

    //Checks for permission before proceeding in the method below
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();//Will display indicator showing that permission is granted
                    }

                } else {
                    Toast.makeText(this, "No permission granted!", Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }


    public String readSMS() {//THIS METHOD IS INVOLVED IN READING SMS MESSAGES, USED IN CASES WHERE THE USER WANTS TO CONTROL THE RATE OF SMS MESSAGES BEING SENT
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_SMS)) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_SMS}, 1);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_SMS}, 1);
            }
        } else {

        }
        StringBuilder smsBuilder = new StringBuilder();
        final String SMS_URI_INBOX = "content://sms/inbox";
        final String SMS_URI_ALL = "content://sms/";



        try {
            Uri uri = Uri.parse(SMS_URI_INBOX);
            String[] projection = new String[]{"_id", "address", "person", "body", "date", "type"};
            Cursor cur = getContentResolver().query(uri, projection,"address= "+"'"+phoneNumber+"'", null, "date desc");


            if (cur.moveToFirst()) {
                int index_Address = cur.getColumnIndex("address");
                int index_Person = cur.getColumnIndex("person");
                int index_Body = cur.getColumnIndex("body");
                int index_Date = cur.getColumnIndex("date");
                int index_Type = cur.getColumnIndex("type");
                do {
                    String strAddress = cur.getString(index_Address);
                    int intPerson = cur.getInt(index_Person);
                    String strbody = cur.getString(index_Body);
                    smsBuilder.append(strbody);
                    break;
                }
                while (cur.moveToNext());

                if (!cur.isClosed()) {
                    cur.close();
                    cur = null;
                }

            } else {
                smsBuilder.append("no result!");
            } // end if

            System.out.println("SMS: " + smsBuilder.toString());
          //  smsView.setText(smsBuilder.toString());
            smsMessage = smsBuilder.toString();
        } catch (SQLiteException ex) {
            Log.d("SQLiteException", ex.getMessage());
        }/* catch (ParseException e) {
            e.printStackTrace();
        }*/ catch (Exception e) {
            e.printStackTrace();


        }
        return smsMessage;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void findLocation(final int smsSendRate) {
        final String []coordinates=new String[2];
        //System.out.println("finding location !!!");
        lManager=(LocationManager)getSystemService(LOCATION_SERVICE);
        listener=new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Double latitude=location.getLatitude();
                Double longitude=location.getLongitude();
                //System.out.println(latitude+" !!! "+longitude);
                coordinates[0]=Double.toString(latitude);
                coordinates[1]=Double.toString(longitude);
                String smsMessage = readSMS();
                if(smsMessage.equals("stop"))
                    resetActivity();
                try {
                    sendSMS(coordinates);
                    Thread t = new Thread() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(smsSendRate);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        resetActivity();  //RESET ACTIVITY
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    t.start();
                   // resetActivity();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

               // System.out.println(latitude+" !!! "+longitude);
            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        lManager.requestLocationUpdates("gps", smsSendRate, 0, listener);//Only call listener every few minutes, rate in second parameter determined by get send rate
    }

 /*   public int smsRateTask(int scheduledTime) {
        System.out.println("RATE CHANGED TO "+scheduledTime+" !!!!");
        int smsTimer = scheduledTime*1000;
        return smsTimer;

    }*/
    public void stopHandle(){
        //System.out.println("stopping sms!!!");
        Toast.makeText(this, "SMS messaging stopped", Toast.LENGTH_SHORT).show();
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(30000);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            resetActivity();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();
    }

    public void resetActivity(){
        finish();
        startActivity(getIntent());

    }
}



