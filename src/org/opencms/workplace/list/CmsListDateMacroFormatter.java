/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/list/CmsListDateMacroFormatter.java,v $
 * Date   : $Date: 2005/06/23 07:58:47 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.list;

import org.opencms.i18n.CmsMessageContainer;

import java.util.Date;
import java.util.Locale;

/**
 * Formatter for dates.<p>
 * 
 * The 'never' message will be displayed if the date is null or <code>{@link Date#getTime()}==0</code>.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.3 $ 
 * 
 * @since 6.0.0 
 */
public class CmsListDateMacroFormatter extends CmsListMacroFormatter {

    /** Constant for never. */
    private final CmsMessageContainer m_never;

    /**
     * Default constructor that sets the mask to use.<p>
     * 
     * @param mask pattern for <code>{@link java.text.MessageFormat}</code>
     * @param never message (without args) for the 'never' message
     */
    public CmsListDateMacroFormatter(CmsMessageContainer mask, CmsMessageContainer never) {

        super(mask);
        m_never = never;
    }

    /**
     * @see org.opencms.workplace.list.CmsListMacroFormatter#format(java.lang.Object, java.util.Locale)
     */
    public String format(Object data, Locale locale) {

        if (data == null) {
            return m_never.key(locale);
        }
        if (data instanceof Date) {
            if (((Date)data).getTime() == 0) {
                return m_never.key(locale);
            }
        }
        return super.format(data, locale);
    }
}
