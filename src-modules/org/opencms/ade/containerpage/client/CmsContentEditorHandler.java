/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/Attic/CmsContentEditorHandler.java,v $
 * Date   : $Date: 2011/04/30 15:28:20 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.containerpage.client;

import org.opencms.gwt.client.ui.contenteditor.CmsContentEditorDialog;
import org.opencms.gwt.client.ui.contenteditor.I_CmsContentEditorHandler;
import org.opencms.util.CmsUUID;

/**
 * The container-page editor implementation of the XML content editor handler.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public class CmsContentEditorHandler implements I_CmsContentEditorHandler {

    /** The currently edited element's id. */
    private String m_currentElementId;

    /** The depending element's id. */
    private String m_dependingElementId;

    /** The container-page handler. */
    private CmsContainerpageHandler m_handler;

    /**
     * Constructor.<p>
     * 
     * @param handler the container-page handler
     */
    public CmsContentEditorHandler(CmsContainerpageHandler handler) {

        m_handler = handler;
    }

    /**
     * @see org.opencms.gwt.client.ui.contenteditor.I_CmsContentEditorHandler#onClose(java.lang.String, boolean)
     */
    public void onClose(String sitePath, boolean isNew) {

        if (m_dependingElementId != null) {
            m_handler.reloadElements(m_currentElementId, m_dependingElementId);
            m_dependingElementId = null;
        } else {
            m_handler.reloadElements(m_currentElementId);
        }
        m_currentElementId = null;
    }

    /**
     * Opens the XML content editor.<p>
     * 
     * @param elementId the element id
     * @param sitePath the element site-path
     * @param isNew <code>true</code> to create a new resource
     */
    public void openDialog(String elementId, String sitePath, boolean isNew) {

        openDialog(elementId, sitePath, isNew, null);
    }

    /**
     * Opens the XML content editor.<p>
     * 
     * @param elementId the element id
     * @param sitePath the element site-path
     * @param isNew <code>true</code> to create a new resource
     * @param dependingElementId id of the element that needs to be refreshed when editing is finished
     */
    public void openDialog(String elementId, String sitePath, boolean isNew, String dependingElementId) {

        m_currentElementId = elementId;
        m_dependingElementId = dependingElementId;
        CmsUUID structureId = new CmsUUID(CmsContainerpageController.getServerId(m_currentElementId));
        CmsContentEditorDialog.get().openEditDialog(structureId, sitePath, isNew, this);
    }

}
