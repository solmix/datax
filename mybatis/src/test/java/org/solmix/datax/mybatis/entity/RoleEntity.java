package org.solmix.datax.mybatis.entity;

import java.io.Serializable;

public class RoleEntity implements Serializable {

	private static final long serialVersionUID = -3401437659906022291L;

	private String roleId;
	private String roleName;
	private String roleTitle;
    
    public String getRoleId() {
        return roleId;
    }
    
    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }
    
    public String getRoleName() {
        return roleName;
    }
    
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
    
    public String getRoleTitle() {
        return roleTitle;
    }
    
    public void setRoleTitle(String roleTitle) {
        this.roleTitle = roleTitle;
    }
	
}
