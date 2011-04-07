/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/Attic/CmsIconUtil.java,v $
 * Date   : $Date: 2011/04/07 15:10:50 $
 * Version: $Revision: 1.7 $
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
 * @version $Revision: 1.7 $
 * 
 * @since 8.0.0
 */
public class CmsIconUtil extends org.opencms.gwt.shared.CmsIconUtil {

    /**
     * Inner helper class for building the CSS rules.<p>
     */
    public static class CssBuilder {

        /** The buffer into which the CSS is written. */
        private StringBuffer m_buffer = new StringBuffer();

        /** The selector prefix. */
        private String m_prefix;

        /**
         * Creates a new CSS builder with a given selector prefix.<p>
         * 
         * @param prefix the selector prefix 
         */
        public CssBuilder(String prefix) {

            m_prefix = prefix;
        }

        /**
         * Builds the CSS for all resource types.<p>
         * 
         * @return a string containing the CSS rules for all resource types 
         */
        public String buildResourceIconCss() {

            m_buffer.append(buildUnknownIconCss());
            for (I_CmsResourceType type : OpenCms.getResourceManager().getResourceTypes()) {
                addCssForType(type);
            }
            return m_buffer.toString();
        }

        /**
         * Builds the CSS for the icon for unknown resource types.<p>
         * 
         * @return the CSS for unknown resource type icons
         */
        public String buildUnknownIconCss() {

            String unknown = getIconUri(OpenCms.getWorkplaceManager().getExplorerTypeSetting(
                CmsResourceTypeUnknownFile.getStaticTypeName()).getBigIconIfAvailable());
            String template = "%1$s div.%2$s {\n  background: transparent scroll 50%% 50%% no-repeat url(\"%3$s\");\n}\n\n";
            return String.format(template, m_prefix == null ? "" : m_prefix, TYPE_ICON_CLASS, unknown);
        }

        /**
         * Writes the CSS for a single icon rule to a buffer.<p>
         * 
         * @param typeName the name of the resource type 
         * @param rule the icon rule 
         */
        private void addCssForIconRule(String typeName, CmsIconRule rule) {

            String extension = rule.getExtension();
            if (rule.getBigIcon() != null) {
                CmsIconCssRuleBuilder cssBig = new CmsIconCssRuleBuilder(m_prefix);
                cssBig.addSelectorForSubType(typeName, extension, false);
                cssBig.setImageUri(getIconUri(rule.getBigIcon()));
                cssBig.writeCss(m_buffer);

                CmsIconCssRuleBuilder cssSmall = new CmsIconCssRuleBuilder(m_prefix);
                cssSmall.addSelectorForSubType(typeName, extension, true);
                cssSmall.setImageUri(getIconUri(rule.getIcon()));
                cssSmall.writeCss(m_buffer);

            } else {
                CmsIconCssRuleBuilder css = new CmsIconCssRuleBuilder(m_prefix);
                css.addSelectorForSubType(typeName, extension, false);
                css.addSelectorForSubType(typeName, extension, true);
                css.setImageUri(getIconUri(rule.getIcon()));
                css.writeCss(m_buffer);

            }
        }

        /**
         * Helper method for appending the CSS for a single resource type to a buffer.<p>
         * 
         * @param type the resource type for which the CSS should be generated 
         */
        private void addCssForType(I_CmsResourceType type) {

            String typeName = type.getTypeName();
            CmsExplorerTypeSettings settings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(typeName);
            if (settings == null) {
                return;
            }
            if (settings.getBigIcon() != null) {
                CmsIconCssRuleBuilder css = new CmsIconCssRuleBuilder(m_prefix);
                css.setImageUri(getIconUri(settings.getBigIcon()));
                css.addSelectorForType(typeName, false);
                css.writeCss(m_buffer);

                CmsIconCssRuleBuilder cssSmall = new CmsIconCssRuleBuilder(m_prefix);
                cssSmall.setImageUri(getIconUri(settings.getIcon()));
                cssSmall.addSelectorForType(typeName, true);
                cssSmall.writeCss(m_buffer);
            } else {
                CmsIconCssRuleBuilder css = new CmsIconCssRuleBuilder(m_prefix);
                css.setImageUri(getIconUri(settings.getIcon()));
                css.addSelectorForType(typeName, true);
                css.addSelectorForType(typeName, false);
                css.writeCss(m_buffer);
            }

            Map<String, CmsIconRule> iconRules = settings.getIconRules();
            for (Map.Entry<String, CmsIconRule> entry : iconRules.entrySet()) {
                CmsIconRule rule = entry.getValue();
                addCssForIconRule(typeName, rule);
            }
        }

        /**
         * Converts an icon file name to a full icon URI.<p>
         * 
         * @param icon the file name of the icon
         * 
         * @return the full icon uri 
         */
        private String getIconUri(String icon) {

            return CmsWorkplace.getResourceUri(CmsWorkplace.RES_PATH_FILETYPES + icon);
        }

    }

    /**
     * Builds the CSS for all resource types.<p>
     * 
     * @param prefix the prefix to prepend to each selector 
     * 
     * @return a string containing the CSS rules for all resource types 
     */
    public static String buildResourceIconCss(String prefix) {

        CssBuilder builder = new CssBuilder(prefix);
        return builder.buildResourceIconCss();
    }

}
