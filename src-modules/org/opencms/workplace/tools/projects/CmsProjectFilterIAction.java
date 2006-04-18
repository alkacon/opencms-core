/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/projects/Attic/CmsProjectFilterIAction.java,v $
 * Date   : $Date: 2006/04/18 16:14:03 $
 * Version: $Revision: 1.1.2.1 $
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

package org.opencms.workplace.tools.projects;

import org.opencms.db.CmsProjectResourcesDisplayMode;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.widgets.A_CmsWidget;
import org.opencms.workplace.list.CmsListIndependentAction;

import java.util.List;

/**
 * Independent action to switch the resource state filter.<p>
 * 
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 6.0.0 
 */
public class CmsProjectFilterIAction extends CmsListIndependentAction {

    /** The selected filter. */
    private CmsProjectResourcesDisplayMode m_filter = CmsProjectResourcesDisplayMode.ALL_CHANGES;

    /**
     * Default Constructor.<p>
     * 
     * @param id unique id
     */
    public CmsProjectFilterIAction(String id) {

        super(id);
    }

    /**
     * Returns the filter.<p>
     *
     * @return the filter
     */
    public CmsProjectResourcesDisplayMode getFilter() {

        return m_filter;
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#getHelpText()
     */
    public CmsMessageContainer getHelpText() {

        return Messages.get().container(A_CmsWidget.LABEL_PREFIX + m_filter.getMode() + A_CmsWidget.HELP_POSTFIX);
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#getName()
     */
    public CmsMessageContainer getName() {

        return Messages.get().container(A_CmsWidget.LABEL_PREFIX + m_filter.getMode());
    }

    /**
     * Sets the filter.<p>
     *
     * @param filter the filter to set
     */
    public void setFilter(CmsProjectResourcesDisplayMode filter) {

        m_filter = filter;
    }

    /**
     * Toggles the filter.<p>
     */
    public void toggle() {

        List values = CmsProjectResourcesDisplayMode.VALUES;
        m_filter = (CmsProjectResourcesDisplayMode)values.get((values.indexOf(m_filter) + 1) % values.size());
    }
}