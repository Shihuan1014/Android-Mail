package edu.hnu.mail.page.contact;

import java.util.List;

import edu.hnu.mail.BaseView;
import edu.hnu.mail.data.entity.Contact;
import edu.hnu.mail.page.mailbox.MailBoxContract;

public class ContactContract {

    interface View {
        void showContactList(List<Contact> list);
        void showLoadError();
        void showUpdateError();
        void showSuccess();
    }

    interface Presenter {
        void getContactList();
        void addContact(Contact contact);
        void deleteContact(String mailAddress);
    }
}
