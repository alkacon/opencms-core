/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/tools/A_CmsToolHandler.java,v $
 * Date   : $Date: 2005/06/21 15:50:00 $
 * Version: $Revision: 1.13 $
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
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.i18n.CmsMessages;
import org.opencms.jsp.CmsJspNavBuilder;
import org.opencms.jsp.CmsJspNavElement;
import org.opencms.main.CmsException;
import org.opencms.util.CmsStringUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Helper class to build easily other admin tool handlers.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com) 
 * @version $Revision: 1.13 $
 * @since 5.7.3
 */
public abstract class A_CmsToolHandler implements I_CmsToolHandler {

    /** Property for the params arg.<p> */
    public static final String C_ARG_PARAM_NAME = "params";

    /** Property for the path arg.<p> */
    public static final String C_ARG_PATH_NAME = "path";

    /** Property for the args.<p> */
    public static final String C_ARGS_PROPERTY_DEFINITION = "admintoolhandler-args";

    /** Argument separator.<p> */
    public static final String C_ARGUMENT_SEPARATOR = "|";

    /** Default disabled help text constant.<p> */
    public static final String C_DEFAULT_DISABLED_HELPTEXT = "${key." + Messages.GUI_TOOLS_DISABLED_HELP_0 + "}";

    /** Argument name and value separator.<p> */
    public static final String C_VALUE_SEPARATOR = ":";

    /** Help text or description if disabled. */
    private String m_disabledHelpText;

    /** Group to be included in. */
    private String m_group;

    /** Help text or description. */
    private String m_helpText;

    /** Icon path (32x32). */
    private String m_iconPath;

    /** Link pointer. */
    private String m_link;

    /** Display name. */
    private String m_name;

    /** Needed parameters. */
    private String m_parameters;

    /** Tool path to install in. */
    private String m_path;

    /** Relative position in group. */
    private float m_position;

    /** Menu item name. */
    private String m_shortName;

    /** Small icon path (16x16). */
    private String m_smallIconPath;

