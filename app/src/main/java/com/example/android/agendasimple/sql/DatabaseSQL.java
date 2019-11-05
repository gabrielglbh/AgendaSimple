package com.example.android.agendasimple.sql;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.example.android.agendasimple.contentProvider.ContactContract.ContactEntry;

import java.util.ArrayList;

public class DatabaseSQL extends SQLiteOpenHelper {

    private static final String DB = "contacts";
    private static final int VERSION = 15;

    public static final String TABLE_NAME = "contacts";

    public static final String COLUMN_1 = "NAME";
    public static final String COLUMN_2 = "PHONE_NUMBER";
    public static final String COLUMN_3 = "PHONE";
    public static final String COLUMN_4 = "HOME_ADDRESS";
    public static final String COLUMN_5 = "EMAIL";
    public static final String COLUMN_6 = "COLOR_BUBBLE";
    public static final String COLUMN_7 = "FAVOURITE";
    public static final String COLUMN_8 = "DATE";

    private static DatabaseSQL sInstance;
    private Context context;
    private final String[] ALL_COLUMNS = {
            ContactEntry.COLUMN_1,
            ContactEntry.COLUMN_2,
            ContactEntry.COLUMN_3,
            ContactEntry.COLUMN_4,
            ContactEntry.COLUMN_5,
            ContactEntry.COLUMN_6,
            ContactEntry.COLUMN_7,
            ContactEntry.COLUMN_8
    };

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
        this.context = context;
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
                COLUMN_7 + " VARCHAR(1), " +
                COLUMN_8 + " VARCHAR(23))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    /**
     * insertContact: Query para insertar un nuevo contacto en la base de datos
     * @param e: Entidad a insertar con los campos recogidos de ContactOverview
     * @return Uri del elemento insertado. Null si se ha producido errors
     * */
    public Uri insertContact(ContactEntity e){
        Uri uri = ContactEntry.CONTENT_URI;
        ContentResolver cr = context.getContentResolver();
        ContentValues contentValues = new ContentValues();

        contentValues.put(COLUMN_1, e.getNAME());
        contentValues.put(COLUMN_2, e.getPHONE_NUMBER());
        contentValues.put(COLUMN_3, e.getPHONE());
        contentValues.put(COLUMN_4, e.getHOME_ADDRESS());
        contentValues.put(COLUMN_5, e.getEMAIL());
        contentValues.put(COLUMN_6, e.getCOLOR_BUBBLE());
        contentValues.put(COLUMN_7, e.getFAVOURITE());
        contentValues.put(COLUMN_8, e.getDATE());

        return cr.insert(uri, contentValues);
    }

    /**
     * updateContact: Query para hacer update de un contacto
     * @param e: Entidad a modificar con los campos actualizados de ContactOverview
     * @return numero de filas afectadas. -1 si no se ha producido update
     * */
    public int updateContact(ContactEntity e){
        Uri uri = ContactEntry.CONTENT_URI;
        ContentResolver cr = context.getContentResolver();
        ContentValues contentValues = new ContentValues();

        contentValues.put(COLUMN_1, e.getNAME());
        contentValues.put(COLUMN_2, e.getPHONE_NUMBER());
        contentValues.put(COLUMN_3, e.getPHONE());
        contentValues.put(COLUMN_4, e.getHOME_ADDRESS());
        contentValues.put(COLUMN_5, e.getEMAIL());
        contentValues.put(COLUMN_6, e.getCOLOR_BUBBLE());
        contentValues.put(COLUMN_7, e.getFAVOURITE());
        contentValues.put(COLUMN_8, e.getDATE());

        return cr.update(uri, contentValues, ContactEntry.COLUMN_2 + "=?", new String[] { e.getPHONE_NUMBER() });
    }

    /**
     * deleteContact: Query para hacer update de un contacto
     * @param NUMBER: Primary Key del contacto a eliminar
     * @return numero de filas afectadas. -1 si no se ha producido la eliminación
     * */
    public int deleteContact(String NUMBER){
        Uri uri = ContactEntry.CONTENT_URI;
        ContentResolver cr = context.getContentResolver();
        return cr.delete(uri, ContactEntry.COLUMN_2 + "=?", new String[] { NUMBER });
    }

    /**
     * deleteAllContacts: Query para hacer update de un contacto
     * @return numero de filas afectadas. -1 si no se ha producido la eliminación
     * */
    public int deleteAllContacts(){
        Uri uri = ContactEntry.CONTENT_URI;
        ContentResolver cr = context.getContentResolver();
        return cr.delete(uri, null, null);
    }

    /**
     * getAllContacts: Query para cargar todos los contactos de la base de datos
     * @return ArrayList de contactos con todos los campos disponibles o null si la consulta ha fallado
     * */
    public ArrayList<ContactEntity> getAllContacts(){
        Uri uri = ContactEntry.CONTENT_URI;
        ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(uri, ALL_COLUMNS, null, null,
                ContactEntry.COLUMN_7 + " ASC, " + ContactEntry.COLUMN_1 + " ASC");

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
                        c.getString(c.getColumnIndex(COLUMN_7)),
                        c.getString(c.getColumnIndex(COLUMN_8)))
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
        Uri uri = ContactEntry.CONTENT_URI;
        ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(uri, null, ContactEntry.COLUMN_2 + "=?", new String[] { NUMBER }, null);

        if (c.getCount() > 0 && c.moveToNext()) {
            ContactEntity contact = new ContactEntity(
                    c.getString(c.getColumnIndex(COLUMN_1)),
                    c.getString(c.getColumnIndex(COLUMN_2)),
                    c.getString(c.getColumnIndex(COLUMN_3)),
                    c.getString(c.getColumnIndex(COLUMN_4)),
                    c.getString(c.getColumnIndex(COLUMN_5)),
                    c.getString(c.getColumnIndex(COLUMN_6)),
                    c.getString(c.getColumnIndex(COLUMN_7)),
                    c.getString(c.getColumnIndex(COLUMN_8))
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
        Uri uri = ContactEntry.CONTENT_URI;
        ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(uri, null, ContactEntry.COLUMN_1 + " LIKE ?", new String[] { QUERY + "%" }, null);

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
                        c.getString(c.getColumnIndex(COLUMN_7)),
                        c.getString(c.getColumnIndex(COLUMN_8))
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
