package edu.hnu.mail.page.contact;

import java.util.List;

import edu.hnu.mail.data.entity.Contact;
import edu.hnu.mail.data.source.ContactDataSource;

public class ContactPresenter implements ContactContract.Presenter {

    private ContactDataSource contactDataSource;
    private ContactContract.View mView;

    public ContactPresenter(ContactDataSource contactDataSource,ContactContract.View mView){
        this.contactDataSource = contactDataSource;
        this.mView = mView;
    }

    @Override
    public void getContactList() {
        contactDataSource.getContactList(new ContactDataSource.LoadCallBack() {
            @Override
            public void onLoadSuccess(List<Contact> list) {
                mView.showContactList(list);
            }

            @Override
            public void onLoadFailure() {
                mView.showLoadError();
            }
        });
    }

    @Override
    public void addContact(Contact contact) {
        contactDataSource.addContact(contact, new ContactDataSource.UpdateCallBack() {
            @Override
            public void onUpdateSuccess() {
                mView.showSuccess();
            }

            @Override
            public void onUpdateFailure() {
                mView.showUpdateError();
            }
        });
    }

    @Override
    public void deleteContact(String mailAddress) {
        contactDataSource.deleteContact(mailAddress, new ContactDataSource.UpdateCallBack() {
            @Override
            public void onUpdateSuccess() {
                mView.showSuccess();
            }

            @Override
            public void onUpdateFailure() {
                mView.showUpdateError();
            }
        });
    }

}
