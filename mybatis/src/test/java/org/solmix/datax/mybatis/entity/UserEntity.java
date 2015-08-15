package org.solmix.datax.mybatis.entity;

import java.io.Serializable;


public class UserEntity implements Serializable
{

    
    private static final long serialVersionUID = 4532486884865133982L;
    protected String userId;
    protected String userName;
    protected String password;
    protected String trueName;
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getTrueName() {
        return trueName;
    }
    
    public void setTrueName(String trueName) {
        this.trueName = trueName;
    }
   
}
