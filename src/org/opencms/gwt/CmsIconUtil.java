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
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.main.CmsEvent;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.workplace.explorer.CmsIconRule;

import java.util.Map;

/**
 * Utility class to generate the resource icon CSS.<p>
 *
 * @since 8.0.0
 */
public final class CmsIconUtil extends org.opencms.gwt.shared.CmsIconUtil implements I_CmsEventListener {

    /**
     * Inner helper class for building the CSS rules.<p>
     */
    public static class CssBuilder {

        /** The buffer into which the CSS is written. */
        private StringBuffer m_buffer = new StringBuffer(1024);

        /**
         * Builds the CSS for all resource types.<p>
         *
         * @return a string containing the CSS rules for all resource types
         */
        public String buildResourceIconCss() {

            m_buffer.append(buildUnknownIconCss());
            for (CmsExplorerTypeSettings type : OpenCms.getWorkplaceManager().getExplorerTypeSettings()) {
                addCssForType(type);
            }
            addPseudoTypes();
            addResourceNotFoundIconRule();
            return m_buffer.toString();
        }

        /**
         * Builds the CSS for the icon for unknown resource types.<p>
         *
         * @return the CSS for unknown resource type icons
         */
        public String buildUnknownIconCss() {

            String unknown = getIconUri(
                OpenCms.getWorkplaceManager().getExplorerTypeSetting(
                    CmsResourceTypeUnknownFile.getStaticTypeName()).getBigIconIfAvailable());
            String template = "div.%1$s, span.%1$s { background: transparent scroll 50%% 50%% no-repeat url(\"%2$s\");} ";

            return String.format(template, TYPE_ICON_CLASS, unknown);
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
                CmsIconCssRuleBuilder cssBig = new CmsIconCssRuleBuilder();
                cssBig.addSelectorForSubType(typeName, extension, false);
                cssBig.setImageUri(getIconUri(rule.getBigIcon()));
                cssBig.writeCss(m_buffer);

                CmsIconCssRuleBuilder cssSmall = new CmsIconCssRuleBuilder();
                cssSmall.addSelectorForSubType(typeName, extension, true);
                cssSmall.setImageUri(getIconUri(rule.getIcon()));
                cssSmall.writeCss(m_buffer);

            } else {
                CmsIconCssRuleBuilder css = new CmsIconCssRuleBuilder();
                css.addSelectorForSubType(typeName, extension, false);
                css.addSelectorForSubType(typeName, extension, true);
                css.setImageUri(getIconUri(rule.getIcon()));
                css.writeCss(m_buffer);

            }
        }

        /**
         * Helper method for appending the CSS for a single resource type to a buffer.<p>
         *
         * @param explorerType the explorer type for which the CSS should be generated
         */
        private void addCssForType(CmsExplorerTypeSettings explorerType) {

            String typeName = explorerType.getName();
            if (explorerType.getBigIcon() != null) {
                CmsIconCssRuleBuilder css = new CmsIconCssRuleBuilder();
                css.setImageUri(getIconUri(explorerType.getBigIcon()));
                css.addSelectorForType(typeName, false);
                css.writeCss(m_buffer);

                CmsIconCssRuleBuilder cssSmall = new CmsIconCssRuleBuilder();
                cssSmall.setImageUri(getIconUri(explorerType.getIcon()));
                cssSmall.addSelectorForType(typeName, true);
                cssSmall.writeCss(m_buffer);
            } else if (explorerType.getOriginalIcon() != null) {
                CmsIconCssRuleBuilder css = new CmsIconCssRuleBuilder();
                css.setImageUri(getIconUri(explorerType.getIcon()));
                css.addSelectorForType(typeName, true);
                css.addSelectorForType(typeName, false);
                css.writeCss(m_buffer);
            } else {
                CmsIconCssRuleBuilder css = new CmsIconCssRuleBuilder();
                css.setImageUri(getIconUri(CmsExplorerTypeSettings.DEFAULT_BIG_ICON));
                css.addSelectorForType(typeName, false);
                css.writeCss(m_buffer);

                CmsIconCssRuleBuilder cssSmall = new CmsIconCssRuleBuilder();
                cssSmall.setImageUri(getIconUri(CmsExplorerTypeSettings.DEFAULT_NORMAL_ICON));
                cssSmall.addSelectorForType(typeName, true);
                cssSmall.writeCss(m_buffer);
            }

            Map<String, CmsIconRule> iconRules = explorerType.getIconRules();
            for (Map.Entry<String, CmsIconRule> entry : iconRules.entrySet()) {
                CmsIconRule rule = entry.getValue();
                addCssForIconRule(typeName, rule);
            }
        }

