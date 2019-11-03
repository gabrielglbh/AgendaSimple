package com.example.android.agendasimple;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.android.agendasimple.sql.ContactEntity;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.regex.Pattern;

public class ContactOverview extends AppCompatActivity {

    private TextInputEditText inputName, inputNumber, inputPhone, inputHome, inputEmail;
    private FloatingActionButton checkContact;
    private AppBarLayout appbar;
    private CollapsingToolbarLayout collapsingToolbar;
    private Toolbar toolbar;

    private ImageView nameIcon, numberIcon, phoneIcon, homeIcon, emailIcon, headerImage;
    private Toast mToast;
    private AlertDialog alert;
    private Menu menu;
    private ProgressBar savingContact;

    private ContactEntity contact;
    private int mode = 1; // Especifica si se ha de llenar los campos del contacto o no
    private String NUMBER, FAVOURITE = "1";
    private final int RESULT_LOAD_IMG = 123;

    private final String BUNDLE_LIKE = "ISLIKEPRESSED";
    private final String BUNDLE_OVERFLOWN = "ISOVERFLOWN";
    private boolean isLikedPressed = false;
    private boolean isOverflown = false;

    private Pattern namePattern = Pattern.compile("[A-Za-zñáéíóúÑÁÉÍÓÚ A-Za-z]+");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_overview);

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
    protected void onStop() {
        super.onStop();
        setSharedPreferences();
    }

    @Override
    protected void onStart() {
        super.onStart();
        getSharedPreferences();
    }

    @Override
    public void onBackPressed() {
        checkErrors();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.contact_overview_menu, menu);
        this.menu = menu;
        if (mode == 0) {
            if (contact.getFAVOURITE().equals("0")) {
                menu.getItem(1).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_heart_filled));
                FAVOURITE = "0";
                isLikedPressed = true;
            } else {
                menu.getItem(1).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_heart));
                FAVOURITE = "1";
            }
        }

        if (mode == 0) {
            if (getImageFromStorage() != null) {
                menu.getItem(0).setVisible(true);
            } else {
                menu.getItem(0).setVisible(false);
            }
        } else {
            menu.getItem(0).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            case R.id.menu_save:
                checkErrors();
                break;
            case R.id.menu_fav:
                if (!isLikedPressed) {
                    menu.getItem(1).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_heart_filled));
                    FAVOURITE = "0";
                    isLikedPressed = true;
                } else {
                    menu.getItem(1).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_heart));
                    FAVOURITE = "1";
                    isLikedPressed = false;
                }
                break;
            case R.id.menu_remove_img:
                removeImageStorage();
                break;
        }
        return true;
    }

    /**
     * Recoge la imagen de la galería
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == RESULT_LOAD_IMG) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                headerImage.setImageBitmap(rotate(selectedImage, 90));
                toolbar.setBackgroundColor(Color.TRANSPARENT);
                menu.getItem(0).setVisible(true);
            } catch (NullPointerException | IOException e) {
                e.printStackTrace();
                makeToast(getString(R.string.insertion_image_error));
            }
        } else {
            makeToast(getString(R.string.insertion_image_error));
        }
    }

    private void setSharedPreferences() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(BUNDLE_LIKE, isLikedPressed);
        editor.putBoolean(BUNDLE_OVERFLOWN, isOverflown);
        editor.apply();
    }

    private void getSharedPreferences() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        isLikedPressed = sp.getBoolean(BUNDLE_LIKE, false);
        if (isLikedPressed) FAVOURITE = "0";
        else FAVOURITE = "1";
        isOverflown = sp.getBoolean(BUNDLE_OVERFLOWN, false);
    }

    /**
     * setViews: Configura las vistas iniciales
     *            0 = Se rellenan al entrar a la actividad desde un contacto ya existente
     *            1 = Se quedan vacios para poder añadir un contacto
     *
     * Se crea la administración y visibilidad de los botones para eliminar texto (setClearButtons).
     * Se crea el TextWatcher para los contadores de los campos de name, phone y number.
     * Se crea el manejo del FAB.
     * Se customiza el AppBar y el CollapseActionView.
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
        collapsingToolbar = findViewById(R.id.collapsingToolbar);
        toolbar = findViewById(R.id.toolbar);

        headerImage = findViewById(R.id.headerImage);
        savingContact = findViewById(R.id.savingContact);

        setAppBar();

        final TextInputLayout layoutName = findViewById(R.id.tiet_name_layout);
        final TextInputLayout layoutNumber = findViewById(R.id.tiet_number_layout);
        final TextInputLayout layoutPhone = findViewById(R.id.tiet_home_phone_layout);

        checkContact = findViewById(R.id.floating_share);

        setTextWatchers(inputName, layoutName);
        setTextWatchers(inputNumber, layoutNumber);
        setTextWatchers(inputPhone, layoutPhone);
        setFloatingActionButton();

        if (mode == 0) {
            collapsingToolbar.setTitle(getString(R.string.modify_contact_title));
            contact = MainActivity.sql.getContact(NUMBER);
            setContact();
        } else {
            collapsingToolbar.setTitle(getString(R.string.contact_overview_label));
            final int color = ContextCompat.getColor(this, R.color.colorPrimary);
            setUIToBubbleColor(nameIcon, color);
            setUIToBubbleColor(numberIcon, color);
            setUIToBubbleColor(phoneIcon, color);
            setUIToBubbleColor(homeIcon, color);
            setUIToBubbleColor(emailIcon, color);
        }
    }

    private void setContact() {
        final int color = Color.parseColor(contact.getCOLOR_BUBBLE());
        final Bitmap bitmap = getImageFromStorage();

        if (Build.VERSION.SDK_INT >= 21) {
            Window w = getWindow();
            w.setStatusBarColor(color);

            setUIToBubbleColor(nameIcon, color);
            setUIToBubbleColor(numberIcon, color);
            setUIToBubbleColor(phoneIcon, color);
            setUIToBubbleColor(homeIcon, color);
            setUIToBubbleColor(emailIcon, color);
        }

        appbar.setBackgroundColor(color);
        collapsingToolbar.setContentScrimColor(color);

        if (bitmap != null) {
            headerImage.setImageBitmap(bitmap);
            toolbar.setBackgroundColor(Color.TRANSPARENT);
        } else {
            toolbar.setBackgroundColor(color);
        }

        inputName.setText(contact.getNAME());
        inputNumber.setText(contact.getPHONE_NUMBER());
        inputPhone.setText(contact.getPHONE());
        inputHome.setText(contact.getHOME_ADDRESS());
        inputEmail.setText(contact.getEMAIL());
    }

    private void setAppBar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setFloatingActionButton() {
        checkContact.setScaleX(0);
        checkContact.setScaleY(0);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            final Interpolator interpolator = AnimationUtils.loadInterpolator(getBaseContext(), android.R.interpolator.fast_out_slow_in);
            checkContact.animate()
                    .scaleX(1)
                    .scaleY(1)
                    .setInterpolator(interpolator)
                    .setDuration(600);
        }

        checkContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
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
                if (charSequence.length() > layout.getCounterMaxLength()) {
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

    /**
     * validateTextFields: Validación de los campos de Name y Email.
     * */
    private boolean validateTextFields(String name, String email, String number) {
        boolean n, num, em;

        if (name.trim().isEmpty() && !namePattern.matcher(name).matches()) {
            inputName.setError(getString(R.string.error_field));
            n = false;
        } else { n = true; }

        if (number.trim().isEmpty()) {
            inputNumber.setError(getString(R.string.error_field));
            num = false;
        } else { num = true; }

        if (!email.trim().isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            inputEmail.setError(getString(R.string.error_field));
            em = false;
        } else { em = true; }

        hideKeyboard();
        return n && num && em;
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

        if (validateTextFields(name, email, number) && !isOverflown) {
            if (name.trim().isEmpty() && number.trim().isEmpty() && phone.trim().isEmpty() &&
                    address.trim().isEmpty() && email.trim().isEmpty()) {
                finish();
            } else if (inputName.getText().toString().trim().isEmpty() ||
                    inputNumber.getText().toString().trim().isEmpty()) {
                createDialog(getString(R.string.invalid_insertion_1));
            } else {
                ContactEntity c = new ContactEntity(name, number, phone, address, email,
                        MainActivity.colors[r.nextInt(MainActivity.colors.length)], FAVOURITE);
                try {
                    Bitmap bitmap = ((BitmapDrawable) headerImage.getDrawable()).getBitmap();
                    saveImageToStorage(bitmap);
                    insert(c);
                } catch (Exception e) {
                    removeImageStorage();
                    insert(c);
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

        if (validateTextFields(name, email, number) && !isOverflown) {
            if (contact.getNAME().equals(name) && contact.getPHONE_NUMBER().equals(number) &&
                    contact.getPHONE().equals(phone) && contact.getHOME_ADDRESS().equals(address) &&
                    contact.getEMAIL().equals(email)) {
                if (!FAVOURITE.equals(contact.getFAVOURITE())) {
                    ContactEntity c = new ContactEntity(name, number, phone, address, email, contact.getCOLOR_BUBBLE(), FAVOURITE);
                    try {
                        Bitmap bitmap = ((BitmapDrawable) headerImage.getDrawable()).getBitmap();
                        saveImageToStorage(bitmap);
                        update(c);
                    } catch (Exception e) {
                        removeImageStorage();
                        update(c);
                    }
                } else {
                    try {
                        Bitmap bitmap = ((BitmapDrawable) headerImage.getDrawable()).getBitmap();
                        saveImageToStorage(bitmap);
                        finish();
                    } catch (Exception e) {
                        removeImageStorage();
                        finish();
                    }
                }
            } else if (name.trim().isEmpty() && number.trim().isEmpty() && phone.trim().isEmpty() &&
                    address.trim().isEmpty() && email.trim().isEmpty()) {
                if (!MainActivity.sql.deleteContact(NUMBER)) {
                    makeToast(getString(R.string.deletion_failed));
                } else {
                    if (!removeImageStorage()) {
                        Toast.makeText(this, R.string.error_delete_img, Toast.LENGTH_SHORT).show();
                    }
                }
                finish();
            } else {
                if (name.trim().isEmpty() || number.trim().isEmpty()) {
                    createDialog(getString(R.string.invalid_insertion_1));
                } else {
                    if (MainActivity.sql.getContact(number) != null) {
                        ContactEntity c = new ContactEntity(name, number, phone, address, email, contact.getCOLOR_BUBBLE(), FAVOURITE);
                        try {
                            Bitmap bitmap = ((BitmapDrawable) headerImage.getDrawable()).getBitmap();
                            saveImageToStorage(bitmap);
                            update(c);
                        } catch (Exception e) {
                            removeImageStorage();
                            update(c);
                        }
                    } else {
                        if (MainActivity.sql.deleteContact(contact.getPHONE_NUMBER())) {
                            if (!removeImageStorage()) {
                                Toast.makeText(this, R.string.error_delete_img, Toast.LENGTH_SHORT).show();
                            } else {
                                ContactEntity c = new ContactEntity(name, number, phone, address, email, contact.getCOLOR_BUBBLE(), FAVOURITE);
                                try {
                                    Bitmap bitmap = ((BitmapDrawable) headerImage.getDrawable()).getBitmap();
                                    saveImageToStorage(bitmap);
                                    insert(c);
                                } catch (Exception e) {
                                    removeImageStorage();
                                    insert(c);
                                }
                            }
                        } else {
                            makeToast(getString(R.string.deletion_failed));
                        }
                    }
                }
            }
        }
    }

    /**
     * Métodos para guardar, recoger y eliminar la imagen del storage
     * */
    // TODO: Permisos
    private void saveImageToStorage(final Bitmap bitmap) {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), getString(R.string.app_name));
            boolean isDirCreated = dir.mkdir();
            if (dir.exists() || isDirCreated) {
                try {
                    File file = new File(dir, inputNumber.getText().toString() + "_" + inputName.getText().toString() + ".png");
                    FileOutputStream out = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();
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

    private Bitmap getImageFromStorage() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), getString(R.string.app_name));
            if (dir.exists()) {
                File file = new File(dir, contact.getPHONE_NUMBER() + "_" + contact.getNAME() + ".png");
                return BitmapFactory.decodeFile(file.getPath());
            } else {
                makeToast(getString(R.string.import_to_SD));
            }
        } else {
            makeToast(getString(R.string.import_to_SD));
        }
        return null;
    }

    private boolean removeImageStorage() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), getString(R.string.app_name));
            if (dir.exists()) {
                File file = new File(dir, contact.getPHONE_NUMBER() + "_" + contact.getNAME() + ".png");
                headerImage.setImageBitmap(null);
                collapsingToolbar.setContentScrimColor(Color.parseColor(contact.getCOLOR_BUBBLE()));
                menu.getItem(0).setVisible(false);
                return file.delete();
            }
        }
        return false;
    }

    /**
     * Métodos auxiliares para la validación de errores:
     *
     *  En los campos,
     *  En la inserción, modificación y actualización
     *
     * También maneja:
     *
     *  El cierre del teclado virtual
     *  La creación de toasts y alertDialogs
     *  El check de permisos
     *  Y la rotación de las imágenes
     * */
    private void checkErrors() {
        if (inputName.getError() != null || inputNumber.getError() != null) {
            createDialog(getString(R.string.error_on_back));
        } else {
            savingContact.setVisibility(View.VISIBLE);
            if (mode == 1) {
                validateAndInsertContact();
            } else {
                validateAndUpdateContact();
            }
            savingContact.setVisibility(View.GONE);
        }
    }

    private void insert(ContactEntity c) {
        if (MainActivity.sql.insertContact(c)) {
            finish();
        } else {
            createDialog(getString(R.string.insertion_failed));
        }
    }

    private void update(ContactEntity c) {
        if (MainActivity.sql.updateContact(c)) {
            finish();
        } else {
            makeToast(getString(R.string.update_failed));
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

    private Bitmap rotate(Bitmap bitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
}
