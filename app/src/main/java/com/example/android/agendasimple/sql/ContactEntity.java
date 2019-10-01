package com.example.android.agendasimple.sql;

public class ContactEntity {

    private int ID;
    private String NAME;
    private String PHONE_NUMBER;
    private String PHONE;
    private String HOME_ADDRESS;
    private String EMAIL;
    private String COLOR_BUBBLE;

    public ContactEntity(int ID, String NAME, String PHONE_NUMBER, String PHONE, String HOME_ADDRESS,
                         String EMAIL, String COLOR_BUBBLE) {
        setID(ID);
        setNAME(NAME);
        setPHONE_NUMBER(PHONE_NUMBER);
        setPHONE(PHONE);
        setHOME_ADDRESS(HOME_ADDRESS);
        setEMAIL(EMAIL);
        setCOLOR_BUBBLE(COLOR_BUBBLE);
    }

    public int getID() {
        return ID;
    }

    public String getNAME() {
        return NAME;
    }

    public String getPHONE_NUMBER() {
        return PHONE_NUMBER;
    }

    public String getPHONE() {
        return PHONE;
    }

    public String getHOME_ADDRESS() {
        return HOME_ADDRESS;
    }

    public String getEMAIL() {
        return EMAIL;
    }

    public String getCOLOR_BUBBLE() {
        return COLOR_BUBBLE;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public void setNAME(String NAME) {
        this.NAME = NAME;
    }

    public void setPHONE_NUMBER(String PHONE_NUMBER) {
        this.PHONE_NUMBER = PHONE_NUMBER;
    }

    public void setPHONE(String PHONE) {
        this.PHONE = PHONE;
    }

    public void setHOME_ADDRESS(String HOME_ADDRESS) {
        this.HOME_ADDRESS = HOME_ADDRESS;
    }

    public void setEMAIL(String EMAIL) {
        this.EMAIL = EMAIL;
    }

    public void setCOLOR_BUBBLE(String COLOR_BUBBLE) {
        this.COLOR_BUBBLE = COLOR_BUBBLE;
    }
}
