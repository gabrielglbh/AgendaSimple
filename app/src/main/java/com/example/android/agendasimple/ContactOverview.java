package com.example.android.agendasimple;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.agendasimple.sql.ContactEntity;

import java.util.Random;

public class ContactOverview extends AppCompatActivity {

    private TextView nameHint, numberHint, phoneHint, homeHint, emailHint;
    private EditText editName, editNumber, editPhone, editHome, editEmail;
    private TextView guideName, guideNumber, guidePhone;
    private TextView nameIcon, numberIcon, phoneIcon, homeIcon, emailIcon;
    private Toast mToast;

    private ContactEntity contact;
    private int mode = 1; // Especifica si se ha de llenar los campos del contacto o no
    private String NUMBER;

    private boolean alreadyUpdated = false;

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
    protected void onDestroy() {
        if (!alreadyUpdated) {
            if (mode == 1) {
                validateAndInsertContact();
            } else {
                validateAndUpdateContact();
            }
        }
        super.onDestroy();
    }

    /**
     * setViews: Configura las vistas iniciales
     *            0 = Se rellenan al entrar a la actividad desde un contacto ya existente
     *            1 = Se quedan vacios para poder añadir un contacto
     *
     * Se crean y establecen numerosos eventos para embellecer la UI como:
     *            el uso de IME Options para pasar a otro EditText (setHierarchyBetweenEditTextsOnImeOpts),
     *            el recuento de letras permitidas por campo (setLimitWordCount) o
     *            el cambio de color del label de cada EditText al estar activo (setEditHints).
     *
     * Si se accede a esta actividad para hacer un update del contacto, los colores de los iconos y
     * la actionBar/statusBar se cambian al mismo que la burbuja de MainActivity relativa al contacto.
     * (setUIToBubbleColor)
     */
    private void setViews() {

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
        nameIcon = findViewById(R.id.icon_name);
        numberIcon = findViewById(R.id.icon_number);
        phoneIcon = findViewById(R.id.icon_home_phone);
        homeIcon = findViewById(R.id.icon_home);
        emailIcon = findViewById(R.id.icon_mail);

        setEditHints();
        setLimitWordCount();
        setHierarchyBetweenEditTextsOnImeOpts();

        if (mode == 0) {
            setTitle(getString(R.string.modify_contact_title));
            contact = MainActivity.sql.getContact(NUMBER);
            setContact();
        } else {
            setTitle(getString(R.string.contact_overview_label));
            editName.requestFocus();
        }
    }

    private void setContact() {
        int color = Color.parseColor(contact.getCOLOR_BUBBLE());

        if (Build.VERSION.SDK_INT >= 21) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(color));
            Window w = getWindow();
            w.setStatusBarColor(color);

