/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.jsp;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.ade.configuration.CmsResourceTypeConfig;
import org.opencms.ade.contenteditor.shared.CmsEditorConstants;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsFolder;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.collectors.A_CmsResourceCollector;
import org.opencms.file.collectors.I_CmsCollectorPostCreateHandler;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.flex.CmsFlexController;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.editors.directedit.CmsDirectEditButtonSelection;
import org.opencms.workplace.editors.directedit.CmsDirectEditParams;

import java.util.Locale;

import javax.servlet.jsp.JspException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

/** This tag is used to attach an edit provider to a snippet of HTML. */
public class CmsJspTagEdit extends CmsJspScopedVarBodyTagSuport {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspTagEdit.class);

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = -3781368910893187306L;

    /** Identifier to indicate that the new link should be handled by this tag - not by a {@link org.opencms.file.collectors.I_CmsResourceCollector}. */
    public static final String NEW_LINK_IDENTIFIER = "__edit__";

    /** UUID of the content to edit. */
    private String m_uuid;

    /** Flag, indicating if the delete option should be displayed. */
    private boolean m_canDelete;

    /** Flag, indicating if the create option should be displayed. */
    private boolean m_canCreate;

    /** Flag, indicating if during rendering the "startDirectEdit" part has been rendered, but not the "endDirectEdit" part. */
    private boolean m_isEditOpen;

    /** The resource to edit. */
    private CmsResource m_resource;

    /** The type of the resource that should be created. */
    private I_CmsResourceType m_createType;

    /** The tag attribute's value, specifying the path to the (sub)sitemap where new content should be created. */
    private String m_creationSiteMapAttribute;

    /** The (sub)sitemap where new content should be created. */
    private CmsFolder m_creationSiteMap;

    /** The CmsObject from the current request context. */
    private CmsObject m_cms;

    /** The buttons shown in the edit options. */
    private CmsDirectEditButtonSelection m_buttonSelection;

    /** The fully qualified class name of the post create handler to use. */
    private String m_postCreateHandler;

    /** Creates a new resource.
     * @param cmsObject The CmsObject of the current request context.
     * @param newLink A string, specifying where which new content should be created.
     * @param locale The locale for which the
     * @param sitePath site path of the currently edited content.
     * @param modelFileName not used.
     * @param mode optional creation mode
     * @param postCreateHandler optional class name of an {@link I_CmsCollectorPostCreateHandler} which is invoked after the content has been created.
     * @return The site-path of the newly created resource.
     */
    public static String createResource(
        final CmsObject cmsObject,
        final String newLink,
        final Locale locale,
        final String sitePath,
        final String modelFileName,
        final String mode,
        final String postCreateHandler) {

        String[] newLinkParts = newLink.split("\\|");
        String rootPath = newLinkParts[1];
        String typeName = newLinkParts[2];
        CmsFile modelFile = null;
        if (StringUtils.equalsIgnoreCase(mode, CmsEditorConstants.MODE_COPY)) {
            try {
                modelFile = cmsObject.readFile(sitePath);
            } catch (CmsException e) {
                // TODO: localize.
                LOG.warn(
                    "The resource at path" + sitePath + "could not be read. Thus it can not be used as model file.",
                    e);
            }
        }
        CmsADEConfigData adeConfig = OpenCms.getADEManager().lookupConfiguration(cmsObject, rootPath);
        CmsResourceTypeConfig typeConfig = adeConfig.getResourceType(typeName);
        CmsResource newElement = null;
        try {
            newElement = typeConfig.createNewElement(cmsObject, modelFile, rootPath);
            I_CmsCollectorPostCreateHandler handler = A_CmsResourceCollector.getPostCreateHandler(postCreateHandler);
            handler.onCreate(cmsObject, cmsObject.readFile(newElement), modelFile != null);
        } catch (CmsException e) {
            // TODO: Localize and improve error message.
            LOG.error("Could not create resource.", e);
        }
        return newElement == null ? null : cmsObject.getSitePath(newElement);
    }

    /**
     * @see javax.servlet.jsp.tagext.BodyTagSupport#doEndTag()
     */
    @Override
    public int doEndTag() throws JspException {

        // TODO: Override release?
        if (m_isEditOpen) {
            CmsJspTagEditable.endDirectEdit(pageContext);
        }
        release();
        return super.doEndTag();
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    @Override
    public int doStartTag() throws CmsIllegalArgumentException {

        // initialize the content load tag
        init();
        addDirectEditStart();
        return EVAL_BODY_INCLUDE;
    }

    /**
     * @see org.opencms.jsp.CmsJspScopedVarBodyTagSuport#release()
     */
    @Override
    public void release() {

        m_canCreate = false;
        m_canDelete = false;
        m_buttonSelection = null;
        m_cms = null;
        m_creationSiteMap = null;
        m_creationSiteMapAttribute = null;
        m_createType = null;
        m_isEditOpen = false;
        m_resource = null;
        m_uuid = null;
        super.release();
    }

    /** Setter for the "create" attribute of the tag.
     * @param canCreate value of the tag's attribute "create".
     */
    public void setCreate(final Boolean canCreate) {

        m_canCreate = canCreate == null ? false : canCreate.booleanValue();
    }

    /** Setter for the "createType" attribute of the tag.
     * @param typeName value of the "createType" attribute of the tag.
     */
    public void setCreateType(final String typeName) {

        try {
            m_createType = OpenCms.getResourceManager().getResourceType(typeName);
        } catch (CmsLoaderException e) {
            // TODO: localize log entry
            LOG.warn("Resource type" + typeName + "cannot be loaded.", e);
        }
    }

    /** Setter for the "creationSiteMap" attribute of the tag.
     * @param sitePath value of the "creationSiteMap" attribute of the tag.
     */
    public void setCreationSiteMap(final String sitePath) {

        m_creationSiteMapAttribute = sitePath;
    }

    /**Setter for the "delete" attribute of the tag.
     * @param canDelete value of the "delete" attribute of the tag.
     */
    public void setDelete(final Boolean canDelete) {

        m_canDelete = canDelete == null ? false : canDelete.booleanValue();
    }

    /** Setter for the "postCreateHandler" attribute of the tag.
     * @param postCreateHandler fully qualified class name of the {@link I_CmsCollectorPostCreateHandler} to use.
     */
    public void setPostCreateHandler(final String postCreateHandler) {

        m_postCreateHandler = postCreateHandler;
    }

    /** Setter for the uuid attribute of the tag, providing the uuid of content that should be edited.
     * If no valid uuid of an existing resource is given, it is assumed the tag is only used for creating new contents.
     * @param uuid the uuid of the content that should be edited.
     */
    public void setUuid(final String uuid) {

        m_uuid = uuid;
    }

    /**
     * Initialization for the tag, called from {@link #doStartTag()}.
     */
    protected void init() {

        initCmsObject();
        initResourceToEdit();
        initCreateInfo();
        initDirectEditButtonSelection();
    }

    /**
     * Renders the "startDirectEdit" part, if it can/should be rendered at all.
     */
    private void addDirectEditStart() {

        if (null != m_buttonSelection) { // editing is possible at all
            String resourceName = m_resource != null
            ? m_cms.getSitePath(m_resource)
            : m_cms.getRequestContext().getFolderUri();
            String newLink = createNewLink();
            CmsDirectEditParams editParams = new CmsDirectEditParams(resourceName, m_buttonSelection, null, newLink);
            editParams.setPostCreateHandler(m_postCreateHandler);
            try {
                CmsJspTagEditable.startDirectEdit(pageContext, editParams);
                m_isEditOpen = true;
            } catch (JspException e) {
                // TODO: Localize and improve error message.
                LOG.error("Could not create direct edit start.", e);
            }
        }

    }

    /** Creates the String specifying where which type of resource has to be created.
     * @return The String identifying which type of resource has to be created where.
     *
     * @see #createResource(CmsObject, String, Locale, String, String, String, String)
     */
    private String createNewLink() {

        if (m_createType != null) {
            String rootPath = m_creationSiteMap != null
            ? m_creationSiteMap.getRootPath()
            : m_cms.addSiteRoot(m_cms.getRequestContext().getFolderUri());
            String typeName = m_createType.getTypeName();
            StringBuffer newLink = new StringBuffer(NEW_LINK_IDENTIFIER);
            newLink.append('|');
            newLink.append(rootPath);
            newLink.append('|');
            newLink.append(typeName);

            return newLink.toString();
        }
        return null;
    }

    /**
     * Initializes {@link #m_cms} with the current CmsObject.
     */
    private void initCmsObject() {

        CmsFlexController controller = CmsFlexController.getController(pageContext.getRequest());
        m_cms = controller.getCmsObject();

    }

    /**
     * Constructs the creation sub-site and the creation type from values given as attributes of the tag.
     */
    private void initCreateInfo() {

        // set the creation sub-site from the tag's attribute
        if (null != m_creationSiteMapAttribute) {
            try {
                m_creationSiteMap = m_cms.readFolder(m_creationSiteMapAttribute);
            } catch (CmsException e) {

                // TODO: Localize log output.
                LOG.warn("The provided creation sitemap " + m_creationSiteMapAttribute + " is not a VFS folder.", e);
            }
        }

        // reconstruct create type from the edit-resource if necessary
        if (m_canCreate && (m_createType == null) && (m_resource != null)) {
            m_createType = OpenCms.getResourceManager().getResourceType(m_resource);
        }

    }

    /**
     * Initializes the button selection shown in the edit options according to the values of the tag's attributes.
     */
    private void initDirectEditButtonSelection() {

        if (null != m_resource) {
            if (m_createType != null) {
                if (m_canDelete) {
                    m_buttonSelection = CmsDirectEditButtonSelection.EDIT_DELETE_NEW;
                } else {
                    m_buttonSelection = CmsDirectEditButtonSelection.EDIT_NEW;
                }
            } else {
                if (m_canDelete) {
                    m_buttonSelection = CmsDirectEditButtonSelection.EDIT_DELETE;
                } else {
                    m_buttonSelection = CmsDirectEditButtonSelection.EDIT;
                }
            }
        } else {
            if (m_createType != null) {
                m_buttonSelection = CmsDirectEditButtonSelection.NEW;
            }
        }
    }

    /**
     * Initializes the resource to edit according to the uuid provided via the tag's attribute "uuid".
     */
    private void initResourceToEdit() {

        if (m_uuid != null) {
            try {
                CmsUUID uuid = new CmsUUID(m_uuid);
                m_resource = m_cms.readResource(uuid);

            } catch (NumberFormatException | CmsException e) {
                // TODO: Localize debug message.
                LOG.warn("UUID was not valid or there is no resource with the given UUID.", e);
            }
        }

    }
}
