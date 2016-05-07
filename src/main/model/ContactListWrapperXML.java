package main.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Helper class to wrap a list of contacts. This is used for saving the
 * list of contacts to XML.
 */

@XmlRootElement(name = "contacts")
public class ContactListWrapperXML {

    private List<Contact> contacts;

    @XmlElement(name = "contact")
    public List<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
    }
}
