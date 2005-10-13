/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/list/CmsListOpenResourceAction.java,v $
 * Date   : $Date: 2005/10/13 13:26:55 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.site.CmsSiteManager;
import org.opencms.util.CmsStringUtil;

/**
 * Opens the selected resource in a new window.<p>
 * 
 * Be sure your list updates the cms context, overriding the {@link org.opencms.workplace.list.A_CmsListDialog#getList()} method, like:
 * <pre>
 *       // assure we have the right cms
 *       CmsHtmlList list = super.getList();
 *       if (list != null) {
 *           CmsListColumnDefinition col = list.getMetadata().getColumnDefinition(LIST_COLUMN_NAME);
 *           if (col != null) {
 *               ((CmsListOpenResourceAction)col.getDefaultAction(LIST_DEFACTION_OPEN)).setCms(getCms());
 *           }
 *       }
 *       return list;
 * </pre>
 * 
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 6.0.0 
 */
public class CmsListOpenResourceAction extends A_CmsListDefaultJsAction {

    /** Cms context. */
    private CmsObject m_cms;

    /** Id of the column with the resource root path. */
    private final String m_resColumnPathId;

    /**
     * Default Constructor.<p>
     * 
     * @param id the unique id
     * @param cms the cms context
     * @param resColumnPathId the id of the column with the resource root path
     */
    public CmsListOpenResourceAction(String id, CmsObject cms, String resColumnPathId) {

        super(id);
        m_resColumnPathId = resColumnPathId;
        m_cms = cms;
        setName(Messages.get().container(Messages.GUI_OPENRESOURCE_ACTION_NAME_0));
        setHelpText(Messages.get().container(Messages.GUI_OPENRESOURCE_ACTION_HELP_0));
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#getHelpText()
     */
    public CmsMessageContainer getHelpText() {

        if (isEnabled()) {
            return super.getHelpText();
        }
        return Messages.get().container(Messages.GUI_OPENRESOURCE_ACTION_DISABLED_HELP_0);
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#isEnabled()
     */
    public boolean isEnabled() {

        if (getResourceName() != null) {
            return super.isEnabled();
        }
        return false;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDefaultJsAction#jsCode()
     */
    public String jsCode() {

        StringBuffer jsCode = new StringBuffer(256);
        jsCode.append("javascript:top.openwinfull('");
        jsCode.append(getResourceName());
        jsCode.append("')");
        return jsCode.toString();
    }

    /**
     * Sets the cms context.<p>
     *
     * @param cms the cms context to set
     */
    public void setCms(CmsObject cms) {

        m_cms = cms;
    }

    /**
     * Returns the most possible right resource name.<p>
     * 
     * @return the most possible right resource name
     */
    private String getResourceName() {

        String resource = getItem().get(m_resColumnPathId).toString();
        if (!m_cms.existsResource(resource, CmsResourceFilter.DEFAULT)) {
            String siteRoot = CmsSiteManager.getSiteRoot(resource);
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(siteRoot)) {
                resource = resource.substring(siteRoot.length());
            }
            if (!m_cms.existsResource(resource, CmsResourceFilter.DEFAULT)) {
                resource = null;
            }
        }
        return resource;
    }
}