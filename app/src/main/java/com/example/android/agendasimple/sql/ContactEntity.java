package com.example.android.agendasimple.sql;

public class ContactEntity {

    private String NAME;
    private String PHONE_NUMBER;
    private String PHONE;
    private String HOME_ADDRESS;
    private String EMAIL;
    private String COLOR_BUBBLE;
    private int FAVOURITE; // 0 si es favorito, 1 si no
    private String DATE; // Si no hay citas = getString(R.string.schedule_day)
    private long CALENDAR_ID; // 0 si no hay evento

    public ContactEntity(String NAME, String PHONE_NUMBER, String PHONE, String HOME_ADDRESS,
                         String EMAIL, String COLOR_BUBBLE, int FAVOURITE, String DATE,
                         long CALENDAR_ID) {
        setNAME(NAME);
        setPHONE_NUMBER(PHONE_NUMBER);
        setPHONE(PHONE);
        setHOME_ADDRESS(HOME_ADDRESS);
        setEMAIL(EMAIL);
        setCOLOR_BUBBLE(COLOR_BUBBLE);
        setFAVOURITE(FAVOURITE);
        setDATE(DATE);
        setCALENDAR_ID(CALENDAR_ID);
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

    public int getFAVOURITE() {
        return FAVOURITE;
    }

    public String getDATE() {
        return DATE;
    }

    public long getCALENDAR_ID() {
        return CALENDAR_ID;
    }

    private void setNAME(String NAME) {
        this.NAME = NAME;
    }

    private void setPHONE_NUMBER(String PHONE_NUMBER) {
        this.PHONE_NUMBER = PHONE_NUMBER;
    }

    private void setPHONE(String PHONE) {
        this.PHONE = PHONE;
    }

    private void setHOME_ADDRESS(String HOME_ADDRESS) {
        this.HOME_ADDRESS = HOME_ADDRESS;
    }

    private void setEMAIL(String EMAIL) {
        this.EMAIL = EMAIL;
    }

    private void setCOLOR_BUBBLE(String COLOR_BUBBLE) {
        this.COLOR_BUBBLE = COLOR_BUBBLE;
    }

    private void setFAVOURITE(int FAVOURITE) {
        this.FAVOURITE = FAVOURITE;
    }

    public void setDATE(String DATE) {
        this.DATE = DATE;
    }

    public void setCALENDAR_ID(long CALENDAR_ID) {
        this.CALENDAR_ID = CALENDAR_ID;
    }
}
