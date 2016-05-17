package main.model;

public class ContactJSON {

    private String name;
    private String mobile;
    private String work;
    private String home;
    private String additional;

    public ContactJSON() {
        this(null, null, null, null, null);
    }

    public ContactJSON(String name, String mobile, String work, String home, String additional) {
        this.name = name;
        this.mobile = mobile;
        this.work = work;
        this.home = home;
        this.additional = additional;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getWork() {
        return work;
    }

    public void setWork(String work) {
        this.work = work;
    }

    public String getHome() {
        return home;
    }

    public void setHome(String home) {
        this.home = home;
    }

    public String getAdditional() {
        return additional;
    }

    public void setAdditional(String additional) {
        this.additional = additional;
    }
}
