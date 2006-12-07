/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/setup/xml/Attic/CmsSetupXmlManager.java,v $
 * Date   : $Date: 2006/12/07 12:25:48 $
 * Version: $Revision: 1.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.setup.xml;

import org.opencms.i18n.CmsEncoder;
import org.opencms.setup.CmsSetupBean;
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
 * @author Michael Moossen
 * 
 * @version $Revision: 1.4 $ 
 * 
 * @since 6.1.8 
 */
public class CmsSetupXmlManager {

    /** List of xml update plugins. */
    private List m_plugins;

    /** User selected plugins to execute. */
    private List m_selectedPlugins;

    /** Map of file sorted plugins. */
    private Map m_sortedPlugins;

    /**
     * Default constructor.<p>
     */
    public CmsSetupXmlManager() {

        m_selectedPlugins = new ArrayList();
        m_plugins = new ArrayList();
        // put the plugins here in chronological order (or first remove then add)

        // search
        m_plugins.add(new CmsXmlRemovePageSearchIndexSource1());
        m_plugins.add(new CmsXmlRemoveSysSearchIndex());
        m_plugins.add(new CmsXmlAddDEHelpSearchIndex());

        // system
        m_plugins.add(new CmsXmlAddBackupResourceHandler());
        m_plugins.add(new CmsXmlAddContentNotification());

        // vfs
        m_plugins.add(new CmsXmlReplaceHtmlAreaWidgets());
        m_plugins.add(new CmsXmlAddImageLoader());
        m_plugins.add(new CmsXmlAddImgGalleryParam());
        m_plugins.add(new CmsXmlAddXmlContentWidgets());
        m_plugins.add(new CmsXmlAddMimeTypes());

        // workplace
        m_plugins.add(new CmsXmlAddAvailabilityContextMenu());
        m_plugins.add(new CmsXmlAddMultiContextMenu());
        m_plugins.add(new CmsXmlUpdateHistoryContextMenu());
        m_plugins.add(new CmsXmlAddImgGalleryContextMenues());
        m_plugins.add(new CmsXmlAddPublishButtonAppearance());
        m_plugins.add(new CmsXmlUpdateDefaultPermissions());
        m_plugins.add(new CmsXmlAddAutoSetFeatures());
        m_plugins.add(new CmsXmlUpdateLocalizationKeys());

        setup();
    }

    /**
     * Executes all user selected plugins.<p>
     * 
     * @param setupBean the setup bean
     * 
     * @throws Exception if something goes wrong
     */
    public void execute(CmsSetupBean setupBean) throws Exception {

        Iterator it = m_selectedPlugins.iterator();
        while (it.hasNext()) {
            String id = (String)it.next();
            int d = id.lastIndexOf(".xml") + ".xml".length();
            String fileName = id.substring(0, d);
            int pos = Integer.parseInt(id.substring(d));
            List plugins = (List)m_sortedPlugins.get(fileName);
            I_CmsSetupXmlUpdate plugin = (I_CmsSetupXmlUpdate)plugins.get(pos);
            plugin.execute(setupBean);
        }
        setupBean.getXmlHelper().writeAll();
    }

    /**
     * Returns the plugins.<p>
     * 
     * @return a map of [filenames, list of plugins]
     */
    public Map getPlugins() {

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
        Iterator itFiles = m_sortedPlugins.keySet().iterator();
        while (itFiles.hasNext()) {
            String fileName = (String)itFiles.next();
            Iterator itPlugins = ((List)m_sortedPlugins.get(fileName)).iterator();
            StringBuffer code = new StringBuffer(256);
            for (int i = 0; itPlugins.hasNext(); i++) {
                I_CmsSetupXmlUpdate plugin = (I_CmsSetupXmlUpdate)itPlugins.next();
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

        m_sortedPlugins = new HashMap();
        Iterator it = m_plugins.iterator();
        while (it.hasNext()) {
            I_CmsSetupXmlUpdate plugin = (I_CmsSetupXmlUpdate)it.next();
            List list = (List)m_sortedPlugins.get(plugin.getXmlFilename());
            if (list == null) {
                list = new ArrayList();
                m_sortedPlugins.put(plugin.getXmlFilename(), list);
            }
            list.add(plugin);
        }
    }
}