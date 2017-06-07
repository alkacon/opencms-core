/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.file.types;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.db.CmsSecurityManager;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResource.CmsResourceDeleteMode;
import org.opencms.file.CmsResource.CmsResourceUndoMode;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.json.JSONObject;
import org.opencms.jsp.util.CmsJspLinkMacroResolver;
import org.opencms.loader.CmsJspLoader;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsLink;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.containerpage.CmsFormatterBean;
import org.opencms.xml.containerpage.CmsFormatterConfiguration;
import org.opencms.xml.containerpage.I_CmsFormatterBean;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;

/**
 * Resource type descriptor for the type "jsp".<p>
 *
 * Ensures that some required file properties are attached to new JSPs.<p>
 *
 * The value for the encoding properties of a new JSP usually is the
 * system default encoding, but this can be overwritten by
 * a configuration parameters set in <code>opencms-vfs.xml</code>.<p>
 *
 * @since 6.0.0
 */
public class CmsResourceTypeJsp extends A_CmsResourceTypeLinkParseable {

    /** Key for formatter setting container maximum width. */
    public static final String FORMATTER_SETTING_MAX_WIDTH = "maxwidth";

    /** Key for formatter setting container minimum width. */
    public static final String FORMATTER_SETTING_MIN_WIDTH = "minwidth";

    /** Key for formatter setting container type. */
    public static final String FORMATTER_SETTING_TYPE = "type";

    /** The type id of the containerpage_template resource type. */
    private static final int CONTAINERPAGE_TEMPLATE_TYPE_ID = 21;

    /** The type name of the containerpage_template resource type. */
    private static final String CONTAINERPAGE_TEMPLATE_TYPE_NAME = "containerpage_template";

    /** The type id of the JSP resource type. */
    private static final int JSP_RESOURCE_TYPE_ID = 4;

    /** Static reference to the log. */
    private static final Log LOG = CmsLog.getLog(org.opencms.file.types.CmsResourceTypeJsp.class);

    /** The registered JSP resource type id's.    */
    private static List<Integer> m_jspResourceTypeIds = new ArrayList<Integer>();

    /** The name of this resource type. */
    private static final String RESOURCE_TYPE_NAME = "jsp";

    /** JSP Loader instance. */
    protected CmsJspLoader m_jspLoader;

    /**
     * Returns the type id of the containerpage_template resource type.<p>
     *
     * @return the type id of the containerpage_template resource type
     */
    public static int getContainerPageTemplateTypeId() {

        return CONTAINERPAGE_TEMPLATE_TYPE_ID;
    }

    /**
     * Returns the type name of the containerpage_template resource type.<p>
     *
     * @return the type name of the containerpage_template resource type
     */
    public static String getContainerPageTemplateTypeName() {

        return CONTAINERPAGE_TEMPLATE_TYPE_NAME;
    }

    /**
     * Returns the registered JSP resource type id's.<p>
     *
     * @return the resource type id's
     */
    public static List<Integer> getJspResourceTypeIds() {

        return m_jspResourceTypeIds;
    }

    /**
     * Returns the type id of the (default)JSP resource type.<p>
     *
     * @return the type id of this (default)JSP resource type
     */
    public static int getJSPTypeId() {

        return JSP_RESOURCE_TYPE_ID;
    }

    /**
     * Returns the static type name of this (default) resource type.<p>
     *
     * @return the static type name of this (default) resource type
     */
    public static String getStaticTypeName() {

        return RESOURCE_TYPE_NAME;
    }

    /**
     * Returns <code>true</code> in case the given resource is a JSP.<p>
     *
     * Internally this checks if the given resource type has an id that is registered as a JSP resource type.<p>
     *
     * @param resource the resource to check
     *
     * @return <code>true</code> in case the given resource is a JSP
     *
     * @since 8.0.0
     */
    public static boolean isJsp(CmsResource resource) {

        return resource == null ? false : isJspTypeId(resource.getTypeId());
    }

