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

import java.math.BigInteger;
import java.util.List;

import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.RelationshipDirection;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.spi.Holder;

/**
 * Abstract repository superclass.<p>
 * 
 * This class was introduced to separate the CMIS methods which are not supported from those which are,
 * so only unsupported operations should go into this class.<p>
 */
public class A_CmsCmisRepository {

    /**
     * Performs a query on the repository.<p>
     * 
     * @param context the call context
     * @param statement the query 
     * @param searchAllVersions flag to search all versions 
     * @param includeAllowableActions flag to include allowable actions 
     * @param includeRelationships flag to include relationships 
     * @param renditionFilter the filter string for renditions 
     * @param maxItems the maximum number of items to return 
     * @param skipCount the number of items to skip
     *  
     * @return the query result objects
     */
    public synchronized ObjectList query(
        CallContext context,
        String statement,
        Boolean searchAllVersions,
        Boolean includeAllowableActions,
        IncludeRelationships includeRelationships,
        String renditionFilter,
        BigInteger maxItems,
        BigInteger skipCount) {

        // TODO: Auto-generated method stub
        throw notSupported();
    }

    /**
     * Helper method to create exceptions for unsupported features.<p>
     * 
     * @return the created exception 
     */
    private RuntimeException notSupported() {

        return new CmisNotSupportedException("Not supported");

    }

    /**
     * 
     * @see org.apache.chemistry.opencmis.commons.spi.PolicyService#applyPolicy(java.lang.String, java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public synchronized void applyPolicy(CallContext context, String policyId, String objectId) {

        throw notSupported();

    }

    /**
     * 
     * @see org.apache.chemistry.opencmis.commons.spi.PolicyService#removePolicy(java.lang.String, java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public synchronized void removePolicy(CallContext context, String policyId, String objectId) {

        throw notSupported();

    }

    /**
     * 
     * @see org.apache.chemistry.opencmis.commons.spi.PolicyService#getAppliedPolicies(java.lang.String, java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public synchronized List<ObjectData> getAppliedPolicies(CallContext context, String objectId, String filter) {

        throw notSupported();
    }

    /**
     * 
     * @see org.apache.chemistry.opencmis.commons.server.CmisService#applyAcl(java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.enums.AclPropagation)
     */
    public synchronized Acl applyAcl(CallContext context, String objectId, Acl aces, AclPropagation aclPropagation) {

        throw notSupported();
    }

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
        Boolean allVersions) {

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
        Boolean major,
        Properties properties,
        ContentStream contentStream,
        String checkinComment,
        List<String> policies,
        Acl addAces,
        Acl removeAces) {

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
        Boolean major,
        String filter,
        Boolean includeAllowableActions,
        IncludeRelationships includeRelationships,
        String renditionFilter,
        Boolean includePolicyIds,
        Boolean includeAcl) {

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
        Boolean major,
        String filter) {

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
        Boolean includeAllowableActions) {

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
     * @return
     */
    public synchronized ObjectList getContentChanges(
        CallContext context,
        Holder<String> changeLogToken,
        Boolean includeProperties,
        String filter,
        Boolean includePolicyIds,
        Boolean includeAcl,
        BigInteger maxItems) {

        throw notSupported();
    }

    /**
     * Gets the relationships for an object.<p>
     * 
     * @param context the call context
     * @param objectId the object id 
     * @param includeSubRelationshipTypes flag to include relationship subtypes 
     * @param relationshipDirection the direction for the relations 
     * @param typeId the relation type id 
     * @param filter the property filter 
     * @param includeAllowableActions flag to include allowable actions  
     * @param maxItems the maximum number of items to return 
     * @param skipCount the number of items to skip 
     * 
     * @return the relationships for the object
     */
    public synchronized ObjectList getObjectRelationships(
        CallContext context,
        String objectId,
        Boolean includeSubRelationshipTypes,
        RelationshipDirection relationshipDirection,
        String typeId,
        String filter,
        Boolean includeAllowableActions,
        BigInteger maxItems,
        BigInteger skipCount) {

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
     * Corresponds to CMIS getCheckedOutDocs service method.<p>
     *  
     * @param context
     * @param folderId
     * @param filter
     * @param orderBy
     * @param includeAllowableActions
     * @param includeRelationships
     * @param renditionFilter
     * @param maxItems
     * @param skipCount
     * 
     * @return a list of CMIS objects 
     */
    public synchronized ObjectList getCheckedOutDocs(
        CallContext context,
        String folderId,
        String filter,
        String orderBy,
        Boolean includeAllowableActions,
        IncludeRelationships includeRelationships,
        String renditionFilter,
        BigInteger maxItems,
        BigInteger skipCount) {

        throw notSupported();
    }

    /**
     * Creates a relationship.<p>
     * 
     * @param context the call context 
     * @param properties the properties 
     * @param policies the policies 
     * @param addAces the ACEs to add
     * @param removeAces the ACEs to remove 
     * 
     * @return the new relationship id 
     */
    public synchronized String createRelationship(
        CallContext context,
        Properties properties,
        List<String> policies,
        Acl addAces,
        Acl removeAces) {

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
     * Applies ACL to an object.<p>
     * 
     * @param context the call context 
     * @param objectId the object id 
     * @param addAces the ACEs to add 
     * @param removeAces the ACEs to remove 
     * @param aclPropagation the ACL propagation 
     * @param extension extension data
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

}
