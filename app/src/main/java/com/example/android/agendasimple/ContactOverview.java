package com.example.android.agendasimple;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

public class ContactOverview extends AppCompatActivity {

    private TextView nameHint, numberHint, phoneHint, homeHint, emailHint;
    private EditText editName, editNumber, editPhone, editHome, editEmail;
    private TextView guideName, guideNumber, guidePhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_overview);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent i = getIntent();
        if (i.hasExtra(MainActivity.OVERVIEW_MODE)) { setViews(i.getIntExtra(MainActivity.OVERVIEW_MODE, 0)); }
        else { setViews(1); }
    }

    /**
     *
     * setViews: Configura las vistas iniciales
     *
     * @param mode: Especifica si se ha de llenar los campos del contacto o no
     *
     *            0 = Se rellenan al entrar a la actividad desde un contacto ya existente
     *            1 = Se quedan vacios para poder añadir un contacto
     *
     */
    // TODO: mode == 0 hacer load del contacto y set de los campos
    private void setViews(int mode) {

        nameHint = findViewById(R.id.hint_name);
        editName = findViewById(R.id.input_name);
        guideName = findViewById(R.id.guide_count_name);
        numberHint = findViewById(R.id.hint_number);
        editNumber = findViewById(R.id.input_number);
        guideNumber = findViewById(R.id.guide_count_number);
        phoneHint = findViewById(R.id.hint_home_phone);
        editPhone = findViewById(R.id.input_home_phone);
        guidePhone = findViewById(R.id.guide_count_home_phone);
        homeHint = findViewById(R.id.hint_home);
        editHome = findViewById(R.id.input_home);
        emailHint = findViewById(R.id.hint_mail);
        editEmail = findViewById(R.id.input_mail);

        setEditHints();
        setGuideCount();

        if (mode == 0) {
            setTitle(getString(R.string.modify_contact_title));
        } else {
            setTitle(getString(R.string.contact_overview_label));
            editName.requestFocus();
        }
    }

    /**
     *
     * setEditHints: Establece la lógica para embellecer la vista al hacer focus a un EditText para
     * que la hint del mismo se ponga de otro color
     *
     * Se utiliza el evento onFocusChanged
     *
     * */
    private void setEditHints() {
        editName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focused) {
                if (focused) nameHint.setTextColor(ContextCompat.getColor(getApplicationContext(),
                        R.color.colorAccent));
                else nameHint.setTextColor(ContextCompat.getColor(getApplicationContext(),
                        R.color.text));
            }
        });

        editNumber.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focused) {
                if (focused) numberHint.setTextColor(ContextCompat.getColor(getApplicationContext(),
                        R.color.colorAccent));
                else numberHint.setTextColor(ContextCompat.getColor(getApplicationContext(),
                        R.color.text));
            }
        });

        editPhone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focused) {
                if (focused) phoneHint.setTextColor(ContextCompat.getColor(getApplicationContext(),
                        R.color.colorAccent));
                else phoneHint.setTextColor(ContextCompat.getColor(getApplicationContext(),
                        R.color.text));
            }
        });

        editHome.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focused) {
                if (focused) homeHint.setTextColor(ContextCompat.getColor(getApplicationContext(),
                        R.color.colorAccent));
                else homeHint.setTextColor(ContextCompat.getColor(getApplicationContext(),
                        R.color.text));
            }
        });

        editEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focused) {
                if (focused) emailHint.setTextColor(ContextCompat.getColor(getApplicationContext(),
                        R.color.colorAccent));
                else emailHint.setTextColor(ContextCompat.getColor(getApplicationContext(),
                        R.color.text));
            }
        });
    }

    /**
     *
     * setGuideCount: Establece la lógica del recuento de caracteres en el campo de name, phone y number.
     * Si se llega al límite no se permite escribir más
     *
     * Se utiliza el evento onTextChanged
     *
     * */
    private void setGuideCount() {
        editName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (i+1 <= 30) {
                    String count = (i + 1) + "/30";
                    guideName.setText(count);
                } else {
                    editName.setText(editName.getText().toString());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

        editNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (i+1 <= 12) {
                    String count = (i+1) + "/12";
                    guideNumber.setText(count);
                } else {
                    editNumber.setText(editNumber.getText().toString());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

        editPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (i+1 <= 12) {
                    String count = (i+1) + "/12";
                    guidePhone.setText(count);
                } else {
                    editPhone.setText(editPhone.getText().toString());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.contact_overview_menu, menu);
        return true;
    }

    // TODO: Save on finish the contact and check if it is empty
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            case R.id.menu_save:
                finish();
                break;
        }
        return true;
    }

    /**
     *
     * dispatchTouchEvent: Evento que sirve para quitar focus del EditText al dar a otra parte de la
     * pantalla
     *
     * */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }
}
