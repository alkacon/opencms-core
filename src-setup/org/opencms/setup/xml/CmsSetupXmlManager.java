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

package org.opencms.setup.xml;

import org.opencms.i18n.CmsEncoder;
import org.opencms.setup.CmsSetupBean;
import org.opencms.setup.CmsUpdateInfo;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Manages all changes to be made to xml configuration files.<p>
 *
 * @since 6.1.8
 */
public class CmsSetupXmlManager {

    /** List of xml update plugins. */
    private List<I_CmsSetupXmlUpdate> m_plugins;

    /** User selected plugins to execute. */
    private List<String> m_selectedPlugins;

    /** Map of file sorted plugins. */
    private Map<String, List<I_CmsSetupXmlUpdate>> m_sortedPlugins;

    /**
     * Default constructor.<p>
     */
    public CmsSetupXmlManager() {

        // empty
    }

    /**
     * Executes all user selected plugins.<p>
     *
     * @param setupBean the setup bean
     *
     * @throws Exception if something goes wrong
     */
    public void execute(CmsSetupBean setupBean) throws Exception {

        Iterator<String> it = m_selectedPlugins.iterator();
        while (it.hasNext()) {
            String id = it.next();
            int d = id.lastIndexOf(".xml") + ".xml".length();
            String fileName = id.substring(0, d);
            int pos = Integer.parseInt(id.substring(d));
            List<I_CmsSetupXmlUpdate> plugins = m_sortedPlugins.get(fileName);
            I_CmsSetupXmlUpdate plugin = plugins.get(pos);
            plugin.execute(setupBean);
        }
        setupBean.getXmlHelper().writeAll();
    }

    /**
     * Returns the plugins.<p>
     *
     * @return a map of [filenames, list of plugins]
     */
    public Map<String, List<I_CmsSetupXmlUpdate>> getPlugins() {

        return Collections.unmodifiableMap(m_sortedPlugins);
    }

    /**
     * Returns html for displaying a plugin selection box.<p>
     *
     * @param setupBean the setup bean
     *
     * @return html code
     *
     * @throws Exception if something goes wrong
     */
    public String htmlAvailablePlugins(CmsSetupBean setupBean) throws Exception {

        StringBuffer html = new StringBuffer(1024);
        Iterator<String> itFiles = m_sortedPlugins.keySet().iterator();
        while (itFiles.hasNext()) {
            String fileName = itFiles.next();
            Iterator<I_CmsSetupXmlUpdate> itPlugins = m_sortedPlugins.get(fileName).iterator();
            StringBuffer code = new StringBuffer(256);
            for (int i = 0; itPlugins.hasNext(); i++) {
                I_CmsSetupXmlUpdate plugin = itPlugins.next();
                if (plugin.validate(setupBean)) {
                    code.append(htmlPlugin(setupBean, plugin, i));
                }
            }
            if (code.length() > 0) {
                html.append("<tr><th colspan='2' align='left'>");
                html.append(fileName);
                html.append("</th></tr>\n");
                html.append(code.toString());
            }
        }
        return html.toString();
    }

