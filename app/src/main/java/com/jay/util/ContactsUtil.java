package com.jay.util;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.text.TextUtils;

import com.jay.javabean.ContactBean;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobObject;

/**
 * 联系人工具类
 */

public class ContactsUtil {

    private static String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;

    private static String SORT_KEY_PRIMARY = ContactsContract.Contacts.SORT_KEY_PRIMARY;

    private static Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;

    private static String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;

    /**
     * 获取所有联系人信息
     *
     * @param resolver
     * @return
     */
    public static List<BmobObject> queryAllContacts(ContentResolver resolver) {
        //获取联系人的光标
        Cursor cursor = resolver.query(CONTENT_URI, null, null, null, null);
        List<BmobObject> contactsList = new ArrayList<>();
        while (cursor.moveToNext()) {
            //获得联系人ID
            String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            //获得联系人姓名
            String name = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME));
            //4.4以上为phonebook_label
            if (Build.VERSION.SDK_INT >= 19) {
                SORT_KEY_PRIMARY = "phonebook_label";
            } else {
                SORT_KEY_PRIMARY = ContactsContract.Contacts.SORT_KEY_PRIMARY;
            }
            //获取姓名拼音
            String pinyin = cursor.getString(cursor.getColumnIndex(SORT_KEY_PRIMARY));
            pinyin = parsePinyin(pinyin);
            //获得联系人手机号码
            Cursor phone = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + id, null, null);
            ArrayList<String> telList = new ArrayList<>();
            //取得电话号码(可能存在多个号码)
            while (phone != null && phone.moveToNext()) {
                int phoneFieldColumnIndex = phone.getColumnIndex(NUMBER);
                String phoneNumber = phone.getString(phoneFieldColumnIndex);
                if (!TextUtils.isEmpty(phoneNumber)) {
                    telList.add(phoneNumber);
                }
            }
            phone.close();
            ContactBean contacts = new ContactBean();
            contacts.setName(name);
            contacts.setTel(telList);
            contacts.setSortkey(pinyin);
            contactsList.add(contacts);
        }
        cursor.close();
        return contactsList;
    }

    /**
     * 批量添加联系人，发现重复联系人会自动合并
     */
    public static void insertContacts(ContentResolver resolver, List<ContactBean> contacts) throws RemoteException, OperationApplicationException {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        for (ContactBean contact : contacts) {
       //     List<Integer> ids = queryContactIdByName(resolver, contact.getName());
            List<ContentProviderOperation> operation = null;
            //如果同名联系人有很多个，那么执行合并操作
//            if (ids.size() > 0) {
//                operation = mergeContact(ids);
//            }
//            //联系人不存在则新建联系人
//            else {
//                operation = insertContact(resolver, contact, operations.size());
//            }
            operation = insertContact(resolver, contact, operations.size());
            if (operation != null) {
                operations.addAll(operation);
            }
        }
        //执行批处理操作
        resolver.applyBatch(ContactsContract.AUTHORITY, operations);
    }

    /**
     * 删除指定姓名的联系人
     *
     * @param resolver
     * @param name
     * @throws RemoteException
     * @throws OperationApplicationException
     */
    public static void deleteContactByName(ContentResolver resolver, String name) throws RemoteException, OperationApplicationException {
        List<Integer> ids = queryContactIdByName(resolver, name);
        deleteContactById(resolver, ids);
    }


    /**
     * 删除指定id的联系人
     *
     * @param resolver
     * @param ids
     * @throws RemoteException
     * @throws OperationApplicationException
     */
    public static void deleteContactById(ContentResolver resolver, List<Integer> ids) throws RemoteException, OperationApplicationException {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        for (int id : ids) {
            //delete contact
            ops.add(ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI)
                    .withSelection(ContactsContract.RawContacts.CONTACT_ID + "=?" + id, null)
                    .build());
            //delete contact information such as phone number,email
            ops.add(ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
                    .withSelection(ContactsContract.Data.CONTACT_ID + "=" + id, null)
                    .build());
        }
        resolver.applyBatch(ContactsContract.AUTHORITY, ops);
    }

    /**
     * 插入一个联系人
     *
     * @param resolver
     * @param contact
     * @param rawContactInsertIndex 相对于第几个操作的结果
     * @throws RemoteException
     * @throws OperationApplicationException
     */
    public static List<ContentProviderOperation> insertContact(ContentResolver resolver, ContactBean contact, int rawContactInsertIndex) throws RemoteException, OperationApplicationException {
        Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
        List<ContentProviderOperation> operations = new ArrayList<>();
        ContentProviderOperation op1 = ContentProviderOperation.newInsert(uri)
                .withValue("account_name", null)
                .build();
        operations.add(op1);

        uri = Uri.parse("content://com.android.contacts/data");
        //添加姓名
        ContentProviderOperation op2 = ContentProviderOperation.newInsert(uri)
                .withValueBackReference("raw_contact_id", rawContactInsertIndex)
                .withValue("mimetype", "vnd.android.cursor.item/name")
                .withValue("data2", contact.getName())
                .build();
        operations.add(op2);
        //添加电话号码
        ArrayList<String> stringArrayList = contact.getTel();
        for (String tel : stringArrayList) {
            ContentProviderOperation op3 = ContentProviderOperation.newInsert(uri)
                    .withValueBackReference("raw_contact_id", rawContactInsertIndex)
                    .withValue("mimetype", "vnd.android.cursor.item/phone_v2")
                    .withValue("data1", tel)
                    .withValue("data2", "2")
                    .build();
            operations.add(op3);
        }
        return operations;
    }

