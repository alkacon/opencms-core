/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

import java.text.MessageFormat;
import java.util.Locale;

/**
 * Default implementation of a direct action for a html list column that can execute java script code.<p>
 * 
 * @since 6.0.0 
 */
public abstract class A_CmsListDirectJsAction extends CmsListDirectAction {

    /**
     * Default Constructor.<p>
     * 
     * @param id unique id
     */
    public A_CmsListDirectJsAction(String id) {

        super(id);
    }

    /**
     * @see org.opencms.workplace.list.CmsListDirectAction#resolveOnClic(java.util.Locale)
     */
    @Override
    protected String resolveOnClic(Locale locale) {

        String confirmationMessage = getConfirmationMessage().key(locale);
        if ((getColumnForTexts() != null) && (getItem().get(getColumnForTexts()) != null)) {
            confirmationMessage = new MessageFormat(confirmationMessage, locale).format(new Object[] {getItem().get(
                getColumnForTexts())});
        }
        StringBuffer onClic = new StringBuffer(128);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(confirmationMessage)) {
            onClic.append("if (confirm('");
            onClic.append(confirmationMessage);
            onClic.append("')) { ");
        }
        onClic.append(jsCode());
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(confirmationMessage)) {
            onClic.append(" } ");
        }
        return onClic.toString();
    }

    /**
     * The js code to execute.<p>
     * 
     * @return js code to execute
     */
    public abstract String jsCode();
}