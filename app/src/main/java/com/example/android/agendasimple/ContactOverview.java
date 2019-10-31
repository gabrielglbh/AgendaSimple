package com.example.android.agendasimple;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.agendasimple.sql.ContactEntity;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Random;
import java.util.regex.Pattern;

public class ContactOverview extends AppCompatActivity {

    private TextInputEditText inputName, inputNumber, inputPhone, inputHome, inputEmail;
    private TextInputLayout layoutName, layoutNumber, layoutPhone;
    private FloatingActionButton favContact;
    private AppBarLayout appbar;

    private ImageView nameIcon, numberIcon, phoneIcon, homeIcon, emailIcon;
    private Toast mToast;
    private AlertDialog alert;

    private ContactEntity contact;
    private int mode = 1; // Especifica si se ha de llenar los campos del contacto o no
    private String NUMBER, FAVOURITE = "1";

    private boolean isLikedPressed = false;
    private boolean isOverflown = false;

    private Pattern namePattern = Pattern.compile("[a-zA-Z]+");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_overview);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent i = getIntent();
        if (i.hasExtra(MainActivity.OVERVIEW_MODE)) {
            mode = i.getIntExtra(MainActivity.OVERVIEW_MODE, 0);
            if (i.hasExtra(MainActivity.NUMBER_OF_CONTACTS)) {
                NUMBER = i.getStringExtra(MainActivity.NUMBER_OF_CONTACTS);
            }
            setViews();
        }
        else {
            setViews();
        }
    }

    @Override
    public void onBackPressed() {
        if (mode == 1) {
            validateAndInsertContact();
        } else {
            validateAndUpdateContact();
        }
    }

    /**
     * setViews: Configura las vistas iniciales
     *            0 = Se rellenan al entrar a la actividad desde un contacto ya existente
     *            1 = Se quedan vacios para poder añadir un contacto
     *
     * Se crea la administración y visibilidad de los botones para eliminar texto (setClearButtons).
     * Se crea el TextWatcher para los contadores de los campos de name, phone y number.
     * Se crea el manejo del FAB.
     *
     * Si se accede a esta actividad para hacer un update del contacto, los colores de los iconos y
     * la actionBar/statusBar se cambian al mismo que la burbuja de MainActivity relativa al contacto.
     * (setUIToBubbleColor)
     */
    private void setViews() {
        inputName = findViewById(R.id.tiet_name);
        inputNumber = findViewById(R.id.tiet_number);
        inputPhone = findViewById(R.id.tiet_home_phone);
        inputHome = findViewById(R.id.tiet_home);
        inputEmail = findViewById(R.id.tiet_mail);

        nameIcon = findViewById(R.id.icon_name);
        numberIcon = findViewById(R.id.icon_number);
        phoneIcon = findViewById(R.id.icon_home_phone);
        homeIcon = findViewById(R.id.icon_home);
        emailIcon = findViewById(R.id.icon_mail);

        appbar = findViewById(R.id.appBarLayout);

        layoutName = findViewById(R.id.tiet_name_layout);
        layoutNumber = findViewById(R.id.tiet_number_layout);
        layoutPhone = findViewById(R.id.tiet_home_phone_layout);

        favContact = findViewById(R.id.floating_share);

        setTextWatchers(inputName, layoutName);
        setTextWatchers(inputNumber, layoutNumber);
        setTextWatchers(inputPhone, layoutPhone);

        if (mode == 0) {
            setTitle(getString(R.string.modify_contact_title));
            contact = MainActivity.sql.getContact(NUMBER);
            setContact();
            setFAB();
        } else {
            setTitle(getString(R.string.contact_overview_label));
        }
    }

    private void setContact() {
        int color = Color.parseColor(contact.getCOLOR_BUBBLE());

        if (Build.VERSION.SDK_INT >= 21) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(color));
            Window w = getWindow();
            w.setStatusBarColor(color);

            setUIToBubbleColor(nameIcon, color);
            setUIToBubbleColor(numberIcon, color);
            setUIToBubbleColor(phoneIcon, color);
            setUIToBubbleColor(homeIcon, color);
            setUIToBubbleColor(emailIcon, color);
        }

        appbar.setBackgroundColor(color);

        inputName.setText(contact.getNAME());
        inputNumber.setText(contact.getPHONE_NUMBER());
        inputPhone.setText(contact.getPHONE());
        inputHome.setText(contact.getHOME_ADDRESS());
        inputEmail.setText(contact.getEMAIL());
    }

    private void setFAB() {
        if (mode == 0) {
            if (contact.getFAVOURITE().equals("0")) {
                favContact.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_heart_filled));
                FAVOURITE = "0";
                isLikedPressed = true;
            } else {
                favContact.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_heart));
                FAVOURITE = "1";
            }
        }

        favContact.setScaleX(0);
        favContact.setScaleY(0);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            final Interpolator interpolator = AnimationUtils.loadInterpolator(getBaseContext(), android.R.interpolator.fast_out_slow_in);
            favContact.animate()
                    .scaleX(1)
                    .scaleY(1)
                    .setInterpolator(interpolator)
                    .setDuration(600);
        }

        favContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isLikedPressed) {
                    favContact.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_heart_filled));
                    FAVOURITE = "0";
                    isLikedPressed = true;
                } else {
                    favContact.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_heart));
                    FAVOURITE = "1";
                    isLikedPressed = false;
                }
            }
        });
    }

    private void setUIToBubbleColor(ImageView t, int color) {
        DrawableCompat.setTint(t.getDrawable(), color);
    }

    private void setTextWatchers(final TextInputEditText text, final TextInputLayout layout) {
        text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() >= layout.getCounterMaxLength()) {
                    isOverflown = true;
                    text.setError(getString(R.string.overflow_chars));
                } else {
                    isOverflown = false;
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            case R.id.menu_save:
                if (mode == 1) {
                    validateAndInsertContact();
                } else {
                    validateAndUpdateContact();
                }
                break;
        }
        return true;
    }

    /**
     * validateTextFields: Validación de los campos de Name y Email.
     * */
    private boolean validateTextFields(String name, String email) {
        if (name != null && email != null) {
            if (!namePattern.matcher(name).matches()) {
                inputName.setError(getString(R.string.error_field));
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    inputEmail.setError(getString(R.string.error_field));
                }
                return false;
            }
        }
        return true;
    }

    /**
     * validateAndInsertContact: Inserta el contacto
     *      Si el contacto está vacío --> finish
     *      Si el nombre o el número están vacios --> dialog(discard o no)
     *      Si el contacto es válido --> INSERT
     * */
    private void validateAndInsertContact() {
        hideKeyboard();

        String name = inputName.getText().toString();
        String number = inputNumber.getText().toString();
        String phone = inputPhone.getText().toString();
        String address = inputHome.getText().toString();
        String email = inputEmail.getText().toString();
        Random r = new Random();

        if (validateTextFields(name, email) && !isOverflown) {
            if (name.trim().isEmpty() && number.trim().isEmpty() && phone.trim().isEmpty() &&
                    address.trim().isEmpty() && email.trim().isEmpty()) {
                finish();
            } else if (inputName.getText().toString().trim().isEmpty() ||
                    inputNumber.getText().toString().trim().isEmpty()) {
                createDialog(getString(R.string.invalid_insertion_1));
            } else {
                ContactEntity c = new ContactEntity(name, number, phone, address, email,
                        MainActivity.colors[r.nextInt(MainActivity.colors.length)], FAVOURITE);
                if (MainActivity.sql.insertContact(c)) {
                    finish();
                } else {
                    createDialog(getString(R.string.insertion_failed));
                }
            }
        }
    }

    /**
     * validateAndUpdateContact: Modifica el contacto
     *      Si el contacto está vacío --> DELETE
     *      Si el contacto es exactamente igual que al abrirlo --> finish
     *      Si el contacto es exactamente igual que al abrirlo, pero se cambia el FAV --> UPDATE
     *      Si el nombre o el número están vacios --> dialog(discard o no)
     *      Si el contacto es válido -->
     *              - Check si el número (PK) está en la base de datos -->
     *                      - Si: dialog(discard o no)
     *                      - No: DELETE & UPDATE
     * */
    private void validateAndUpdateContact() {
        hideKeyboard();

        String name = inputName.getText().toString();
        String number = inputNumber.getText().toString();
        String phone = inputPhone.getText().toString();
        String address = inputHome.getText().toString();
        String email = inputEmail.getText().toString();

        if (validateTextFields(name, email) && !isOverflown) {
            if (contact.getNAME().equals(name) && contact.getPHONE_NUMBER().equals(number) &&
                    contact.getPHONE().equals(phone) && contact.getHOME_ADDRESS().equals(address) &&
                    contact.getEMAIL().equals(email)) {
                if (!FAVOURITE.equals(contact.getFAVOURITE())) {
                    ContactEntity c = new ContactEntity(name, number, phone, address, email, contact.getCOLOR_BUBBLE(), FAVOURITE);
                    if (MainActivity.sql.updateContact(c)) {
                        finish();
                    } else {
                        makeToast(getString(R.string.update_failed));
                    }
                } else {
                    finish();
                }
            } else if (name.trim().isEmpty() && number.trim().isEmpty() && phone.trim().isEmpty() &&
                    address.trim().isEmpty() && email.trim().isEmpty()) {
                if (!MainActivity.sql.deleteContact(NUMBER)) {
                    makeToast(getString(R.string.deletion_failed));
                }
                finish();
            } else {
                if (name.trim().isEmpty() || number.trim().isEmpty()) {
                    createDialog(getString(R.string.invalid_insertion_1));
                } else {
                    if (MainActivity.sql.getContact(number) != null) {
                        ContactEntity c = new ContactEntity(name, number, phone, address, email, contact.getCOLOR_BUBBLE(), FAVOURITE);
                        if (MainActivity.sql.updateContact(c)) {
                            finish();
                        } else {
                            makeToast(getString(R.string.update_failed));
                        }
                    } else {
                        if (MainActivity.sql.deleteContact(contact.getPHONE_NUMBER())) {
                            ContactEntity c = new ContactEntity(name, number, phone, address, email, contact.getCOLOR_BUBBLE(), FAVOURITE);
                            if (MainActivity.sql.insertContact(c)) {
                                finish();
                            } else {
                                makeToast(getString(R.string.insertion_failed));
                            }
                        } else {
                            makeToast(getString(R.string.deletion_failed));
                        }
                    }
                }
            }
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    private void makeToast(String msg) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        mToast.show();
    }

    private void createDialog(String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(getString(R.string.msg_dialog));
        builder.setNegativeButton(getString(R.string.no_dialog), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alert.dismiss();
            }
        });
        builder.setPositiveButton(getString(R.string.yes_dialog), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        alert = builder.create();
        alert.show();
    }
}
