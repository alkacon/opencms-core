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
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsPrincipal;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsSecurityException;
import org.opencms.util.CmsUUID;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNameConstraintViolationException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyBooleanImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDateTimeImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDecimalImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyHtmlImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIdImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIntegerImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyUriImpl;

/**
 * Utility class for operations which are frequently used by CMIS service methods.<p>
 */
public final class CmsCmisUtil {

    /**
     * Private constructor to prevent instantiation.<p>
     */
    private CmsCmisUtil() { /* Prevent instantiation. */

    }

    /**
     * Adds an action to a set of actions if a condition is fulfilled.<p>
     *
     * @param aas the set of actions
     * @param action the action to add
     * @param condition the value of the condition for adding the action
     */
    public static void addAction(Set<Action> aas, Action action, boolean condition) {

        if (condition) {
            aas.add(action);
        }
    }

    /**
     * Helper method to add the dynamic properties for a resource.<p>
     *
     * @param cms the current CMS context
     * @param typeManager the type manager instance
     * @param props the properties to which the dynamic properties should be added
     * @param typeId the type id
     * @param resource the resource
     * @param filter the property filter
     */
    public static void addDynamicProperties(
        CmsObject cms,
        CmsCmisTypeManager typeManager,
        PropertiesImpl props,
        String typeId,
        CmsResource resource,
        Set<String> filter) {

        List<I_CmsPropertyProvider> providers = typeManager.getPropertyProviders();
        for (I_CmsPropertyProvider provider : providers) {
            String propertyName = CmsCmisTypeManager.PROPERTY_PREFIX_DYNAMIC + provider.getName();
            if (!checkAddProperty(typeManager, props, typeId, filter, propertyName)) {
                continue;
            }
            try {
                String value = provider.getPropertyValue(cms, resource);
                addPropertyString(typeManager, props, typeId, filter, propertyName, value);
            } catch (Throwable t) {
                addPropertyString(typeManager, props, typeId, filter, propertyName, null);
            }
        }
    }

    /**
     * Adds bigint property to a PropertiesImpl.<p>
     *
     *
     * @param typeManager the type manager
     * @param props the properties
     * @param typeId the type id
     * @param filter the property filter string
     * @param id the property id
     * @param value the property value
     */
    public static void addPropertyBigInteger(
        CmsCmisTypeManager typeManager,
        PropertiesImpl props,
        String typeId,
        Set<String> filter,
        String id,
        BigInteger value) {

        if (!checkAddProperty(typeManager, props, typeId, filter, id)) {
            return;
        }

        props.addProperty(new PropertyIntegerImpl(id, value));
    }

    /**
     * Adds a boolean property to a PropertiesImpl.<p>
     *
     * @param typeManager
     * @param props the properties
     * @param typeId the type id
     * @param filter the property filter string
     * @param id the property id
     * @param value the property value
     */
    public static void addPropertyBoolean(
        CmsCmisTypeManager typeManager,
        PropertiesImpl props,
        String typeId,
        Set<String> filter,
        String id,
        boolean value) {

        if (!checkAddProperty(typeManager, props, typeId, filter, id)) {
            return;
        }

        props.addProperty(new PropertyBooleanImpl(id, Boolean.valueOf(value)));
    }

    /**
     * Adds a date/time property to a PropertiesImpl.<p>
     *
     * @param typeManager the type manager
     * @param props the properties
     * @param typeId the type id
     * @param filter the property filter string
     * @param id the property id
     * @param value the property value
     */
    public static void addPropertyDateTime(
        CmsCmisTypeManager typeManager,
        PropertiesImpl props,
        String typeId,
        Set<String> filter,
        String id,
        GregorianCalendar value) {

        if (!checkAddProperty(typeManager, props, typeId, filter, id)) {
            return;
        }

        props.addProperty(new PropertyDateTimeImpl(id, value));
    }

