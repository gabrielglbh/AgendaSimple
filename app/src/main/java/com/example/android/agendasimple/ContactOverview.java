package com.example.android.agendasimple;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ImageView;

import com.example.android.agendasimple.fragments.ContentContactFragment;
import com.example.android.agendasimple.sql.ContactEntity;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.io.InputStream;

public class ContactOverview extends AppCompatActivity implements ContentContactFragment.onUpdateList {

    private FloatingActionButton checkContact;
    public static AppBarLayout appbar;
    public static CollapsingToolbarLayout collapsingToolbar;
    public static Toolbar toolbar;

    public static ImageView headerImage;
    public static Menu menu;

    private ContactEntity contact;
    private int mode = 1; // Especifica si se ha de llenar los campos del contacto o no
    public static String NUMBER, FAVOURITE = "1";
    private final int RESULT_LOAD_IMG = 123;

    private boolean isLikedPressed = false;

    private ContentContactFragment frag;

    // TODO: Al rotar el dispositivo en esta actividad, ir a la primera.
    //  Ahora mismo para arreglar esto, screenOrientation = portrait en el manisfest en la
    //  sección de la actividad.

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
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            // ID del contendor del layout de la actividad
            frag = new ContentContactFragment(ContactOverview.this, this, NUMBER, mode);
            fragmentTransaction.add(R.id.nested_scroll_view, frag);
            fragmentTransaction.commit();
            setViews();
        }
        else {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            frag = new ContentContactFragment(ContactOverview.this, this, NUMBER, mode);
            fragmentTransaction.add(R.id.nested_scroll_view, frag);
            fragmentTransaction.commit();
            setViews();
        }
    }

    /**
     * onBackPressed: Funcionalidad de validación de los campos al pulsar el botón de hardware 'atrás'
     * */
    @Override
    public void onBackPressed() {
        frag.checkErrors();
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
            if (frag.getImageFromStorage() != null) {
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
                frag.checkErrors();
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
                frag.removeImageStorage();
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
                headerImage.setImageBitmap(frag.rotate(selectedImage, frag.getRealPathFromURI(imageUri)));
                toolbar.setBackgroundColor(Color.TRANSPARENT);
                menu.getItem(0).setVisible(true);
                if (Build.VERSION.SDK_INT >= 21) {
                    Window w = getWindow();
                    w.setStatusBarColor(Color.TRANSPARENT);
                }
            } catch (NullPointerException | IOException e) {
                e.printStackTrace();
                frag.makeToast(getString(R.string.insertion_image_error));
            }
        } else {
            frag.makeToast(getString(R.string.insertion_image_error));
        }
    }

    @Override
    public void onUpdateContactToList() { }

    /**
     * setViews: Busca y configura los elementos de la vista actual
     */
    private void setViews() {
        appbar = findViewById(R.id.appBarLayout);
        collapsingToolbar = findViewById(R.id.collapsingToolbar);
        toolbar = findViewById(R.id.toolbar);

        headerImage = findViewById(R.id.headerImage);
        checkContact = findViewById(R.id.floating_share);

        setAppBar();
        setFloatingActionButton();

        if (mode == 0) {
            collapsingToolbar.setTitle(getString(R.string.modify_contact_title));
            contact = MainActivity.sql.getContact(NUMBER);
        } else {
            collapsingToolbar.setTitle(getString(R.string.contact_overview_label));
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
                        if (contact == null) {
                            w.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                        }
                        else w.setStatusBarColor(Color.parseColor(contact.getCOLOR_BUBBLE()));
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
            final Interpolator interpolator = AnimationUtils.loadInterpolator(getBaseContext(),
                    android.R.interpolator.fast_out_slow_in);
            checkContact.animate()
                    .scaleX(1)
                    .scaleY(1)
                    .setInterpolator(interpolator)
                    .setDuration(600);
        }

        checkContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                frag.hideKeyboard();
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
            }
        });
    }
}
