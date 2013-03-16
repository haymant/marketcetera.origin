package org.marketcetera.webservices.systemmodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.*;

import org.marketcetera.api.systemmodel.Permission;
import org.marketcetera.api.systemmodel.Role;
import org.marketcetera.core.security.AssignToRole;
import org.marketcetera.core.security.MutableProvisioning;
import org.marketcetera.core.security.Provisioning;
import org.marketcetera.core.security.User;

/* $License$ */

/**
 * Provides a web services capable version of a <code>Provisioning</code> value.
 *
 * @author <a href="mailto:colin@marketcetera.com">Colin DuPlantis</a>
 * @version $Id$
 * @since $Release$
 */
@XmlRootElement(name="provisioning")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder={ "permissions", "users", "roles", "assignments" })
public class WebServicesProvisioning
        implements MutableProvisioning, Serializable
{
    /**
     * Create a new WebServicesProvisioning instance.
     */
    public WebServicesProvisioning() {}
    /**
     * Create a new WebServicesProvisioning instance.
     *
     * @param inData a <code>Provisioning</code> value
     */
    public WebServicesProvisioning(Provisioning inData)
    {
        copyAttributes(inData);
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("WebServicesProvisioning [users=").append(users).append(", roles=").append(roles)
                .append(", permissions=").append(permissions).append(", assignments=").append(assignments).append("]");
        return builder.toString();
    }
    /* (non-Javadoc)
     * @see org.marketcetera.api.security.Provisioning#getUsers()
     */
    @Override
    public Set<User> getUsers()
    {
        return users;
    }
    /* (non-Javadoc)
     * @see org.marketcetera.api.security.Provisioning#getRoles()
     */
    @Override
    public Set<Role> getRoles()
    {
        return roles;
    }
    /* (non-Javadoc)
     * @see org.marketcetera.api.security.Provisioning#getPermissions()
     */
    @Override
    public Set<Permission> getPermissions()
    {
        return permissions;
    }
    /* (non-Javadoc)
     * @see org.marketcetera.api.security.Provisioning#getAssignments()
     */
    @Override
    public List<AssignToRole> getAssignments()
    {
        return assignments;
    }
    /* (non-Javadoc)
     * @see org.marketcetera.api.security.Provisioning#getMutableView()
     */
    @Override
    public MutableProvisioning getMutableView()
    {
        return this;
    }
    /* (non-Javadoc)
     * @see org.marketcetera.api.security.MutableProvisioning#setUsers(java.util.Set)
     */
    @Override
    public void setUsers(Set<User> inUsers)
    {
        users = inUsers;
    }
    /* (non-Javadoc)
     * @see org.marketcetera.api.security.MutableProvisioning#setRoles(java.util.Set)
     */
    @Override
    public void setRoles(Set<Role> inRoles)
    {
        roles = inRoles;
    }
    /* (non-Javadoc)
     * @see org.marketcetera.api.security.MutableProvisioning#setPermissions(java.util.Set)
     */
    @Override
    public void setPermissions(Set<Permission> inPermissions)
    {
        permissions = inPermissions;
    }
    /* (non-Javadoc)
     * @see org.marketcetera.api.security.MutableProvisioning#setAssignments(java.util.List)
     */
    @Override
    public void setAssignments(List<AssignToRole> inAssignments)
    {
        assignments = inAssignments;
    }
    /**
     * Copies the attributes from the given value to this object.
     *
     * @param inData a <code>Provisioning</code> value
     */
    private void copyAttributes(Provisioning inData)
    {
        if(inData == null) {
            return;
        }
        if(inData.getUsers() != null) {
            for(User user : inData.getUsers()) {
                users.add(user instanceof WebServicesUser ? user : new WebServicesUser(user));
            }
        }
        if(inData.getRoles() != null) {
            for(Role role : inData.getRoles()) {
                roles.add(role instanceof WebServicesRole ? role : new WebServicesRole(role));
            }
        }
        if(inData.getPermissions() != null) {
            for(Permission permission : inData.getPermissions()) {
                permissions.add(permission instanceof WebServicesPermission ? permission : new WebServicesPermission(permission));
            }
        }
        if(inData.getAssignments() != null) {
            for(AssignToRole assignment : inData.getAssignments()) {
                assignments.add(assignment instanceof WebServicesAssignToRole ? assignment : new WebServicesAssignToRole(assignment));
            }
        }
    }
    /**
     * users value
     */
    @XmlElementWrapper(name="users")
    @XmlElement(name="user",type=WebServicesUser.class)
    private Set<User> users = new HashSet<User>();
    /**
     * roles value
     */
    @XmlElementWrapper(name="roles")
    @XmlElement(name="role",type=WebServicesRole.class)
    private Set<Role> roles = new HashSet<Role>();
    /**
     * permissions value
     */
    @XmlElementWrapper(name="permissions")
    @XmlElement(name="permission",type=WebServicesPermission.class)
    private Set<Permission> permissions = new HashSet<Permission>();
    /**
     * assignments value
     */
    @XmlElementWrapper(name="assignments")
    @XmlElement(name="assignToRole",type=WebServicesAssignToRole.class)
    private List<AssignToRole> assignments = new ArrayList<AssignToRole>();
    private static final long serialVersionUID = 1L;
}