    /**
     * Adds the default value of property if defined.
     *
     * @param props the Properties object
     * @param propDef the property definition
     *
     * @return true if the property could be added
     */
    @SuppressWarnings("unchecked")
    public static boolean addPropertyDefault(PropertiesImpl props, PropertyDefinition<?> propDef) {

        if ((props == null) || (props.getProperties() == null)) {
            throw new IllegalArgumentException("Props must not be null!");
        }

        if (propDef == null) {
            return false;
        }

        List<?> defaultValue = propDef.getDefaultValue();
        if ((defaultValue != null) && (!defaultValue.isEmpty())) {
            switch (propDef.getPropertyType()) {
                case BOOLEAN:
                    props.addProperty(new PropertyBooleanImpl(propDef.getId(), (List<Boolean>)defaultValue));
                    break;
                case DATETIME:
                    props.addProperty(new PropertyDateTimeImpl(propDef.getId(), (List<GregorianCalendar>)defaultValue));
                    break;
                case DECIMAL:
                    props.addProperty(new PropertyDecimalImpl(propDef.getId(), (List<BigDecimal>)defaultValue));
                    break;
                case HTML:
                    props.addProperty(new PropertyHtmlImpl(propDef.getId(), (List<String>)defaultValue));
                    break;
                case ID:
                    props.addProperty(new PropertyIdImpl(propDef.getId(), (List<String>)defaultValue));
                    break;
                case INTEGER:
                    props.addProperty(new PropertyIntegerImpl(propDef.getId(), (List<BigInteger>)defaultValue));
                    break;
                case STRING:
                    props.addProperty(new PropertyStringImpl(propDef.getId(), (List<String>)defaultValue));
                    break;
                case URI:
                    props.addProperty(new PropertyUriImpl(propDef.getId(), (List<String>)defaultValue));
                    break;
                default:
                    throw new RuntimeException("Unknown datatype! Spec change?");
            }

            return true;
        }

        return false;
    }

    /**
     * Helper method for adding an id-valued property.<p>
     *
     * @param typeManager the type manager
     * @param props the properties to add to
     * @param typeId the type id
     * @param filter the property filter
     * @param id the property id
     * @param value the property value
     */
    public static void addPropertyId(
        CmsCmisTypeManager typeManager,
        PropertiesImpl props,
        String typeId,
        Set<String> filter,
        String id,
        String value) {

        if (!checkAddProperty(typeManager, props, typeId, filter, id)) {
            return;
        }
        PropertyIdImpl result = new PropertyIdImpl(id, value);
        result.setQueryName(id);

        props.addProperty(result);
    }

    /**
     * Helper method for adding an id-list-valued property.<p>
     *
     * @param typeManager
     * @param props the properties to add to
     * @param typeId the type id
     * @param filter the property filter
     * @param id the property id
     * @param value the property value
     */
    public static void addPropertyIdList(
        CmsCmisTypeManager typeManager,
        PropertiesImpl props,
        String typeId,
        Set<String> filter,
        String id,
        List<String> value) {

        if (!checkAddProperty(typeManager, props, typeId, filter, id)) {
            return;
        }

        props.addProperty(new PropertyIdImpl(id, value));
    }

    /**
     * Adds an integer property to a PropertiesImpl.<p>
     *
     * @param typeManager the type manager
     * @param props the properties
     * @param typeId the type id
     * @param filter the property filter string
     * @param id the property id
     * @param value the property value
     */
    public static void addPropertyInteger(
        CmsCmisTypeManager typeManager,
        PropertiesImpl props,
        String typeId,
        Set<String> filter,
        String id,
        long value) {

        addPropertyBigInteger(typeManager, props, typeId, filter, id, BigInteger.valueOf(value));
    }