    /**
     * Returns <code>true</code> in case the given resource type id is a JSP type.<p>
     *
     * Internally this checks if the given resource type id is registered as a JSP resource type.<p>
     *
     * @param typeId the resource type id to check
     *
     * @return <code>true</code> in case the given resource type id is a JSP type
     *
     * @since 8.0.0
     */
    public static boolean isJspTypeId(int typeId) {

        return m_jspResourceTypeIds.contains(Integer.valueOf(typeId));
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#chtype(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.file.CmsResource, int)
     */
    @Override
    public void chtype(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource, int type)
    throws CmsException {

        Set<String> references = getReferencingStrongLinks(cms, resource);
        super.chtype(cms, securityManager, resource, type);
        removeReferencingFromCache(references);
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#deleteResource(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.file.CmsResource, org.opencms.file.CmsResource.CmsResourceDeleteMode)
     */
    @Override
    public void deleteResource(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        CmsResourceDeleteMode siblingMode) throws CmsException {

        Set<String> references = getReferencingStrongLinks(cms, resource);
        super.deleteResource(cms, securityManager, resource, siblingMode);
        removeReferencingFromCache(references);
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#getFormattersForResource(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    @Override
    public CmsFormatterConfiguration getFormattersForResource(CmsObject cms, CmsResource res) {

        // by default JSPs may be rendered within any container with itself as formatter
        String containerType = CmsFormatterBean.WILDCARD_TYPE;
        int minWidth = 1;
        int maxWidth = Integer.MAX_VALUE;
        try {
            // check template property to override default settings
            String formatterSetting = cms.readPropertyObject(
                res,
                CmsPropertyDefinition.PROPERTY_TEMPLATE,
                false).getValue();
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(formatterSetting)) {
                JSONObject setting = new JSONObject(formatterSetting);
                if (setting.has(FORMATTER_SETTING_TYPE)) {
                    containerType = setting.getString(FORMATTER_SETTING_TYPE);
                }
                if (setting.has(FORMATTER_SETTING_MIN_WIDTH)) {
                    minWidth = setting.getInt(FORMATTER_SETTING_MIN_WIDTH);
                }
                if (setting.has(FORMATTER_SETTING_MAX_WIDTH)) {
                    maxWidth = setting.getInt(FORMATTER_SETTING_MAX_WIDTH);
                }
            }
        } catch (Exception e) {
            LOG.error(
                Messages.get().getBundle().key(
                    Messages.ERR_PARSING_FORMATTER_SETTINGS_FROM_PROPERTY_2,
                    res.getName(),
                    CmsPropertyDefinition.PROPERTY_TEMPLATE),
                e);
        }
        CmsFormatterBean selfFormatter = new CmsFormatterBean(
            containerType,
            res.getRootPath(),
            res.getStructureId(),
            minWidth,
            maxWidth,
            true,
            false,
            res.getRootPath());

        return CmsFormatterConfiguration.create(cms, Collections.<I_CmsFormatterBean> singletonList(selfFormatter));
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#getLoaderId()
     */
    @Override
    public int getLoaderId() {

        return CmsJspLoader.RESOURCE_LOADER_ID;
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#initConfiguration(java.lang.String, java.lang.String, String)
     */
    @Override
    public void initConfiguration(String name, String id, String className) throws CmsConfigurationException {

        super.initConfiguration(name, id, className);
        // set static members with values from the configuration
        addTypeId(m_typeId);
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#initialize(org.opencms.file.CmsObject)
     */
    @Override
    public void initialize(CmsObject cms) {

        super.initialize(cms);
        try {
            m_jspLoader = (CmsJspLoader)OpenCms.getResourceManager().getLoader(CmsJspLoader.RESOURCE_LOADER_ID);
        } catch (ArrayIndexOutOfBoundsException e) {
            // ignore, loader not configured
        }
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#moveResource(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.file.CmsResource, java.lang.String)
     */
    @Override
    public void moveResource(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        String destination) throws CmsException, CmsIllegalArgumentException {

        Set<String> references = getReferencingStrongLinks(cms, resource);
        super.moveResource(cms, securityManager, resource, destination);
        removeReferencingFromCache(references);
    }

    /**
     * @see org.opencms.relations.I_CmsLinkParseable#parseLinks(org.opencms.file.CmsObject, org.opencms.file.CmsFile)
     */
    public List<CmsLink> parseLinks(CmsObject cms, CmsFile file) {

        CmsJspLinkMacroResolver macroResolver = new CmsJspLinkMacroResolver(cms, file.getRootPath(), false);
        String encoding = CmsLocaleManager.getResourceEncoding(cms, file);
        String content = CmsEncoder.createString(file.getContents(), encoding);
        macroResolver.resolveMacros(content); // ignore return value
        return macroResolver.getLinks();
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#replaceResource(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.file.CmsResource, int, byte[], java.util.List)
     */
    @Override
    public void replaceResource(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        int type,
        byte[] content,
        List<CmsProperty> properties) throws CmsException {

        Set<String> references = getReferencingStrongLinks(cms, resource);
        super.replaceResource(cms, securityManager, resource, type, content, properties);
        removeReferencingFromCache(references);
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#restoreResource(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.file.CmsResource, int)
     */
    @Override
    public void restoreResource(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource, int version)
    throws CmsException {

        Set<String> references = getReferencingStrongLinks(cms, resource);
        super.restoreResource(cms, securityManager, resource, version);
        removeReferencingFromCache(references);
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#setDateExpired(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.file.CmsResource, long, boolean)
     */
    @Override
    public void setDateExpired(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        long dateExpired,
        boolean recursive) throws CmsException {

        Set<String> references = getReferencingStrongLinks(cms, resource);
        super.setDateExpired(cms, securityManager, resource, dateExpired, recursive);
        removeReferencingFromCache(references);
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#setDateLastModified(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.file.CmsResource, long, boolean)
     */
    @Override
    public void setDateLastModified(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        long dateLastModified,
        boolean recursive) throws CmsException {

        Set<String> references = getReferencingStrongLinks(cms, resource);
        super.setDateLastModified(cms, securityManager, resource, dateLastModified, recursive);
        removeReferencingFromCache(references);
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#setDateReleased(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.file.CmsResource, long, boolean)
     */
    @Override
    public void setDateReleased(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        long dateReleased,
        boolean recursive) throws CmsException {

        Set<String> references = getReferencingStrongLinks(cms, resource);
        super.setDateReleased(cms, securityManager, resource, dateReleased, recursive);
        removeReferencingFromCache(references);
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#undoChanges(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.file.CmsResource, org.opencms.file.CmsResource.CmsResourceUndoMode)
     */
    @Override
    public void undoChanges(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        CmsResourceUndoMode mode) throws CmsException {

        Set<String> references = getReferencingStrongLinks(cms, resource);
        super.undoChanges(cms, securityManager, resource, mode);
        if (m_jspLoader != null) {
            // we need to remove the JSP explicitly because undoing the changes also
            // resets the last modification date, so the automatic mechanism for updating
            // JSPs based on modification dates doesn't work.
            m_jspLoader.removeOfflineJspFromRepository(resource);
        }
        removeReferencingFromCache(references);
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#writeFile(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.file.CmsFile)
     */
    @Override
    public CmsFile writeFile(CmsObject cms, CmsSecurityManager securityManager, CmsFile resource) throws CmsException {

        // actualize the link paths and/or ids
        CmsJspLinkMacroResolver macroResolver = new CmsJspLinkMacroResolver(cms, resource.getRootPath(), false);
        String encoding = CmsLocaleManager.getResourceEncoding(cms, resource);
        String content = CmsEncoder.createString(resource.getContents(), encoding);
        content = macroResolver.resolveMacros(content);
        try {
            resource.setContents(content.getBytes(encoding));
        } catch (UnsupportedEncodingException e) {
            // this should usually never happen since the encoding is already used before
            resource.setContents(content.getBytes());
        }
        // write the content with the 'right' links
        Set<String> references = getReferencingStrongLinks(cms, resource);
        CmsFile file = super.writeFile(cms, securityManager, resource);
        removeReferencingFromCache(references);
        return file;
    }

    /**
     * Returns a set of root paths of files that are including the given resource using the 'link.strong' macro.<p>
     *
     * @param cms the current cms context
     * @param resource the resource to check
     *
     * @return the set of referencing paths
     *
     * @throws CmsException if something goes wrong
     */
    protected Set<String> getReferencingStrongLinks(CmsObject cms, CmsResource resource) throws CmsException {

        Set<String> references = new HashSet<String>();
        if (m_jspLoader == null) {
            return references;
        }
        m_jspLoader.getReferencingStrongLinks(cms, resource, references);
        return references;
    }

    /**
     * Removes the referencing resources from the cache.<p>
     *
     * @param references the references to remove
     */
    protected void removeReferencingFromCache(Set<String> references) {

        if (m_jspLoader != null) {
            m_jspLoader.removeFromCache(references, false);
        }
    }

    /**
     * Adds another resource type id to the registered JSP resource type id's.<p>
     *
     * @param typeId the resource type id to add
     */
    private void addTypeId(int typeId) {

        m_jspResourceTypeIds.add(Integer.valueOf(typeId));
    }
}