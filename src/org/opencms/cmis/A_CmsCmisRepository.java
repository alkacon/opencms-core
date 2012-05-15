/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.cmis;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.main.CmsException;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsPrincipal;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsUUID;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.spi.Holder;

/**
 * Abstract repository superclass.<p>
 * 
 * This class was introduced to separate the CMIS methods which are not supported from those which are,
 * so only unsupported operations and utility should go into this class.<p>
 */
public abstract class A_CmsCmisRepository {

    /** The type manager instance. */
    protected CmsCmisTypeManager m_typeManager;

    /**
     * Adds an object to a folder (multifiling). <p>
     * 
     * @param context the call context 
     * @param objectId the object id 
     * @param folderId the folder id 
     * @param allVersions flag to include all versions
     */
    public synchronized void addObjectToFolder(
        CallContext context,
        String objectId,
        String folderId,
        boolean allVersions) {

        throw notSupported();

    }

    /**
     * Applies ACL to an object.<p>
     * 
     * @param context the call context 
     * @param objectId the object id 
     * @param addAces the ACEs to add 
     * @param removeAces the ACEs to remove 
     * @param aclPropagation the ACL propagation 
     *  
     * @return the new ACL 
     */
    public synchronized Acl applyAcl(
        CallContext context,
        String objectId,
        Acl addAces,
        Acl removeAces,
        AclPropagation aclPropagation) {

        throw notSupported();
    }

    /**
     * Changes the ACL for an object.<p>
     *  
     * @param context the call context 
     * @param objectId the object id 
     * @param aces the access control entries 
     * @param aclPropagation the propagation mode 
     * 
     * @return the new ACL 
     */
    public synchronized Acl applyAcl(CallContext context, String objectId, Acl aces, AclPropagation aclPropagation) {

        throw notSupported();
    }

    /**
     * Applies a policy to an object.<p>
     * 
     * @param context the call context 
     * @param policyId the policy id 
     * @param objectId the object id 
     */
    public synchronized void applyPolicy(CallContext context, String policyId, String objectId) {

        throw notSupported();

    }

    /**
     * Cancels a checkout.<p>
     * 
     * @param context the call context 
     * @param objectId the object id 
     */
    public synchronized void cancelCheckOut(CallContext context, String objectId) {

        throw notSupported();

    }

    /**
     * Checks in a document.<p>
     *  
     * @param context the call context 
     * @param objectId the object id 
     * @param major the major version flag 
     * @param properties the properties 
     * @param contentStream the content stream 
     * @param checkinComment the check-in comment 
     * @param policies the policies 
     * @param addAces the ACEs to add
     * @param removeAces the ACEs to remove 
     */
    public synchronized void checkIn(
        CallContext context,
        Holder<String> objectId,
        boolean major,
        Properties properties,
        ContentStream contentStream,
        String checkinComment,
        List<String> policies,
        Acl addAces,
        Acl removeAces) {

        throw notSupported();

    }

    /**
     * Checks out an object.<p>
     * 
     * @param context the call context 
     * @param objectId the object id 
     * @param contentCopied indicator whether the content was copied
     */
    public synchronized void checkOut(CallContext context, Holder<String> objectId, Holder<Boolean> contentCopied) {

        throw notSupported();

    }

    /**
     * Creates a policy.<p>
     * 
     * @param context the call context 
     * @param properties the properties 
     * @param folderId the folder id 
     * @param policies the policies 
     * @param addAces the ACEs to add 
     * @param removeAces the ACEs to remove 
     * 
     * @return the new object id
     */
    public synchronized String createPolicy(
        CallContext context,
        Properties properties,
        String folderId,
        List<String> policies,
        Acl addAces,
        Acl removeAces) {

        throw notSupported();
    }

    /**
     * Gets all versions of an object.<p>
     * 
     * @param context the call context
     * @param objectId the object id 
     * @param versionSeriesId the version series id 
     * @param filter the property filter string 
     * @param includeAllowableActions the flag to include allowable actions
     * 
     * @return the list of versions 
     */
    public synchronized List<ObjectData> getAllVersions(
        CallContext context,
        String objectId,
        String versionSeriesId,
        String filter,
        boolean includeAllowableActions) {

        throw notSupported();
    }

    /**
     * Gets the policies for an object.<p>
     * 
     * @param context the call context 
     * @param objectId the object id
     * @param filter the property filter
     *  
     * @return the policies for the object 
     */
    public synchronized List<ObjectData> getAppliedPolicies(CallContext context, String objectId, String filter) {

        throw notSupported();
    }

    /**
     * Gets content changes from the repository.<p>
     * 
     * @param context the call context 
     * @param changeLogToken the change log token 
     * @param includeProperties flag to include properties 
     * @param filter filter string for properties 
     * @param includePolicyIds flag to include policy ids  
     * @param includeAcl flag to include ACLs 
     * @param maxItems maximum number of items to return
     *  
     * @return the list of content changes 
     */
    public synchronized ObjectList getContentChanges(
        CallContext context,
        Holder<String> changeLogToken,
        boolean includeProperties,
        String filter,
        boolean includePolicyIds,
        boolean includeAcl,
        BigInteger maxItems) {

        throw notSupported();
    }

