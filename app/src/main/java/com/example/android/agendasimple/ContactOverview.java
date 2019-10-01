package com.example.android.agendasimple;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
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
        setImeOptions();

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
                int curr = editName.getText().toString().length();
                if (curr != 0) {
                    String count = curr + "/30";
                    guideName.setText(count);
                } else {
                    String count = getString(R.string.guideline_name);
                    guideName.setText(count);
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
                int curr = editNumber.getText().toString().length();
                if (curr != 0) {
                    String count = curr + "/12";
                    guideNumber.setText(count);
                } else {
                    String count = getString(R.string.guideline_number);
                    guideNumber.setText(count);
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
                int curr = editPhone.getText().toString().length();
                if (curr != 0) {
                    String count = curr + "/12";
                    guidePhone.setText(count);
                } else {
                    String count = getString(R.string.guideline_number);
                    guidePhone.setText(count);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });
    }

    /**
     *
     * setImeOptions: Establece la jerarquía entre los EditText al darle al botón de DONE del
     * teclado software
     *
     * Name -> Phone Number
     * Phone Number -> Phone
     * Phone -> Home Address
     * Home Address -> Email
     * Email -> El teclado se cierra
     *
     * */
    private void setImeOptions() {
        editName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    editName.clearFocus();
                    editNumber.requestFocus();
                    return true;
                }
                return false;
            }
        });

        editNumber.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    editNumber.clearFocus();
                    editPhone.requestFocus();
                    return true;
                }
                return false;
            }
        });

        editPhone.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    editPhone.clearFocus();
                    editHome.requestFocus();
                    return true;
                }
                return false;
            }
        });

        editHome.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    editHome.clearFocus();
                    editEmail.requestFocus();
                    return true;
                }
                return false;
            }
        });

        editEmail.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    editEmail.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editEmail.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
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
}
