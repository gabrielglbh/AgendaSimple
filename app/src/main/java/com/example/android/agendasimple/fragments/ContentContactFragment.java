package com.example.android.agendasimple.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.CalendarContract.Reminders;
import android.provider.CalendarContract.Events;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.android.agendasimple.ContactOverview;
import com.example.android.agendasimple.MainActivity;
import com.example.android.agendasimple.R;
import com.example.android.agendasimple.sql.ContactEntity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Random;
import java.util.TimeZone;
import java.util.regex.Pattern;

import static android.app.Activity.RESULT_OK;

public class ContentContactFragment extends Fragment {

    private Switch addAlarm;
    private TextInputEditText inputName, inputNumber, inputPhone, inputHome, inputEmail;
    private ImageView nameIcon, numberIcon, phoneIcon, homeIcon, emailIcon, bookmarkIcon,
                        dateDialog, timeDialog, favoriteLandscape, imgLandscape, iconPhoto,
                        checkContact;
    private CardView cardLandscape;
    private TextInputLayout tietEvent;
    private TextInputEditText eventDialog;
    private TextView inputDate, date_display, time_display, title_dialog;
    private Button add_scheduled, cancel_scheduled;
    private Toast mToast;
    private AlertDialog alert, alarmBuilder;

    private onUpdateList listener;

    private ContactEntity contact;

    private String NUMBER;
    private int FAVOURITE = 0;
    private boolean isOverflown = false;
    private String timeToDisplay, dateToDisplay, eventToDisplay;
    private int mode; // Especifica si se ha de llenar los campos del contacto o no
    private boolean isOnPortraitMode;
    private boolean isLikedPressed = false;
    private boolean isAlarmSet = true;
    private long eventID = 0;

    private Pattern namePattern = Pattern.compile("[A-Za-zñáéíóúÑÁÉÍÓÚ A-Za-z()/0-9]+");
    private Context ctx;

    private final int RESULT_LOAD_IMG = 123;
    private final int WRITE_CALENDAR_CODE = 12342;
    private final int WRITE_EXTERNAL_STORAGE_CODE = 46345;

    public ContentContactFragment() { }

