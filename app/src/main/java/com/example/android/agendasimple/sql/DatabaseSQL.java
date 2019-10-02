package com.example.android.agendasimple.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DatabaseSQL extends SQLiteOpenHelper {

    private static final String DB = "contacts";
    private static final String TABLE_NAME = "contact";
    private static final int VERSION = 2;
    private static final String coloum_1 = "ID";
    private static final String coloum_2 = "NAME";
    private static final String coloum_3 = "PHONE_NUMBER";
    private static final String coloum_4 = "PHONE";
    private static final String coloum_5 = "HOME_ADDRESS";
    private static final String coloum_6 = "EMAIL";
    private static final String coloum_7 = "COLOR_BUBBLE";

    public DatabaseSQL(Context context) {
        super(context, DB, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_NAME + "(" + coloum_1 + " INTEGER PRIMARY KEY " +
                "AUTOINCREMENT, " + coloum_2 + " TEXT, " + coloum_3 + " TEXT, " + coloum_4 + " TEXT, " +
                "" + coloum_5 + " TEXT, " + coloum_6 + " TEXT, " +
                "" + coloum_7 + " INTEGER NOT NULL DEFAULT 0)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    /**
     *
     * insertContact: Query para insertar un nuevo contacto en la base de datos
     *
     * @return true si la inserción ha ido bien, false si ha ocurrido un error
     *
     * */
    public boolean insertContact(ContactEntity e){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(coloum_1, e.getID());
        contentValues.put(coloum_2, e.getNAME());
        contentValues.put(coloum_3, e.getPHONE_NUMBER());
        contentValues.put(coloum_4, e.getPHONE());
        contentValues.put(coloum_5, e.getHOME_ADDRESS());
        contentValues.put(coloum_6, e.getEMAIL());
        contentValues.put(coloum_7, e.getCOLOR_BUBBLE());

        long result = db.insert(TABLE_NAME,null, contentValues);

        return result != -1;
    }

    /**
     *
     * updateContact: Query para hacer update de un contacto
     *
     * @return true si la inserción ha ido bien, false si ha ocurrido un error
     *
     * */
    public boolean updateContact(ContactEntity e){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(coloum_1, e.getID());
        contentValues.put(coloum_2, e.getNAME());
        contentValues.put(coloum_3, e.getPHONE_NUMBER());
        contentValues.put(coloum_4, e.getPHONE());
        contentValues.put(coloum_5, e.getHOME_ADDRESS());
        contentValues.put(coloum_6, e.getEMAIL());
        contentValues.put(coloum_7, e.getCOLOR_BUBBLE());

        long result = db.update(TABLE_NAME, contentValues, "ID=?", new String[] { Integer.toString(e.getID()) });

        return result != -1;
    }

    /**
     *
     * deleteContact: Query para hacer update de un contacto
     *
     * @return true si la inserción ha ido bien, false si ha ocurrido un error
     *
     * */
    public boolean deleteContact(int ID){
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(TABLE_NAME,"ID=?", new String[] { Integer.toString(ID) });

        return result != -1;
    }

    /**
     *
     * getAllContacts: Query para cargar todos los contactos de la base de datos
     *
     * @return ArrayList de contactos con todos los campos disponibles o null si la consulta ha fallado
     *
     * */
    public ArrayList<ContactEntity> getAllContacts(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + coloum_2 + " ASC";
        Cursor c = db.rawQuery(query, null);

        if (c.getCount() > 0) {
            ArrayList<ContactEntity> contacts = new ArrayList<>();
            while (c.moveToNext()) {
                contacts.add(new ContactEntity(
                        c.getInt(c.getColumnIndex(coloum_1)),
                        c.getString(c.getColumnIndex(coloum_2)),
                        c.getString(c.getColumnIndex(coloum_3)),
                        c.getString(c.getColumnIndex(coloum_4)),
                        c.getString(c.getColumnIndex(coloum_5)),
                        c.getString(c.getColumnIndex(coloum_6)),
                        c.getString(c.getColumnIndex(coloum_7)))
                );
            }
            c.close();
            return contacts;
        } else {
            c.close();
            return null;
        }
    }

    /**
     *
     * getContact: Query para cargar un determinado contacto de la base de datos determinado por ID
     *
     * @return del contacto en cuestión o null si la consulta ha fallado
     *
     * */
    public ContactEntity getContact(int ID){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + coloum_1 + " = ?";
        Cursor c = db.rawQuery(query, new String[] { String.valueOf(ID) });

        if (c.getCount() > 0 && c.moveToNext()) {
            ContactEntity contact = new ContactEntity(
                    c.getInt(c.getColumnIndex(coloum_1)),
                    c.getString(c.getColumnIndex(coloum_2)),
                    c.getString(c.getColumnIndex(coloum_3)),
                    c.getString(c.getColumnIndex(coloum_4)),
                    c.getString(c.getColumnIndex(coloum_5)),
                    c.getString(c.getColumnIndex(coloum_6)),
                    c.getString(c.getColumnIndex(coloum_7))
            );
            c.close();
            return contact;
        } else {
            c.close();
            return null;
        }
    }
}
