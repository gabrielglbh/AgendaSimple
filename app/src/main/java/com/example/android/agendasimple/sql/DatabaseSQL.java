package com.example.android.agendasimple.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.agendasimple.contentProvider.ContactContract.ContactEntry;

import java.util.ArrayList;

public class DatabaseSQL extends SQLiteOpenHelper {

    private static final String DB = "contacts";
    private static final int VERSION = 10;

    private static DatabaseSQL sInstance;

    /**
     * Se sigue el patrón del SINGLETON para la creación de referencia a la base de datos
     * para mantener una sola instancia a lo largo del ciclo de vida completo de la aplicación
     * */
    public static synchronized DatabaseSQL getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DatabaseSQL(context.getApplicationContext());
        }
        return sInstance;
    }

    public DatabaseSQL(Context context) {
        super(context, DB, null, VERSION);
    }

    /**
     * PRIMARY KEY: el número del contacto
     * */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " +
                ContactEntry.TABLE_NAME + "(" +
                ContactEntry.COLUMN_1 + " VARCHAR(20), " +
                ContactEntry.COLUMN_2 + " VARCHAR(12) PRIMARY KEY, " +
                ContactEntry.COLUMN_3 + " VARCHAR(12), " +
                ContactEntry.COLUMN_4 + " VARCHAR(40), " +
                ContactEntry.COLUMN_5 + " VARCHAR(30), " +
                ContactEntry.COLUMN_6 + " VARCHAR(7), " +
                ContactEntry.COLUMN_7 + " VARCHAR(1))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ContactEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    /**
     * insertContact: Query para insertar un nuevo contacto en la base de datos
     * @param e: Entidad a insertar con los campos recogidos de ContactOverview
     * @return true si la inserción ha ido bien, false si ha ocurrido un error
     * */
    public boolean insertContact(ContactEntity e){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(ContactEntry.COLUMN_1, e.getNAME());
        contentValues.put(ContactEntry.COLUMN_2, e.getPHONE_NUMBER());
        contentValues.put(ContactEntry.COLUMN_3, e.getPHONE());
        contentValues.put(ContactEntry.COLUMN_4, e.getHOME_ADDRESS());
        contentValues.put(ContactEntry.COLUMN_5, e.getEMAIL());
        contentValues.put(ContactEntry.COLUMN_6, e.getCOLOR_BUBBLE());
        contentValues.put(ContactEntry.COLUMN_7, e.getFAVOURITE());

        long result = db.insert(ContactEntry.TABLE_NAME,null, contentValues);

        return result != -1;
    }

    /**
     * updateContact: Query para hacer update de un contacto
     * @param e: Entidad a modificar con los campos actualizados de ContactOverview
     * @return true si la inserción ha ido bien, false si ha ocurrido un error
     * */
    public boolean updateContact(ContactEntity e){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(ContactEntry.COLUMN_1, e.getNAME());
        contentValues.put(ContactEntry.COLUMN_2, e.getPHONE_NUMBER());
        contentValues.put(ContactEntry.COLUMN_3, e.getPHONE());
        contentValues.put(ContactEntry.COLUMN_4, e.getHOME_ADDRESS());
        contentValues.put(ContactEntry.COLUMN_5, e.getEMAIL());
        contentValues.put(ContactEntry.COLUMN_6, e.getCOLOR_BUBBLE());
        contentValues.put(ContactEntry.COLUMN_7, e.getFAVOURITE());

        long result = db.update(ContactEntry.TABLE_NAME, contentValues,
                ContactEntry.COLUMN_2 + "=?",
                new String[] { e.getPHONE_NUMBER() });

        return result != -1;
    }

    /**
     * deleteContact: Query para hacer update de un contacto
     * @param NUMBER: Primary Key del contacto a eliminar
     * @return true si la inserción ha ido bien, false si ha ocurrido un error
     * */
    public boolean deleteContact(String NUMBER){
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(ContactEntry.TABLE_NAME,
                ContactEntry.COLUMN_2 + "=?",
                new String[] { NUMBER });
        return result != -1;
    }

    /**
     * deleteAllContacts: Query para hacer update de un contacto
     * @return true si la inserción ha ido bien, false si ha ocurrido un error
     * */
    public boolean deleteAllContacts(){
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(ContactEntry.TABLE_NAME,null, null);
        return result != -1;
    }

    /**
     * getAllContacts: Query para cargar todos los contactos de la base de datos
     * @return ArrayList de contactos con todos los campos disponibles o null si la consulta ha fallado
     * */
    public ArrayList<ContactEntity> getAllContacts(){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " +
                ContactEntry.TABLE_NAME + " ORDER BY " +
                ContactEntry.COLUMN_7 + " ASC, " +
                ContactEntry.COLUMN_1 + " ASC";
        Cursor c = db.rawQuery(query, null);

        if (c.getCount() > 0) {
            ArrayList<ContactEntity> contacts = new ArrayList<>();
            while (c.moveToNext()) {
                contacts.add(new ContactEntity(
                        c.getString(c.getColumnIndex(ContactEntry.COLUMN_1)),
                        c.getString(c.getColumnIndex(ContactEntry.COLUMN_2)),
                        c.getString(c.getColumnIndex(ContactEntry.COLUMN_3)),
                        c.getString(c.getColumnIndex(ContactEntry.COLUMN_4)),
                        c.getString(c.getColumnIndex(ContactEntry.COLUMN_5)),
                        c.getString(c.getColumnIndex(ContactEntry.COLUMN_6)),
                        c.getString(c.getColumnIndex(ContactEntry.COLUMN_7)))
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
     * getContact: Query para cargar un determinado contacto de la base de datos determinado por ID
     * @param NUMBER: Primary Key del contacto a seleccionar
     * @return del contacto en cuestión o null si la consulta ha fallado
     * */
    public ContactEntity getContact(String NUMBER){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + ContactEntry.TABLE_NAME + " WHERE " + ContactEntry.COLUMN_2 + " = ?";
        Cursor c = db.rawQuery(query, new String[] { NUMBER });

        if (c.getCount() > 0 && c.moveToNext()) {
            ContactEntity contact = new ContactEntity(
                    c.getString(c.getColumnIndex(ContactEntry.COLUMN_1)),
                    c.getString(c.getColumnIndex(ContactEntry.COLUMN_2)),
                    c.getString(c.getColumnIndex(ContactEntry.COLUMN_3)),
                    c.getString(c.getColumnIndex(ContactEntry.COLUMN_4)),
                    c.getString(c.getColumnIndex(ContactEntry.COLUMN_5)),
                    c.getString(c.getColumnIndex(ContactEntry.COLUMN_6)),
                    c.getString(c.getColumnIndex(ContactEntry.COLUMN_7))
            );
            c.close();
            return contact;
        } else {
            c.close();
            return null;
        }
    }

    /**
     * getSearchedContacts: Query para cargar todos los contactos que lleven en NAME el texto QUERY
     * @param QUERY: Texto introducido por el usuario que sirve para la WHERE clause
     * @return ArrayList de contactos con todos los campos disponibles o null si la consulta ha fallado
     * */
    public ArrayList<ContactEntity> getSearchedContacts(String QUERY){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + ContactEntry.TABLE_NAME + " WHERE " + ContactEntry.COLUMN_1 + " LIKE ?";
        Cursor c = db.rawQuery(query, new String[] { QUERY + "%" });

        if (c.getCount() > 0) {
            ArrayList<ContactEntity> contacts = new ArrayList<>();
            while (c.moveToNext()) {
                contacts.add(new ContactEntity(
                        c.getString(c.getColumnIndex(ContactEntry.COLUMN_1)),
                        c.getString(c.getColumnIndex(ContactEntry.COLUMN_2)),
                        c.getString(c.getColumnIndex(ContactEntry.COLUMN_3)),
                        c.getString(c.getColumnIndex(ContactEntry.COLUMN_4)),
                        c.getString(c.getColumnIndex(ContactEntry.COLUMN_5)),
                        c.getString(c.getColumnIndex(ContactEntry.COLUMN_6)),
                        c.getString(c.getColumnIndex(ContactEntry.COLUMN_7))
                ));
            }
            c.close();
            return contacts;
        } else {
            c.close();
            return null;
        }
    }
}
