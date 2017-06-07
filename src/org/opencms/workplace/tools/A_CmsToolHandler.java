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

package org.opencms.workplace.tools;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsMessages;
import org.opencms.jsp.CmsJspNavBuilder;
import org.opencms.jsp.CmsJspNavElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Helper class to build easily other admin tool handlers.<p>
 *
 * @since 6.0.0
 */
public abstract class A_CmsToolHandler implements I_CmsToolHandler {

    /** Property for the parameters argument.<p> */
    public static final String ARG_PARAM_NAME = "params";

    /** Property for the path argument.<p> */
    public static final String ARG_PATH_NAME = "path";

    /** Property definition for the arguments.<p> */
    public static final String ARGS_PROPERTY_DEFINITION = "admintoolhandler-args";

    /** Argument separator.<p> */
    public static final String ARGUMENT_SEPARATOR = "|";

    /** Default disabled help text constant.<p> */
    public static final String DEFAULT_DISABLED_HELPTEXT = "${key." + Messages.GUI_TOOLS_DISABLED_HELP_0 + "}";

    /** Argument name and value separator.<p> */
    public static final String VALUE_SEPARATOR = ":";

    /** Property for the confirmation message argument.<p> */
    private static final String ARG_CONFIRMATION_NAME = "confirmation";

    /** The static log object for this class. */
    private static final Log LOG = CmsLog.getLog(A_CmsToolHandler.class);

