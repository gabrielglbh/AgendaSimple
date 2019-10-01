package com.example.android.agendasimple;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity implements AgendaAdapter.ContactClickListener {

    private RecyclerView rv;
    private AgendaAdapter adapter;
    private FloatingActionButton addContact;
    private SearchView searchWidget;

    public static final String OVERVIEW_MODE = "OVERVIEW_MODE";
    public final int MODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addContact = findViewById(R.id.fab_add_contact);
        addContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goTo = new Intent(getApplicationContext(), ContactOverview.class);
                startActivity(goTo);
            }
        });

        setRecyclerView();
    }

    /**
     *
     * setRecyclerView: Configuración del RecyclerView para la lista de contactos, extraída de la
     * base de datos
     *
     * */
    // TODO: Cargar contactos de DB
    private void setRecyclerView() {
        rv = findViewById(R.id.recycler_view_contacts);
        adapter = new AgendaAdapter(6, this, getApplicationContext());

        LinearLayoutManager lm = new LinearLayoutManager(this);
        rv.setLayoutManager(lm);
        rv.setHasFixedSize(false);
        rv.setAdapter(adapter);
    }

    @Override
    public void onContactClicked(int pos) {
        Intent goTo = new Intent(this, ContactOverview.class);
        goTo.putExtra(OVERVIEW_MODE, MODE);
        startActivity(goTo);
    }

    /**
     *
     * onCreateOptionsMenu: Se crea e infla el menu de MainActivity junto con la configuración
     * del widget de búsqueda de contacto searchWidget
     *
     * onMenuItemActionExpand: Se abre el teclado software
     *
     * onMenuItemActionCollapse: Se cierra el teclado software
     *
     * @param menu: Variable en la que se guarda el menú creado a partir del xml
     *
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
     *
     * setSearchWidget: Se configura el SearchView encargado de la búsqueda de contactos
     *
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
     *
     * onOptionsItemSelected: Se administran las opciones del menú, en este caso la exportación
     * de contactos a o desde la tarjeta SD de todos los contactos
     *
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

    /**
     *
     * openKeyboard: Abre el teclado
     *
     * */
    private void openKeyboard() {
        searchWidget.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    /**
     *
     * closeKeyboard: Cierra el teclado
     *
     * */
    private void closeKeyboard() {
        if (searchWidget != null) {
            searchWidget.clearFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(searchWidget.getWindowToken(), 0);
        }
    }
}
