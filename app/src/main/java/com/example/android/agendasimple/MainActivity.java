package com.example.android.agendasimple;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;

import com.example.android.agendasimple.sql.ContactEntity;
import com.example.android.agendasimple.sql.DatabaseSQL;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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

    private ArrayList<ContactEntity> contacts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sql = new DatabaseSQL(this);

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
     *
     * @param menu: Variable en la que se guarda el menú creado a partir del xml
     * */
    // TODO: Update RecyclerView with queryText := name
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
     * */
    // TODO: Perform Search in DB
    private void setSearchWidget() {
        searchWidget.setQueryHint(getString(R.string.search_contact_hint));
        searchWidget.setInputType(InputType.TYPE_CLASS_TEXT);
        searchWidget.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        searchWidget.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchWidget.setIconifiedByDefault(false);

        searchWidget.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                searchWidget.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }

    /**
     * onOptionsItemSelected: Se administran las opciones del menú, en este caso la exportación
     * de contactos a o desde la tarjeta SD de todos los contactos
     *
     * @param item: item seleccionado del menu
     * */
    // TODO: Access to SD Card
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.export_from_contacts:

                break;
            case R.id.export_to_contacts:

                break;
        }
        return true;
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