    /**
     * Initializes the plug-ins.<p>
     *
     * @param detectedVersion detected mayor version
     */
    public void initialize(double detectedVersion) {

        m_selectedPlugins = new ArrayList<String>();
        m_plugins = new ArrayList<I_CmsSetupXmlUpdate>();
        // put the plugins here in chronological order (or first remove then add)

        if (detectedVersion < 7) {
            // importexport
            m_plugins.add(new org.opencms.setup.xml.v7.CmsXmlRemoveResourcesToRender());
            m_plugins.add(new org.opencms.setup.xml.v7.CmsXmlAddResourcesToRender());

            // workplace
            m_plugins.add(new org.opencms.setup.xml.v7.CmsXmlUpdateDefaultProperties());
            m_plugins.add(new org.opencms.setup.xml.v7.CmsXmlAddAvailabilityContextMenu());
            m_plugins.add(new org.opencms.setup.xml.v7.CmsXmlAddMultiContextMenu());
            m_plugins.add(new org.opencms.setup.xml.v7.CmsXmlUpdateHistoryContextMenu());
            m_plugins.add(new org.opencms.setup.xml.v7.CmsXmlAddImgGalleryContextMenues());
            m_plugins.add(new org.opencms.setup.xml.v7.CmsXmlAddPublishButtonAppearance());
            m_plugins.add(new org.opencms.setup.xml.v7.CmsXmlUpdateDefaultPermissions());
            m_plugins.add(new org.opencms.setup.xml.v7.CmsXmlAddAutoSetFeatures());
            m_plugins.add(new org.opencms.setup.xml.v7.CmsXmlUpdateLocalizationKeys());
        }

        // importexport
        m_plugins.add(new org.opencms.setup.xml.v7.CmsXmlRemoveImmutables());
        m_plugins.add(new org.opencms.setup.xml.v7.CmsXmlRemoveImportVersions());
        m_plugins.add(new org.opencms.setup.xml.v7.CmsXmlRemoveImportHandlers());
        m_plugins.add(new org.opencms.setup.xml.v7.CmsXmlAddImportVersions());
        m_plugins.add(new org.opencms.setup.xml.v7.CmsXmlAddImmutables());

        // search
        m_plugins.add(new org.opencms.setup.xml.v7.CmsXmlRemovePageSearchIndexSource1());
        m_plugins.add(new org.opencms.setup.xml.v7.CmsXmlRemoveSysSearchIndex());
        m_plugins.add(new org.opencms.setup.xml.v8.CmsXmlAddSolrSearch());

        // system
        m_plugins.add(new org.opencms.setup.xml.v7.CmsXmlRemoveResourceHandlers());
        m_plugins.add(new org.opencms.setup.xml.v7.CmsXmlAddContentNotification());
        m_plugins.add(new org.opencms.setup.xml.v7.CmsXmlAddResourceHandlers());

        m_plugins.add(new org.opencms.setup.xml.v8.CmsXmlAddResourceHandlers());
        m_plugins.add(new org.opencms.setup.xml.v8.CmsXmlAddAdeConfig());
        m_plugins.add(new org.opencms.setup.xml.v8.CmsXmlAddTimezone());
        m_plugins.add(new org.opencms.setup.xml.v8.CmsXmlUpdateFlexcache());
        m_plugins.add(new org.opencms.setup.xml.v8.CmsXmlAddSharedFolderConfiguration());

        m_plugins.add(new org.opencms.setup.xml.v10.CmsXmlAddRequestHandlers());

        // vfs
        m_plugins.add(new org.opencms.setup.xml.v7.CmsXmlRemoveResourceLoaders());
        m_plugins.add(new org.opencms.setup.xml.v7.CmsXmlRemoveResourceTypes());
        m_plugins.add(new org.opencms.setup.xml.v7.CmsXmlReplaceHtmlAreaWidgets());
        m_plugins.add(new org.opencms.setup.xml.v7.CmsXmlAddImageLoader());
        m_plugins.add(new org.opencms.setup.xml.v7.CmsXmlAddImgGalleryParam());
        m_plugins.add(new org.opencms.setup.xml.v7.CmsXmlChangeGalleryClasses());
        m_plugins.add(new org.opencms.setup.xml.v7.CmsXmlAddXmlContentWidgets());
        m_plugins.add(new org.opencms.setup.xml.v7.CmsXmlAddXmlSchemaTypes());
        m_plugins.add(new org.opencms.setup.xml.v7.CmsXmlAddMimeTypes());
        m_plugins.add(new org.opencms.setup.xml.v7.CmsXmlAddResourceTypes());
        m_plugins.add(new org.opencms.setup.xml.v7.CmsXmlAddHtmlConverters());

        m_plugins.add(new org.opencms.setup.xml.v8.CmsXmlAddLoaders());
        m_plugins.add(new org.opencms.setup.xml.v8.CmsXmlAddJspLoaderParams());
        m_plugins.add(new org.opencms.setup.xml.v8.CmsXmlAddResourceTypeParams());
        m_plugins.add(new org.opencms.setup.xml.v8.CmsXmlAddWidgets());
        m_plugins.add(new org.opencms.setup.xml.v8.CmsXmlAddCollectors());
        m_plugins.add(new org.opencms.setup.xml.v8.CmsXmlAddTypeMappings());
        m_plugins.add(new org.opencms.setup.xml.v8.CmsXmlAddTranslationRules());
        //m_plugins.add(new org.opencms.setup.xml.v8.CmsXmlUpdateSchemaTypes());

        // workplace
        m_plugins.add(new org.opencms.setup.xml.v7.CmsXmlUpdateDirectEditProvider());
        m_plugins.add(new org.opencms.setup.xml.v7.CmsXmlAddContextMenuItems());
        m_plugins.add(new org.opencms.setup.xml.v7.CmsXmlFixContextMenuItems());

        m_plugins.add(new org.opencms.setup.xml.v8.CmsXmlAddUnknownFile());
        m_plugins.add(new org.opencms.setup.xml.v8.CmsXmlAddWizardETypeDesc());
        m_plugins.add(new org.opencms.setup.xml.v8.CmsXmlUpdateOpenGallery());

        m_plugins.add(new org.opencms.setup.xml.v8.CmsXmlAddIconRules());
        m_plugins.add(new org.opencms.setup.xml.v8.CmsXmlUpdateMenuRules());
        m_plugins.add(new org.opencms.setup.xml.v8.CmsXmlChangeDefaultDirectEditProvider());
        m_plugins.add(new org.opencms.setup.xml.v8.CmsXmlUpdateContextMenuEntries());
        m_plugins.add(new org.opencms.setup.xml.v8.CmsXmlChangeDefaultUpload());
        m_plugins.add(new org.opencms.setup.xml.v8.CmsXmlUpdateExplorerTypes());
        if (CmsUpdateInfo.INSTANCE.needToSetCategoryFolder()) {
            m_plugins.add(new org.opencms.setup.xml.v8.CmsXmlSetCategoryFolder());
        }

        m_plugins.add(new org.opencms.setup.xml.v9.CmsXmlUpdateDefaultPermissions());
        m_plugins.add(new org.opencms.setup.xml.v9.CmsXmlCleanUpSearchConfiguration());

        m_plugins.add(new org.opencms.setup.xml.v9.CmsXmlUpdateContextMenuEntries());
        m_plugins.add(new org.opencms.setup.xml.v10.CmsXmlUpdateFiletypeIcons());
        m_plugins.add(new org.opencms.setup.xml.v10.CmsXmlChangeExplorerTypeAccess());
        m_plugins.add(new org.opencms.setup.xml.v10.CmsXmlUpdateAvailabilityMenuEntries());
        m_plugins.add(new org.opencms.setup.xml.v10.CmsXmlUpdatePreferences());

        setup();
    }

