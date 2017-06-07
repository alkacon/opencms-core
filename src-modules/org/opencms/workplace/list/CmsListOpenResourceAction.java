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
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

/**
 * Opens the selected resource in a new window.<p>
 *
 * @since 6.0.0
 */
public class CmsListOpenResourceAction extends A_CmsListDefaultJsAction {

    /** Id of the column with the resource root path. */
    private final String m_resColumnPathId;

    /**
     * Default Constructor.<p>
     *
     * @param id the unique id
     * @param resColumnPathId the id of the column with the resource root path
     */
    public CmsListOpenResourceAction(String id, String resColumnPathId) {

        super(id);
        m_resColumnPathId = resColumnPathId;
        setName(Messages.get().container(Messages.GUI_OPENRESOURCE_ACTION_NAME_0));
        setHelpText(Messages.get().container(Messages.GUI_OPENRESOURCE_ACTION_HELP_0));
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#getHelpText()
     */
    @Override
    public CmsMessageContainer getHelpText() {

        if (isEnabled()) {
            return super.getHelpText();
        }
        return Messages.get().container(Messages.GUI_OPENRESOURCE_ACTION_DISABLED_HELP_0);
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#isEnabled()
     */
    @Override
    public boolean isEnabled() {

        if (getResourceName() != null) {
            return super.isEnabled();
        }
        return false;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDefaultJsAction#jsCode()
     */
    @Override
    public String jsCode() {

        StringBuffer jsCode = new StringBuffer(256);
        jsCode.append("javascript:top.openwinfull('");
        jsCode.append(getResourceName());
        jsCode.append("', true)");
        return jsCode.toString();
    }

    /**
     * Returns the most possible right resource name.<p>
     *
     * @return the most possible right resource name
     */
    protected String getResourceName() {

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