    /**
     * Adds a string property to a PropertiesImpl.<p>
     *
     * @param typeManager
     * @param props the properties
     * @param typeId the type id
     * @param filter the property filter string
     * @param id the property id
     * @param value the property value
     */
    public static void addPropertyString(
        CmsCmisTypeManager typeManager,
        PropertiesImpl props,
        String typeId,
        Set<String> filter,
        String id,
        String value) {

        if (!checkAddProperty(typeManager, props, typeId, filter, id)) {
            return;
        }
        PropertyStringImpl result = new PropertyStringImpl(id, value);
        result.setQueryName(id);
        props.addProperty(result);
    }

    /**
     * Checks whether a property can be added to a Properties.
     *
     * @param typeManager
     * @param properties the properties object
     * @param typeId the type id
     * @param filter the property filter
     * @param id the property id
     *
     * @return true if the property should be added
     */
    public static boolean checkAddProperty(
        CmsCmisTypeManager typeManager,
        Properties properties,
        String typeId,
        Set<String> filter,
        String id) {

        if ((properties == null) || (properties.getProperties() == null)) {
            throw new IllegalArgumentException("Properties must not be null!");
        }

        if (id == null) {
            throw new IllegalArgumentException("Id must not be null!");
        }

        TypeDefinition type = typeManager.getType(typeId);
        if (type == null) {
            throw new IllegalArgumentException("Unknown type: " + typeId);
        }
        if (!type.getPropertyDefinitions().containsKey(id)) {
            throw new IllegalArgumentException("Unknown property: " + id);
        }

        String queryName = type.getPropertyDefinitions().get(id).getQueryName();

        if ((queryName != null) && (filter != null)) {
            if (!filter.contains(queryName)) {
                return false;
            } else {
                filter.remove(queryName);
            }
        }

        return true;
    }