    /**
     * Bean property setter method user from the <code>step_5_xmlupdate.jsp</code>.<p>
     *
     * @param value the value to set
     */
    public void setSelectedPlugins(String value) {

        m_selectedPlugins = CmsStringUtil.splitAsList(value, "|", true);
    }

    /**
     * Generates html code for the given plugin at the given position.<p>
     *
     * @param plugin the plugin
     * @param pos the position
     * @param setupBean the setup bean
     *
     * @return html code
     *
     * @throws Exception if something goes wrong
     */
    private String htmlPlugin(CmsSetupBean setupBean, I_CmsSetupXmlUpdate plugin, int pos) throws Exception {

        StringBuffer html = new StringBuffer(256);
        String id = plugin.getXmlFilename() + pos;
        html.append("\t<tr>\n");
        html.append("\t\t<td style='vertical-align: top;' nowrap>\n");
        html.append("\t\t\t<input type='checkbox' name='availablePlugins' value='");
        html.append(id);
        html.append("' checked='checked'>\n");
        html.append("\t\t</td>\n");
        html.append("\t\t<td style='vertical-align: top; width: 100%; padding-top: 4px;'>\n\t\t\t");
        html.append("<a href=\"javascript:switchview('").append(id).append("');\">");
        html.append(plugin.getName()).append("</a><br>\n");
        html.append("\t<div id='").append(id).append("' style='display: none;'>\n");
        String codeToChange = plugin.getCodeToChange(setupBean);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(codeToChange)) {
            html.append("<pre class='code'>");
            html.append(CmsEncoder.escapeXml(codeToChange));
            html.append("</pre>\n");
        }
        html.append("\t</div>\n");
        html.append("\n\t\t</td>\n");
        html.append("\t</tr>\n");
        return html.toString();
    }

    /**
     * Sort the plugins by filename.<p>
     */
    private void setup() {

        m_sortedPlugins = new HashMap<String, List<I_CmsSetupXmlUpdate>>();
        Iterator<I_CmsSetupXmlUpdate> it = m_plugins.iterator();
        while (it.hasNext()) {
            I_CmsSetupXmlUpdate plugin = it.next();
            List<I_CmsSetupXmlUpdate> list = m_sortedPlugins.get(plugin.getXmlFilename());
            if (list == null) {
                list = new ArrayList<I_CmsSetupXmlUpdate>();
                m_sortedPlugins.put(plugin.getXmlFilename(), list);
            }
            list.add(plugin);
        }
    }
}
