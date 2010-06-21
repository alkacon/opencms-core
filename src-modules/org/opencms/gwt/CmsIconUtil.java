/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/Attic/CmsIconUtil.java,v $
 * Date   : $Date: 2010/06/21 10:01:40 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.gwt;

import org.opencms.file.types.CmsResourceTypeUnknownFile;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.workplace.explorer.CmsIconRule;

import java.util.Map;

/**
 * Utility class to generate the resource icon CSS.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public class CmsIconUtil extends org.opencms.gwt.shared.CmsIconUtil {

    /**
     * Builds the CSS for all resource types.<p>
     * 
     * @return a string containing the CSS rules for all resource types 
     */
    public static String buildResourceIconCss() {

        StringBuffer buffer = new StringBuffer();
        buffer.append(buildUnknownIconCss());
        for (I_CmsResourceType type : OpenCms.getResourceManager().getResourceTypes()) {
            addCssForType(buffer, type);
        }
        return buffer.toString();
    }

    /**
     * Builds the CSS for a given combination of resource type and file name extension.<p>
     * 
     * @param type the resource type 
     * @param suffix the file name extension
     * @param image the icon which should be used for the type/suffix combination 
     * 
     * @return the CSS for the type/suffix combination 
     */
    public static String buildSubTypeIconCss(String type, String suffix, String image) {

        String template = ".%1$s.%2$s.%3$s {\n  background-image: url(\"%4$s\");\n}\n\n";
        return String.format(template, TYPE_ICON_CLASS, getResourceTypeIconClass(type), getResourceSubTypeIconClass(
            type,
            suffix), image);
    }

    /**
     * Returns the default CSS for a given resource type.<p>
     * 
     * @param type the resource type 
     * @param image the icon which should be used for the resource type
     * 
     * @return the CSS for the resource type
     */
    public static String buildTypeIconCss(String type, String image) {

        String template = ".%1$s.%2$s {\n  background-image: url(\"%3$s\");\n}\n\n";
        return String.format(template, TYPE_ICON_CLASS, getResourceTypeIconClass(type), image);
    }

    /**
     * Builds the CSS for the icon for unknown resource types.<p>
     * 
     * @return the CSS for unknown resource type icons
     */
    public static String buildUnknownIconCss() {

        String unknown = getIconUri(OpenCms.getWorkplaceManager().getExplorerTypeSetting(
            CmsResourceTypeUnknownFile.getStaticTypeName()).getBestIcon());
        String template = ".%1$s {\n  background: transparent scroll 50%% 50%% no-repeat url(\"%2$s\");\n  width: 16px;\n  height:16px;\n}\n\n";
        return String.format(template, TYPE_ICON_CLASS, unknown);
    }

    /**
     * Adds all the CSS rules for a given resource type to a string buffer.<p>
     * 
     * @param buffer the string buffer which the CSS should be added to 
     * @param type the resource type for which the CSS should be generated 
     */
    private static void addCssForType(StringBuffer buffer, I_CmsResourceType type) {

        String typeName = type.getTypeName();

        CmsExplorerTypeSettings settings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(typeName);
        if (settings != null) {
            buffer.append(buildTypeIconCss(typeName, getIconUri(settings.getBestIcon())));
            Map<String, CmsIconRule> iconRules = settings.getIconRules();
            for (Map.Entry<String, CmsIconRule> entry : iconRules.entrySet()) {
                String extension = entry.getKey();
                CmsIconRule rule = entry.getValue();
                buffer.append(buildSubTypeIconCss(typeName, extension, getIconUri(rule.getBestIcon())));
            }
        }
    }

    /**
     * Converts an icon file name to a full icon URI.<p>
     * 
     * @param icon the file name of the icon
     * 
     * @return the full icon uri 
     */
    private static String getIconUri(String icon) {

        return CmsWorkplace.getResourceUri(CmsWorkplace.RES_PATH_FILETYPES + icon);
    }

}
