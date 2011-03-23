/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/list/CmsListRadioMultiAction.java,v $
 * Date   : $Date: 2011/03/23 14:51:23 $
 * Version: $Revision: 1.9 $
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

import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.tools.A_CmsHtmlIconButton;
import org.opencms.workplace.tools.CmsHtmlIconButtonStyleEnum;

import java.util.Iterator;
import java.util.List;

/**
 * Default implementation of a list radio multi action.<p>
 * 
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.9 $ 
 * 
 * @since 6.0.0 
 */
public class CmsListRadioMultiAction extends CmsListMultiAction {

    /** A list of ids of related list item selection action ids. */
    private final List m_relatedActionIds;

    /**
     * Default Constructor.<p>
     * 
     * @param id the unique id
     * @param relatedActionIds the ids of the related item selection actions
     */
    public CmsListRadioMultiAction(String id, List relatedActionIds) {

        super(id);
        m_relatedActionIds = relatedActionIds;
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#buttonHtml(CmsWorkplace)
     */
    @Override
    public String buttonHtml(CmsWorkplace wp) {

        if (!isVisible()) {
            return "";
        }
        if (isEnabled()) {
            String onClic = "listRSelMAction('"
                + getListId()
                + "','"
                + getId()
                + "', '"
                + CmsStringUtil.escapeJavaScript(wp.resolveMacros(getConfirmationMessage().key(wp.getLocale())))
                + "', "
                + CmsHtmlList.NO_SELECTION_MATCH_HELP_VAR
                + getId()
                + ", '"
                + getRelatedActionIds()
                + "');";
            return A_CmsHtmlIconButton.defaultButtonHtml(
                CmsHtmlIconButtonStyleEnum.SMALL_ICON_TEXT,
                getId(),
                getName().key(wp.getLocale()),
                getHelpText().key(wp.getLocale()),
                isEnabled(),
                getIconPath(),
                null,
                onClic);
        }
        return "";
    }

    /**
     * Returns the number of expected selections.<p>
     *
     * @return the number of expected selections
     */
    public int getSelections() {

        return m_relatedActionIds.size();
    }

    /**
     * Returns a comma separated list of related list iem selection action ids.<p>
     * 
     * @return a comma separated list of related list iem selection action ids
     */
    private String getRelatedActionIds() {

        StringBuffer ret = new StringBuffer(32);
        Iterator it = m_relatedActionIds.iterator();
        while (it.hasNext()) {
            ret.append(it.next().toString().trim());
            if (it.hasNext()) {
                ret.append(',');
            }
        }
        return ret.toString();
    }
}