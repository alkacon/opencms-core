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

import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;

/**
 * Default implementation of a independent action for a html list column that can execute java script code.<p>
 *
 * @since 6.0.0
 */
public abstract class A_CmsListIndependentJsAction extends CmsListIndependentAction {

    /**
     * Default Constructor.<p>
     *
     * @param id unique id
     */
    public A_CmsListIndependentJsAction(String id) {

        super(id);
    }

    /**
     * @see org.opencms.workplace.list.CmsListIndependentAction#resolveOnClic(org.opencms.workplace.CmsWorkplace)
     */
    @Override
    protected String resolveOnClic(CmsWorkplace wp) {

        String confirmationMessage = getConfirmationMessage().key(wp.getLocale());
        StringBuffer onClic = new StringBuffer(128);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(confirmationMessage)) {
            onClic.append("if (confirm('");
            onClic.append(CmsStringUtil.escapeJavaScript(confirmationMessage));
            onClic.append("')) { ");
        }
        onClic.append(jsCode(wp));
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(confirmationMessage)) {
            onClic.append(" } ");
        }
        return onClic.toString();
    }

    /**
     * The js code to execute.<p>
     *
     * @param wp the workplace context
     *
     * @return js code to execute
     */
    public abstract String jsCode(CmsWorkplace wp);
}