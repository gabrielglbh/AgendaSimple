package com.example.android.agendasimple.fragments;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.android.agendasimple.AgendaAdapter;
import com.example.android.agendasimple.ContactOverview;
import com.example.android.agendasimple.MainActivity;
import com.example.android.agendasimple.R;
import com.example.android.agendasimple.sql.ContactEntity;
import com.example.android.agendasimple.utils.SwipeHandler;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ContactListFragment extends Fragment implements AgendaAdapter.ContactClickListener {

    private Context ctx;
    private BottomSheetDialog dialog;
    private RecyclerView rv;
    private AgendaAdapter adapter;
    private ItemTouchHelper touchHelper;

    private boolean isOnPortraitMode = false;
    private onContactClickedToFragment listener;

    public ContactListFragment() { }

    public ContactListFragment(Context ctx, onContactClickedToFragment listener) {
        this.ctx = ctx;
        this.listener = listener;
    }

    public interface onContactClickedToFragment {
        void passDataToFragment(String num);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View content = inflater.inflate(R.layout.content_list_fragment, null);
        setRecyclerView(content);

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            isOnPortraitMode = false;
        } else {
            isOnPortraitMode = true;
        }
        return content;
    }

    /**
     * setRecyclerView: Se cargan todos los contactos con getAllContacts en el RecyclerView, y se
     * ancla el itemTouchHelper para la eliminación de contactos al RecyclerView
     * */
    private void setRecyclerView(View content) {
        rv = content.findViewById(R.id.recycler_view_contacts);
        adapter = new AgendaAdapter(this, ctx);
        adapter.setContactList(MainActivity.sql.getAllContacts());

        LinearLayoutManager lm = new LinearLayoutManager(ctx);
        rv.setLayoutManager(lm);
        rv.setHasFixedSize(false);
        rv.setAdapter(adapter);

        touchHelper = new ItemTouchHelper(new SwipeHandler(getActivity(), adapter));
        touchHelper.attachToRecyclerView(rv);
    }

    public void setContactsIntoAdapter(ArrayList<ContactEntity> contacts) {
        adapter.setContactList(contacts);
    }

    /**
     * onContactClicked: Handler para el click de un contacto para pasar a ContactOverview
     * @param num: PHONE_NUMBER del contacto en la posición clickada del RecyclerView
     * */
    @Override
    public void onContactClicked(String num) {
        if (isOnPortraitMode) {
            Intent goTo = new Intent(ctx, ContactOverview.class);
            goTo.putExtra(MainActivity.OVERVIEW_MODE, 0);
            goTo.putExtra(MainActivity.NUMBER_OF_CONTACTS, num);
            startActivity(goTo);
        } else {
            listener.passDataToFragment(num);
        }
    }

    /**
     * onLongContactClicked: Expande el bottom sheet para acciones recurrentes con los contactos
     * @param number: PHONE_NUMBER del contacto en la posición clickada del RecyclerView
     * @param name: NAME del contacto en la posición clickada del RecyclerView
     * @param phone: PHONE del contacto en la posición clickada del RecyclerView
     * @param home: HOME_ADDRESS del contacto en la posición clickada del RecyclerView
     * @param email: EMAIL del contacto en la posición clickada del RecyclerView
     * @param bubble: COLOR_BUBBLE del contacto en la posición clickada del RecyclerView
     * @param favorite: FAVOURITE del contacto en la posición clickada del RecyclerView
     * @param eventId: CALENDAR_ID del contacto en la posición clickada del RecyclerView
     * @param position: Posición del contacto en el RecyclerView
     * */
    @Override
    // TODO: notifyDataSetChanged()
    public void onLongContactClicked(final String number, final String name, final String phone,
                                     final String home, final String email, final String bubble,
                                     final int favorite, final String date, final long eventId,
                                     final int position) {
        Button view_button, fav_button, del_button;
        TextView title_bottom_sheet, date_bottom_sheet;

        View view = getLayoutInflater().inflate(R.layout.bottom_sheet, null);
        dialog = new BottomSheetDialog(ctx);
        dialog.setContentView(view);

        title_bottom_sheet = view.findViewById(R.id.title_bottom_sheet);
        view_button = view.findViewById(R.id.button_see);
        fav_button = view.findViewById(R.id.button_favourite);
        del_button = view.findViewById(R.id.button_delete);
        date_bottom_sheet = view.findViewById(R.id.has_date_bottom_sheet);

        title_bottom_sheet.setText(name);
        if (favorite == 1) {
            fav_button.setText(getString(R.string.favourite_sheet));
        } else {
            fav_button.setText(getString(R.string.delete_fav_sheet));
        }

        if (!date.equals(getString(R.string.schedule_day))) {
            date_bottom_sheet.setVisibility(View.VISIBLE);
            String dateText = date.replace("\n", " ");
            date_bottom_sheet.setText(dateText);
        } else {
            date_bottom_sheet.setVisibility(View.GONE);
        }

        view_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                onContactClicked(number);
            }
        });
        fav_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                final ContactEntity c;
                if (favorite == 1) {
                    c = new ContactEntity(name, number, phone, home, email, bubble, 0, date, eventId);
                } else {
                    c = new ContactEntity(name, number, phone, home, email, bubble, 1, date, eventId);
                }
                MainActivity.sql.updateContact(c);
                adapter.setContactList(MainActivity.sql.getAllContacts());
            }
        });
        del_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if (eventId != 0) {
                    ContentResolver cr = ctx.getContentResolver();
                    Uri deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId);
                    cr.delete(deleteUri, null, null);
                }
                MainActivity.sql.deleteContact(number);
                adapter.setContactList(MainActivity.sql.getAllContacts());
            }
        });

        dialog.show();
    }
}