    public ContentContactFragment(Context ctx, onUpdateList listener, String NUMBER, int mode) {
        this.NUMBER = NUMBER;
        this.mode = mode;
        this.ctx = ctx;
        this.listener = listener;

        int orientation = ctx.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            isOnPortraitMode = false;
        } else {
            isOnPortraitMode = true;
        }
    }

    public interface onUpdateList {
        void onUpdateContactToList();
    }

    /**********************************************************************************************/

    /****************************** Métodos de Control Sobreescritos ******************************/

    /**********************************************************************************************/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View content = inflater.inflate(R.layout.content_contact_overview, container, false);

        if (ctx != null) {
            setViews(content);
            createScheduledDateDialog();

            final TextInputLayout layoutName = content.findViewById(R.id.tiet_name_layout);
            final TextInputLayout layoutNumber = content.findViewById(R.id.tiet_number_layout);
            final TextInputLayout layoutPhone = content.findViewById(R.id.tiet_home_phone_layout);

            setTextWatchers(inputName, layoutName);
            setTextWatchers(inputNumber, layoutNumber);
            setTextWatchers(inputPhone, layoutPhone);

            populateFragment(mode, NUMBER);
        }

        return content;
    }

    /**
     * onActivityResult: Recoge la imagen de la galería
     * */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == RESULT_LOAD_IMG) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = ctx.getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                imgLandscape.setImageBitmap(rotate(selectedImage, getRealPathFromURI(imageUri)));
                iconPhoto.setVisibility(View.GONE);
            } catch (NullPointerException | IOException e) {
                e.printStackTrace();
                if (contact != null) {
                    if (getImageFromStorage() != null) {
                        iconPhoto.setVisibility(View.GONE);
                    } else {
                        iconPhoto.setVisibility(View.VISIBLE);
                    }
                } else {
                    iconPhoto.setVisibility(View.VISIBLE);
                }
                makeToast(getString(R.string.insertion_image_error));
            }
        } else {
            if (contact != null) {
                if (getImageFromStorage() != null) {
                    iconPhoto.setVisibility(View.GONE);
                } else {
                    iconPhoto.setVisibility(View.VISIBLE);
                }
            } else {
                iconPhoto.setVisibility(View.VISIBLE);
            }
            makeToast(getString(R.string.insertion_image_error));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == WRITE_CALENDAR_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeViewsFromDialog();
            }
        } else if (requestCode == WRITE_EXTERNAL_STORAGE_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                hideKeyboard();
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
            }
        }
    }

    /**********************************************************************************************/

    /********************************** Métodos de Inicialización *********************************/

    /**********************************************************************************************/

    /**
     * checkPermits: Método que verifica si se han permitido los permisos necesarios.
     **/
    private boolean checkPermits(String permission){
        if (Build.VERSION.SDK_INT >= 6) {
            if (ActivityCompat.checkSelfPermission(ctx, permission) != PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        } else {
            return false;
        }
        return false;
    }

    /**
     * setViews: Crea las views del fragmento y crea los listeners necesarios
     * */
    private void setViews(View content) {
        inputName = content.findViewById(R.id.tiet_name);
        inputNumber = content.findViewById(R.id.tiet_number);
        inputPhone = content.findViewById(R.id.tiet_home_phone);
        inputHome = content.findViewById(R.id.tiet_home);
        inputEmail = content.findViewById(R.id.tiet_mail);
        inputDate = content.findViewById(R.id.add_date);

        nameIcon = content.findViewById(R.id.icon_name);
        numberIcon = content.findViewById(R.id.icon_number);
        phoneIcon = content.findViewById(R.id.icon_home_phone);
        homeIcon = content.findViewById(R.id.icon_home);
        emailIcon = content.findViewById(R.id.icon_mail);
        bookmarkIcon = content.findViewById(R.id.icon_date);

        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) bookmarkIcon.getLayoutParams();
        if (isOnPortraitMode) {
            lp.topMargin = dpToPx(48);
            lp.leftMargin = dpToPx(16);
        } else {
            lp.topMargin = dpToPx(112);
            lp.leftMargin = dpToPx(16);
        }
        bookmarkIcon.setLayoutParams(lp);

        favoriteLandscape = content.findViewById(R.id.favorite_landscape);
        favoriteLandscape.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isLikedPressed) {
                    favoriteLandscape.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_heart_filled));
                    FAVOURITE = 0;
                    isLikedPressed = true;
                } else {
                    favoriteLandscape.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_heart_landscape));
                    FAVOURITE = 1;
                    isLikedPressed = false;
                }
            }
        });

        cardLandscape = content.findViewById(R.id.icon_contact_landscape);
        cardLandscape.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkPermits(Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                        checkPermits(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    requestPermissions(new String[] {
                            Manifest.permission.WRITE_EXTERNAL_STORAGE }, WRITE_EXTERNAL_STORAGE_CODE);
                } else {
                    hideKeyboard();
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
                }
            }
        });

        checkContact = content.findViewById(R.id.check_contact);
        checkContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkErrors();
            }
        });

        imgLandscape = content.findViewById(R.id.img_contact_landscape);
        iconPhoto = content.findViewById(R.id.icon_photo);

        if (isOnPortraitMode) {
            favoriteLandscape.setVisibility(View.GONE);
            cardLandscape.setVisibility(View.GONE);
            iconPhoto.setVisibility(View.GONE);
            checkContact.setVisibility(View.GONE);
        } else {
            favoriteLandscape.setVisibility(View.VISIBLE);
            cardLandscape.setVisibility(View.VISIBLE);
            iconPhoto.setVisibility(View.VISIBLE);
            checkContact.setVisibility(View.VISIBLE);
        }
    }

    /**
     * populateFragment: Popula el fragmento con datos dependiendo si el contacto existe o no
     * @param mode: Modo en el que popular el fragmento
     * @param num: Número y clave primaria para la búsqueda del contacto
     * */
    public void populateFragment(int mode, String num) {
        resetView();
        this.mode = mode;
        if (mode == 0) {
            if (num == null) contact = MainActivity.sql.getContact(NUMBER);
            else contact = MainActivity.sql.getContact(num);
            setContact();
        } else {
            if (isOnPortraitMode) {
                ContactOverview.collapsingToolbar.setTitle(getString(R.string.contact_overview_label));
            } else {
                favoriteLandscape.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_heart_landscape));
                FAVOURITE = 1;
                isLikedPressed = false;
            }
            final int color = ContextCompat.getColor(ctx, R.color.colorPrimary);
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
        eventID = contact.getCALENDAR_ID();

        if (isOnPortraitMode) {
            ContactOverview.collapsingToolbar.setTitle(getString(R.string.modify_contact_title));
        } else {
            if (contact.getFAVOURITE() == 0) {
                favoriteLandscape.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_heart_filled));
                FAVOURITE = 0;
                isLikedPressed = true;
            } else {
                favoriteLandscape.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_heart_landscape));
                FAVOURITE = 1;
                isLikedPressed = false;
            }
        }

        if (Build.VERSION.SDK_INT >= 21) {
            if (isOnPortraitMode) {
                Window w = getActivity().getWindow();
                if (bitmap != null) {
                    w.setStatusBarColor(Color.TRANSPARENT);
                } else {
                    w.setStatusBarColor(color);
                }
            }

            setUIToBubbleColor(nameIcon, color);
            setUIToBubbleColor(numberIcon, color);
            setUIToBubbleColor(phoneIcon, color);
            setUIToBubbleColor(homeIcon, color);
            setUIToBubbleColor(emailIcon, color);
            setUIToBubbleColor(bookmarkIcon, color);
        }

        if (isOnPortraitMode) {
            ContactOverview.appbar.setBackgroundColor(color);
            ContactOverview.collapsingToolbar.setContentScrimColor(color);

            if (bitmap != null) {
                ContactOverview.headerImage.setImageBitmap(bitmap);
                ContactOverview.toolbar.setBackgroundColor(Color.TRANSPARENT);
            } else {
                ContactOverview.toolbar.setBackgroundColor(color);
            }
        } else {
            if (bitmap != null) {
                imgLandscape.setImageBitmap(bitmap);
                iconPhoto.setVisibility(View.GONE);
            } else {
                iconPhoto.setVisibility(View.VISIBLE);
            }
        }

        inputName.setText(contact.getNAME());
        inputNumber.setText(contact.getPHONE_NUMBER());
        inputPhone.setText(contact.getPHONE());
        inputHome.setText(contact.getHOME_ADDRESS());
        inputEmail.setText(contact.getEMAIL());
        inputDate.setText(contact.getDATE());
        if (!inputDate.getText().toString().equals(getString(R.string.schedule_day))) {
            eventToDisplay = contact.getDATE().substring(0, contact.getDATE().indexOf("\n"));
            dateToDisplay = contact.getDATE().substring(contact.getDATE().indexOf("\n") + 1,
                    inputDate.getText().length() - 7);
            timeToDisplay = contact.getDATE().substring(inputDate.getText().length() - 6);
        }
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
     * resetView: Reset de los campos y redibujado de los iconos y demás botones
     * */
    private void resetView() {
        final int color = ContextCompat.getColor(ctx, R.color.colorPrimary);
        inputName.setText("");
        inputNumber.setText("");
        inputPhone.setText("");
        inputEmail.setText("");
        inputHome.setText("");
        setUIToBubbleColor(nameIcon, color);
        setUIToBubbleColor(numberIcon, color);
        setUIToBubbleColor(phoneIcon, color);
        setUIToBubbleColor(homeIcon, color);
        setUIToBubbleColor(emailIcon, color);
        setUIToBubbleColor(bookmarkIcon, color);
        inputDate.setText(getString(R.string.schedule_day));
        favoriteLandscape.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_heart_landscape));
        FAVOURITE = 1;
        isLikedPressed = false;
        imgLandscape.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.background_circle));
        DrawableCompat.setTint(imgLandscape.getDrawable(), ContextCompat.getColor(ctx, R.color.colorAccent));
        iconPhoto.setVisibility(View.VISIBLE);
    }

    /**********************************************************************************************/

    /****************** Métodos de creación y manejo de eventos del AlertDialog *******************/

    /**********************************************************************************************/

    /**
     * createScheduledDateDialog: Creación del custom dialog para la adición de una cita con un contacto
     * */
    private void createScheduledDateDialog() {
        inputDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkPermits(Manifest.permission.WRITE_CALENDAR)) {
                    requestPermissions(new String[]{
                            Manifest.permission.WRITE_CALENDAR}, WRITE_CALENDAR_CODE);
                } else {
                    initializeViewsFromDialog();
                }
            }
        });
    }

    /**
     * initializeViewsFromDialog: Creación de los listeners y populación del dialogo
     * */
    private void initializeViewsFromDialog() {
            hideKeyboard();

            View customView = getLayoutInflater().inflate(R.layout.create_alert_dialog, null);
            title_dialog = customView.findViewById(R.id.title_dialog);
            time_display = customView.findViewById(R.id.et_show_time);
            time_display.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openTimePickerDialog();
                }
            });

            date_display = customView.findViewById(R.id.et_show_date);
            date_display.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openDatePickerDialog();
                }
            });

            add_scheduled = customView.findViewById(R.id.add_button);
            add_scheduled.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addDate();
                }
            });

            cancel_scheduled = customView.findViewById(R.id.button_cancel);
            cancel_scheduled.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    cancelDate();
                }
            });

            dateDialog = customView.findViewById(R.id.date_dialog);
            dateDialog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    hideKeyboard();
                    openDatePickerDialog();
                }
            });

            timeDialog = customView.findViewById(R.id.time_dialog);
            timeDialog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    hideKeyboard();
                    openTimePickerDialog();
                }
            });

            tietEvent = customView.findViewById(R.id.tiet_event);
            eventDialog = customView.findViewById(R.id.dialog_event);
            eventDialog.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (charSequence.length() > tietEvent.getCounterMaxLength()) {
                        eventDialog.setError(getString(R.string.dialog_err_large));
                    } else if (charSequence.length() == 0) {
                        eventDialog.setError(getString(R.string.dialog_event_name));
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            });

            addAlarm = customView.findViewById(R.id.switch_alarm_on_calendar);
            addAlarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    isAlarmSet = b;
                }
            });

            if (timeToDisplay != null && dateToDisplay != null && eventToDisplay != null) {
                title_dialog.setText(getString(R.string.modify_date));
                add_scheduled.setText(getString(R.string.modify_scheduled_date));
                cancel_scheduled.setVisibility(View.VISIBLE);
                time_display.setText(timeToDisplay);
                time_display.setTextColor(Color.BLACK);
                date_display.setText(dateToDisplay);
                date_display.setTextColor(Color.BLACK);
                eventDialog.setText(eventToDisplay);
            } else {
                title_dialog.setText(getString(R.string.add_date));
                add_scheduled.setText(getString(R.string.finish_scheduled_date));
                cancel_scheduled.setVisibility(View.GONE);
            }

            AlertDialog.Builder alarm = new AlertDialog.Builder(ctx)
                    .setView(customView);
            alarmBuilder = alarm.create();
            alarmBuilder.show();
        }

    /**
     * openDatePickerDialog: Abre el DatePicker con la fecha actual y guarda la fecha seleccionada
     * */
    private void openDatePickerDialog() {
        DatePickerDialog mDatePicker;
        Calendar mcurrentDate = Calendar.getInstance();
        final int mYear = mcurrentDate.get(Calendar.YEAR);
        final int mMonth = mcurrentDate.get(Calendar.MONTH);
        final int mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);

        mDatePicker = new DatePickerDialog(ctx, R.style.PickerDialogTheme,
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
    private void openTimePickerDialog() {
        TimePickerDialog mTimePicker;
        Calendar mcurrentTime = Calendar.getInstance();
        final int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        final int minute = mcurrentTime.get(Calendar.MINUTE);

        mTimePicker = new TimePickerDialog(ctx, R.style.PickerDialogTheme,
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
    private void addDate() {
        if (timeToDisplay != null && dateToDisplay != null && eventDialog.getError() == null) {
            eventToDisplay = eventDialog.getText().toString();
            alarmBuilder.dismiss();
            String schedule = eventToDisplay + "\n" + dateToDisplay.trim() + " " + getString(R.string.at_time) + " " + timeToDisplay.trim();
            inputDate.setText(schedule);

            addDateToGoogleCalendar(dateToDisplay.trim(), timeToDisplay.trim(), eventToDisplay, inputName.getText().toString());
        } else {
            makeToast(getString(R.string.error_field));
        }
    }

    /**
     * cancelDate: Elimina la cita
     * */
    private void cancelDate() {
        alarmBuilder.dismiss();
        timeToDisplay = null;
        dateToDisplay = null;
        inputDate.setText(getString(R.string.schedule_day));

        cancelDateOnGoogleCalendar();
    }

    /**
     * addDateToGoogleCalendar: Adición y modificación del evento en Google Calendar, además de
     * la creación un reminder del mismo en base a un switch
     * */
    private void addDateToGoogleCalendar(final String date, final String time, final String title,
                                         final String name) {
        if (name.trim().isEmpty()) {
            makeToast(getString(R.string.calendar_err));
        }
        else {
            long calID = 1;
            ContentResolver cr = ctx.getContentResolver();
            ContentValues values = new ContentValues();

            Calendar beginTime = Calendar.getInstance();
            beginTime.set(Integer.parseInt(date.substring(6)),
                    Integer.parseInt(date.substring(3, 5)) - 1,
                    Integer.parseInt(date.substring(0, 2)),
                    (time.charAt(0) == '0' ?
                            Integer.parseInt(time.substring(1, 2)) :
                            Integer.parseInt(time.substring(0, 2))) - 1,
                    Integer.parseInt(time.substring(3)));
            final long startMillis = beginTime.getTimeInMillis();
            Calendar endTime = Calendar.getInstance();

            endTime.set(Integer.parseInt(date.substring(6)),
                    Integer.parseInt(date.substring(3, 5)) - 1,
                    Integer.parseInt(date.substring(0, 2)),
                    (time.charAt(0) == '0' ?
                            Integer.parseInt(time.substring(1, 2)) :
                            Integer.parseInt(time.substring(0, 2))),
                    Integer.parseInt(time.substring(3)));
            final long endMillis = endTime.getTimeInMillis();

            if (cancel_scheduled.getVisibility() == View.GONE) {
                values.put(Events.DTSTART, startMillis);
                values.put(Events.DTEND, endMillis);
                values.put(Events.TITLE, title + " " +  getString(R.string.calendar_with) + " " + name);
                values.put(Events.CALENDAR_ID, calID);
                values.put(Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_CALENDAR)
                        != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                Uri uri = cr.insert(Events.CONTENT_URI, values);
                eventID = Long.parseLong(uri.getLastPathSegment());
            } else {
                values.put(Events.DTSTART, startMillis);
                values.put(Events.DTEND, endMillis);
                values.put(Events.TITLE, title + " " + getString(R.string.calendar_with) + " " + name);
                Uri updateUri = ContentUris.withAppendedId(Events.CONTENT_URI, eventID);
                cr.update(updateUri, values, null, null);
            }
        }

        if (isAlarmSet) {
            ContentResolver cr = ctx.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(Reminders.MINUTES, 30);
            values.put(Reminders.EVENT_ID, eventID);
            values.put(Reminders.METHOD, Reminders.METHOD_ALERT);
            cr.insert(Reminders.CONTENT_URI, values);
        }
    }

    /**
     * cancelDateGoogleCalendar: Cancela el evento asociado a la cita en Google Calendar
     * */
    private void cancelDateOnGoogleCalendar() {
        ContentResolver cr = ctx.getContentResolver();
        Uri deleteUri = ContentUris.withAppendedId(Events.CONTENT_URI, eventID);
        cr.delete(deleteUri, null, null);
    }

    /**********************************************************************************************/

    /****************************** Métodos de Validación del Contacto ****************************/

    /**********************************************************************************************/

    /**
     * validateTextFields: Validación de los campos de NAME, EMAIL y NUMBER. NAME y NUMBER son los únicos
     * obligatorios.
     * */
    private boolean validateTextFields(String name, String email, String number) {
        boolean n, num, em;

        if (name.trim().isEmpty() || !namePattern.matcher(name).matches()) {
            inputName.setError(getString(R.string.error_field));
            n = false;
        } else { n = true; }

        if (number.trim().isEmpty()) {
            inputNumber.setError(getString(R.string.error_field));
            num = false;
        } else { num = true; }

        if (num && n) {
            if (!email.trim().isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                inputEmail.setError(getString(R.string.error_field));
                em = false;
            } else {
                em = true;
            }
        } else {
            if (!email.trim().isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                inputEmail.setError(getString(R.string.error_field));
                em = false;
            } else {
                em = true;
            }
        }

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

        if (validateTextFields(name, email, number) && !isOverflown) {
            if (name.trim().isEmpty() && number.trim().isEmpty() && phone.trim().isEmpty() &&
                    address.trim().isEmpty() && email.trim().isEmpty()) {
                if (isOnPortraitMode) {
                    getActivity().finish();
                } else {
                    makeToast(getString(R.string.frag_contact_discarded));
                }
            } else if (inputName.getText().toString().trim().isEmpty() ||
                    inputNumber.getText().toString().trim().isEmpty()) {
                createDialog(getString(R.string.invalid_insertion_1));
            } else {
                String scheduledDate = checkIsDate(date);
                ContactEntity c = getContact(true, name, number, phone, address, email, scheduledDate);
                try {
                    Bitmap bitmap;
                    if (isOnPortraitMode) {
                        bitmap = ((BitmapDrawable) ContactOverview.headerImage.getDrawable()).getBitmap();
                    } else {
                        bitmap = ((BitmapDrawable) imgLandscape.getDrawable()).getBitmap();
                    }
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
                String scheduledDate = checkIsDate(date);
                ContactEntity c = getContact(false, name, number, phone, address, email, scheduledDate);
                isOkForUpdate(c);
            } else if (name.trim().isEmpty() && number.trim().isEmpty() && phone.trim().isEmpty() &&
                    address.trim().isEmpty() && email.trim().isEmpty()) {
                if (MainActivity.sql.deleteContact(NUMBER) == -1) {
                    makeToast(getString(R.string.deletion_failed));
                } else {
                    if (!removeImageStorage()) {
                        makeToast(getString(R.string.error_delete_img));
                    }
                }
                if (isOnPortraitMode) {
                    getActivity().finish();
                } else {
                    makeToast(getString(R.string.frag_contact_discarded));
                }
            } else {
                if (name.trim().isEmpty() || number.trim().isEmpty()) {
                    createDialog(getString(R.string.invalid_insertion_1));
                } else {
                    if (MainActivity.sql.getContact(number) != null) {
                        String scheduledDate = checkIsDate(date);
                        ContactEntity c = getContact(false, name, number, phone, address, email, scheduledDate);
                        isOkForUpdate(c);
                    } else {
                        if (MainActivity.sql.deleteContact(contact.getPHONE_NUMBER()) != -1) {
                            if (!removeImageStorage()) {
                                makeToast(getString(R.string.error_delete_img));
                            } else {
                                String scheduledDate = checkIsDate(date);
                                ContactEntity c = getContact(false, name, number, phone, address, email, scheduledDate);
                                try {
                                    Bitmap bitmap;
                                    if (isOnPortraitMode) {
                                        bitmap = ((BitmapDrawable) ContactOverview.headerImage.getDrawable()).getBitmap();
                                    } else {
                                        bitmap = ((BitmapDrawable) imgLandscape.getDrawable()).getBitmap();
                                    }
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
     * checkErrors: Validación de errores en base a los textWatchers y Patterns.
     * */
    public void checkErrors() {
        if (inputName.getError() != null || inputNumber.getError() != null) {
            createDialog(getString(R.string.error_on_back));
        } else {
            if (mode == 1) {
                validateAndInsertContact();
            } else {
                validateAndUpdateContact();
            }
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
            Bitmap bitmap;
            if (isOnPortraitMode) {
                bitmap = ((BitmapDrawable) ContactOverview.headerImage.getDrawable()).getBitmap();
            } else {
                bitmap = ((BitmapDrawable) imgLandscape.getDrawable()).getBitmap();
            }
            saveImageToStorage(bitmap);
            update(c);
        } catch (Exception e) {
            removeImageStorage();
            update(c);
        }
    }

    private ContactEntity getContact(boolean insertMode, String name, String number, String phone,
                                     String address, String email, String scheduledDate) {
        if (isOnPortraitMode) {
            if (insertMode) {
                Random r = new Random();
                return new ContactEntity(name, number, phone, address, email,
                        MainActivity.colors[r.nextInt(MainActivity.colors.length)], ContactOverview.FAVOURITE
                        , scheduledDate, eventID);
            } else {
                return new ContactEntity(name, number, phone, address, email,
                        contact.getCOLOR_BUBBLE(), ContactOverview.FAVOURITE, scheduledDate, eventID);
            }
        } else {
            if (insertMode) {
                Random r = new Random();
                return new ContactEntity(name, number, phone, address, email,
                        MainActivity.colors[r.nextInt(MainActivity.colors.length)], FAVOURITE, scheduledDate, eventID);
            } else {
                return new ContactEntity(name, number, phone, address, email,
                        contact.getCOLOR_BUBBLE(), FAVOURITE, scheduledDate, eventID);
            }
        }
    }

    /**********************************************************************************************/

    /********* Métodos de Administración del External Storage y Acceso a Base de Datos ************/

    /**********************************************************************************************/

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

    public Bitmap getImageFromStorage() {
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

    public boolean removeImageStorage() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), getString(R.string.file_path_contact_images));
            if (dir.exists()) {
                File file = new File(dir, contact.getPHONE_NUMBER() + ".png");
                if (isOnPortraitMode) {
                    ContactOverview.headerImage.setImageBitmap(null);
                    ContactOverview.collapsingToolbar.setContentScrimColor(Color.parseColor(contact.getCOLOR_BUBBLE()));
                    ContactOverview.menu.getItem(0).setVisible(false);
                } else {
                    imgLandscape.setImageBitmap(null);
                    iconPhoto.setVisibility(View.VISIBLE);
                }
                return file.delete();
            }
        }
        return false;
    }

    private void insert(ContactEntity c) {
        if (MainActivity.sql.insertContact(c) != null) {
            if (isOnPortraitMode) {
                getActivity().finish();
            } else {
                resetView();
                makeToast(getString(R.string.frag_contact_saved));
            }
            listener.onUpdateContactToList();
        } else {
            createDialog(getString(R.string.insertion_failed));
        }
    }

    private void update(ContactEntity c) {
        if (MainActivity.sql.updateContact(c) != -1) {
            if (isOnPortraitMode) {
                getActivity().finish();
            } else {
                resetView();
                makeToast(getString(R.string.frag_contact_saved));
            }
            listener.onUpdateContactToList();
        } else {
            makeToast(getString(R.string.update_failed));
        }
    }

    /**********************************************************************************************/

    /************************************ Métodos de Utilidad *************************************/

    /**********************************************************************************************/

    public void hideKeyboard() {
        inputName.clearFocus();
        inputNumber.clearFocus();
        inputPhone.clearFocus();
        inputHome.clearFocus();
        inputEmail.clearFocus();

        InputMethodManager imm = (InputMethodManager) ctx.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getActivity().findViewById(android.R.id.content).getWindowToken(), 0);
    }

    public void makeToast(String msg) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(ctx, msg, Toast.LENGTH_SHORT);
        mToast.show();
    }

    private void createDialog(String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
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
                if (isOnPortraitMode) {
                    if (eventID != 0) {
                        cancelDateOnGoogleCalendar();
                    }
                    getActivity().finish();
                }
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
    public Bitmap rotate(Bitmap bitmap, String path) {
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
        Cursor cursor = ctx.getContentResolver().query(contentURI, proj, null, null, null);
        if(cursor.moveToFirst()){
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

    /**
     * dpToPx: Conversión de DP a Píxeles
     * @return Pixeles convertidos
     * */
    private int dpToPx(int dp) {
        return (int) (dp * ctx.getResources().getDisplayMetrics().density);
    }
}
