/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/list/CmsListMacroFormatter.java,v $
 * Date   : $Date: 2005/04/22 08:38:52 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.workplace.CmsWorkplace;

import java.text.MessageFormat;

/**
 * This list formatter uses the <code>{@link MessageFormat}</code> class for macro like formatting.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com)
 * @version $Revision: 1.1 $
 * @since 5.7.3
 */
public class CmsListMacroFormatter implements I_CmsListFormatter {

    /** pattern for <code>{@link MessageFormat}</code>. */
    private final String m_mask;

    /**
     * Default constructor that sets the mask to use.<p>
     * 
     * @param mask pattern for <code>{@link MessageFormat}</code>
     */
    public CmsListMacroFormatter(String mask) {

        m_mask = mask;
    }

    /**
     * @see org.opencms.workplace.list.I_CmsListFormatter#format(java.lang.Object, CmsWorkplace)
     */
    public String format(Object data, CmsWorkplace wp) {

        if (data==null) {
            return null;
        }
        String locMask = wp.resolveMacros(m_mask);
        MessageFormat formatter = new MessageFormat(locMask, wp.getLocale());
        return formatter.format(new Object[] {data});
    }
}