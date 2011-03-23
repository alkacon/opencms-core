/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/directedit/CmsDirectEditParams.java,v $
 * Date   : $Date: 2011/03/23 14:50:40 $
 * Version: $Revision: 1.8 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.workplace.editors.directedit;

/**
 * A parameter set to start a direct edit element, for internal use only.<p>
 * 
 * @author Alexander Kandzior
 * 
 * @version $Revision: 1.8 $ 
 * 
 * @since 6.2.3
 */
public class CmsDirectEditParams {

    /** The selected element in the target content.*/
    protected String m_element;

    /** The link to the current page useed when closing an editor or dialog. */
    protected String m_linkForClose;

    /** The link to create a new VFS resource of the edited type. */
    protected String m_linkForNew;

    /** The direct edit mode to use. */
    protected CmsDirectEditMode m_mode;

    /** The direct edit options to display buttons for. */
    protected CmsDirectEditButtonSelection m_options;

    /** The edit target VFS resource name. */
    protected String m_resourceName;

    /**
     * Creates a new direct edit parameter set usually used for including the head HTML.<p>
     * 
     * @param linkForClose the link to the current page useed when closing an editor or dialog
     */
    public CmsDirectEditParams(String linkForClose) {

        m_resourceName = null;
        m_options = null;
        m_element = null;
        m_linkForNew = null;
        m_linkForClose = linkForClose;
        m_mode = CmsDirectEditMode.TRUE;
    }

    /**
     * Creates a new direct edit parameter set usually used within a XML content load loop for a <code>xmlcontent</code>.<p>
     * 
     * @param resourceName the edit target VFS resource name
     * @param options the direct edit options to display buttons for
     * @param linkForNew the link to create a new VFS resource of the edited type
     * @param mode the direct edit mode to use
     */
    public CmsDirectEditParams(
        String resourceName,
        CmsDirectEditButtonSelection options,
        CmsDirectEditMode mode,
        String linkForNew) {

        m_resourceName = resourceName;
        m_options = options;
        m_element = null;
        m_linkForNew = linkForNew;
        m_linkForClose = null;
        m_mode = mode != null ? mode : CmsDirectEditMode.TRUE;
    }

    /**
     * Creates a new direct edit parameter set usually used within a <code>cms:include</code> call for a <code>xmlpage</code>.<p>
     * 
     * @param resourceName the edit target VFS resource name
     * @param element the selected element in the target content
     */
    public CmsDirectEditParams(String resourceName, String element) {

        m_resourceName = resourceName;
        m_options = CmsDirectEditButtonSelection.EDIT;
        m_element = element;
        m_linkForNew = null;
        m_linkForClose = null;
        m_mode = CmsDirectEditMode.TRUE;
    }

    /**
     * Returns the direct edit buttons selection to display.<p>
     *
     * @return the direct edit buttons selection to display
     */
    public CmsDirectEditButtonSelection getButtonSelection() {

        return m_options;
    }

    /**
     * Returns the selected element in the target content.<p>
     *
     * @return the selected element in the target content
     */
    public String getElement() {

        return m_element;
    }

    /**
     * Returns the link to the current page useed when closing an editor or dialog.<p>
     *
     * @return the link to the current page useed when closing an editor or dialog
     */
    public String getLinkForClose() {

        return m_linkForClose;
    }

    /**
     * Returns the link to delete the selected VFS resource.<p>
     *
     * @return the link to delete the selected VFS resource
     */
    public String getLinkForDelete() {

        return "/system/workplace/commons/delete.jsp";
    }

    /**
     * Returns the link to edit the selected VFS resource (element).<p>
     *
     * @return the link to edit the selected VFS resource (element)
     */
    public String getLinkForEdit() {

        return "/system/workplace/editors/editor.jsp";
    }

    /**
     * Returns the link to create a new VFS resource of the edited type.<p>
     *
     * @return the link to create a new VFS resource of the edited type
     */
    public String getLinkForNew() {

        return m_linkForNew;
    }

    /**
     * Returns the direct edit mode.<p>
     * 
     * @return the direct edit mode
     */
    public CmsDirectEditMode getMode() {

        return m_mode;
    }

    /**
     * Returns the edit target VFS resource name.<p>
     *
     * @return the edit target VFS resource name
     */
    public String getResourceName() {

        return m_resourceName;
    }
}