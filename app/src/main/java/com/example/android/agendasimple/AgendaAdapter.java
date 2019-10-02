package com.example.android.agendasimple;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.agendasimple.sql.ContactEntity;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class AgendaAdapter extends RecyclerView.Adapter<AgendaAdapter.Contact> {

    private ContactClickListener listener;
    private Context ctx;
    private ArrayList<ContactEntity> contacts = new ArrayList<>();

    public AgendaAdapter(ContactClickListener listener, Context ctx) {
        this.listener = listener;
        this.ctx = ctx;
    }

    @NonNull
    @Override
    public Contact onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.contact_element, parent, false);
        return new Contact(view);
    }

    /**
     * onBindViewHolder: Hace set de cada elemento del RecyclerView y lo pobla en la UI, además
     * de programar su funcionalidad
     * */
    @Override
    public void onBindViewHolder(@NonNull Contact holder, int i) {
        final int position = holder.getAdapterPosition();
        Drawable icon = ContextCompat.getDrawable(ctx, R.drawable.background_circle);
        icon.setColorFilter(Color.parseColor(contacts.get(position).getCOLOR_BUBBLE()), PorterDuff.Mode.SRC_ATOP);
        holder.icon_contact.setBackground(icon);

        holder.name_contact.setText(contacts.get(position).getNAME());
        holder.number_contact.setText(contacts.get(position).getPHONE_NUMBER());
        holder.initial_contact.setText(contacts.get(position).getNAME().substring(0, 1));

        holder.call_to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + contacts.get(position).getPHONE_NUMBER()));
                if (intent.resolveActivity(view.getContext().getPackageManager()) != null) {
                    view.getContext().startActivity(intent);
                } else {
                    Toast.makeText(ctx, ctx.getString(R.string.invalid_phone_call), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (contacts == null) {
            return 0;
        } else {
            return contacts.size();
        }
    }

    /**
     * setContactList: Método auxiliar para implementar la lista dinámicamente
     * */
    public void setContactList(ArrayList<ContactEntity> contacts) {
        this.contacts = contacts;
        notifyDataSetChanged();
    }

    public ArrayList<ContactEntity> getContactList() {
        return this.contacts;
    }

    public interface ContactClickListener {
        void onContactClicked(String number);
    }

    class Contact extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView icon_contact, initial_contact, name_contact, number_contact, call_to;

        public Contact(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            icon_contact = itemView.findViewById(R.id.icon_contact);
            initial_contact = itemView.findViewById(R.id.initial_contact);
            name_contact = itemView.findViewById(R.id.name_contact);
            number_contact = itemView.findViewById(R.id.number_contact);
            call_to = itemView.findViewById(R.id.call_to);
        }

        @Override
        public void onClick(View view) {
            String num = contacts.get(getAdapterPosition()).getPHONE_NUMBER();
            listener.onContactClicked(num);
        }
    }

}
