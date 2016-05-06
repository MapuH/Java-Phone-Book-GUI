package main.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Contact {

    private StringProperty name;
    private StringProperty mobile;
    private StringProperty work;
    private StringProperty home;
    private StringProperty additional;

    public Contact() {
        this(null, null, null, null, null);
    }

    public Contact(String name, String mobile, String work, String home, String additional) {
        this.name = new SimpleStringProperty(name);
        this.mobile = new SimpleStringProperty(mobile);
        this.work = new SimpleStringProperty(work);
        this.home = new SimpleStringProperty(home);
        this.additional = new SimpleStringProperty(additional);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getMobile() {
        return mobile.get();
    }

    public StringProperty mobileProperty() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile.set(mobile);
    }

    public String getWork() {
        return work.get();
    }

    public StringProperty workProperty() {
        return work;
    }

    public void setWork(String work) {
        this.work.set(work);
    }

    public String getHome() {
        return home.get();
    }

    public StringProperty homeProperty() {
        return home;
    }

    public void setHome(String home) {
        this.home.set(home);
    }

    public String getAdditional() {
        return additional.get();
    }

    public StringProperty additionalProperty() {
        return additional;
    }

    public void setAdditional(String additional) {
        this.additional.set(additional);
    }
}

