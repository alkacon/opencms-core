/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/publishqueue/Attic/CmsPublishQueueTwoListsDialog.java,v $
 * Date   : $Date: 2006/11/29 14:54:02 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.workplace.tools.publishqueue;

import org.opencms.workplace.list.CmsTwoListsDialog;

/**
 * Provides a two lists dialog for the personal queue of publish reports and the global publish queue.<p> 
 *
 * @author Raphael Schnuck
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 6.5.5
 */
public class CmsPublishQueueTwoListsDialog extends CmsTwoListsDialog {

    /**
     * Default constructor.<p>
     * 
     * @param wp1 the CmsPersonalPublishQueueList instance for the first list
     * @param wp2 the CmsPublishQueueList instance for the second list
     */
    public CmsPublishQueueTwoListsDialog(CmsPublishQueuePersonalList wp1, CmsPublishQueueList wp2) {

        super(wp1, wp2);
    }

    /**
     * Add javascript code before the list to change the value of the view selector.<p>
     * 
     * @return custom html code
     */
    protected String customHtmlStart() {

        StringBuffer result = new StringBuffer(8);
        result.append("<script type=\"text/javascript\">\n");
        result.append("adminVal=\""
            + getActiveWp().getJsp().link("/system/workplace/views/admin/admin-fs.jsp")
            + "\";\n");
        result.append("for(var i = 0; i < top.frames[0].document.getElementsByName(\"wpView\")[0].length; i++){\n");
        result.append("if(top.frames[0].document.getElementsByName(\"wpView\")[0].options[i].value==adminVal){\n");
        result.append("top.frames[0].document.getElementsByName(\"wpView\")[0].options[i].selected=true;\n");
        result.append("break;\n");
        result.append("}\n}\n");
        result.append("</script>");
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.list.CmsTwoListsDialog#defaultActionHtml()
     */
    protected String defaultActionHtml() {

        StringBuffer result = new StringBuffer(2048);
        result.append(defaultActionHtmlStart());
        result.append(customHtmlStart());
        result.append(defaultActionHtmlContent());
        result.append(defaultActionHtmlEnd());
        return result.toString();
    }
}