    /** Confirmation message. */
    private String m_confirmationMessage;

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
     * Returns the confirmation Message.<p>
     *
     * @return the confirmation Message
     */
    public String getConfirmationMessage() {

        return m_confirmationMessage;
    }

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
     * @see org.opencms.workplace.tools.I_CmsToolHandler#getParameters(org.opencms.workplace.CmsWorkplace)
     */
    public Map<String, String[]> getParameters(CmsWorkplace wp) {

        Map<String, String[]> argMap = new HashMap<String, String[]>();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_parameters)) {
            String toolParams = CmsEncoder.decode(wp.resolveMacros(m_parameters));
            Iterator<String> itArgs = CmsStringUtil.splitAsList(toolParams, "&").iterator();
            while (itArgs.hasNext()) {
                String arg = itArgs.next();
                int pos = arg.indexOf("=");
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(arg.substring(pos + 1))) {
                    argMap.put(arg.substring(0, pos), new String[] {arg.substring(pos + 1)});
                }
            }
        }
        return argMap;
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
     * @see org.opencms.workplace.tools.I_CmsToolHandler#isEnabled(org.opencms.workplace.CmsWorkplace)
     */
    public boolean isEnabled(CmsWorkplace wp) {

        return isEnabled(wp.getCms());
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsToolHandler#isVisible(org.opencms.workplace.CmsWorkplace)
     */
    public boolean isVisible(CmsWorkplace wp) {

        return isVisible(wp.getCms());
    }

    /**
     * Sets the confirmation Message.<p>
     *
     * @param confirmationMessage the confirmation Message to set
     */
    public void setConfirmationMessage(String confirmationMessage) {

        m_confirmationMessage = confirmationMessage;
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
     * Sets the parameter string.<p>
     *
     * @param paramString the parameter string to set
     */
    public void setParameterString(String paramString) {

        m_parameters = paramString;
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
     * Default implementation.<p>
     *
     * It takes the icon path from <code>{@link org.opencms.file.CmsPropertyDefinition#PROPERTY_NAVIMAGE}</code> property,
     * or uses a default icon if undefined, the name is taken from the
     * <code>{@link org.opencms.file.CmsPropertyDefinition#PROPERTY_NAVTEXT}</code> property,
     * or uses the <code>{@link org.opencms.file.CmsPropertyDefinition#PROPERTY_TITLE}</code> property if undefined,
     * or an default text, if still undefined. if you want 2 different names, one for the big icon tools and one for
     * the menu/navbar entries, use a <code>{@link A_CmsToolHandler#VALUE_SEPARATOR}</code> to separate them in the property.
     * (if you do so, the first one is for big icons and the second one for menu/navbar entries). the help text is taken from the
     * <code>{@link org.opencms.file.CmsPropertyDefinition#PROPERTY_DESCRIPTION}</code> property or a
     * default text if undefined, if you want to customize a help text while disabled, use a
     * <code>{@link A_CmsToolHandler#VALUE_SEPARATOR}</code> as a separator in the same property.<p>
     *
     * The group is taken from the <code>{@link org.opencms.file.CmsPropertyDefinition#PROPERTY_NAVINFO}</code> property,
     * the position from the <code>{@link org.opencms.file.CmsPropertyDefinition#PROPERTY_NAVPOS}</code>
     * and the install path is given by the folder structure if the <code>{@link #ARGS_PROPERTY_DEFINITION}</code>
     * property does not include path information.<p>
     *
     * For the icon path you can specify 2 paths separated by a <code>{@link A_CmsToolHandler#VALUE_SEPARATOR}</code>,
     * the first one will be used as a group icon (32x32), and the second as an menu icon (16x16). The paths are relative
     * to the /system/workplace/resources/ folder. If the tool is disabled, the names of the icons are composed as
     * ${name}_disabled.${ext}<p>
     *
     * The confirmation message is taken from the <code>{@link #ARGS_PROPERTY_DEFINITION}</code> with key
     * <code>#ARG_CONFIRMATION_NAME</code>
     *
     * @see org.opencms.workplace.tools.I_CmsToolHandler#setup(org.opencms.file.CmsObject, CmsToolRootHandler, java.lang.String)
     */
    public boolean setup(CmsObject cms, CmsToolRootHandler root, String resourcePath) {

        try {
            resourcePath = cms.getSitePath(cms.readResource(resourcePath));
        } catch (CmsException e) {
            // should not happen
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        CmsJspNavElement navElem = new CmsJspNavBuilder(cms).getNavigationForResource(resourcePath);

        String name = navElem.getNavText();
        if (CmsMessages.isUnknownKey(name)) {
            name = navElem.getTitle();
        }
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(name)) {
            name = "${key." + Messages.GUI_TOOLS_DEFAULT_NAME_0 + "}";
        }
        String shortName = name;
        if (name.indexOf(VALUE_SEPARATOR) >= 0) {
            shortName = name.substring(0, name.indexOf(VALUE_SEPARATOR));
            name = name.substring(name.indexOf(VALUE_SEPARATOR) + 1);
        }
        setName(name);
        setShortName(shortName);

        String iconPath = navElem.getNavImage();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(iconPath)) {
            iconPath = "admin/images/default_tool_big.png:admin/images/default_tool_small.png";
        }
        String smallIconPath = iconPath;
        if (iconPath.indexOf(VALUE_SEPARATOR) >= 0) {
            smallIconPath = iconPath.substring(iconPath.indexOf(VALUE_SEPARATOR) + 1);
            iconPath = iconPath.substring(0, iconPath.indexOf(VALUE_SEPARATOR));
        }
        setIconPath(iconPath);
        setSmallIconPath(smallIconPath);

        String helpText = navElem.getDescription();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(helpText)) {
            helpText = "${key." + Messages.GUI_TOOLS_DEFAULT_HELP_0 + "}";
        }
        String disabledHelpText = DEFAULT_DISABLED_HELPTEXT;
        if (helpText.indexOf(VALUE_SEPARATOR) >= 0) {
            disabledHelpText = helpText.substring(helpText.indexOf(VALUE_SEPARATOR) + 1);
            helpText = helpText.substring(0, helpText.indexOf(VALUE_SEPARATOR));
        }
        setHelpText(helpText);
        setDisabledHelpText(disabledHelpText);

        String group = navElem.getInfo();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(group)) {
            group = "${key." + Messages.GUI_TOOLS_DEFAULT_GROUP_0 + "}";
        }

        String path = resourcePath;
        setLink(cms, resourcePath);
        if (CmsResource.isFolder(path)) {
            path = CmsToolManager.TOOLPATH_SEPARATOR
                + resourcePath.substring(
                    root.getUri().length(),
                    resourcePath.lastIndexOf(CmsToolManager.TOOLPATH_SEPARATOR));
        } else {
            if (resourcePath.lastIndexOf('.') > -1) {
                path = CmsToolManager.TOOLPATH_SEPARATOR
                    + resourcePath.substring(root.getUri().length(), resourcePath.lastIndexOf('.'));
            } else {
                path = CmsToolManager.TOOLPATH_SEPARATOR + resourcePath.substring(root.getUri().length());
            }
        }
        // install point
        setPath(path);
        setGroup(group);
        setPosition(navElem.getNavPosition());

        // parameters
        setParameters(cms, resourcePath);

        return !path.equals(resourcePath);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return m_path + " - " + m_group + " - " + m_position;
    }

    /**
     * Sets the link for the given resource.<p>
     *
     * Use the <code>resourcePath</code> as link if it is not a folder.
     *
     * If it is a folder, try to use the folder default file property value as link.
     * if not use the {@link CmsToolManager#VIEW_JSPPAGE_LOCATION}.
     *
     * @param cms the cms context
     * @param resourcePath the path to the resource to set the link for
     */
    protected void setLink(CmsObject cms, String resourcePath) {

        String link = resourcePath;
        try {
            // make sure the res is a folder
            cms.readFolder(resourcePath);

            // adjust the path
            if (resourcePath.endsWith(CmsToolManager.TOOLPATH_SEPARATOR)) {
                resourcePath = resourcePath.substring(0, resourcePath.lastIndexOf(CmsToolManager.TOOLPATH_SEPARATOR));
            }

            // set admin page as link
            link = CmsToolManager.VIEW_JSPPAGE_LOCATION;

            // try to use the folder default file as link
            CmsProperty prop = cms.readPropertyObject(resourcePath, CmsPropertyDefinition.PROPERTY_DEFAULT_FILE, false);
            String defFile = "index.jsp";
            if (!prop.isNullProperty()) {
                defFile = prop.getValue();
            }
            if (!defFile.startsWith(CmsToolManager.TOOLPATH_SEPARATOR)) {
                // try to use this relative link
                defFile = resourcePath + CmsToolManager.TOOLPATH_SEPARATOR + defFile;
            }
            if (defFile.indexOf("?") > 0) {
                if (cms.existsResource(defFile.substring(0, defFile.indexOf("?")))) {
                    link = defFile;
                }
            } else if (cms.existsResource(defFile)) {
                link = defFile;
            }
        } catch (CmsException e) {
            // not a folder or no default file, ignore
        }

        setLink(link);
    }

    /**
     * Sets the needed properties from the {@link #ARGS_PROPERTY_DEFINITION} property of the given resource.<p>
     *
     * @param cms the cms context
     * @param resourcePath the path to the resource to read the property from
     */
    protected void setParameters(CmsObject cms, String resourcePath) {

        try {
            CmsProperty prop = cms.readPropertyObject(resourcePath, ARGS_PROPERTY_DEFINITION, false);
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(prop.getValue())) {
                Map<String, String> argsMap = new HashMap<String, String>();
                Iterator<String> itArgs = CmsStringUtil.splitAsList(prop.getValue(), ARGUMENT_SEPARATOR).iterator();
                while (itArgs.hasNext()) {
                    String arg = "";
                    try {
                        arg = itArgs.next();
                        int pos = arg.indexOf(VALUE_SEPARATOR);
                        argsMap.put(arg.substring(0, pos), arg.substring(pos + 1));
                    } catch (StringIndexOutOfBoundsException e) {
                        LOG.error("sep: " + VALUE_SEPARATOR + "arg: " + arg);
                        throw e;
                    }
                }
                if (argsMap.get(ARG_PATH_NAME) != null) {
                    setPath(argsMap.get(ARG_PATH_NAME));
                }
                if (argsMap.get(ARG_CONFIRMATION_NAME) != null) {
                    setConfirmationMessage(argsMap.get(ARG_CONFIRMATION_NAME));
                }
                if (argsMap.get(ARG_PARAM_NAME) != null) {
                    setParameterString(argsMap.get(ARG_PARAM_NAME));
                }
            }
        } catch (CmsException e) {
            // should never happen
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
    }
}