//    /**
//     * 合并联系人
//     *
//     * @param ids
//     * @throws RemoteException
//     * @throws OperationApplicationException
//     */
//    @Nullable
//    public static List<ContentProviderOperation> mergeContact(List<Integer> ids) throws RemoteException, OperationApplicationException {
//        int id = ids.get(0);
//        List<ContentProviderOperation> operations = new ArrayList<>();
//        for (int i = 1; i < ids.size(); i++) {
//            ContentProviderOperation operation = buildJoinContactDiff(id, ids.get(i));
//            operations.add(operation);
//        }
//        return operations;
//    }

//    /**
//     * 批处理合并联系人
//     */
//    private static ContentProviderOperation buildJoinContactDiff(long id1, long id2) {
//        ContentProviderOperation.Builder builder =
//                ContentProviderOperation.newUpdate(ContactsContract.AggregationExceptions.CONTENT_URI);
//        builder.withValue(ContactsContract.AggregationExceptions.TYPE, ContactsContract.AggregationExceptions.TYPE_KEEP_TOGETHER);
//        builder.withValue(ContactsContract.AggregationExceptions.RAW_CONTACT_ID1, id1);
//        builder.withValue(ContactsContract.AggregationExceptions.RAW_CONTACT_ID2, id2);
//        builder.withYieldAllowed(true);
//        return builder.build();
//    }

    /**
     * 获取联系人Id（根据姓名）
     *
     * @param resolver ContentResolver对象
     * @param name     待搜索的联系人姓名
     * @return 联系人id，如果联系人不存在返回“0”
     */
    public static List<Integer> queryContactIdByName(ContentResolver resolver, String name) {
        List<Integer> ids = new ArrayList<>();
        Cursor cursor = resolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                new String[]{ContactsContract.Contacts._ID},
                ContactsContract.Contacts.DISPLAY_NAME + "= ? ",
                new String[]{name},
                null);
        while (cursor != null && cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            ids.add(id);
        }
        if (cursor != null)
            cursor.close();
        return ids;
    }


    /**
     * 将拼音转为单个字符
     *
     * @param pinyin 姓名的拼音
     * @return 拼音大写首字母
     */
    private static String parsePinyin(String pinyin) {
        String result = "#";
        if (!TextUtils.isEmpty(pinyin)) {
            result = pinyin.substring(0, 1);
            if (!result.matches("[A-Za-z]")) {
                result = "#";
            }
        }
        return result.toUpperCase();
    }

}
