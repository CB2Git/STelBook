package com.jay.javabean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

import cn.bmob.v3.BmobObject;

/**
 * 联系人列表对应类
 * Created by Jay on 2016/7/10.
 */
public class ContactBean extends BmobObject implements Parcelable {
    //联系人姓名
    private String name;
    //联系人电话
    private ArrayList<String> tel;
    //此联系人所属版本号
    private String version;
    //排序的文字 # A-Z
    private String sortkey;
    // 联系人所属用户
    private UserBean user;

    public ContactBean() {
        this.setTableName("Contacts");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getTel() {
        return tel;
    }

    public void setTel(ArrayList<String> tel) {
        this.tel = tel;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getSortkey() {
        return sortkey;
    }

    public void setSortkey(String sortkey) {
        this.sortkey = sortkey;
    }

    public UserBean getUser() {
        return user;
    }

    public void setUser(UserBean user) {
        this.user = user;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeStringList(this.tel);
        dest.writeString(this.version);
        dest.writeString(this.sortkey);
        dest.writeSerializable(this.user);
    }

    protected ContactBean(Parcel in) {
        this.name = in.readString();
        this.tel = in.createStringArrayList();
        this.version = in.readString();
        this.sortkey = in.readString();
        this.user = (UserBean) in.readSerializable();
    }

    public static final Parcelable.Creator<ContactBean> CREATOR = new Parcelable.Creator<ContactBean>() {
        @Override
        public ContactBean createFromParcel(Parcel source) {
            return new ContactBean(source);
        }

        @Override
        public ContactBean[] newArray(int size) {
            return new ContactBean[size];
        }
    };
}
