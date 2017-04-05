package com.jay.stelbook.contactlist;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jay.javabean.ContactBean;
import com.jay.stelbook.contactlist.base.IContactsPresenter;
import com.jay.stelbook.contactlist.base.IContactsView;

import org.json.JSONArray;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;

/**
 * Created by ${jay} on ${2016/8/17
 */
public class ContactsPresenter implements IContactsPresenter {

    private IContactsView mContactsView;

    public ContactsPresenter(IContactsView contactsView) {
        this.mContactsView = contactsView;
    }

    @Override
    public void queryAllContacts(BmobUser user, String versionId) {
        BmobQuery<ContactBean> bmobQuery = new BmobQuery<>("Contacts");
        bmobQuery.addWhereEqualTo("version", versionId);
        bmobQuery.addWhereEqualTo("user", user);
        bmobQuery.order("sortkey");
        bmobQuery.findObjectsByTable(new QueryListener<JSONArray>() {
            @Override
            public void done(JSONArray jsonArray, BmobException e) {
                if (e == null) {
                    Gson gson = new Gson();
                    List<ContactBean> ContactList = gson.fromJson(jsonArray.toString(), new TypeToken<List<ContactBean>>() {
                    }.getType());
                    mContactsView.displayAllContacts(ContactList);
                } else {
                    mContactsView.showTip("error", 1);
                }
            }
        });
    }
}