        /**
         * Adds icon rule for pseudo resource types.<p>
         */
        private void addPseudoTypes() {

            CmsExplorerTypeSettings navlevel = new CmsExplorerTypeSettings();
            navlevel.setName(CmsGwtConstants.TYPE_NAVLEVEL);
            navlevel.setIcon(ICON_NAV_LEVEL_SMALL);
            navlevel.setBigIcon(ICON_NAV_LEVEL_BIG);
            addCssForType(navlevel);
            CmsExplorerTypeSettings modelgroupReuse = new CmsExplorerTypeSettings();
            modelgroupReuse.setName(CmsGwtConstants.TYPE_MODELGROUP_REUSE);
            modelgroupReuse.setIcon(ICON_MODEL_GROUP_REUSE_SMALL);
            modelgroupReuse.setBigIcon(ICON_MODEL_GROUP_REUSE_BIG);
            addCssForType(modelgroupReuse);
            CmsExplorerTypeSettings modelgroupPage = new CmsExplorerTypeSettings();
            modelgroupPage.setName(CmsGwtConstants.TYPE_MODELGROUP_PAGE);
            modelgroupPage.setIcon(ICON_MODEL_GROUP_SMALL);
            modelgroupPage.setBigIcon(ICON_MODEL_GROUP_BIG);
            addCssForType(modelgroupPage);
        }

        /**
         * Adds an icon rule for resource not found.<p>
         */
        private void addResourceNotFoundIconRule() {

            CmsIconCssRuleBuilder cssBig = new CmsIconCssRuleBuilder();
            cssBig.addSelectorForType(TYPE_RESOURCE_NOT_FOUND, false);
            cssBig.setImageUri(getIconUri(NOT_FOUND_ICON_BIG));
            cssBig.writeCss(m_buffer);

            CmsIconCssRuleBuilder cssSmall = new CmsIconCssRuleBuilder();
            cssSmall.addSelectorForType(TYPE_RESOURCE_NOT_FOUND, true);
            cssSmall.setImageUri(getIconUri(NOT_FOUND_ICON_SMALL));
            cssSmall.writeCss(m_buffer);
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

    /** Pseudo type icon. */
    public static final String ICON_MODEL_GROUP_BIG = "modelpage_groups_big.png";

    /** Pseudo type icon. */
    public static final String ICON_MODEL_GROUP_REUSE_BIG = "modelgroup_reuse_big.png";

    /** Pseudo type icon. */
    public static final String ICON_MODEL_GROUP_REUSE_SMALL = "modelgroup_reuse.png";

    /** Pseudo type icon. */
    public static final String ICON_MODEL_GROUP_SMALL = "modelpage_groups.png";

    /** Pseudo type icon. */
    public static final String ICON_NAV_LEVEL_BIG = "navlevel_big.png";

    /** Pseudo type icon. */
    public static final String ICON_NAV_LEVEL_SMALL = "navlevel.png";

    /** The big resource not found icon name. */
    public static final String NOT_FOUND_ICON_BIG = "resourceNotFoundBig.png";

    /** The small resource not found icon name. */
    public static final String NOT_FOUND_ICON_SMALL = "resourceNotFoundSmall.png";

    /** The cached CSS. */
    private static String m_cachedCss;

    /** Flag indicating the 'clear caches' event listener has been registered. */
    private static boolean m_listenerRegistered;

    /**
     * Constructor.<p>
     */
    private CmsIconUtil() {

    }

    /**
     * Builds the CSS for all resource types.<p>
     *
     * @return a string containing the CSS rules for all resource types
     */
    public static String buildResourceIconCss() {

        if (!m_listenerRegistered) {
            registerListener();
        }
        if (m_cachedCss == null) {
            rebuildCss();
        }
        return m_cachedCss;
    }

    /**
     * Rebuilds the icon CSS.<p>
     */
    private static synchronized void rebuildCss() {

        if (m_cachedCss == null) {
            CssBuilder builder = new CssBuilder();
            m_cachedCss = builder.buildResourceIconCss();
        }
    }

    /**
     * Registers the 'clear caches' event listener.<p>
     */
    private static synchronized void registerListener() {

        if (!m_listenerRegistered) {
            OpenCms.getEventManager().addCmsEventListener(
                new CmsIconUtil(),
                new int[] {I_CmsEventListener.EVENT_CLEAR_CACHES});
            m_listenerRegistered = true;
        }
    }

    /**
     * @see org.opencms.main.I_CmsEventListener#cmsEvent(org.opencms.main.CmsEvent)
     */
    public void cmsEvent(CmsEvent event) {

        m_cachedCss = null;
    }

}
