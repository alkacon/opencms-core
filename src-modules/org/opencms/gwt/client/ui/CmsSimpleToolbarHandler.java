/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsSimpleToolbarHandler.java,v $
 * Date   : $Date: 2011/05/27 14:51:46 $
 * Version: $Revision: 1.9 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2011 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.gwt.client.ui;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.shared.CmsContextMenuEntryBean;
import org.opencms.gwt.shared.CmsCoreData.AdeContext;
import org.opencms.util.CmsUUID;

import java.util.List;

/**
 * Very basic implementation of the {@link I_CmsToolbarHandler} interface.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.9 $
 * 
 * @since 8.0.0
 */
public class CmsSimpleToolbarHandler extends A_CmsToolbarHandler {

    /** The currently active button. */
    private I_CmsToolbarButton m_activeButton;

    /** The context menu button. */
    private CmsToolbarContextButton m_contextButton;

    /**
     * @see org.opencms.gwt.client.ui.I_CmsToolbarHandler#activateSelection()
     */
    public void activateSelection() {

        // does nothing for now 
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsToolbarHandler#canEditProperties()
     */
    public boolean canEditProperties() {

        return true;
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsToolbarHandler#canOpenAvailabilityDialog()
     */
    public boolean canOpenAvailabilityDialog() {

        return true;
    }

    /**
     * De-activates the current button.<p> 
     */
    public void deactivateCurrentButton() {

        if (m_activeButton != null) {
            m_activeButton.setActive(false);
            m_activeButton = null;
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsToolbarHandler#getActiveButton()
     */
    public I_CmsToolbarButton getActiveButton() {

        return m_activeButton;
    }

    /**
     * Inserts the context menu.<p>
     *  
     * @param menuBeans the menu beans from the server
     * @param structureId the structure id of the resource at which the workplace should be opened 
     */
    public void insertContextMenu(List<CmsContextMenuEntryBean> menuBeans, CmsUUID structureId) {

        List<I_CmsContextMenuEntry> menuEntries = transformEntries(menuBeans, structureId);
        m_contextButton.showMenu(menuEntries);
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsToolbarHandler#loadContextMenu(org.opencms.util.CmsUUID, org.opencms.gwt.shared.CmsCoreData.AdeContext)
     */
    public void loadContextMenu(final CmsUUID structureId, final AdeContext context) {

        /** The RPC menu action for the container page dialog. */
        CmsRpcAction<List<CmsContextMenuEntryBean>> menuAction = new CmsRpcAction<List<CmsContextMenuEntryBean>>() {

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
            */
            @Override
            public void execute() {

                CmsCoreProvider.getService().getContextMenuEntries(structureId, context, this);
            }

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
            */
            @Override
            public void onResponse(List<CmsContextMenuEntryBean> menuBeans) {

                //@STRUCTUREID
                insertContextMenu(menuBeans, structureId);
            }
        };
        menuAction.execute();

    }

    /**
     * Sets the currently active tool-bar button.<p>
     * 
     * @param button the button
     */
    public void setActiveButton(I_CmsToolbarButton button) {

        m_activeButton = button;
    }

    /** 
     * Sets the context menu button.<p>
     * 
     * @param button the context menu button
     */
    public void setContextMenuButton(CmsToolbarContextButton button) {

        m_contextButton = button;
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsToolbarHandler#useAdeTemplates()
     */
    public boolean useAdeTemplates() {

        return false;
    }

}