    /**
     * Checks whether a name is a valid OpenCms resource name and throws an exception otherwise.<p>
     *
     * @param name the name to check
     */
    public static void checkResourceName(String name) {

        try {
            CmsResource.checkResourceName(name);
        } catch (CmsIllegalArgumentException e) {
            throw new CmisNameConstraintViolationException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Tries to lock a resource and throws an exception if it can't be locked.<p>
     *
     * Returns true only if the resource wasn't already locked before.<p>
     *
     * @param cms the CMS context
     * @param resource the resource to lock
     * @return true if the resource wasn't already locked
     *
     * @throws CmsException if something goes wrong
     */
    public static boolean ensureLock(CmsObject cms, CmsResource resource) throws CmsException {

        CmsLock lock = cms.getLock(resource);
        if (lock.isOwnedBy(cms.getRequestContext().getCurrentUser())) {
            return false;
        }
        cms.lockResourceTemporary(resource);
        return true;
    }

    /**
     * Gets a user-readable name for a principal id read from an ACE.<p>
     *
     * @param cms the current CMS context
     * @param principalId the principal id from the ACE
     * @return the name of the principle
     */
    public static String getAcePrincipalName(CmsObject cms, CmsUUID principalId) {

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
     * Converts an OpenCms ACE to a list of basic CMIS permissions.<p>
     *
     * @param ace the access control entry
     *
     * @return the list of permissions
     */
    public static List<String> getCmisPermissions(CmsAccessControlEntry ace) {

        int permissionBits = ace.getPermissions().getPermissions();
        List<String> result = new ArrayList<String>();
        if (0 != (permissionBits & CmsPermissionSet.PERMISSION_READ)) {
            result.add(A_CmsCmisRepository.CMIS_READ);
        }
        if (0 != (permissionBits & CmsPermissionSet.PERMISSION_WRITE)) {
            result.add(A_CmsCmisRepository.CMIS_WRITE);
        }
        int all = CmsPermissionSet.PERMISSION_WRITE
            | CmsPermissionSet.PERMISSION_READ
            | CmsPermissionSet.PERMISSION_CONTROL
            | CmsPermissionSet.PERMISSION_DIRECT_PUBLISH;
        if ((permissionBits & all) == all) {
            result.add(A_CmsCmisRepository.CMIS_ALL);
        }
        return result;
    }

    /**
     * Converts an OpenCms access control entry to a list of CMIS permissions which represent native OpenCms permissions.<p>
     *
     * @param ace the access control entry
     * @return the list of permissions for the entry
     */
    public static List<String> getNativePermissions(CmsAccessControlEntry ace) {

        List<String> result = getNativePermissions(ace.getPermissions().getAllowedPermissions(), false);
        result.addAll(getNativePermissions(ace.getPermissions().getDeniedPermissions(), true));
        return result;
    }

    /**
     * Converts an OpenCms access control bitset to a list of CMIS permissions representing native OpenCms permissions.<p>
     *
     * @param permissionBits the permission bits
     * @param denied if the permission bitset refers to a list of denied rather than allowed permissions
     *
     * @return the list of native permissions
     */
    public static List<String> getNativePermissions(int permissionBits, boolean denied) {

        List<String> result = new ArrayList<String>();
        String prefix = denied ? "opencms:deny-" : "opencms:";
        if ((permissionBits & CmsPermissionSet.PERMISSION_READ) != 0) {
            result.add(prefix + "read");
        }
        if ((permissionBits & CmsPermissionSet.PERMISSION_WRITE) != 0) {
            result.add(prefix + "write");
        }

        if ((permissionBits & CmsPermissionSet.PERMISSION_VIEW) != 0) {
            result.add(prefix + "view");
        }

        if ((permissionBits & CmsPermissionSet.PERMISSION_CONTROL) != 0) {
            result.add(prefix + "control");
        }

        if ((permissionBits & CmsPermissionSet.PERMISSION_DIRECT_PUBLISH) != 0) {
            result.add(prefix + "publish");
        }
        return result;
    }

    /**
     * Wrap OpenCms into OpenCMIS exceptions and rethrow them.<p>
     *
     * @param e the exception to handle
     */
    public static void handleCmsException(CmsException e) {

        if (e instanceof CmsVfsResourceNotFoundException) {
            throw new CmisObjectNotFoundException(e.getLocalizedMessage(), e);
        } else if (e instanceof CmsSecurityException) {
            throw new CmisUnauthorizedException(e.getLocalizedMessage(), e);
        } else {
            throw new CmisRuntimeException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Checks whether the given resource has any children.<p>
     *
     * @param cms the CMS context
     * @param resource the resource to check
     *
     * @return true if the resource has children
     *
     * @throws CmsException if something goes wrong
     */
    public static boolean hasChildren(CmsObject cms, CmsResource resource) throws CmsException {

        return !cms.getResourcesInFolder(cms.getSitePath(resource), CmsResourceFilter.ALL).isEmpty();
    }

    /**
     * Converts milliseconds into a calendar object.
     *
     * @param millis a time given in milliseconds after epoch
     * @return the calendar object for the given time
     */
    public static GregorianCalendar millisToCalendar(long millis) {

        GregorianCalendar result = new GregorianCalendar();
        result.setTimeZone(TimeZone.getTimeZone("GMT"));
        result.setTimeInMillis((long)(Math.ceil(millis / 1000) * 1000));
        return result;
    }

    /**
     * Splits a filter statement into a collection of properties. If
     * <code>filter</code> is <code>null</code>, empty or one of the properties
     * is '*' , an empty collection will be returned.
     *
     * @param filter the filter string
     * @return the set of components of the filter
     */
    public static Set<String> splitFilter(String filter) {

        if (filter == null) {
            return null;
        }

        if (filter.trim().length() == 0) {
            return null;
        }

        Set<String> result = new LinkedHashSet<String>();
        for (String s : filter.split(",")) {
            s = s.trim();
            if (s.equals("*")) {
                return null;
            } else if (s.length() > 0) {
                result.add(s);
            }
        }

        // set a few base properties
        // query name == id (for base type properties)
        result.add(PropertyIds.OBJECT_ID);
        result.add(PropertyIds.OBJECT_TYPE_ID);
        result.add(PropertyIds.BASE_TYPE_ID);

        return result;
    }

}
