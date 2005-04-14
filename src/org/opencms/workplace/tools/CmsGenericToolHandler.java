/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/tools/Attic/CmsGenericToolHandler.java,v $
 * Date   : $Date: 2005/04/14 13:11:15 $
 * Version: $Revision: 1.4 $
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

package org.opencms.workplace.tools;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.util.CmsStringUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This Tool Handler obtains all the info it need from a single argument.<p>
 * 
 * The formatting of this argument should be a list of parameters given its name and value,
 * this is done separating each parameter from another with a pipe ("<code>|</code>") character,
 * for separating name from value use a colon ("<code>:</code>").<p>  
 * 
 * known parameters are:<br>
 * <ul>
 *  <li><code>name</code>: the name of the admin tool.</li>
 *  <li><code>iconpath</code>: the path to the icon of the admin tool.</li>
 *  <li><code>smalliconpath</code>: the path to the icon to be used in the menu (optional, default: iconpath).</li>
 *  <li><code>helptext</code>: the help text of the admin tool.</li>
 *  <li><code>onlyadmin</code>: the admin tool can only be used as administrator (optional, default:false).</li>
 *  <li><code>onlyoffline</code>: the admin tool can only be used in the offline project (optional, default:false).</li>
 *  <li><code>installpoints</code>: a list of installation points for this admin tool.</li>
 * </ul><p>
 * 
 * The <code>name</code> and <code>helptext</code> can use macros, ie. for i18n.
 * 
 * For more information about installation points, see <code>{@link CmsToolInstallPoint}</code>.<p>
 * 
 * An example for a full argument is:<p>
 * <code>name:Users|iconpath:/resources/icons/users.gif|helptext:This tool manages user accounts|installpoints:/@Principal Management#2,/groups@User Management#1|onlyadmin:true</code><p>
 * 
 * This means that the given resource will be named "Users", the display icon is located under the 
 * path "/resources/icons/users.gif", the displayed help text will be "This tool manages user accounts", 
 * and the tool will be installed in 2 different installation points:<p>
 * <ul>
 *  <li>in the root tool "/", in group "Principal Management", at second position (if there is something at first position), and</li>
 *  <li>in the groups tool "/groups", in group "User Management", at first position.</li>
 * </ul>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com) 
 * @version $Revision: 1.4 $
 * @since 5.7.3
 */
public class CmsGenericToolHandler extends A_CmsToolHandler {

    private static final String C_INSTALLPOINT_SEPARATOR = ",";
    private static final String C_PARAM_HELPTEXT = "helptext";
    private static final String C_PARAM_ICONPATH = "iconpath";
    private static final String C_PARAM_SMALLICONPATH = "smalliconpath";
    private static final String C_PARAM_INSTALLPOINTS = "installpoints";

    private static final String C_PARAM_NAME = "name";
    private boolean m_onlyAdmin = false;

    private boolean m_onlyOffline = false;

    /**
     * Default setup method. <p>
     * 
     * @param cms the cms context
     * @param resourcePath the resource to install as admin tool
     * 
     * @throws CmsException if something goes wrong
     */
    public void setup(CmsObject cms, String resourcePath)
    throws CmsException {

        setup(resourcePath, cms.readPropertyObject(resourcePath, C_PROPERTY_DEFINITION, false).getValue());
    }

    /**
     * Property reader setup method. <p>
     * 
     * @param link the link to asociate the admin tool with
     * @param args the argument string
     */
    private void setup(String link, String args) {

        Map argsMap = new HashMap();
        Iterator itArgs = CmsStringUtil.splitAsList(args, C_ARGS_SEPARATOR).iterator();
        while (itArgs.hasNext()) {
            String arg = (String)itArgs.next();
            int pos = arg.indexOf(C_VALUE_SEPARATOR);
            argsMap.put(arg.substring(0, pos), arg.substring(pos + 1));
        }
        setName((String)argsMap.get(C_PARAM_NAME));
        setLink(link);
        setIconPath((String)argsMap.get(C_PARAM_ICONPATH));
        if (argsMap.get(C_PARAM_SMALLICONPATH) == null) {
            setSmallIconPath((String)argsMap.get(C_PARAM_ICONPATH));
        } else {
            setSmallIconPath((String)argsMap.get(C_PARAM_SMALLICONPATH));
        }
        setHelpText((String)argsMap.get(C_PARAM_HELPTEXT));
        parseInstallPoints((String)argsMap.get(C_PARAM_INSTALLPOINTS));
        if (argsMap.get(C_PARAM_ONLYOFFLINE) != null) {
            m_onlyOffline = true;
        }
        if (argsMap.get(C_PARAM_ONLYADMIN) != null) {
            m_onlyAdmin = true;
        }
    }

    /**
     * @see org.opencms.workplace.tools.A_CmsToolHandler#isEnabled(org.opencms.file.CmsObject)
     */
    public boolean isEnabled(CmsObject cms) {

        boolean ret = super.isEnabled(cms);
        ret = ret && (cms.isAdmin() || !m_onlyAdmin);
        ret = ret && (!cms.getRequestContext().currentProject().isOnlineProject() || !m_onlyOffline);
        return ret;
    }

    /**
     * Parses the installPoint parameter.<p>
     * 
     * @param installPoints the installPoint parameter
     */
    private void parseInstallPoints(String installPoints) {

        Iterator itIPoints = CmsStringUtil.splitAsList(installPoints, C_INSTALLPOINT_SEPARATOR).iterator();
        while (itIPoints.hasNext()) {
            addInstallPoint(new CmsToolInstallPoint((String)itIPoints.next()));
        }
    }

}