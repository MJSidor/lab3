package com.example.lab3;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class AddSmartphoneData extends AppCompatActivity {

    private EditText brand;
    private EditText version;
    private EditText model;
    private EditText www;
    String operationType;
    long id;
    Provider dbProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_smartphone_data);

        brand = (EditText) findViewById(R.id.editText_brand);
        version = (EditText) findViewById(R.id.editText_version);
        model = (EditText) findViewById(R.id.editText_model);
        www = (EditText) findViewById(R.id.editText_www);

        handleOperationType();


    }

    /**
     * Funkcja obsługująca aktywność zależnie od powodu wywołania -
     * dodanie lub edycja danych BD
     */
    public void handleOperationType() {
        Bundle bundleIn = getIntent().getExtras();
        operationType = bundleIn.getString("operationType");
        if (operationType.contains("insert"))
            getSupportActionBar().setTitle("Add a smartphone to the DB");
        if (operationType.contains("update")) {
            getSupportActionBar().setTitle("Update DB entry");
            id = bundleIn.getLong("id");

            //Ustaw przekazane z poprzedniej aktywności wartości w inputach
            brand.setText(bundleIn.getString("brand"));
            model.setText(bundleIn.getString("model"));
        }
    }

    /**
     * Funkcja obsługująca walidację inputów podczas dodawania/edycji
     * zawartości BD
     *
     * @return
     */
    public boolean validate() {
        if (!isDataOK()) {
            showToast("Enter valid data");
            return false;
        } else return true;
    }

    /**
     * Funkcja logiczna sprawdzająca poprawność wprowadzonych danych
     *
     * @return
     */
    public boolean isDataOK() {
        if (brand.getText().toString().length() >= 2 && version.getText().toString().length() >= 3 && model.getText().toString().length() >= 1) {
            return true;
        } else return false;
    }


    /**
     * Funkcja obsługująca przycisk "Cancel" - zakończenie
     * działania aktywności oraz powrót do aktywności wywołującej
     *
     * @param view
     */
    public void cancel(View view) {
        showToast("Cancelled");
        finish();
    }

    public void save(View view) {
        if (validate()) {

            Bundle bundleOut = new Bundle();
            bundleOut.putString("operationType", operationType);
            if (operationType.startsWith("update")) bundleOut.putLong("id", id);
            Intent intentOut = new Intent();
            intentOut.putExtras(bundleOut);
            setResult(RESULT_OK, intentOut);
            if (operationType.startsWith("insert")) addDBentry();
            if (operationType.startsWith("update")) updateDBentry();
            finish();
        }
    }

    /**
     * Funkcja obsługująca wyświetlanie w przeglądarce
     * wprowadzonego do input'u adresu WWW
     *
     * @param view
     */
    public void www(View view) {
        if (www.getText().toString().contains(".") && www.getText().toString().length() >= 5) {
            Uri webpage;
            if (!www.getText().toString().startsWith("http://"))
                webpage = Uri.parse("http://" + www.getText().toString());
            else webpage = Uri.parse(www.getText().toString());
            Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
            startActivity(webIntent);
        } else showToast("Enter valid address");
    }

    /**
     * Funkcja przyspieszająca wyświetlanie toast'ów
     *
     * @param message
     */
    public void showToast(String message) {
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.show();
    }

    /**
     * Funkcja obsługująca dodanie elementu do BD
     * z użyciem providera i wprowadzonych do input'ów danych
     */
    private void addDBentry() {
        ContentValues wartosci = new ContentValues();
        dbProvider = new Provider();
        EditText brand = (EditText)
                findViewById(R.id.editText_brand);
        EditText model = (EditText)
                findViewById(R.id.editText_model);

        wartosci.put("brand", brand.getText().toString());
        wartosci.put("model", model.getText().toString());
        Uri uriNowego = getContentResolver().insert(dbProvider.URI_ZAWARTOSCI, wartosci);
        finish();
    }

    /**
     * Funkcja obsługująca aktualizację elementu BD
     * z użyciem providera i wprowadzonych do input'ów danych
     */
    private void updateDBentry() {
        ContentValues wartosci = new ContentValues();
        dbProvider = new Provider();
        EditText brand = (EditText)
                findViewById(R.id.editText_brand);
        EditText model = (EditText)
                findViewById(R.id.editText_model);

        wartosci.put("brand", brand.getText().toString());
        wartosci.put("model", model.getText().toString());

        getContentResolver().update(dbProvider.URI_ZAWARTOSCI, wartosci, DBHelper.ID + " = " + Long.toString(id), null);
        finish();
    }


}
