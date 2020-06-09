package edu.hnu.mail.data.source;

import java.util.List;

import edu.hnu.mail.data.entity.Contact;

public interface ContactDataSource {

    interface LoadCallBack{

        void onLoadSuccess(List<Contact> list);

        void onLoadFailure();
    }

    interface UpdateCallBack{

        void onUpdateSuccess();

        void onUpdateFailure();

    }

    public void getContactList(LoadCallBack loadCallBack);

    public void deleteContact(String mailAddress,UpdateCallBack updateCallBack);

    public void addContact(Contact contact,UpdateCallBack updateCallBack);
}
