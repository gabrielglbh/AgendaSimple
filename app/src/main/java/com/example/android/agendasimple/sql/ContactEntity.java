package com.example.android.agendasimple.sql;

public class ContactEntity {

    private String NAME;
    private String PHONE_NUMBER;
    private String PHONE;
    private String HOME_ADDRESS;
    private String EMAIL;
    private String COLOR_BUBBLE;
    private String FAVOURITE; // 0 si es favorito, 1 si no
    private String DATE; // Si no hay citas = getString(R.string.schedule_day)

    public ContactEntity(String NAME, String PHONE_NUMBER, String PHONE, String HOME_ADDRESS,
                         String EMAIL, String COLOR_BUBBLE, String FAVOURITE, String DATE) {
        setNAME(NAME);
        setPHONE_NUMBER(PHONE_NUMBER);
        setPHONE(PHONE);
        setHOME_ADDRESS(HOME_ADDRESS);
        setEMAIL(EMAIL);
        setCOLOR_BUBBLE(COLOR_BUBBLE);
        setFAVOURITE(FAVOURITE);
        setDATE(DATE);
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

    public String getFAVOURITE() {
        return FAVOURITE;
    }

    public String getDATE() {
        return DATE;
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

    private void setFAVOURITE(String FAVOURITE) {
        this.FAVOURITE = FAVOURITE;
    }

    public void setDATE(String DATE) {
        this.DATE = DATE;
    }
}
