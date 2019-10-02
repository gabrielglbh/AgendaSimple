package com.example.android.agendasimple;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.android.agendasimple.sql.ContactEntity;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
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
        return makeMovementFlags(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
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
        if(!MainActivity.sql.deleteContact(contact.getPHONE_NUMBER())) {
            Toast.makeText(ctx, ctx.getString(R.string.deletion_failed), Toast.LENGTH_SHORT).show();
        } else {
            contacts.remove(position);
            adapter.setContactList(contacts);
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
}
