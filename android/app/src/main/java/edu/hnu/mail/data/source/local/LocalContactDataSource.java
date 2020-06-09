package edu.hnu.mail.data.source.local;

import android.content.Context;

import java.util.List;

import edu.hnu.mail.data.dao.AttachmentDao;
import edu.hnu.mail.data.dao.ContactDao;
import edu.hnu.mail.data.dao.DaoManager;
import edu.hnu.mail.data.dao.DaoSession;
import edu.hnu.mail.data.dao.DataDao;
import edu.hnu.mail.data.dao.MailDao;
import edu.hnu.mail.data.entity.Contact;
import edu.hnu.mail.data.source.ContactDataSource;

public class LocalContactDataSource implements ContactDataSource {


    private DaoSession daoSession;
    private ContactDao contactDao;
    private Context context;

    public LocalContactDataSource(Context context){
        daoSession = DaoManager.getInstance(context).getDaoSession();
        contactDao = daoSession.getContactDao();
        this.context = context;
    }

    @Override
    public void getContactList(LoadCallBack loadCallBack) {
        List<Contact> list = contactDao.queryBuilder()
                .orderAsc(ContactDao.Properties.UserName)
                .list();
        System.out.println("getContactList" + list.size());
        loadCallBack.onLoadSuccess(list);
    }

    @Override
    public void deleteContact(String mailAddress, UpdateCallBack updateCallBack) {
        contactDao.queryBuilder()
                .where(ContactDao.Properties.MailAddress.eq(mailAddress))
                .buildDelete().executeDeleteWithoutDetachingEntities();
        updateCallBack.onUpdateSuccess();
    }

    @Override
    public void addContact(Contact contact, UpdateCallBack updateCallBack) {
        contactDao.insert(contact);
        updateCallBack.onUpdateSuccess();
    }
}
