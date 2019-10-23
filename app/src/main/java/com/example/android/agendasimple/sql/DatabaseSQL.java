package com.example.android.agendasimple.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseSQL extends SQLiteOpenHelper {

    private static final String DB = "contacts";
    private static final int VERSION = 11;

    public static final String TABLE_NAME = "contacts";

    public static final String COLUMN_1 = "NAME";
    public static final String COLUMN_2 = "PHONE_NUMBER";
    public static final String COLUMN_3 = "PHONE";
    public static final String COLUMN_4 = "HOME_ADDRESS";
    public static final String COLUMN_5 = "EMAIL";
    public static final String COLUMN_6 = "COLOR_BUBBLE";
    public static final String COLUMN_7 = "FAVOURITE";

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
                TABLE_NAME + "(" +
                COLUMN_1 + " VARCHAR(20), " +
                COLUMN_2 + " VARCHAR(12) PRIMARY KEY, " +
                COLUMN_3 + " VARCHAR(12), " +
                COLUMN_4 + " VARCHAR(40), " +
                COLUMN_5 + " VARCHAR(30), " +
                COLUMN_6 + " VARCHAR(7), " +
                COLUMN_7 + " VARCHAR(1))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
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

        contentValues.put(COLUMN_1, e.getNAME());
        contentValues.put(COLUMN_2, e.getPHONE_NUMBER());
        contentValues.put(COLUMN_3, e.getPHONE());
        contentValues.put(COLUMN_4, e.getHOME_ADDRESS());
        contentValues.put(COLUMN_5, e.getEMAIL());
        contentValues.put(COLUMN_6, e.getCOLOR_BUBBLE());
        contentValues.put(COLUMN_7, e.getFAVOURITE());

        long result = db.insert(TABLE_NAME,null, contentValues);

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

        contentValues.put(COLUMN_1, e.getNAME());
        contentValues.put(COLUMN_2, e.getPHONE_NUMBER());
        contentValues.put(COLUMN_3, e.getPHONE());
        contentValues.put(COLUMN_4, e.getHOME_ADDRESS());
        contentValues.put(COLUMN_5, e.getEMAIL());
        contentValues.put(COLUMN_6, e.getCOLOR_BUBBLE());
        contentValues.put(COLUMN_7, e.getFAVOURITE());

        long result = db.update(TABLE_NAME, contentValues,
                COLUMN_2 + "=?",
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
        long result = db.delete(TABLE_NAME,
                COLUMN_2 + "=?",
                new String[] { NUMBER });
        return result != -1;
    }

    /**
     * deleteAllContacts: Query para hacer update de un contacto
     * @return true si la inserción ha ido bien, false si ha ocurrido un error
     * */
    public boolean deleteAllContacts(){
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(TABLE_NAME,null, null);
        return result != -1;
    }

    /**
     * getAllContacts: Query para cargar todos los contactos de la base de datos
     * @return ArrayList de contactos con todos los campos disponibles o null si la consulta ha fallado
     * */
    public ArrayList<ContactEntity> getAllContacts(){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " +
                TABLE_NAME + " ORDER BY " +
                COLUMN_7 + " ASC, " +
                COLUMN_1 + " ASC";
        Cursor c = db.rawQuery(query, null);

        if (c.getCount() > 0) {
            ArrayList<ContactEntity> contacts = new ArrayList<>();
            while (c.moveToNext()) {
                contacts.add(new ContactEntity(
                        c.getString(c.getColumnIndex(COLUMN_1)),
                        c.getString(c.getColumnIndex(COLUMN_2)),
                        c.getString(c.getColumnIndex(COLUMN_3)),
                        c.getString(c.getColumnIndex(COLUMN_4)),
                        c.getString(c.getColumnIndex(COLUMN_5)),
                        c.getString(c.getColumnIndex(COLUMN_6)),
                        c.getString(c.getColumnIndex(COLUMN_7)))
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
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_2 + " = ?";
        Cursor c = db.rawQuery(query, new String[] { NUMBER });

        if (c.getCount() > 0 && c.moveToNext()) {
            ContactEntity contact = new ContactEntity(
                    c.getString(c.getColumnIndex(COLUMN_1)),
                    c.getString(c.getColumnIndex(COLUMN_2)),
                    c.getString(c.getColumnIndex(COLUMN_3)),
                    c.getString(c.getColumnIndex(COLUMN_4)),
                    c.getString(c.getColumnIndex(COLUMN_5)),
                    c.getString(c.getColumnIndex(COLUMN_6)),
                    c.getString(c.getColumnIndex(COLUMN_7))
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
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_1 + " LIKE ?";
        Cursor c = db.rawQuery(query, new String[] { QUERY + "%" });

        if (c.getCount() > 0) {
            ArrayList<ContactEntity> contacts = new ArrayList<>();
            while (c.moveToNext()) {
                contacts.add(new ContactEntity(
                        c.getString(c.getColumnIndex(COLUMN_1)),
                        c.getString(c.getColumnIndex(COLUMN_2)),
                        c.getString(c.getColumnIndex(COLUMN_3)),
                        c.getString(c.getColumnIndex(COLUMN_4)),
                        c.getString(c.getColumnIndex(COLUMN_5)),
                        c.getString(c.getColumnIndex(COLUMN_6)),
                        c.getString(c.getColumnIndex(COLUMN_7))
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
