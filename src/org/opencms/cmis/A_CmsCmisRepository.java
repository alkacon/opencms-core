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
import org.apache.chemistry.opencmis.commons.spi.Holder;

/**
 * Abstract repository superclass.<p>
 *
 * This class was introduced to separate the CMIS methods which are not supported from those which are,
 * so only unsupported operations and utility should go into this class.<p>
 */
public abstract class A_CmsCmisRepository implements I_CmsCmisRepository {

    /** cmis:all permission. */
    public static final String CMIS_ALL = "cmis:all";

    /** cmis:read permission. */
    public static final String CMIS_READ = "cmis:read";

    /** cmis:write permission. */
    public static final String CMIS_WRITE = "cmis:write";

    /** The type manager instance. */
    protected CmsCmisTypeManager m_typeManager;

    /**
     * @see org.opencms.cmis.I_CmsCmisRepository#addObjectToFolder(org.opencms.cmis.CmsCmisCallContext, java.lang.String, java.lang.String, boolean)
     */
    public synchronized void addObjectToFolder(
        CmsCmisCallContext context,
        String objectId,
        String folderId,
        boolean allVersions) {

        throw notSupported();

    }

    /**
     * @see org.opencms.cmis.I_CmsCmisRepository#applyAcl(org.opencms.cmis.CmsCmisCallContext, java.lang.String, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.enums.AclPropagation)
     */
    public synchronized Acl applyAcl(
        CmsCmisCallContext context,
        String objectId,
        Acl addAces,
        Acl removeAces,
        AclPropagation aclPropagation) {

        throw notSupported();
    }

    /**
     * @see org.opencms.cmis.I_CmsCmisRepository#applyAcl(org.opencms.cmis.CmsCmisCallContext, java.lang.String, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.enums.AclPropagation)
     */
    public synchronized Acl applyAcl(
        CmsCmisCallContext context,
        String objectId,
        Acl aces,
        AclPropagation aclPropagation) {

        throw notSupported();
    }

    /**
     * @see org.opencms.cmis.I_CmsCmisRepository#applyPolicy(org.opencms.cmis.CmsCmisCallContext, java.lang.String, java.lang.String)
     */
    public synchronized void applyPolicy(CmsCmisCallContext context, String policyId, String objectId) {

        throw notSupported();

    }

    /**
     * @see org.opencms.cmis.I_CmsCmisRepository#cancelCheckOut(org.opencms.cmis.CmsCmisCallContext, java.lang.String)
     */
    public synchronized void cancelCheckOut(CmsCmisCallContext context, String objectId) {

        throw notSupported();

    }

    /**
     * @see org.opencms.cmis.I_CmsCmisRepository#checkIn(org.opencms.cmis.CmsCmisCallContext, org.apache.chemistry.opencmis.commons.spi.Holder, boolean, org.apache.chemistry.opencmis.commons.data.Properties, org.apache.chemistry.opencmis.commons.data.ContentStream, java.lang.String, java.util.List, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.Acl)
     */
    public synchronized void checkIn(
        CmsCmisCallContext context,
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
     * @see org.opencms.cmis.I_CmsCmisRepository#checkOut(org.opencms.cmis.CmsCmisCallContext, org.apache.chemistry.opencmis.commons.spi.Holder, org.apache.chemistry.opencmis.commons.spi.Holder)
     */
    public synchronized void checkOut(
        CmsCmisCallContext context,
        Holder<String> objectId,
        Holder<Boolean> contentCopied) {

        throw notSupported();

    }

    /**
     * @see org.opencms.cmis.I_CmsCmisRepository#createPolicy(org.opencms.cmis.CmsCmisCallContext, org.apache.chemistry.opencmis.commons.data.Properties, java.lang.String, java.util.List, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.Acl)
     */
    public synchronized String createPolicy(
        CmsCmisCallContext context,
        Properties properties,
        String folderId,
        List<String> policies,
        Acl addAces,
        Acl removeAces) {

        throw notSupported();
    }

    /**
     * @see org.opencms.cmis.I_CmsCmisRepository#getAllVersions(org.opencms.cmis.CmsCmisCallContext, java.lang.String, java.lang.String, java.lang.String, boolean)
     */
    public synchronized List<ObjectData> getAllVersions(
        CmsCmisCallContext context,
        String objectId,
        String versionSeriesId,
        String filter,
        boolean includeAllowableActions) {

        throw notSupported();
    }

    /**
     * @see org.opencms.cmis.I_CmsCmisRepository#getAppliedPolicies(org.opencms.cmis.CmsCmisCallContext, java.lang.String, java.lang.String)
     */
    public synchronized List<ObjectData> getAppliedPolicies(
        CmsCmisCallContext context,
        String objectId,
        String filter) {

        throw notSupported();
    }

    /**
     * @see org.opencms.cmis.I_CmsCmisRepository#getContentChanges(org.opencms.cmis.CmsCmisCallContext, org.apache.chemistry.opencmis.commons.spi.Holder, boolean, java.lang.String, boolean, boolean, java.math.BigInteger)
     */
    public synchronized ObjectList getContentChanges(
        CmsCmisCallContext context,
        Holder<String> changeLogToken,
        boolean includeProperties,
        String filter,
        boolean includePolicyIds,
        boolean includeAcl,
        BigInteger maxItems) {

        throw notSupported();
    }

    /**
     * @see org.opencms.cmis.I_CmsCmisRepository#getObjectOfLatestVersion(org.opencms.cmis.CmsCmisCallContext, java.lang.String, java.lang.String, boolean, java.lang.String, boolean, org.apache.chemistry.opencmis.commons.enums.IncludeRelationships, java.lang.String, boolean, boolean)
     */
    public synchronized ObjectData getObjectOfLatestVersion(
        CmsCmisCallContext context,
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
     * @see org.opencms.cmis.I_CmsCmisRepository#getPropertiesOfLatestVersion(org.opencms.cmis.CmsCmisCallContext, java.lang.String, java.lang.String, boolean, java.lang.String)
     */
    public synchronized Properties getPropertiesOfLatestVersion(
        CmsCmisCallContext context,
        String objectId,
        String versionSeriesId,
        boolean major,
        String filter) {

        throw notSupported();
    }

    /**
     * @see org.opencms.cmis.I_CmsCmisRepository#query(org.opencms.cmis.CmsCmisCallContext, java.lang.String, boolean, boolean, org.apache.chemistry.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.math.BigInteger, java.math.BigInteger)
     */
    public synchronized ObjectList query(
        CmsCmisCallContext context,
        String statement,
        boolean searchAllVersions,
        boolean includeAllowableActions,
        IncludeRelationships includeRelationships,
        String renditionFilter,
        BigInteger maxItems,
        BigInteger skipCount) {

        throw notSupported();
    }

    /**
     * @see org.opencms.cmis.I_CmsCmisRepository#removeObjectFromFolder(org.opencms.cmis.CmsCmisCallContext, java.lang.String, java.lang.String)
     */
    public synchronized void removeObjectFromFolder(CmsCmisCallContext context, String objectId, String folderId) {

        throw notSupported();

    }

    /**
     * @see org.opencms.cmis.I_CmsCmisRepository#removePolicy(org.opencms.cmis.CmsCmisCallContext, java.lang.String, java.lang.String)
     */
    public synchronized void removePolicy(CmsCmisCallContext context, String policyId, String objectId) {

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
        return Arrays.copyOfRange(content, (int)offsetLong, Math.min(content.length, (int)(offsetLong + lengthLong)));
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
