package com.example.android.agendasimple.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.CalendarContract;
import android.view.View;
import android.widget.Toast;

import com.example.android.agendasimple.AgendaAdapter;
import com.example.android.agendasimple.MainActivity;
import com.example.android.agendasimple.R;
import com.example.android.agendasimple.fragments.ContentContactFragment;
import com.example.android.agendasimple.sql.ContactEntity;

import java.io.File;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class SwipeHandler extends ItemTouchHelper.Callback {

    private AgendaAdapter adapter;
    private Drawable icon;
    private ColorDrawable background;
    private Context ctx;

    public SwipeHandler(Context ctx, AgendaAdapter adapter) {
        this.adapter = adapter;
        this.ctx = ctx;
        icon = ContextCompat.getDrawable(ctx, R.drawable.ic_delete);
        background = new ColorDrawable(ContextCompat.getColor(ctx, R.color.danger));
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(0, ItemTouchHelper.LEFT);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        ArrayList<ContactEntity> contacts = adapter.getContactList();
        ContactEntity contact = contacts.get(viewHolder.getAdapterPosition());
        if (MainActivity.sql.deleteContact(contact.getPHONE_NUMBER()) == -1) {
            Toast.makeText(ctx, ctx.getString(R.string.deletion_failed), Toast.LENGTH_SHORT).show();
        } else {
            removeImageStorage(contact.getPHONE_NUMBER());
            if (contact.getCALENDAR_ID() != 0) {
                ContentResolver cr = ctx.getContentResolver();
                Uri deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, contact.getCALENDAR_ID());
                cr.delete(deleteUri, null, null);
            }
            contacts.remove(position);
            adapter.notifyItemRemoved(position);

            try {
                FragmentManager fragmentManager = ((MainActivity) ctx).getSupportFragmentManager();
                ContentContactFragment f = (ContentContactFragment) fragmentManager.getFragments().get(0);
                f.resetView();
            } catch (Exception err) {}
        }
    }

    /**
     * onChildDraw: Hace posbile el dibujo de "detrás" del elemento en el RecyclerView con la ayuda de
     * icon y background definidos en el constructor
     * */
    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                            int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        View v = viewHolder.itemView;
        int backgroundCornerOffset = 20;

        int height = v.getHeight();
        int top = v.getTop();
        int left = v.getLeft();
        int right = v.getRight();
        int bottom = v.getBottom();
        int iconIntrHeight = icon.getIntrinsicHeight();
        int iconIntrWidth = icon.getIntrinsicWidth();

        int iconMargin = (height - iconIntrHeight) / 2;
        int iconTop = top + iconMargin;
        int iconBottom = iconTop + iconIntrHeight;

        if (dX > 0) {
            int iconLeft = left + iconMargin;
            int iconRight = iconLeft + iconIntrWidth;
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
            background.setBounds(left, top, left + ((int) dX) + backgroundCornerOffset, bottom);
        }
        else if (dX < 0) {
            int iconLeft = right - iconMargin - iconIntrWidth;
            int iconRight = right - iconMargin;
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
            background.setBounds(right + ((int) dX) - backgroundCornerOffset, top, right, bottom);
        } else {
            background.setBounds(0, 0, 0, 0);
        }
        background.draw(c);
        icon.draw(c);
    }

    private boolean removeImageStorage(String number) {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), ctx.getString(R.string.file_path_contact_images));
            if (dir.exists()) {
                File file = new File(dir, number + ".png");
                return file.delete();
            }
        }
        return false;
    }
}
