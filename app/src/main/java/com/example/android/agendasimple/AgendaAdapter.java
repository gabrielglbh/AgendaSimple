package com.example.android.agendasimple;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Random;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class AgendaAdapter extends RecyclerView.Adapter<AgendaAdapter.Contact> {

    private ContactClickListener listener;
    private int numberOfItems;
    private Context ctx;
    private int[] colors = { R.color.shape_1, R.color.shape_2, R.color.shape_3, R.color.shape_4,
            R.color.shape_5, R.color.shape_6 };
    private String[] names = { "Gabriel García", "Marta Macías", "Raúl García", "Mª Jesús López",
            "Guillermo Escobero", "Sergio Valdivieso"};
    private String[] numbers = { "3453455", "56856788", "76754332", "423467586",
            "9999876", "876543456"};

    public AgendaAdapter(int items, ContactClickListener listener, Context ctx) {
        numberOfItems = items;
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

    @Override
    public void onBindViewHolder(@NonNull Contact holder, int i) {
        int position = holder.getAdapterPosition();
        Drawable icon = ContextCompat.getDrawable(ctx, R.drawable.background_circle);
        Random rand = new Random();
        icon.setColorFilter(ContextCompat.getColor(ctx, colors[rand.nextInt(5)]), PorterDuff.Mode.SRC_ATOP);
        holder.icon_contact.setBackground(icon);

        holder.name_contact.setText(names[position]);
        holder.number_contact.setText(numbers[position]);
        holder.initial_contact.setText(names[position].substring(0,1));
    }

    @Override
    public int getItemCount() {
        return numberOfItems;
    }

    public interface ContactClickListener {
        void onContactClicked(int pos);
    }

    class Contact extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView icon_contact, initial_contact, name_contact, number_contact;

        public Contact(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            icon_contact = itemView.findViewById(R.id.icon_contact);
            initial_contact = itemView.findViewById(R.id.initial_contact);
            name_contact = itemView.findViewById(R.id.name_contact);
            number_contact = itemView.findViewById(R.id.number_contact);
        }

        @Override
        public void onClick(View view) {
            int pos = getAdapterPosition();
            listener.onContactClicked(pos);
        }
    }

}
