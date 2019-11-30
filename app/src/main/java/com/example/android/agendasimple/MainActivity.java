package com.example.android.agendasimple;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.android.agendasimple.fragments.ContactListFragment;
import com.example.android.agendasimple.fragments.ContentContactFragment;
import com.example.android.agendasimple.sql.ContactEntity;
import com.example.android.agendasimple.sql.DatabaseSQL;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

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
import java.util.Random;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements ContentContactFragment.onUpdateList,
        ContactListFragment.onContactClickedToFragment {

    private FloatingActionButton addContact;
    private SearchView searchWidget;
    public static DatabaseSQL sql;
    private Menu menu;
    private Toast mToast;

    private Toolbar toolbar;
    private DrawerLayout drawer;
    private NavigationView navigation;

    // ID que se pasa a ContactOverview para saber si se está modificando el contacto o añadiendo uno nuevo
    public static final String OVERVIEW_MODE = "OVERVIEW_MODE";
    // Número de telefono que se pasa a ContactOverview para saber la PK de contacto con el buscar para cargar los campos del
    // contacto seleccionado
    public static final String NUMBER_OF_CONTACTS = "NUMBER_CONTACT";
    private boolean getDatesContacts = false;
    private boolean isOnPortraitMode;

    private final String nameJSON = "name";
    private final String numberJSON = "number";
    private final String phoneJSON = "phone";
    private final String addressJSON = "address";
    private final String emailJSON = "email";
    private final String favouriteJSON = "favourite";
    private final String contactsJSON = "contacts";
    private final String infoJSON = "info";

    private final String[] permissions= new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_CONTACTS};
    private final int CODE_WRITE_ES = 0;
    private final int CODE_READ_ES = 1;
    private final int CODE_READ_CONTACT = 2;

    public static String[] colors;

    private ArrayList<ContactEntity> contacts = new ArrayList<>();

    private ContentContactFragment fragContact;
    private ContactListFragment fragList;

    private Pattern namePattern = Pattern.compile("[A-Za-zñáéíóúÑÁÉÍÓÚ A-Za-z()/0-9]+");

    /**********************************************************************************************/

    /******************************** Implementación de interfaces ********************************/

    /**********************************************************************************************/

    /**
     * onUpdateContactToList: Toma de contacto con el fragmento para
     * cuando se hace update de un contacto, notificarselo al adapter para actualizar el RecyclerView
     * */
    @Override
    public void onUpdateContactToList() {
        contacts = sql.getAllContacts();
        fragList.setContactsIntoAdapter(contacts);
    }

    /**
     * passDataToFragment: Función del fragmento lista que llama al fragmento contacto
     * para abrir los datos del contacto seleccionado
     **/
    @Override
    public void passDataToFragment(String num) {
        fragContact.populateFragment(0, num);
    }

    /**********************************************************************************************/

    /****************************** Métodos de Control Sobreescritos ******************************/

    /**********************************************************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        colors = new String[]{
                "#397A74",
                "#E4A15F",
                "#5FCF97",
                "#DF5353",
                "#5383D6",
                "#E0C95E"
        };

        sql = DatabaseSQL.getInstance(this);

        toolbar = findViewById(R.id.toolbar_main);
        drawer = findViewById(R.id.drawer);
        navigation = findViewById(R.id.navigation_menu);

        setAppBarAndNavMenu();
        setFloatingActionButton();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        for (int x = 0; x < fragmentManager.getFragments().size(); x++) {
            transaction.remove(fragmentManager.getFragments().get(x));
        }

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            fragContact = new ContentContactFragment(this, this, "", 1);
            transaction.add(R.id.container_fragment_content_contact, fragContact);

            fragList = new ContactListFragment(this, this);
            transaction.add(R.id.container_fragment_contact_list_land, fragList);

            isOnPortraitMode = false;
            addContact.hide();
        } else {
            fragList = new ContactListFragment(this, this);
            transaction.add(R.id.container_fragment_contact_list, fragList);

            addContact.show();
            isOnPortraitMode = true;
        }
        transaction.commit();
    }

    /**
     * onResume: Se cargan todos los contactos con getAllContacts en el RecyclerView, añadiendo
     * los nuevos contactos posibles
     * */
    @Override
    protected void onResume() {
        contacts = sql.getAllContacts();
        fragList.setContactsIntoAdapter(contacts);
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CODE_WRITE_ES: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    exportToSD();
                }
                break;
            }
            case CODE_READ_ES: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    sql.deleteAllContacts();
                    importFromSD();
                }
                break;
            }
            case CODE_READ_CONTACT: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    sql.deleteAllContacts();
                    importFromContacts();
                }
                break;
            }
        }
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

        if (isOnPortraitMode) {
            menu.findItem(R.id.add_contact).setVisible(false);
        } else {
            menu.findItem(R.id.add_contact).setVisible(true);
        }

        final MenuItem m = menu.findItem(R.id.search_contact);
        m.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                openKeyboard();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                fragList.setContactsIntoAdapter(sql.getAllContacts());
                closeKeyboard();
                return true;
            }
        });

        searchWidget = (SearchView) m.getActionView();
        setSearchWidget();
        this.menu = menu;
        return true;
    }

    /**
     * onOptionsItemSelected: Se administran las opciones del menú, en este caso la exportación
     * de contactos a o desde la tarjeta SD de todos los contactos
     * Se verifica en cada botón si los permisos están correctamente permitidos
     *
     * @param item: item seleccionado del menu
     * */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.get_date_contacts){
            if (getDatesContacts) {
                menu.getItem(1).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_calendar_menu));
                contacts = sql.getAllContacts();
            } else {
                menu.getItem(1).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_contacts));
                contacts = sql.getDatesWithContacts();
            }
            getDatesContacts = !getDatesContacts;
            fragList.setContactsIntoAdapter(contacts);
        } else if (item.getItemId() == R.id.add_contact) {
            fragContact.populateFragment(1, null);
        }
        return true;
    }

    /**********************************************************************************************/

    /********************************** Métodos de Inicialización *********************************/

    /**********************************************************************************************/

    /**
     * checkPermits: Método que verifica si se han permitido los permisos necesarios.
     **/
    private boolean checkPermits(String permission){
        if (Build.VERSION.SDK_INT >= 6) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        } else {
            return false;
        }
        return false;
    }

    public void openNavigationMenu(View v) {
        drawer.openDrawer(GravityCompat.START);
    }

    private void setAppBarAndNavMenu() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        navigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.export_to_contacts:
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (checkPermits(permissions[0])) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{permissions[0]}, CODE_WRITE_ES);
                            } else {
                                exportToSD();
                            }
                        } else {
                            exportToSD();
                        }
                        drawer.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.export_from_contacts:
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (checkPermits(permissions[1])) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{permissions[1]}, CODE_READ_ES);
                            } else {
                                sql.deleteAllContacts();
                                importFromSD();
                            }
                        } else {
                            sql.deleteAllContacts();
                            importFromSD();
                        }
                        drawer.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.export_from_content_provider:
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (checkPermits(permissions[2])) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{permissions[2]}, CODE_READ_CONTACT);
                            } else {
                                sql.deleteAllContacts();
                                importFromContacts();
                            }
                        } else {
                            sql.deleteAllContacts();
                            importFromContacts();
                        }
                        drawer.closeDrawer(GravityCompat.START);
                        break;
                }
                return true;
            }
        });
    }

    private void setFloatingActionButton() {
        addContact = findViewById(R.id.fab_add_contact);
        addContact.setScaleX(0);
        addContact.setScaleY(0);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            final Interpolator interpolator = AnimationUtils.loadInterpolator(getBaseContext(),
                    android.R.interpolator.fast_out_slow_in);
            addContact.animate()
                    .scaleX(1)
                    .scaleY(1)
                    .setInterpolator(interpolator)
                    .setDuration(600);
        }

        addContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goTo = new Intent(getApplicationContext(), ContactOverview.class);
                startActivity(goTo);
            }
        });
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
                fragList.setContactsIntoAdapter(sql.getSearchedContacts(query, getDatesContacts));
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.trim().isEmpty()) {
                    fragList.setContactsIntoAdapter(sql.getSearchedContacts(newText, getDatesContacts));
                } else {
                    if (!getDatesContacts) {
                        fragList.setContactsIntoAdapter(sql.getAllContacts());
                    } else {
                        fragList.setContactsIntoAdapter(sql.getDatesWithContacts());
                    }
                }
                return true;
            }
        });
    }

    /**********************************************************************************************/

    /***************************** Métodos de Importación/Exportación *****************************/

    /**********************************************************************************************/

    /**
     * importFromSD: Recoge el PATH al archivo contacts.cnt de la SD del teléfono y se lee el archivo.
     * Además se parsea el String devuelto.
     * */
    private void importFromSD() {
        if (contacts != null) {
            for (int c = 0; c < this.contacts.size(); c++) {
                if (this.contacts.get(c).getCALENDAR_ID() != 0) {
                    ContentResolver cr = getContentResolver();
                    Uri deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI,
                            this.contacts.get(c).getCALENDAR_ID());
                    cr.delete(deleteUri, null, null);
                }
            }
        }

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
                } catch (IOException err) {
                    makeToast(getString(R.string.import_to_SD));
                }
            } else {
                makeToast(getString(R.string.import_to_SD));
            }
        } else {
            makeToast(getString(R.string.import_to_SD));
        }
    }

    /**
     * parseJsonAndPopulateRecyclerView: Parsea el String que contiene el JSON con todos los contactos y
     * popula el RecyclerView con los mismos.
     * Se elimna la tabla de CONTACTS y se popula de nuevo con cada contacto importado.
     * No se importan las citas.
     * */
    private void parseJsonAndPopulateRecyclerView(String jsonObj) {
        ArrayList<ContactEntity> contacts = new ArrayList<>();
        try {
            JSONObject json = new JSONObject(jsonObj);
            JSONArray arr = json.getJSONArray(contactsJSON);
            int sizeOfContactsJSON = arr.length();
            Random r = new Random();

            for (int x = 0; x < sizeOfContactsJSON; x++) {
                JSONObject params = arr.getJSONObject(x);
                String number = params.getString(numberJSON);
                JSONObject info = params.getJSONObject(infoJSON);
                String name = info.getString(nameJSON);
                String phone = info.getString(phoneJSON);
                String address = info.getString(addressJSON);
                String email = info.getString(emailJSON);
                String bubble = colors[r.nextInt(colors.length)];
                int favourite = info.getInt(favouriteJSON);

                ContactEntity c = new ContactEntity(name, number, phone, address, email, bubble, favourite, getString(R.string.schedule_day), 0);
                contacts.add(c);
                sql.insertContact(c);
            }

            this.contacts = contacts;
            fragList.setContactsIntoAdapter(contacts);
            makeToast(getString(R.string.success_import_to));
        } catch (JSONException err) {
            makeToast(getString(R.string.import_to_SD));
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
                } catch (IOException err) {
                    makeToast(getString(R.string.export_to_SD));
                }
            } else {
                makeToast(getString(R.string.export_to_SD));
            }
        } else {
            makeToast(getString(R.string.export_to_SD));
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
     *                  "favorite": getFAVOURITE
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

        if (contacts != null) {
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
                                                .put(favouriteJSON, contacts.get(x).getFAVOURITE())
                                        )));
                    } else {
                        contactsArray.put(new JSONObject()
                                .put(numberJSON, contacts.get(x).getPHONE_NUMBER())
                                .put(infoJSON, new JSONObject()
                                        .put(nameJSON, contacts.get(x).getNAME())
                                        .put(phoneJSON, contacts.get(x).getPHONE())
                                        .put(addressJSON, contacts.get(x).getHOME_ADDRESS())
                                        .put(emailJSON, contacts.get(x).getEMAIL())
                                        .put(favouriteJSON, contacts.get(x).getFAVOURITE())
                                ));
                    }
                    makeToast(getString(R.string.success_export_to));
                } catch (JSONException err) {
                    makeToast(getString(R.string.export_to_SD));
                }
            }
        } else {
            makeToast(getString(R.string.no_cont_to_export));
        }

        return contactFormatted;
    }

    /**
     * importFromContacts: Método que gracias al contentProvider que ofrece contactos, hace una query
     * para sacar todos los contactos del teléfono y por cada uno de ellos se guarda en el fromato
     * adecuado para su importación a la aplicación de MiAgenda.
     * */
    private void importFromContacts() {
        ArrayList<ContactEntity> contacts = new ArrayList<>();
        int countNotImported = 0;

        if (this.contacts != null) {
            for (int c = 0; c < this.contacts.size(); c++) {
                if (this.contacts.get(c).getCALENDAR_ID() != 0) {
                    ContentResolver cr = getContentResolver();
                    Uri deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI,
                            this.contacts.get(c).getCALENDAR_ID());
                    cr.delete(deleteUri, null, null);
                }
            }
        }

        Cursor cursor = getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI,
                new String[]{
                        ContactsContract.Contacts._ID,
                        ContactsContract.Contacts.HAS_PHONE_NUMBER,
                        ContactsContract.Contacts.DISPLAY_NAME
                },
                null,
                null,
                ContactsContract.Contacts.DISPLAY_NAME + " ASC"
        );

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                Random r = new Random();
                String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String number = "";

                if (hasPhone.equals("1")) {
                    Cursor phones = getContentResolver().query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId,
                            null, null);

                    while (phones.moveToNext()) {
                        number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        if (number.contains(" ")) {
                            number = number.replace(" ", "");
                        }
                    }
                    phones.close();
                }

                ContactEntity c = new ContactEntity(
                        name,
                        number,
                        "",
                        "",
                        "",
                        colors[r.nextInt(colors.length)],
                        1,
                        getString(R.string.schedule_day),
                        0
                );

                if (!name.trim().isEmpty() && namePattern.matcher(name).matches()) {
                    contacts.add(c);
                    try {
                        sql.insertContact(c);
                    } catch (Exception err) {
                        makeToast(getString(R.string.insertion_failed));
                    }
                } else {
                    countNotImported++;
                }
            }
        }

        cursor.close();
        this.contacts = contacts;
        fragList.setContactsIntoAdapter(contacts);
        if (countNotImported > 0) {
            mToast = Toast.makeText(this, countNotImported + " " +
                    getString(R.string.import_dialog_disclaimer), Toast.LENGTH_LONG);
            mToast.show();
        } else {
            makeToast(getString(R.string.success_import_to));
        }
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

    public void makeToast(String msg) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        mToast.show();
    }
}
