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

package org.opencms.ui.editors;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.I_CmsAppUIContext;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBrowserFrame;
import org.opencms.ui.components.CmsConfirmationDialog;

import org.apache.commons.logging.Log;

import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ExternalResource;

/**
 * Class to extended by frame based editors.<p>
 */
public abstract class A_CmsFrameEditor implements I_CmsEditor, ViewChangeListener {

    /** Log instance for this class. */
    private static final Log LOG = CmsLog.getLog(A_CmsFrameEditor.class);

    /** The serial version id. */
    private static final long serialVersionUID = 6944345583913510988L;

    /** The editor state. */
    protected CmsEditorStateExtension m_editorState;

    /** Flag indicating a view change. */
    boolean m_leaving;

    /** The currently edited resource. */
    CmsResource m_resource;

    /** The frame component. */
    private CmsBrowserFrame m_frame;

    /**
     * @see com.vaadin.navigator.ViewChangeListener#afterViewChange(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
     */
    public void afterViewChange(ViewChangeEvent event) {

        // nothing to do
    }

    /**
     * @see com.vaadin.navigator.ViewChangeListener#beforeViewChange(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
     */
    public boolean beforeViewChange(final ViewChangeEvent event) {

        if (!m_leaving && m_editorState.hasChanges()) {
            final String target = event.getViewName();
            CmsConfirmationDialog.show(
                CmsVaadinUtils.getMessageText(Messages.GUI_EDITOR_CLOSE_CAPTION_0),
                CmsVaadinUtils.getMessageText(Messages.GUI_EDITOR_CLOSE_TEXT_0),
                new Runnable() {

                    public void run() {

                        leaveEditor(event.getNavigator(), target);
                    }
                });
            return false;
        } else if (!m_leaving) {
            tryUnlock();
        }

        return true;
    }

    /**
     * @see org.opencms.ui.editors.I_CmsEditor#initUI(org.opencms.ui.apps.I_CmsAppUIContext, org.opencms.file.CmsResource, java.lang.String)
     */
    public void initUI(I_CmsAppUIContext context, CmsResource resource, String backLink) {

        m_resource = resource;
        CmsObject cms = A_CmsUI.getCmsObject();
        String sitepath = cms.getSitePath(m_resource);
        String link = OpenCms.getLinkManager().substituteLinkForRootPath(cms, getEditorUri());
        m_frame = new CmsBrowserFrame();
        m_frame.setDescription("Editor");
        m_frame.setName("edit");
        m_frame.setSource(new ExternalResource(link + "?resource=" + sitepath + "&backlink=" + backLink));
        m_frame.setSizeFull();
        context.showInfoArea(false);
        context.hideToolbar();
        m_frame.addStyleName("o-editor-frame");
        context.setAppContent(m_frame);
        context.setAppTitle(
            CmsVaadinUtils.getMessageText(
                Messages.GUI_CONTENT_EDITOR_TITLE_2,
                resource.getName(),
                CmsResource.getParentFolder(sitepath)));
        m_editorState = new CmsEditorStateExtension(m_frame);
    }

    /**
     * Returns the editor URI.<p>
     *
     * @return the editor URI
     */
    protected abstract String getEditorUri();

    /**
     * Leaves the editor view.<p>
     *
     * @param navigator the navigator instance
     * @param target the target view
     */
    void leaveEditor(Navigator navigator, String target) {

        m_leaving = true;
        tryUnlock();
        navigator.navigateTo(target);
    }

    /**
     * Tries to unlock the current resource.<p>
     */
    private void tryUnlock() {

        if (m_resource != null) {
            try {
                A_CmsUI.getCmsObject().unlockResource(m_resource);
            } catch (CmsException e) {
                LOG.debug("Unlocking resource " + m_resource.getRootPath() + " failed", e);
            }
        }
    }
}