    /**
     * @see org.opencms.workplace.tools.I_CmsToolHandler#getDisabledHelpText()
     */
    public String getDisabledHelpText() {

        return m_disabledHelpText;
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsToolHandler#getGroup()
     */
    public String getGroup() {

        return m_group;
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsToolHandler#getHelpText()
     */
    public String getHelpText() {

        return m_helpText;
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsToolHandler#getIconPath()
     */
    public String getIconPath() {

        return m_iconPath;
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsToolHandler#getLink()
     */
    public String getLink() {

        return m_link;
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsToolHandler#getName()
     */
    public String getName() {

        return m_name;
    }

    /**
     * Returns the needed parameters.<p>
     *
     * @return the parameters
     */
    public String getParameters() {

        return m_parameters;
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsToolHandler#getPath()
     */
    public String getPath() {

        return m_path;
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsToolHandler#getPosition()
     */
    public float getPosition() {

        return m_position;
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsToolHandler#getShortName()
     */
    public String getShortName() {

        return m_shortName;
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsToolHandler#getSmallIconPath()
     */
    public String getSmallIconPath() {

        return m_smallIconPath;
    }

    /**
     * Sets the help text if disabled.<p>
     *
     * @param disabledHelpText the help text to set
     */
    public void setDisabledHelpText(String disabledHelpText) {

        m_disabledHelpText = disabledHelpText;
    }

    /**
     * Sets the group.<p>
     *
     * @param group the group to set
     */
    public void setGroup(String group) {

        m_group = group;
    }

    /**
     * Sets the help text.<p>
     *
     * @param helpText the help text to set
     */
    public void setHelpText(String helpText) {

        m_helpText = helpText;
    }

    /**
     * Sets the icon path.<p>
     *
     * @param iconPath the icon path to set
     */
    public void setIconPath(String iconPath) {

        m_iconPath = iconPath;
    }

    /**
     * Sets the link.<p>
     *
     * @param link the link to set
     */
    public void setLink(String link) {

        m_link = link;
    }

    /**
     * Sets the name.<p>
     *
     * @param name the name to set
     */
    public void setName(String name) {

        m_name = name;
    }

    /**
     * Sets the parameters.<p>
     *
     * @param parameters the parameters to set
     */
    public void setParameters(String parameters) {

        m_parameters = parameters;
    }

    /**
     * Sets the path.<p>
     *
     * @param path the path to set
     */
    public void setPath(String path) {

        m_path = path;
    }

    /**
     * Sets the position.<p>
     *
     * @param position the position to set
     */
    public void setPosition(float position) {

        m_position = position;
    }

    /**
     * Sets the short name.<p>
     *
     * @param shortName the short name to set
     */
    public void setShortName(String shortName) {

        m_shortName = shortName;
    }

    /**
     * Sets the small icon path.<p>
     *
     * @param smallIconPath the samll icon path to set
     */
    public void setSmallIconPath(String smallIconPath) {

        m_smallIconPath = smallIconPath;
    }

    /**
     * Default implementation.
     * 
     * It takes the icon path from <code>{@link org.opencms.jsp.CmsJspNavElement#C_PROPERTY_NAVIMAGE}</code> property, 
     * or uses a default icon if undefined, the name is taken from the 
     * <code>{@link org.opencms.file.CmsPropertyDefinition#PROPERTY_NAVTEXT}</code> property, 
     * or uses the <code>{@link org.opencms.file.CmsPropertyDefinition#PROPERTY_TITLE}</code> property if undefined, 
     * or an default text, if still undefined. if you want 2 different names, one for the big icon tools and one for 
     * the menu/navbar entries, use a <code>{@link A_CmsToolHandler#C_VALUE_SEPARATOR}</code> to separate them in the property.
     * (if you do so, the first one is for big icons and the second one for menu/navbar entries). the help text is taken from the 
     * <code>{@link org.opencms.file.CmsPropertyDefinition#PROPERTY_DESCRIPTION}</code> property or a
     * default text if undefined, if you want to custumize a help text while disabled, use a 
     * <code>{@link A_CmsToolHandler#C_VALUE_SEPARATOR}</code> as a separator in the same property.<p> 
     * 
     * The group is taken from the <code>{@link org.opencms.jsp.CmsJspNavElement#C_PROPERTY_NAVINFO}</code> property,
     * the position from the <code>{@link org.opencms.file.CmsPropertyDefinition#PROPERTY_NAVPOS}</code>
     * and the install path is given by the folder structure if the <code>{@link #C_ARGS_PROPERTY_DEFINITION}</code>
     * property does not include path information.<p>
     * 
     * For the icon path you can specify 2 paths separated by a <code>{@link A_CmsToolHandler#C_VALUE_SEPARATOR}</code>, 
     * the first one will be used as a group icon (32x32), and the second as an menu icon (16x16). <p>
     * 
     * @see org.opencms.workplace.tools.I_CmsToolHandler#setup(org.opencms.file.CmsObject, java.lang.String)
     */
    public boolean setup(CmsObject cms, String resourcePath) {

        CmsJspNavElement navElem = CmsJspNavBuilder.getNavigationForResource(cms, resourcePath);

        String name = navElem.getNavText();
        if (CmsMessages.isUnknownKey(name)) {
            name = navElem.getTitle();
        }
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(name)) {
            name = "${key." + Messages.GUI_TOOLS_DEFAULT_NAME_0 + "}";
        }
        String shortName = name;
        if (name.indexOf(C_VALUE_SEPARATOR) >= 0) {
            shortName = name.substring(name.indexOf(C_VALUE_SEPARATOR) + 1);
            name = name.substring(0, name.indexOf(C_VALUE_SEPARATOR));
        }
        setName(name);
        setShortName(shortName);

        String iconPath = navElem.getNavImage();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(iconPath)) {
            iconPath = "admin/images/default_tool_big.png:admin/images/default_tool_small.png";
        }
        String smallIconPath = iconPath;
        if (iconPath.indexOf(C_VALUE_SEPARATOR) >= 0) {
            smallIconPath = iconPath.substring(iconPath.indexOf(C_VALUE_SEPARATOR) + 1);
            iconPath = iconPath.substring(0, iconPath.indexOf(C_VALUE_SEPARATOR));
        }
        setIconPath(iconPath);
        setSmallIconPath(smallIconPath);

        String helpText = navElem.getDescription();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(helpText)) {
            helpText = "${key." + Messages.GUI_TOOLS_DEFAULT_HELP_0 + "}";
        }
        String disabledHelpText = C_DEFAULT_DISABLED_HELPTEXT;
        if (helpText.indexOf(C_VALUE_SEPARATOR) >= 0) {
            disabledHelpText = helpText.substring(helpText.indexOf(C_VALUE_SEPARATOR) + 1);
            helpText = helpText.substring(0, helpText.indexOf(C_VALUE_SEPARATOR));
        }
        setHelpText(helpText);
        setDisabledHelpText(disabledHelpText);

        String group = navElem.getInfo();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(group)) {
            group = "${key." + Messages.GUI_TOOLS_DEFAULT_GROUP_0 + "}";
        }

        String link = resourcePath;

        String path = resourcePath;
        boolean isFolder = false;
        try {
            // make sure the res is a folder 
            cms.readFolder(resourcePath);
            isFolder = true;

            // adjust the path
            if (path.endsWith(CmsToolManager.C_TOOLPATH_SEPARATOR)) {
                path = path.substring(0, path.lastIndexOf(CmsToolManager.C_TOOLPATH_SEPARATOR));
            }

            // set admin page as link
            link = CmsToolManager.C_VIEW_JSPPAGE_LOCATION;

            // try to use the folder def file as link
            CmsProperty prop = cms.readPropertyObject(path, CmsPropertyDefinition.PROPERTY_DEFAULT_FILE, true);
            String defFile = "index.html";
            if (!prop.isNullProperty()) {
                defFile = prop.getValue();
            }
            if (!defFile.startsWith(CmsToolManager.C_TOOLPATH_SEPARATOR)) {
                // try to use this relative link
                defFile = path + CmsToolManager.C_TOOLPATH_SEPARATOR + defFile;
            }
            cms.readResource(defFile);
            link = defFile;

        } catch (CmsException e) {
            // noop
        }

        setLink(link);
        if (isFolder) {
            path = resourcePath.substring(
                CmsToolManager.C_ADMINTOOLS_ROOT_LOCATION.length(),
                resourcePath.lastIndexOf(CmsToolManager.C_TOOLPATH_SEPARATOR));
        } else {
            path = resourcePath.substring(
                CmsToolManager.C_ADMINTOOLS_ROOT_LOCATION.length(),
                resourcePath.lastIndexOf('.'));
        }

        try {
            CmsProperty prop = cms.readPropertyObject(resourcePath, C_ARGS_PROPERTY_DEFINITION, false);
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(prop.getValue())) {
                Map argsMap = new HashMap();
                Iterator itArgs = CmsStringUtil.splitAsList(prop.getValue(), C_ARGUMENT_SEPARATOR).iterator();
                while (itArgs.hasNext()) {
                    String arg = (String)itArgs.next();
                    int pos = arg.indexOf(C_VALUE_SEPARATOR);
                    argsMap.put(arg.substring(0, pos), arg.substring(pos + 1));
                }
                if (argsMap.get(C_ARG_PATH_NAME) != null) {
                    path = (String)argsMap.get(C_ARG_PATH_NAME);
                }
                if (argsMap.get(C_ARG_PARAM_NAME) != null) {
                    setParameters((String)argsMap.get(C_ARG_PARAM_NAME));
                }
            }
        } catch (CmsException e) {
            // noop
        }

        // install point
        setPath(path);
        setGroup(group);
        setPosition(navElem.getNavPosition());

        return true;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {

        return m_path + " - " + m_group + " - " + m_position;
    }
}