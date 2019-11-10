package com.example.android.agendasimple;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.exifinterface.media.ExifInterface;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.android.agendasimple.sql.ContactEntity;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Random;
import java.util.regex.Pattern;

public class ContactOverview extends AppCompatActivity {

    private TextInputEditText inputName, inputNumber, inputPhone, inputHome, inputEmail;
    private FloatingActionButton checkContact;
    private AppBarLayout appbar;
    private CollapsingToolbarLayout collapsingToolbar;
    private Toolbar toolbar;

    private ImageView nameIcon, numberIcon, phoneIcon, homeIcon, emailIcon, headerImage, bookmarkIcon;
    private TextView inputDate, date_display, time_display, title_dialog;
    private Button add_scheduled, cancel_scheduled;
    private Toast mToast;
    private AlertDialog alert, alarmBuilder;
    private Menu menu;
    private ProgressBar savingContact;

    private ContactEntity contact;
    private int mode = 1; // Especifica si se ha de llenar los campos del contacto o no
    private String NUMBER, FAVOURITE = "1";
    private final int RESULT_LOAD_IMG = 123;
    private String timeToDisplay, dateToDisplay;

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

    /**
     * onBackPressed: Funcionalidad de validación de los campos al pulsar el botón de hardware 'atrás'
     * */
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
     * onActivityResult: Recoge la imagen de la galería
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == RESULT_LOAD_IMG) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                headerImage.setImageBitmap(rotate(selectedImage, getRealPathFromURI(imageUri)));
                toolbar.setBackgroundColor(Color.TRANSPARENT);
                menu.getItem(0).setVisible(true);
                if (Build.VERSION.SDK_INT >= 21) {
                    Window w = getWindow();
                    w.setStatusBarColor(Color.TRANSPARENT);
                }
            } catch (NullPointerException | IOException e) {
                e.printStackTrace();
                makeToast(getString(R.string.insertion_image_error));
            }
        } else {
            makeToast(getString(R.string.insertion_image_error));
        }
    }

    /**
     * setViews: Busca y configura los elementos de la vista actual
     */
    private void setViews() {
        inputName = findViewById(R.id.tiet_name);
        inputNumber = findViewById(R.id.tiet_number);
        inputPhone = findViewById(R.id.tiet_home_phone);
        inputHome = findViewById(R.id.tiet_home);
        inputEmail = findViewById(R.id.tiet_mail);
        inputDate = findViewById(R.id.add_date);

        nameIcon = findViewById(R.id.icon_name);
        numberIcon = findViewById(R.id.icon_number);
        phoneIcon = findViewById(R.id.icon_home_phone);
        homeIcon = findViewById(R.id.icon_home);
        emailIcon = findViewById(R.id.icon_mail);
        bookmarkIcon = findViewById(R.id.icon_date);

        appbar = findViewById(R.id.appBarLayout);
        collapsingToolbar = findViewById(R.id.collapsingToolbar);
        toolbar = findViewById(R.id.toolbar);

        headerImage = findViewById(R.id.headerImage);
        savingContact = findViewById(R.id.savingContact);

        setAppBar();
        createScheduledDateDialog();

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
            setUIToBubbleColor(bookmarkIcon, color);
        }
    }

    /**
     * setContact: Para cuando se quiera modificar un contacto, se llama a esta función y se rellenan
     * los campos que estén disponibles del contacto
     * */
    private void setContact() {
        final int color = Color.parseColor(contact.getCOLOR_BUBBLE());
        final Bitmap bitmap = getImageFromStorage();

        if (Build.VERSION.SDK_INT >= 21) {
            Window w = getWindow();
            if (bitmap != null) {
                w.setStatusBarColor(Color.TRANSPARENT);
            } else {
                w.setStatusBarColor(color);
            }

            setUIToBubbleColor(nameIcon, color);
            setUIToBubbleColor(numberIcon, color);
            setUIToBubbleColor(phoneIcon, color);
            setUIToBubbleColor(homeIcon, color);
            setUIToBubbleColor(emailIcon, color);
            setUIToBubbleColor(bookmarkIcon, color);
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
        inputDate.setText(contact.getDATE());
        if (!inputDate.getText().toString().equals(getString(R.string.schedule_day))) {
            timeToDisplay = contact.getDATE().substring(inputDate.getText().length() - 5);
            dateToDisplay = contact.getDATE().substring(0, inputDate.getText().length() - 7);
        }
    }

    /**
     * setAppBar: Configuración de la toolbar de Material
     * */
    private void setAppBar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        appbar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) scrollRange = appBarLayout.getTotalScrollRange();

                if (scrollRange + verticalOffset == 0) {
                    isShow = true;
                    if (Build.VERSION.SDK_INT >= 21) {
                        Window w = getWindow();
                        w.setStatusBarColor(Color.parseColor(contact.getCOLOR_BUBBLE()));
                    }
                } else if (isShow) {
                    isShow = false;
                    if (Build.VERSION.SDK_INT >= 21) {
                        Window w = getWindow();
                        w.setStatusBarColor(Color.TRANSPARENT);
                    }
                }
            }
        });
    }

    /**
     * setFloatingActionButton: Configuración y animación del FAB de agregar imagen de contacto
     * */
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
                hideKeyboard();
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
            }
        });
    }

    /**
     * setUIToBubbleColor: Cambio de color de los iconos en base al BUBBLE_COLOR del contacto asignado
     * aleatoriamente en su creación
     * @param color: Color a pintar
     * @param t: ImageView conteniendo el icono a colorear
     */
    private void setUIToBubbleColor(ImageView t, int color) {
        DrawableCompat.setTint(t.getDrawable(), color);
    }

    /**
     * setTextWatchers: Evento para manejar el error en los campos de NAME, NUMBER y PHONE_NUMBER si
     * hay un número de caracteres erróneo
     * @param text: Texto de donde escuchar el contador del texto
     * @param layout: Layout que contiene a text que contiene el número máximo válido de caracteres
     * */
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
     * createScheduledDateDialog: Creación del custom dialog para la adición de una cita con un contacto
     * */
    private void createScheduledDateDialog() {
        inputDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();

                View customView = getLayoutInflater().inflate(R.layout.create_alert_dialog, null);
                title_dialog = customView.findViewById(R.id.title_dialog);
                time_display = customView.findViewById(R.id.et_show_time);
                date_display = customView.findViewById(R.id.et_show_date);
                add_scheduled = customView.findViewById(R.id.add_button);
                cancel_scheduled = customView.findViewById(R.id.button_cancel);

                if (timeToDisplay != null && dateToDisplay != null) {
                    title_dialog.setText(getString(R.string.modify_date));
                    add_scheduled.setText(getString(R.string.modify_scheduled_date));
                    cancel_scheduled.setVisibility(View.VISIBLE);
                    time_display.setText(timeToDisplay);
                    time_display.setTextColor(Color.BLACK);
                    date_display.setText(dateToDisplay);
                    date_display.setTextColor(Color.BLACK);
                } else {
                    title_dialog.setText(getString(R.string.add_date));
                    add_scheduled.setText(getString(R.string.finish_scheduled_date));
                    cancel_scheduled.setVisibility(View.GONE);
                }

                AlertDialog.Builder alarm = new AlertDialog.Builder(ContactOverview.this)
                        .setView(customView);
                alarmBuilder = alarm.create();
                alarmBuilder.show();
            }
        });
    }

    /**
     * openDatePickerDialog: Abre el DatePicker con la fecha actual y guarda la fecha seleccionada
     * */
    public void openDatePickerDialog(View view) {
        DatePickerDialog mDatePicker;
        Calendar mcurrentDate = Calendar.getInstance();
        final int mYear = mcurrentDate.get(Calendar.YEAR);
        final int mMonth = mcurrentDate.get(Calendar.MONTH);
        final int mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);

        mDatePicker = new DatePickerDialog(ContactOverview.this, R.style.PickerDialogTheme,
                new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int selectedyear,
                                          int selectedmonth, int selectedday) {
                        String day = "0";
                        if (selectedday <= 9) day = day + selectedday;
                        else day = Integer.toString(selectedday);

                        String date = day + "/" +
                                (selectedmonth+1) + "/" +
                                selectedyear;
                        date_display.setText(date);
                        date_display.setTextColor(Color.BLACK);
                        dateToDisplay = date;
                    }
                }, mYear, mMonth, mDay);
        mDatePicker.show();
    }

    /**
     * openTimePickerDialog: Abre el TimePicker con la hora actual y guarda la hora seleccionada
     * */
    public void openTimePickerDialog(View view) {
        TimePickerDialog mTimePicker;
        Calendar mcurrentTime = Calendar.getInstance();
        final int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        final int minute = mcurrentTime.get(Calendar.MINUTE);

        mTimePicker = new TimePickerDialog(this, R.style.PickerDialogTheme,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourSel, int minuteSel) {
                        String min = "0", hour = "0";
                        if (hourSel >= 0 && hourSel <= 9) hour = hour + hourSel;
                        else hour = Integer.toString(hourSel);
                        if (minuteSel >= 0 && minuteSel <= 9) min = min + minuteSel;
                        else min = Integer.toString(minuteSel);

                        String text = hour + ":" + min;
                        time_display.setText(text);
                        time_display.setTextColor(Color.BLACK);
                        timeToDisplay = text;
                    }
                }, hour, minute, true);
        mTimePicker.show();
    }

    /**
     * addDate: Añade o modifica la cita rellenada
     * */
    public void addDate(View view) {
        if (timeToDisplay != null && dateToDisplay != null) {
            alarmBuilder.dismiss();
            String schedule = dateToDisplay + " " + getString(R.string.at_time) + " " + timeToDisplay;
            inputDate.setText(schedule);
        } else {
            makeToast(getString(R.string.error_field));
        }
    }

    /**
     * cancelDate: Elimina la cita
     * */
    public void cancelDate(View view) {
        alarmBuilder.dismiss();
        timeToDisplay = null;
        dateToDisplay = null;
        inputDate.setText(getString(R.string.schedule_day));
    }

    /**
     * validateTextFields: Validación de los campos de NAME, EMAIL y NUMBER. NAME y NUMBER son los únicos
     * obligatorios.
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
        String date = inputDate.getText().toString();
        Random r = new Random();

        if (validateTextFields(name, email, number) && !isOverflown) {
            if (name.trim().isEmpty() && number.trim().isEmpty() && phone.trim().isEmpty() &&
                    address.trim().isEmpty() && email.trim().isEmpty()) {
                finish();
            } else if (inputName.getText().toString().trim().isEmpty() ||
                    inputNumber.getText().toString().trim().isEmpty()) {
                createDialog(getString(R.string.invalid_insertion_1));
            } else {
                String scheduledDate = checkIsDate(date);
                ContactEntity c = new ContactEntity(name, number, phone, address, email,
                        MainActivity.colors[r.nextInt(MainActivity.colors.length)], FAVOURITE, scheduledDate);
                try {
                    Bitmap bitmap = ((BitmapDrawable) headerImage.getDrawable()).getBitmap();
                    saveImageToStorage(bitmap);
                    insert(c);
                } catch (Exception e) {
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
        String date = inputDate.getText().toString();

        if (validateTextFields(name, email, number) && !isOverflown) {
            if (contact.getNAME().equals(name) && contact.getPHONE_NUMBER().equals(number) &&
                    contact.getPHONE().equals(phone) && contact.getHOME_ADDRESS().equals(address) &&
                    contact.getEMAIL().equals(email)) {
                if (!FAVOURITE.equals(contact.getFAVOURITE())) {
                    String scheduledDate = checkIsDate(date);
                    ContactEntity c = new ContactEntity(name, number, phone, address, email,
                            contact.getCOLOR_BUBBLE(), FAVOURITE, scheduledDate);
                    isOkForUpdate(c);
                } else {
                    String scheduledDate = checkIsDate(date);
                    ContactEntity c = new ContactEntity(name, number, phone, address, email,
                            contact.getCOLOR_BUBBLE(), FAVOURITE, scheduledDate);
                    isOkForUpdate(c);
                }
            } else if (name.trim().isEmpty() && number.trim().isEmpty() && phone.trim().isEmpty() &&
                    address.trim().isEmpty() && email.trim().isEmpty()) {
                if (MainActivity.sql.deleteContact(NUMBER) == -1) {
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
                        String scheduledDate = checkIsDate(date);
                        ContactEntity c = new ContactEntity(name, number, phone, address, email,
                                contact.getCOLOR_BUBBLE(), FAVOURITE, scheduledDate);
                        isOkForUpdate(c);
                    } else {
                        if (MainActivity.sql.deleteContact(contact.getPHONE_NUMBER()) != -1) {
                            if (!removeImageStorage()) {
                                Toast.makeText(this, R.string.error_delete_img, Toast.LENGTH_SHORT).show();
                            } else {
                                String scheduledDate = checkIsDate(date);
                                ContactEntity c = new ContactEntity(name, number, phone, address, email,
                                        contact.getCOLOR_BUBBLE(), FAVOURITE, scheduledDate);
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
    private void saveImageToStorage(final Bitmap bitmap) {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), getString(R.string.file_path_contact_images));
            boolean isDirCreated = dir.mkdir();
            if (dir.exists() || isDirCreated) {
                try {
                    File file = new File(dir, inputNumber.getText().toString() + ".png");
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
            File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), getString(R.string.file_path_contact_images));
            if (dir.exists()) {
                File file = new File(dir, contact.getPHONE_NUMBER() + ".png");
                if (file.exists()) return BitmapFactory.decodeFile(file.getPath());
            } else {
                return null;
            }
        } else {
            return null;
        }
        return null;
    }

    private boolean removeImageStorage() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), getString(R.string.file_path_contact_images));
            if (dir.exists()) {
                File file = new File(dir, contact.getPHONE_NUMBER() + ".png");
                headerImage.setImageBitmap(null);
                collapsingToolbar.setContentScrimColor(Color.parseColor(contact.getCOLOR_BUBBLE()));
                menu.getItem(0).setVisible(false);
                return file.delete();
            }
        }
        return false;
    }

    /**
     * checkErrors: Validación de errores en base a los textWatchers y Patterns.
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

    /**
     * checkIsDate: Verificador de si hay una nueva cita o no
     * @param date: Fecha y hora de la cita
     * */
    private String checkIsDate(String date) {
        String s = getString(R.string.schedule_day);
        if (!date.equals(getString(R.string.schedule_day))) {
            s = date;
        }
        return s;
    }

    /**
     * isOkForUpdate: Update de un contacto con su imagen de contacto
     * @param c: Contacto completo modificado
     * */
    private void isOkForUpdate(ContactEntity c) {
        try {
            Bitmap bitmap = ((BitmapDrawable) headerImage.getDrawable()).getBitmap();
            saveImageToStorage(bitmap);
            update(c);
        } catch (Exception e) {
            removeImageStorage();
            update(c);
        }
    }

    private void insert(ContactEntity c) {
        if (MainActivity.sql.insertContact(c) != null) {
            finish();
        } else {
            createDialog(getString(R.string.insertion_failed));
        }
    }

    private void update(ContactEntity c) {
        if (MainActivity.sql.updateContact(c) != -1) {
            finish();
        } else {
            makeToast(getString(R.string.update_failed));
        }
    }

    private void hideKeyboard() {
        inputName.clearFocus();
        inputNumber.clearFocus();
        inputPhone.clearFocus();
        inputHome.clearFocus();
        inputEmail.clearFocus();

        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(findViewById(android.R.id.content).getWindowToken(), 0);
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

    /**
     * rotate: Rotación de la imagen de la galería en base a unos grados
     * @param bitmap: Imagen de la galería
     * @param path: Path de la imagen seleccionada
     * @return Nuevo bitmap (imagen) rotada
     * */
    private Bitmap rotate(Bitmap bitmap, String path) {
        try {
            ExifInterface info = new ExifInterface(path);
            int orientation = info.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            Matrix matrix = new Matrix();

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90f);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180f);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270f);
                    break;
            }

            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (IOException err) {
            err.printStackTrace();
            return null;
        }
    }

    /**
     * getRealPathFromURI: Identifica el URI real de la imagen seleccionada
     * @param contentURI: Uri recogido del intent
     * @return URI real
     * */
    public String getRealPathFromURI(Uri contentURI) {
        String res = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(contentURI, proj, null, null, null);
        if(cursor.moveToFirst()){
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }
}
