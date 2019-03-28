package com.scadsdnd.distributives;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import android.R.color;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.text.format.Time;
import android.content.ContentValues;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.database.Cursor;
import android.database.sqlite.*;
import android.graphics.Color;
import android.graphics.PorterDuff;

public class MainActivity extends Activity {

    String Edit1Text;
    TextView label1;
    Button Btn1;
    TableLayout ResTable;
    EditText Edit1;
    Uri URLF;
    Boolean visible;
    AsyncSearchLocal RLocalSearch;
    AsyncUpdtLocal RLocalUpdt;
    InputStream is, isc;
    Context form1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();
        if (id == R.id.exit) {
            finish();
            System.exit(0);
            return true;
        }
        if (id == R.id.import_item) {
            // Yeah, this way is even better, because it also allows access to files in apps like Dropbox.
            // Dropbox is not shown in the list when ACTION_OPEN_DOCUMENT is used.
            // ACTION_OPEN_DOCUMENT - Local only search
            // ACTION_GET_CONTENT - Local and Clouds search
            Intent myIntent = new Intent(Intent.ACTION_GET_CONTENT);
            myIntent.setType("text/*");
            myIntent.addCategory(Intent.CATEGORY_OPENABLE);
            myIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            if (myIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(Intent.createChooser(myIntent, "Slect CSV with fresh DB"), 123);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    // This is a response "Import" menu item click.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123 && resultCode == RESULT_OK) {

            label1 = (TextView) findViewById(R.id.textView1);

            URLF = data.getData();
            try {
                is = getContentResolver().openInputStream(URLF);
                isc = getContentResolver().openInputStream(URLF);
            } catch (FileNotFoundException e) {
                label1.setText("File not found...");
            } catch (IOException e) {
                label1.setText("File inaccessible...");
            }

            Btn1 = (Button) findViewById(R.id.button1);
            Btn1.setEnabled(false);


            label1.setText("Retrieving data from file...");

            form1 = this;

            RLocalUpdt = new AsyncUpdtLocal();
            RLocalUpdt.execute();

            //getContentResolver().openInputStream(URLF)

            //ResTable = (TableLayout) findViewById(R.id.TableView1);
            //visible = false;
            //form1 = this;
        }
    }

    /**
     * Called when the user clicks the "Search" button
     *
     * @throws IOException
     */
    public void ReqData(View view) throws IOException {
        //Click event

        Edit1 = (EditText) findViewById(R.id.editText1);
        Edit1Text = Edit1.getText().toString();

        label1 = (TextView) findViewById(R.id.textView1);
        Btn1 = (Button) findViewById(R.id.button1);

        label1.setText("Searching...");
        Btn1.setEnabled(false);

        ResTable = (TableLayout) findViewById(R.id.TableView1);

        ResTable.removeAllViews();
        form1 = this;

        Toast Notify = Toast.makeText(this, "Searching local db...", Toast.LENGTH_SHORT);
        Notify.setGravity(16, 0, 0);
        Notify.show();

        RLocalSearch = new AsyncSearchLocal();
        RLocalSearch.execute(Edit1Text);

    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }

    class AsyncSearchLocal extends AsyncTask<String, String, String> {

        TextView[] TbItem;
        int ind = 0;
        int fnd = 0;
        int tot = 0;
        int tri = 0;

        @Override
        protected void onPreExecute() {
            label1.setText("Preparing local DB...");
            ResTable.removeAllViews();
            ProgressBar spiner = (ProgressBar) findViewById(R.id.progressBar1);
            spiner.setVisibility(0);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... req) {

            MySQLHelper dbh = new MySQLHelper(form1);
            SQLiteDatabase db = dbh.getReadableDatabase();
            String[] rcols = {dbh.COLS[0], dbh.COLS[1]};
            //Cursor curs = db.query(MySQLHelper.TAB, rcols, "(instr(LOWER(" + dbh.COLS[0] + "),LOWER('" + requst + "'))>0)", null, null, null, null);
            //Cursor curs = db.query(MySQLHelper.TAB, rcols, "(LENGTH(substr(" + dbh.COLS[0] + ", 1, length(" + dbh.COLS[0] + ") - length('" + requst + "') ) )>0)", null, null, null, null);
            Cursor curs = db.query(MySQLHelper.TAB, rcols, null, null, null, null, null);
            curs.moveToFirst();
            TbItem = new TextView[curs.getColumnCount() + 1];
            while (curs.isAfterLast() == false) {

                if (curs.getString(0).toLowerCase().contains(req[0])) {

                    for (int i = 0; i < curs.getColumnCount(); i++) {
                        publishProgress("Saerching data " + Integer.toString(curs.getPosition()) + " of " + Integer.toString(curs.getCount()) + "...", "cell", curs.getString(i));
                    }
                    publishProgress("Saerching data " + Integer.toString(curs.getPosition()) + " of " + Integer.toString(curs.getCount()) + "...", "row");
                    curs.moveToNext();
                    fnd++;

                } else {
                    curs.moveToNext();
                    publishProgress("Saerching data " + Integer.toString(curs.getPosition()) + " of " + Integer.toString(curs.getCount()) + "...", null);
                }
            }
            tot = curs.getCount();
            curs.close();
            dbh.close();
            return null;
        }

