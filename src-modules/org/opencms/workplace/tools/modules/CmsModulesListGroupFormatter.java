/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/modules/CmsModulesListGroupFormatter.java,v $
 * Date   : $Date: 2005/06/23 09:05:01 $
 * Version: $Revision: 1.3 $
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

package org.opencms.workplace.tools.modules;

import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.list.I_CmsListFormatter;

import java.util.Locale;

/**
 * This list item detail formatter creates a nice output for modules groups.<p>
 * 
 * @author Michael Emmerich 
 * 
 * @version $Revision: 1.3 $ 
 * 
 * @since 6.0.0 
 */
public class CmsModulesListGroupFormatter implements I_CmsListFormatter {

    /**
     * Default constructor.<p>
     */
    public CmsModulesListGroupFormatter() {

        //noop
    }

    /**
     * @see org.opencms.workplace.list.I_CmsListFormatter#format(java.lang.Object, java.util.Locale)
     */
    public String format(Object data, Locale locale) {

        StringBuffer html = new StringBuffer(32);

        String group = (String)data;

        // simple solution so far, if no group name is given, display some dashes
        if (CmsStringUtil.isEmpty(group)) {
            html.append("---");
        } else {
            html.append(group);
        }

        return html.toString();
    }

}