            setUIToBubbleColor(nameIcon, R.drawable.ic_account, color);
            setUIToBubbleColor(numberIcon, R.drawable.ic_smartphone, color);
            setUIToBubbleColor(phoneIcon, R.drawable.ic_call, color);
            setUIToBubbleColor(homeIcon, R.drawable.ic_home, color);
            setUIToBubbleColor(emailIcon, R.drawable.ic_email, color);
        }

        editName.setText(contact.getNAME());
        editNumber.setText(contact.getPHONE_NUMBER());
        editPhone.setText(contact.getPHONE());
        editHome.setText(contact.getHOME_ADDRESS());
        editEmail.setText(contact.getEMAIL());
    }

    // Start of UI and UX methods

    private void setUIToBubbleColor(TextView t, int drawable, int color) {
        Drawable ic_name = ContextCompat.getDrawable(this, drawable);
        ic_name.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        t.setBackground(ic_name);
    }

    private void setEditHints() {
        setOnFocusChangeListeners(editName, nameHint);
        setOnFocusChangeListeners(editNumber, numberHint);
        setOnFocusChangeListeners(editPhone, phoneHint);
        setOnFocusChangeListeners(editHome, homeHint);
        setOnFocusChangeListeners(editEmail, emailHint);
    }

    private void setOnFocusChangeListeners(EditText e, final TextView hint) {
        e.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focused) {
                if (focused) hint.setTextColor(ContextCompat.getColor(getApplicationContext(),
                        R.color.colorAccent));
                else hint.setTextColor(ContextCompat.getColor(getApplicationContext(),
                        R.color.text));
            }
        });
    }

    private void setLimitWordCount() {
        setOnTextChangeListeners(editName, editNumber, guideName);
        setOnTextChangeListeners(editNumber, editPhone, guideNumber);
        setOnTextChangeListeners(editPhone, editHome, guidePhone);
    }

    private void setOnTextChangeListeners(final EditText initial, final EditText next, final TextView t) {
        initial.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                int curr = initial.getText().toString().length();
                String count;
                if (curr != 0) {
                    if (initial == editName) {
                        if (curr <= 20) {
                            count = curr + "/20";
                            t.setText(count);
                        } else {
                            initial.setText(initial.getText().toString().substring(0, curr - 1));
                            initial.clearFocus();
                            next.requestFocus();
                        }
                    } else if (initial == editNumber || initial == editPhone) {
                        if (curr <= 12) {
                            count = curr + "/12";
                            t.setText(count);
                        } else {
                            initial.setText(initial.getText().toString().substring(0, curr - 1));
                            initial.clearFocus();
                            next.requestFocus();
                        }
                    }
                } else {
                    if (initial == editName) {
                        count = getString(R.string.guideline_name);
                        t.setText(count);
                    } else if (initial == editNumber || initial == editPhone){
                        count = getString(R.string.guideline_number);
                        t.setText(count);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });
    }

    private void setHierarchyBetweenEditTextsOnImeOpts() {
        setOnEditorActionListeners(editName, editNumber);
        setOnEditorActionListeners(editNumber, editPhone);
        setOnEditorActionListeners(editPhone, editHome);
        setOnEditorActionListeners(editHome, editEmail);
        setOnEditorActionListeners(editEmail, null);
    }

    private void setOnEditorActionListeners(final TextView focused, final TextView next) {
        if (focused != editEmail) {
            focused.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                    if (i == EditorInfo.IME_ACTION_DONE) {
                        focused.clearFocus();
                        next.requestFocus();
                        return true;
                    }
                    return false;
                }
            });
        } else {
            focused.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                    if (i == EditorInfo.IME_ACTION_DONE) {
                        focused.clearFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(editEmail.getWindowToken(), 0);
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    // End of UI and UX methods

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.contact_overview_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mode == 1) {
                    alreadyUpdated = true;
                    finish();
                } else {
                    validateAndUpdateContact();
                }
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

    private void validateAndInsertContact() {
        String[] colors = { "#008577", "#D17E2B", "#21A763", "#AF2E2E", "#265199", "#C4B22E", "#C251AC" };
        String name = editName.getText().toString();
        String number = editNumber.getText().toString();
        String phone = editPhone.getText().toString();
        String address = editHome.getText().toString();
        String email = editEmail.getText().toString();
        Random r = new Random();

        if (editName.getText().toString().trim().isEmpty() ||
                editNumber.getText().toString().trim().isEmpty()) {
            if (alreadyUpdated) {
                makeToast(getString(R.string.invalid_insertion_1));
            }
        }
        else if (name.trim().isEmpty() && number.trim().isEmpty() && phone.trim().isEmpty() &&
                address.trim().isEmpty() && email.trim().isEmpty()) {
            if(!MainActivity.sql.deleteContact(NUMBER)) {
                makeToast(getString(R.string.deletion_failed));
            }
            alreadyUpdated = true;
            finish();
        }
        else {
            ContactEntity newContact = new ContactEntity(
                    editName.getText().toString(),
                    editNumber.getText().toString(),
                    editPhone.getText().toString(),
                    editHome.getText().toString(),
                    editEmail.getText().toString(),
                    colors[r.nextInt(7)]
            );
            if (MainActivity.sql.insertContact(newContact)) {
                alreadyUpdated = true;
                finish();
            } else {
                makeToast(getString(R.string.insertion_failed));
            }
        }
    }

    private void validateAndUpdateContact() {
        String name = editName.getText().toString();
        String number = editNumber.getText().toString();
        String phone = editPhone.getText().toString();
        String address = editHome.getText().toString();
        String email = editEmail.getText().toString();

        if (contact.getNAME().equals(name) && contact.getPHONE_NUMBER().equals(number) &&
            contact.getPHONE().equals(phone) && contact.getHOME_ADDRESS().equals(address) &&
            contact.getEMAIL().equals(email)) {
            alreadyUpdated = true;
            finish();
        } else if (name.trim().isEmpty() && number.trim().isEmpty() && phone.trim().isEmpty() &&
                address.trim().isEmpty() && email.trim().isEmpty()) {
            if(!MainActivity.sql.deleteContact(NUMBER)) {
                makeToast(getString(R.string.deletion_failed));
            }
            alreadyUpdated = true;
            finish();
        } else {
            ContactEntity c = new ContactEntity(
                    name,
                    number,
                    phone,
                    address,
                    email,
                    contact.getCOLOR_BUBBLE()
            );
            if (MainActivity.sql.updateContact(c)) {
                alreadyUpdated = true;
                finish();
            } else {
                makeToast(getString(R.string.update_failed));
            }
        }
    }

    private void makeToast(String msg) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        mToast.show();
    }
}
