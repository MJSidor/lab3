package com.example.lab3;


import android.content.ContentUris;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.app.LoaderManager;
import android.content.CursorLoader;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.ActionMenuItemView;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements AbsListView.MultiChoiceModeListener, LoaderManager.LoaderCallbacks<Cursor> {

    private DBHelper DBHelper;
    private SQLiteDatabase DB;
    private Cursor kursor;
    private ListView list;
    private SimpleCursorAdapter DBadapter;
    private Provider dbProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DBHelper = new DBHelper(this);
        DB = DBHelper.getWritableDatabase();
        list = (ListView) findViewById(android.R.id.list);

        getSupportActionBar().setTitle("Smartphone database");
        setUpContextualMenu();
        setUpOnClickListener();

        initializeLoader();


    }

    /**
     * Funkcja obsługująca appBar pojawiający się po długim wciśnięciu elementu listView.
     * Obsługuje zliczanie zaznaczonych elementów oraz ich usuwanie
     */
    public void setUpContextualMenu() {
        list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        list.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            int checkedCount = 0;

            private ActionMenuItemView deleteCounter = findViewById(R.id.menu_counter);

            @Override
            public boolean
            onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }



            @Override
            public void
            onDestroyActionMode(ActionMode mode) {
            }

            @Override
            public boolean
            onCreateActionMode(ActionMode mode, Menu menu) { //przy wywołaniu appBar'a
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.contextual_menu, menu);
                checkedCount = 0;
                return true;
            }

            @Override
            public boolean
            onActionItemClicked(ActionMode mode, MenuItem item) { //po wciśnięciu przycisku usuwającego wpisy z BD
                switch (item.getItemId()) {
                    case R.id.deleteSmartphones:
                        deleteSelected();
                        checkedCount = 0;
                        deleteCounter = findViewById(R.id.menu_counter);
                        deleteCounter.setText(Integer.toString(checkedCount));
                        showToast("Deleting selected items...");
                        return true;


                }
                return false;
            }


            @Override
            public void
            onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) { //zaznaczanie lub odznaczanie kolejnych elementów
                if (checked) checkedCount++;
                if (!checked) checkedCount--;
                deleteCounter = findViewById(R.id.menu_counter);
                deleteCounter.setText(Integer.toString(checkedCount));
            }
        });
    }

    /**
     * Funkcja obsługująca krótkie kliknięcie elementu listView -
     * edycja wpisu w bazie danych. Przekazuje do kolejnej aktywności typ wykonywanej operacji
     * (update - w opozycji do insert) oraz id wybranego elementu BD
     */
    public void setUpOnClickListener() {
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), AddSmartphoneData.class);
                intent.putExtra("operationType", "update");
                intent.putExtra("id", id);

                startActivityForResult(intent, new Integer(0));
            }
        });
    }

    /**
     * Funkcja usuwająca zaznaczone w listView elementy z użyciem provider'a
     */
    private void deleteSelected() {
        long checked[] = list.getCheckedItemIds();
        for (int i = 0; i < checked.length; ++i) {
            getContentResolver().delete(ContentUris.withAppendedId(Provider.URI_ZAWARTOSCI, checked[i]), DBHelper.ID + " = " + Long.toString(checked[i]), null);
        }
    }

    /**
     * Funkcja tworząca górny pasek menu na podstawie pliku XML
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    /**
     * Funkcja obsługująca kliknięcie przycisku dodania wpisu do BD
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle menu item selection
        if (item.getItemId() == R.id.addSmartphone) {
            Intent intent = new Intent(this, AddSmartphoneData.class);
            intent.putExtra("operationType", "insert");
            startActivityForResult(intent, new Integer(0));
            return true;
        } else return super.onOptionsItemSelected(item);

    }

    /**
     * Funkcja wywoływana po powrocie z wywołanej zawartości -
     * wyświetla odpowiedniego toast'a w zależności od typu
     * oraz powodzenia wykonanej na BD operacji
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            String operationType = bundle.getString("operationType");
            if (operationType.startsWith("insert")) {
                showToast("New entry added");
            }
            if (operationType.startsWith("update")) {
                showToast("Entry updated");
            }
        }
    }

    /**
     * Funkcja obsługująca zmianę stanu urządzenia - np. obrót
     *
     * @param savedInstanceState
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // save selected items counter
        //savedInstanceState.putInt(STATE_SCORE, currentScore);

        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Funkcja obsługująca zmianę stanu urządzenia - np. obrót
     *
     * @param savedInstanceState
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    /**
     * Funkcja obsługująca zakończenie działania aplikacji - zamyka bazę danych
     */
    @Override
    protected void onDestroy() {
        DB.close();
        super.onDestroy();

    }

    /**
     * Funkcja przyspieszająca wyświetlanie toastów
     */
    public void showToast(String message) {
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {

    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {

    }

    /**
     * Funkcja inicjująca działanie loadera używanego do wyświetlania
     * elementów bazy danych bez potrzeby odświeżania widoku po dokonaniu zmian na BD
     */
    private void initializeLoader() {
        getLoaderManager().initLoader(0,
                null,
                this);

        String[] mapujZ = new String[]{
                DBHelper.COLUMN1, DBHelper.COLUMN2
        };
        int[] mapujDo = new int[]{
                R.id.textView_brand, R.id.textView_model
        };

        DBadapter = new SimpleCursorAdapter(getApplicationContext(), R.layout.list_item, kursor, mapujZ, mapujDo);
        list.setAdapter(DBadapter);
    }

    /**
     * Funkcja wywoływana przy tworzeniu loadera
     *
     * @param id
     * @param args
     * @return
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {DBHelper.ID, DBHelper.COLUMN1, DBHelper.COLUMN2}; // kolumny do wyświetlenia
        CursorLoader cLoader = new CursorLoader(this,
                Provider.URI_ZAWARTOSCI, projection, null, null, null);
        return cLoader;
    }

    /**
     * Funkcja wywoływana przy zakończeniu ładowania danych przez loader'a
     *
     * @param loader
     * @param data
     */
    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor data) {
        //ustawienie danych w adapterze
        DBadapter.swapCursor(data);
    }

    /**
     * Funkcja wywoływana przy restarcie loader'a
     *
     * @param loader
     */
    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {
        DBadapter.swapCursor(null);
    }


}
