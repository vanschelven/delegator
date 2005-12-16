package org.cq2.delegator.examples.adapter;

import org.cq2.delegator.Delegator;

public abstract class OxbridgeBook {

    public static OxbridgeBook create() {
        return (OxbridgeBook) Delegator.extend(OxbridgeBook.class, new Class[]{CambridgeBook.class, OxfordBook.class});
    }
    
    public abstract String getCode();
    public abstract void setCode(String number);
    
    public String getSerialNumber() {
        return getCode();
    }
    
    public void setSerialNumber(String number) {
        setCode(number);
    }
    
}
