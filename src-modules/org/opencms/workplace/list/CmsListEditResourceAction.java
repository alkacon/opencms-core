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

package org.opencms.workplace.list;

import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.lock.CmsLock;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.explorer.CmsResourceUtil;

/**
 * Opens the selected resource in a new window.<p>
 *
 * @since 6.0.0
 */
public class CmsListEditResourceAction extends CmsListDirectAction {

    /** Id of the column with the resource root path. */
    private final String m_resColumnPathId;

    /** The current resource util object. */
    private CmsResourceUtil m_resourceUtil;

    /**
     * Default Constructor.<p>
     *
     * @param id the unique id
     * @param resColumnPathId the id of the column with the resource root path
     */
    public CmsListEditResourceAction(String id, String resColumnPathId) {

        super(id);
        m_resColumnPathId = resColumnPathId;
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#getHelpText()
     */
    @Override
    public CmsMessageContainer getHelpText() {

        CmsMessageContainer helptext = super.getHelpText();
        if (helptext == null) {
            if (isEnabled()) {
                helptext = Messages.get().container(Messages.GUI_EDITRESOURCE_ACTION_HELP_0);
            } else {
                helptext = Messages.get().container(Messages.GUI_EDITRESOURCE_DISABLED_ACTION_HELP_0);
            }
        }
        return helptext;
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#getIconPath()
     */
    @Override
    public String getIconPath() {

        String iconpath = super.getIconPath();
        if (iconpath == null) {
            if (isEnabled()) {
                iconpath = "list/resource_edit.png";
            } else {
                iconpath = "list/resource_edit_disabled.png";
            }
        }
        return iconpath;
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#getName()
     */
    @Override
    public CmsMessageContainer getName() {

        CmsMessageContainer name = super.getName();
        if (name == null) {
            if (isEnabled()) {
                name = Messages.get().container(Messages.GUI_EDITRESOURCE_ACTION_NAME_0);
            } else {
                name = Messages.get().container(Messages.GUI_EDITRESOURCE_DISABLED_ACTION_NAME_0);
            }
        }
        return name;
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#isVisible()
     */
    @Override
    public boolean isVisible() {

        if (getResourceName() != null) {
            try {
                // if resource type if editable
                if (OpenCms.getWorkplaceManager().getEditorHandler().getEditorUri(
                    getResourceName(),
                    getWp().getJsp()) != null) {
                    // check lock state
                    CmsLock lock = getResourceUtil().getLock();
                    if (lock.isNullLock() || lock.isOwnedBy((getWp().getCms().getRequestContext().getCurrentUser()))) {
                        return isEnabled();
                    }
                }
            } catch (Throwable e) {
                // ignore
            }
        }
        return !isEnabled();
    }

    /**
     * @see org.opencms.workplace.list.I_CmsListDirectAction#setItem(org.opencms.workplace.list.CmsListItem)
     */
    @Override
    public void setItem(CmsListItem item) {

        m_resourceUtil = ((A_CmsListExplorerDialog)getWp()).getResourceUtil(item);
        super.setItem(item);
    }

    /**
     * Returns the current result Util.<p>
     *
     * @return the current result Util
     */
    protected CmsResourceUtil getResourceUtil() {

        return m_resourceUtil;
    }

    /**
     * Returns the most possible right resource name.<p>
     *
     * @return the most possible right resource name
     */
    private String getResourceName() {

        String resource = getItem().get(m_resColumnPathId).toString();
        if (!getWp().getCms().existsResource(resource, CmsResourceFilter.DEFAULT)) {
            String siteRoot = OpenCms.getSiteManager().getSiteRoot(resource);
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(siteRoot)) {
                resource = resource.substring(siteRoot.length());
            }
            if (!getWp().getCms().existsResource(resource, CmsResourceFilter.DEFAULT)) {
                resource = null;
            }
        }
        return resource;
    }
}