    /**
     * Gets the object of the latest version.<p>
     * 
     * @param context the call context 
     * @param objectId the object id 
     * @param versionSeriesId the version series id 
     * @param major flag to get the latest major version 
     * @param filter the property filter 
     * @param includeAllowableActions flag to include allowable actions 
     * @param includeRelationships flag to include relationships 
     * @param renditionFilter filter string for renditions 
     * @param includePolicyIds flag to include policies 
     * @param includeAcl flag to include ACLs
     * 
     * @return the data for the latest version 
     */
    public synchronized ObjectData getObjectOfLatestVersion(
        CallContext context,
        String objectId,
        String versionSeriesId,
        boolean major,
        String filter,
        boolean includeAllowableActions,
        IncludeRelationships includeRelationships,
        String renditionFilter,
        boolean includePolicyIds,
        boolean includeAcl) {

        throw notSupported();
    }

    /**
     * Gets the properties of the latest version.<p>
     * 
     * @param context the call context 
     * @param objectId the object id 
     * @param versionSeriesId the version series id 
     * @param major flag to access the latest major version 
     * @param filter the property filter string 
     * 
     * @return the properties from the latest version 
     */
    public synchronized Properties getPropertiesOfLatestVersion(
        CallContext context,
        String objectId,
        String versionSeriesId,
        boolean major,
        String filter) {

        throw notSupported();
    }

    /**
     * Unfiles an object from a folder.<p>
     *  
     * @param context the call context 
     * @param objectId the id of the object to unfile 
     * @param folderId the folder from which the object should be unfiled  
     */
    public synchronized void removeObjectFromFolder(CallContext context, String objectId, String folderId) {

        throw notSupported();

    }

    /**
     * Removes a policy from an object.<p>
     * 
     * @param context the call context
     * @param policyId the policy id 
     * @param objectId the object id
     */
    public synchronized void removePolicy(CallContext context, String policyId, String objectId) {

        throw notSupported();

    }

    /**
     * Copies a range of bytes from an array into a new array.<p>
     * 
     * @param content the content array 
     * @param offset the start offset in the array 
     * @param length the length of the range 
     *  
     * @return the bytes from the given range of the content 
     */
    protected byte[] extractRange(byte[] content, BigInteger offset, BigInteger length) {

        if ((offset == null) && (length == null)) {
            return content;
        }
        if (offset == null) {
            offset = BigInteger.ZERO;
        }
        long offsetLong = offset.longValue();
        if (length == null) {
            length = BigInteger.valueOf(content.length - offsetLong);
        }
        long lengthLong = length.longValue();
        return Arrays.copyOfRange(content, (int)offsetLong, (int)(offsetLong + lengthLong));
    }

    /**
     * Gets a user-readable name for a principal id read from an ACE.<p>
     * 
     * @param cms the current CMS context 
     * @param principalId the principal id from the ACE  
     * @return the name of the principle 
     */
    protected String getAcePrincipalName(CmsObject cms, CmsUUID principalId) {

        if (CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_ID.equals(principalId)) {
            return CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_NAME;
        }
        if (CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_ID.equals(principalId)) {
            return CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_NAME;
        }
        CmsRole role = CmsRole.valueOfId(principalId);
        if (role != null) {
            return role.getRoleName();
        }
        try {
            return CmsPrincipal.readPrincipalIncludingHistory(cms, principalId).getName();
        } catch (CmsException e) {
            return "" + principalId;
        }
    }

    /**
     * Helper method to create OpenCms property objects from a map of CMIS properties.<p>
     * 
     * @param properties the CMIS properties 
     * 
     * @return the OpenCms properties 
     */
    protected List<CmsProperty> getOpenCmsProperties(Map<String, PropertyData<?>> properties) {

        List<CmsProperty> cmsProperties = new ArrayList<CmsProperty>();
        for (Map.Entry<String, PropertyData<?>> entry : properties.entrySet()) {
            String propId = entry.getKey();
            if (propId.startsWith(CmsCmisTypeManager.PROPERTY_PREFIX)) {
                String propName = propId.substring(CmsCmisTypeManager.PROPERTY_PREFIX.length());
                String value = (String)entry.getValue().getFirstValue();
                if (value == null) {
                    value = "";
                }
                cmsProperties.add(new CmsProperty(propName, value, null));
            }
        }
        return cmsProperties;
    }

    /**
     * Helper method to create exceptions for unsupported features.<p>
     * 
     * @return the created exception 
     */
    protected RuntimeException notSupported() {

        return new CmisNotSupportedException("Not supported");

    }
}
