package com.example.android.agendasimple;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.android.agendasimple.sql.ContactEntity;
import com.example.android.agendasimple.sql.DatabaseSQL;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AgendaAdapter.ContactClickListener {

    private RecyclerView rv;
    private AgendaAdapter adapter;
    private FloatingActionButton addContact;
    private SearchView searchWidget;
    public static DatabaseSQL sql;
    private ItemTouchHelper touchHelper;

    // ID que se pasa a ContactOverview para saber si se está modificando el contacto o añadiendo uno nuevo
    public static final String OVERVIEW_MODE = "OVERVIEW_MODE";
    // Número de telefono que se pasa a ContactOverview para saber la PK de contacto con el buscar para cargar los campos del
    // contacto seleccionado
    public static final String NUMBER_OF_CONTACTS = "NUMBER_CONTACT";
    public final int MODE = 0;

    private final String nameJSON = "name";
    private final String numberJSON = "number";
    private final String phoneJSON = "phone";
    private final String addressJSON = "address";
    private final String emailJSON = "email";
    private final String bubbleJSON = "bubble";
    private final String contactsJSON = "contacts";
    private final String infoJSON = "info";

    private ArrayList<ContactEntity> contacts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sql = DatabaseSQL.getInstance(this);

        setRecyclerView();
        setFAB();
    }

    /**
     * onResume: Se cargan todos los contactos con getAllContacts en el RecyclerView, añadiendo
     * los nuevos contactos posibles
     * */
    @Override
    protected void onResume() {
        contacts = sql.getAllContacts();
        adapter.setContactList(contacts);
        super.onResume();
    }

    /**
     * setRecyclerView: Se cargan todos los contactos con getAllContacts en el RecyclerView, y se
     * ancla el itemTouchHelper para la eliminación de contactos al RecyclerView
     * */
    private void setRecyclerView() {
        rv = findViewById(R.id.recycler_view_contacts);
        contacts = sql.getAllContacts();

        adapter = new AgendaAdapter(this, getApplicationContext());
        adapter.setContactList(contacts);

        LinearLayoutManager lm = new LinearLayoutManager(this);
        rv.setLayoutManager(lm);
        rv.setHasFixedSize(false);
        rv.setAdapter(adapter);

        touchHelper = new ItemTouchHelper(new SwipeHandler(this, adapter));
        touchHelper.attachToRecyclerView(rv);
    }

    private void setFAB() {
        addContact = findViewById(R.id.fab_add_contact);
        addContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goTo = new Intent(getApplicationContext(), ContactOverview.class);
                startActivity(goTo);
            }
        });
    }

    /**
     * onContactClicked: Handler para el click de un contacto para pasar a ContactOverview
     * @param num: PHONE_NUMBER del contacto en la posición clickada del RecyclerView
     * */
    @Override
    public void onContactClicked(String num) {
        Intent goTo = new Intent(this, ContactOverview.class);
        goTo.putExtra(OVERVIEW_MODE, MODE);
        goTo.putExtra(NUMBER_OF_CONTACTS, num);
        startActivity(goTo);
    }

    /**
     * onCreateOptionsMenu: Se crea e infla el menu de MainActivity junto con la configuración
     * del widget de búsqueda de contacto searchWidget
     * Cuando se cierra el widget collapsable de búsqueda, se vuelven a cargar todos los contactos
     *
     * @param menu: Variable en la que se guarda el menú creado a partir del xml
     * */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);

        final MenuItem m = menu.findItem(R.id.search_contact);
        m.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                openKeyboard();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                adapter.setContactList(sql.getAllContacts());
                closeKeyboard();
                return true;
            }
        });

        searchWidget = (SearchView) m.getActionView();
        setSearchWidget();
        return true;
    }

    /**
     * setSearchWidget: Se configura el SearchView encargado de la búsqueda de contactos
     * Si no hay texto que parsear, se cargan todos los contactos
     * */
    private void setSearchWidget() {
        searchWidget.setQueryHint(getString(R.string.search_contact_hint));
        searchWidget.setInputType(InputType.TYPE_CLASS_TEXT);
        searchWidget.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        searchWidget.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchWidget.setIconifiedByDefault(false);

        searchWidget.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchWidget.clearFocus();
                adapter.setContactList(sql.getSearchedContacts(query));
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.trim().isEmpty()) {
                    adapter.setContactList(sql.getSearchedContacts(newText));
                } else {
                    adapter.setContactList(sql.getAllContacts());
                }
                return true;
            }
        });
    }

    /**
     * onOptionsItemSelected: Se administran las opciones del menú, en este caso la exportación
     * de contactos a o desde la tarjeta SD de todos los contactos
     *
     * @param item: item seleccionado del menu
     * */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.export_from_contacts:
                importFromSD();
                break;
            case R.id.export_to_contacts:
                exportToSD();
                break;
        }
        return true;
    }

    /**
     * importFromSD: Recoge el PATH al archivo contacts.cnt de la SD del teléfono y se lee el archivo.
     * Además se parsea el String devuelto.
     * */
    private void importFromSD() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), getString(R.string.app_name));
            if (dir.exists()) {
                try {
                    File file = new File(dir, getString(R.string.file_sd));
                    BufferedReader fd = new BufferedReader(
                            new InputStreamReader(new FileInputStream(file)));
                    String jsonObj = fd.readLine();
                    parseJsonAndPopulateRecyclerView(jsonObj);
                    fd.close();
                    Toast.makeText(this, getString(R.string.success_import_to), Toast.LENGTH_SHORT).show();
                } catch (IOException err) {
                    Toast.makeText(this, getString(R.string.import_to_SD), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, getString(R.string.import_to_SD), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, getString(R.string.import_to_SD), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * parseJsonAndPopulateRecyclerView: Parsea el String que contiene el JSON con todos los contactos y
     * popula el RecyclerView con los mismos.
     * Se elimna la tabla de CONTACTS y se popula de nuevo con cada contacto importado.
     * */
    private void parseJsonAndPopulateRecyclerView(String jsonObj) {
        ArrayList<ContactEntity> contacts = new ArrayList<>();

        if (!sql.deleteAllContacts()) {
            Toast.makeText(this, getString(R.string.import_to_SD), Toast.LENGTH_SHORT).show();
        } else {
            try {
                JSONObject json = new JSONObject(jsonObj);
                JSONArray arr = json.getJSONArray(contactsJSON);
                int sizeOfContactsJSON = arr.length();

                for (int x = 0; x < sizeOfContactsJSON; x++) {
                    JSONObject params = arr.getJSONObject(x);
                    String number = params.getString(numberJSON);
                    JSONObject info = params.getJSONObject(infoJSON);
                    String name = info.getString(nameJSON);
                    String phone = info.getString(phoneJSON);
                    String address = info.getString(addressJSON);
                    String email = info.getString(emailJSON);
                    String bubble = info.getString(bubbleJSON);

                    ContactEntity c = new ContactEntity(name, number, phone, address, email, bubble);
                    contacts.add(c);
                    if(!sql.insertContact(c)) {
                        Toast.makeText(this, getString(R.string.import_to_SD), Toast.LENGTH_SHORT).show();
                    }
                }

                adapter.setContactList(contacts);
            } catch (JSONException err) {
                Toast.makeText(this, getString(R.string.import_to_SD), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * exportToSD: Recoge el PATH a la SD del teléfono, se crea un archivo JSON,
     * se crea un directorio "MyAgenda/contacts.cnt" en la SD y se guarda ahí el objeto JSON.
     **/
    private void exportToSD() {
        JSONObject json = buildJSONContacts();

        // External Storage
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), getString(R.string.app_name));
            boolean isDirCreated = dir.mkdir();
            if (dir.exists() || isDirCreated) {
                try {
                    File file = new File(dir, getString(R.string.file_sd));
                    FileWriter fd = new FileWriter(file);
                    fd.write(json.toString());
                    fd.close();
                    Toast.makeText(this, getString(R.string.success_export_to), Toast.LENGTH_SHORT).show();
                } catch (IOException err) {
                    Toast.makeText(this, getString(R.string.export_to_SD), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, getString(R.string.export_to_SD), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, getString(R.string.export_to_SD), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * buildJSONContacts: Crea el objeto JSON a escribir en el fichero de la SD
     * {
     *     "contacts": [
     *          {
     *              "number": getNUMBER,
     *              "info": {
     *                  "name": getNAME,
     *                  "phone": getPHONE,
     *                  "address": getADDRESS,
     *                  "email": getEMAIL,
     *                  "bubble": getBUBBLE
     *              }
     *          },
     *          ...
     *     ]
     * }
     * @return objeto JSON creado con todos los parametros
     */
    private JSONObject buildJSONContacts() {
        JSONObject contactFormatted = new JSONObject();
        JSONArray contactsArray = new JSONArray();

        for (int x = 0; x < contacts.size(); x++) {
            try {
                if (x == 0) {
                    contactFormatted.put(contactsJSON, contactsArray
                            .put(new JSONObject()
                                    .put(numberJSON, contacts.get(x).getPHONE_NUMBER())
                                    .put(infoJSON, new JSONObject()
                                            .put(nameJSON, contacts.get(x).getNAME())
                                            .put(phoneJSON, contacts.get(x).getPHONE())
                                            .put(addressJSON, contacts.get(x).getHOME_ADDRESS())
                                            .put(emailJSON, contacts.get(x).getEMAIL())
                                            .put(bubbleJSON, contacts.get(x).getCOLOR_BUBBLE())
                                    )));
                } else {
                    contactsArray.put(new JSONObject()
                            .put(numberJSON, contacts.get(x).getPHONE_NUMBER())
                            .put(infoJSON, new JSONObject()
                                    .put(nameJSON, contacts.get(x).getNAME())
                                    .put(phoneJSON, contacts.get(x).getPHONE())
                                    .put(addressJSON, contacts.get(x).getHOME_ADDRESS())
                                    .put(emailJSON, contacts.get(x).getEMAIL())
                                    .put(bubbleJSON, contacts.get(x).getCOLOR_BUBBLE())
                            ));
                }
            } catch (JSONException err) {
                Toast.makeText(this, getString(R.string.export_to_SD), Toast.LENGTH_SHORT).show();
            }
        }

        return contactFormatted;
    }

    private void openKeyboard() {
        searchWidget.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    private void closeKeyboard() {
        if (searchWidget != null) {
            searchWidget.clearFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(searchWidget.getWindowToken(), 0);
        }
    }
}