        protected void onProgressUpdate(String... vals) {

            label1.setText(vals[0]);


            if (vals[1] == "cell") {
                TbItem[ind] = new TextView(form1);
                TbItem[ind].setText(vals[2]);
                TbItem[ind].setTextSize(2, 13); // public void setTextSize (int unit, float size) // public static final int COMPLEX_UNIT_SP : TYPE_DIMENSION complex unit: Value is a scaled pixel. Constant Value: 2 (0x00000002)
                TbItem[ind].setTextSize(2, 13);
                if (ind % 2 != 0) {
                    TbItem[ind].setPadding(15, 0, 0, 0);
                } else {
                    TbItem[ind].setPadding(3, 0, 0, 0);
                }
                ind++;
            }
            if (vals[1] == "row") {
                TableRow tr1 = new TableRow(form1);
                if (tri % 2 == 0) {
                    //tr1.getBackground().setColorFilter(Color.BLUE, PorterDuff.Mode.MULTIPLY);
                    tr1.setBackgroundColor(Color.argb(50, 51, 181, 229));
                } else {
                    tr1.setBackgroundColor(Color.argb(50, 175, 210, 223));
                }
                tri++;
                for (int i = 0; i < ind; i++) {
                    tr1.addView(TbItem[i]);
                }
                ResTable.addView(tr1);
                ind = 0;
            }


            super.onProgressUpdate(vals);
        }

        @Override
        protected void onPostExecute(String result) {

            ProgressBar spiner = (ProgressBar) findViewById(R.id.progressBar1);
            spiner.setVisibility(4);

            label1.setText("Found " + Integer.toString(fnd) + " from " + Integer.toString(tot) + " items.");
            Btn1.setEnabled(true);

            super.onPostExecute(result);
        }


    }


    //class Request extends AsyncTask<Params, Progress, Result>
    class AsyncUpdtLocal extends AsyncTask<String, String, String> {

        int ind = 0;
        int brcount = 0;
        MySQLHelper dbh;
        SQLiteDatabase db;

        @Override
        protected void onPreExecute() {
            label1.setText("Retriving data from file...");

            try {
                ResTable.removeAllViews();
            } catch (NullPointerException npe) {

            }

            ProgressBar spiner = (ProgressBar) findViewById(R.id.progressBar1);
            spiner.setVisibility(0);


            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... uri) {

            publishProgress("Refreshing DB...", null);
            dbh = new MySQLHelper(form1);
            db = dbh.getWritableDatabase();
            dbh.onUpgrade(db, 0, 0);

            publishProgress("Reading file...", null);

            //File myFile = new File(uri[0]);

            //try {
            //FileInputStream is = new FileInputStream(myFile);
            //--- Suppose you have input stream `is` of your csv file then:
            BufferedReader readerC = new BufferedReader(new InputStreamReader(isc));
            BufferedReader readerD = new BufferedReader(new InputStreamReader(is));

            try {
                String lineD;
                String lineC;

                publishProgress("Fetcing data...", null);
                ContentValues sqldata = new ContentValues();

                while ((lineC = readerC.readLine()) != null) {
                    brcount++;
                }
                readerC.close();


                while ((lineD = readerD.readLine()) != null) {

                    String[] RowData = lineD.split(";");

                    try {
                        String appname = RowData[0];
                        String apploc = RowData[1];
                        // do something with "data" and "value"

                        sqldata.put(dbh.COLS[0], appname);
                        sqldata.put(dbh.COLS[1], apploc);

                        publishProgress("Fetcing data " + Integer.toString(ind) + " of " + Integer.toString(brcount) + "...", null);
                        db.insert(dbh.TAB, null, sqldata);

                        sqldata.clear();
                    } catch (ArrayIndexOutOfBoundsException ae) {
                        publishProgress("[!] This source is corrupted!", null);
                        cancel(false);
                    }

                    ind++;

                }

                readerD.close();
                dbh.close();

            } catch (IOException e) {
                publishProgress("[!] I/O Error!", null);
                cancel(false);
            }
            // } catch (FileNotFoundException ef) {
            // publishProgress("[!] File NOT found!"+" '"+uri[0]+"'", null);
            // cancel(false);
            //}

            if (isCancelled()) return null;

            return null;
        }

        protected void onProgressUpdate(String... vals) {

            label1.setText(vals[0]);
            super.onProgressUpdate(vals);
        }

        @Override
        protected void onPostExecute(String result) {

            ProgressBar spiner = (ProgressBar) findViewById(R.id.progressBar1);
            spiner.setVisibility(4);

            label1.setText("Found " + Integer.toString(ind) + " items");
            Btn1.setEnabled(true);

            super.onPostExecute(result);
        }

        @Override
        protected void onCancelled() {
            ProgressBar spiner = (ProgressBar) findViewById(R.id.progressBar1);
            spiner.setVisibility(4);
            Btn1.setEnabled(true);
            //label2.setText("Switch 'Global' mode!");
            super.onCancelled();
        }

    }


    public class MySQLHelper extends SQLiteOpenHelper {

        public final static String TAB = "distributives";
        public final String[] COLS = {"name", "location"};

        //public MySQLHelper(Context context, String name, CursorFactory factory, int version) {
        public MySQLHelper(Context context) {
            super(context, "distrs.db", null, 1);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // TODO Auto-generated method stub
            db.execSQL("CREATE TABLE " + TAB + " (" + COLS[0] + " TEXT, " + COLS[1] + " TEXT);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // TODO Auto-generated method stub
            db.execSQL("DROP TABLE IF EXISTS " + TAB + ";");
            onCreate(db);
        }

    }
}



