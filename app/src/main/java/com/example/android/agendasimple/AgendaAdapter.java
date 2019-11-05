package com.example.android.agendasimple;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.agendasimple.sql.ContactEntity;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

public class AgendaAdapter extends RecyclerView.Adapter<AgendaAdapter.Contact> {

    private ContactClickListener listener;
    private Context ctx;
    private BottomSheetBehavior bsb;
    private ArrayList<ContactEntity> contacts = new ArrayList<>();

    public AgendaAdapter(ContactClickListener listener, Context ctx, BottomSheetBehavior bsb) {
        this.listener = listener;
        this.ctx = ctx;
        this.bsb = bsb;
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
     * Se hace set de dos onClickListeners para el acceso a la llamada y para mandar un mensaje de whatsapp
     * */
    @Override
    public void onBindViewHolder(@NonNull Contact holder, int i) {
        final int position = holder.getAdapterPosition();
        final ContactEntity c = contacts.get(position);
        final Bitmap bitmap = isImageInContact(c.getPHONE_NUMBER(), c.getNAME());

        DrawableCompat.setTint(holder.icon_contact.getDrawable(), Color.parseColor(c.getCOLOR_BUBBLE()));
        if (bitmap != null) {
            holder.setIsRecyclable(false);
            holder.icon_contact.setImageBitmap(bitmap);
            holder.initial_contact.setVisibility(View.GONE);
        } else {
            holder.initial_contact.setVisibility(View.VISIBLE);
        }

        if (!c.getDATE().equals(ctx.getString(R.string.schedule_day))) {
            holder.has_date.setVisibility(View.VISIBLE);
        } else {
            holder.has_date.setVisibility(View.GONE);
        }

        holder.name_contact.setText(c.getNAME());
        holder.number_contact.setText(c.getPHONE_NUMBER());
        holder.initial_contact.setText(c.getNAME().substring(0, 1));

        if (c.getFAVOURITE().equals("0")) {
            holder.fav.setVisibility(View.VISIBLE);
        } else {
            holder.fav.setVisibility(View.GONE);
        }

        holder.open_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                PopupMenu popup = new PopupMenu(view.getContext(), view);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.whatsapp_to:
                                closeBottomSheet();
                                openWhatsappConversation(position, view.getContext());
                                break;
                            case R.id.call_to:
                                closeBottomSheet();
                                callDial(position, view.getContext());
                                break;
                            case R.id.mail_to:
                                closeBottomSheet();
                                sendMailTo(position, view.getContext());
                                break;
                        }
                        return true;
                    }
                });
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.options_contact_menu, popup.getMenu());
                popup.show();
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
     * callDial: Abre la pestaña de llamada para llamar con el número deseado
     * @param c: Contexto de la aplicación
     * @param position: Posición del elemento en el RecyclerView
     **/
    private void callDial(int position, Context c) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + contacts.get(position).getPHONE_NUMBER()));
        if (intent.resolveActivity(c.getPackageManager()) != null) {
            c.startActivity(intent);
        } else {
            Toast.makeText(ctx, ctx.getString(R.string.invalid_phone_call), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * openWhatsappConversation: Abre la WhatsApp para mandar un mensaje al contacto deseado
     * @param c: Contexto de la aplicación
     * @param position: Posición del elemento en el RecyclerView
     **/
    private void openWhatsappConversation(int position, Context c) {
        PackageManager pm = c.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_VIEW);

        try {
            String number = contacts.get(position).getPHONE_NUMBER();
            String url;
            if (!number.contains("+34")) {
                url = "https://api.whatsapp.com/send?phone=+34" + number +
                        "&text=" + URLEncoder.encode("", "UTF-8");
            } else {
                url = "https://api.whatsapp.com/send?phone=" + number +
                        "&text=" + URLEncoder.encode("", "UTF-8");
            }
            intent.setPackage("com.whatsapp");
            intent.setData(Uri.parse(url));
            if (intent.resolveActivity(pm) != null) {
                c.startActivity(intent);
            } else {
                Toast.makeText(ctx, ctx.getString(R.string.whatsapp_error), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception err) {
            Toast.makeText(ctx, ctx.getString(R.string.invalid_phone_call), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * sendMailTo: Abre la aplicación de correo electrónico instalada para mandar un correo al contacto deseado
     * @param c: Contexto de la aplicación
     * @param position: Posición del elemento en el RecyclerView
     **/
    private void sendMailTo(int position, Context c) {
        if (contacts.get(position).getEMAIL() == null ||
                contacts.get(position).getEMAIL().trim().isEmpty()) {
            Toast.makeText(ctx, ctx.getString(R.string.email_field_empty), Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:"));
            intent.putExtra(Intent.EXTRA_EMAIL, contacts.get(position).getEMAIL());
            if (intent.resolveActivity(c.getPackageManager()) != null) {
                c.startActivity(Intent.createChooser(intent, ctx.getString(R.string.chooser_email)));
            }
        }
    }

    /**
     * setContactList: Método auxiliar para implementar la lista dinámicamente
     * */
    public void setContactList(ArrayList<ContactEntity> contacts) {
        this.contacts = contacts;
        notifyDataSetChanged();
    }

    public ArrayList<ContactEntity> getContactList() { return this.contacts; }

    /**
     * isImageInContact: Hace retrieve de la imagen asociada al contacto
     * @param number: número del contacto
     * @param name: nombre del contacto
     * @return Bitmap de la imagen
     * */
    private Bitmap isImageInContact(String number, String name) {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), ctx.getString(R.string.app_name));
            if (dir.exists()) {
                File file = new File(dir, number + "_" + name + ".png");
                if (file.exists()) return BitmapFactory.decodeFile(file.getPath());
            } else {
                Toast.makeText(ctx, ctx.getString(R.string.import_to_SD), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(ctx, ctx.getString(R.string.import_to_SD), Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    private void closeBottomSheet() {
        if (bsb.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bsb.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }

    public interface ContactClickListener {
        void onContactClicked(String number);
        void onLongContactClicked(String number, String name, String phone,
                                  String home, String email, String bubble,
                                  String favorite, String date, int position);
    }

    class Contact extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        TextView initial_contact, name_contact, number_contact, open_menu;
        ImageView icon_contact, fav, has_date;

        public Contact(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            icon_contact = itemView.findViewById(R.id.icon_contact);
            initial_contact = itemView.findViewById(R.id.initial_contact);
            name_contact = itemView.findViewById(R.id.name_contact);
            number_contact = itemView.findViewById(R.id.number_contact);
            fav = itemView.findViewById(R.id.favorite_rv);
            has_date = itemView.findViewById(R.id.date_rv);
            open_menu = itemView.findViewById(R.id.open_menu);
        }

        @Override
        public void onClick(View view) {
            String num = contacts.get(getAdapterPosition()).getPHONE_NUMBER();
            listener.onContactClicked(num);
        }

        @Override
        public boolean onLongClick(View view) {
            String num = contacts.get(getAdapterPosition()).getPHONE_NUMBER();
            String name = contacts.get(getAdapterPosition()).getNAME();
            String phone = contacts.get(getAdapterPosition()).getPHONE();
            String home = contacts.get(getAdapterPosition()).getHOME_ADDRESS();
            String email = contacts.get(getAdapterPosition()).getEMAIL();
            String favorite = contacts.get(getAdapterPosition()).getFAVOURITE();
            String bubble = contacts.get(getAdapterPosition()).getCOLOR_BUBBLE();
            String date = contacts.get(getAdapterPosition()).getDATE();
            listener.onLongContactClicked(num, name, phone, home, email, bubble, favorite, date, getAdapterPosition());
            return true;
        }
    }

}
