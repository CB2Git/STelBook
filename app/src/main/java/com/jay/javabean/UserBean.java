package com.jay.javabean;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;

/**
 * 用户对应数据类
 * <p>
 * BmobUser已经自带了用户名、密码、邮箱等
 * Created by ${jay} on ${2016/8/17
 */

public class UserBean extends BmobUser {
    /**
     * 用户头像
     */
    private BmobFile userIcon;

    public BmobFile getUserIcon() {
        return userIcon;
    }

    public void setUserIcon(BmobFile userIcon) {
        this.userIcon = userIcon;
    }
}
