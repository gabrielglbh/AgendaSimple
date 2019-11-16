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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.agendasimple.fragments.ContentContactFragment;
import com.example.android.agendasimple.sql.ContactEntity;
import com.example.android.agendasimple.sql.DatabaseSQL;
import com.example.android.agendasimple.utils.SwipeHandler;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
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

public class MainActivity extends AppCompatActivity implements AgendaAdapter.ContactClickListener {

    private RecyclerView rv;
    private AgendaAdapter adapter;
    private FloatingActionButton addContact;
    private SearchView searchWidget;
    public static DatabaseSQL sql;
    private ItemTouchHelper touchHelper;
    private Menu menu;
    private BottomSheetDialog dialog;

    private Toolbar toolbar;
    private DrawerLayout drawer;
    private NavigationView navigation;

    // ID que se pasa a ContactOverview para saber si se está modificando el contacto o añadiendo uno nuevo
    public static final String OVERVIEW_MODE = "OVERVIEW_MODE";
    // Número de telefono que se pasa a ContactOverview para saber la PK de contacto con el buscar para cargar los campos del
    // contacto seleccionado
    public static final String NUMBER_OF_CONTACTS = "NUMBER_CONTACT";
    public final int MODE = 0;
    private boolean fromFAB = false;
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
        setRecyclerView();
        setFloatingActionButton();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            fragContact = new ContentContactFragment(this, "", 1);
            transaction.add(R.id.container_fragment_content_contact, fragContact);
            transaction.commit();
            isOnPortraitMode = false;
        } else {
            isOnPortraitMode = true;
        }
    }

    /**
     * onResume: Se cargan todos los contactos con getAllContacts en el RecyclerView, añadiendo
     * los nuevos contactos posibles
     * */
    // TODO: notifyDataSetChanged()
    @Override
    protected void onResume() {
        contacts = sql.getAllContacts();
        adapter.setContactList(contacts);
        fromFAB = false;
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
                    if (fromFAB) {
                        Intent goTo = new Intent(getApplicationContext(), ContactOverview.class);
                        startActivity(goTo);
                    }
                    else exportToSD();
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
     * onContactClicked: Handler para el click de un contacto para pasar a ContactOverview
     * @param num: PHONE_NUMBER del contacto en la posición clickada del RecyclerView
     * */
    @Override
    public void onContactClicked(String num) {
        // TODO: Evento para que el fragmento ponga los campos con mode 0
        Intent goTo = new Intent(this, ContactOverview.class);
        goTo.putExtra(OVERVIEW_MODE, MODE);
        goTo.putExtra(NUMBER_OF_CONTACTS, num);
        startActivity(goTo);
    }

    /**
     * onLongContactClicked: Expande el bottom sheet para acciones recurrentes con los contactos
     * @param number: PHONE_NUMBER del contacto en la posición clickada del RecyclerView
     * @param name: NAME del contacto en la posición clickada del RecyclerView
     * @param phone: PHONE del contacto en la posición clickada del RecyclerView
     * @param home: HOME_ADDRESS del contacto en la posición clickada del RecyclerView
     * @param email: EMAIL del contacto en la posición clickada del RecyclerView
     * @param bubble: COLOR_BUBBLE del contacto en la posición clickada del RecyclerView
     * @param favorite: FAVOURITE del contacto en la posición clickada del RecyclerView
     * @param position: Posición del contacto en el RecyclerView
     * */
    @Override
    // TODO: notifyDataSetChanged()
    public void onLongContactClicked(final String number, final String name, final String phone,
                                     final String home, final String email, final String bubble,
                                     final String favorite, final String date, final int position) {
        Button view_button, fav_button, del_button;
        TextView title_bottom_sheet, date_bottom_sheet;

        View view = getLayoutInflater().inflate(R.layout.bottom_sheet, null);
        dialog = new BottomSheetDialog(this);
        dialog.setContentView(view);

        title_bottom_sheet = view.findViewById(R.id.title_bottom_sheet);
        view_button = view.findViewById(R.id.button_see);
        fav_button = view.findViewById(R.id.button_favourite);
        del_button = view.findViewById(R.id.button_delete);
        date_bottom_sheet = view.findViewById(R.id.has_date_bottom_sheet);

        title_bottom_sheet.setText(name);
        if (favorite.equals("1")) {
            fav_button.setText(getString(R.string.favourite_sheet));
        } else {
            fav_button.setText(getString(R.string.delete_fav_sheet));
        }

        if (!date.equals(getString(R.string.schedule_day))) {
            date_bottom_sheet.setVisibility(View.VISIBLE);
            String text = getString(R.string.date_sheet) + ": " + date;
            date_bottom_sheet.setText(text);
        } else {
            date_bottom_sheet.setVisibility(View.GONE);
        }

        view_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                onContactClicked(number);
            }
        });
        fav_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                final ContactEntity c;
                if (favorite.equals("1")) {
                    c = new ContactEntity(name, number, phone, home, email, bubble, "0", date);
                } else {
                    c = new ContactEntity(name, number, phone, home, email, bubble, "1", date);
                }
                sql.updateContact(c);
                contacts = sql.getAllContacts();
                adapter.setContactList(contacts);
            }
        });
        del_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                sql.deleteContact(number);
                contacts.remove(position);
                adapter.notifyItemRemoved(position);
            }
        });

        dialog.show();
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
    // TODO: notifyDataSetChanged()
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
            adapter.setContactList(contacts);
        }
        return true;
    }

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
                fromFAB = true;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkPermits(permissions[0])) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{permissions[0]}, CODE_WRITE_ES);
                    } else {
                        if (isOnPortraitMode) {
                            Intent goTo = new Intent(getApplicationContext(), ContactOverview.class);
                            startActivity(goTo);
                        } else {
                            // TODO: Poner los campos en el fragmento con mode 1
                        }
                    }
                } else {
                    if (isOnPortraitMode) {
                        Intent goTo = new Intent(getApplicationContext(), ContactOverview.class);
                        startActivity(goTo);
                    } else {
                        // TODO: Poner los campos en el fragmento con mode 1
                    }
                }
            }
        });
    }

    /**
     * setSearchWidget: Se configura el SearchView encargado de la búsqueda de contactos
     * Si no hay texto que parsear, se cargan todos los contactos
     * */
    // TODO: notifyDataSetChanged()
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
                adapter.setContactList(sql.getSearchedContacts(query, getDatesContacts));
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.trim().isEmpty()) {
                    adapter.setContactList(sql.getSearchedContacts(newText, getDatesContacts));
                } else {
                    if (!getDatesContacts) {
                        adapter.setContactList(sql.getAllContacts());
                    } else {
                        adapter.setContactList(sql.getDatesWithContacts());
                    }
                }
                return true;
            }
        });
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
     * No se importan las citas.
     * */
    // TODO: notifyDataSetChanged()
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
                String favourite = info.getString(favouriteJSON);

                ContactEntity c = new ContactEntity(name, number, phone, address, email, bubble, favourite, getString(R.string.schedule_day));
                contacts.add(c);
                sql.insertContact(c);
            }

            this.contacts = contacts;
            adapter.setContactList(contacts);
            Toast.makeText(this, getString(R.string.success_import_to), Toast.LENGTH_SHORT).show();
        } catch (JSONException err) {
            Toast.makeText(this, getString(R.string.import_to_SD), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(this, getString(R.string.success_export_to), Toast.LENGTH_SHORT).show();
                } catch (JSONException err) {
                    Toast.makeText(this, getString(R.string.export_to_SD), Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(this, getString(R.string.no_cont_to_export), Toast.LENGTH_SHORT).show();
        }

        return contactFormatted;
    }

    /**
     * importFromContacts: Método que gracias al contentProvider que ofrece contactos, hace una query
     * para sacar todos los contactos del teléfono y por cada uno de ellos se guarda en el fromato
     * adecuado para su importación a la aplicación de MiAgenda.
     * */
    // TODO: notifyDataSetChanged()
    private void importFromContacts() {
        ArrayList<ContactEntity> contacts = new ArrayList<>();

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
                        "1",
                        getString(R.string.schedule_day)
                );
                contacts.add(c);
                try {
                    sql.insertContact(c);
                } catch (Exception err) {
                    Toast.makeText(this, getString(R.string.insertion_failed), Toast.LENGTH_SHORT).show();
                }
            }
        }
        cursor.close();
        this.contacts = contacts;
        adapter.setContactList(contacts);
        Toast.makeText(this, getString(R.string.success_import_to), Toast.LENGTH_SHORT).show();
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

    /**
     * getNewPositionOfContact: Método para calcular la nueva posicion del contacto en la lista.
     * @param id: Clave identificativa del contacto
     * @return: posición nueva
     * */
    private int getNewPositionOfContact(String id) {
        for (int x = 0; x < contacts.size(); x++) {
            if (contacts.get(x).getPHONE_NUMBER().equals(id)) {
                return x;
            }
        }
        return -1;
    }
}
