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

package org.opencms.ui.components;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.sitemanager.CmsSiteManager;
import org.opencms.util.CmsMacroResolver;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;

/**
 * Class for a dialog to show macros of a sitemap folder and allow the user to edit them.<p>
 */
public class CmsMacroResolverDialog extends CmsBasicDialog {

    /**generated vaadin serial id.*/
    private static final long serialVersionUID = 4326570443207345219L;

    /** Action to execute when cancelled. */
    Runnable m_cancelAction;

    /** Action to execute when confirmed. */
    Runnable m_okAction;

    /**Map for storing relations for Vaadin Components to related key values.*/
    private Map<TextField, String> m_bundleComponentKeyMap = new HashMap<TextField, String>();

    /**Form Layout for displaying the bundle valuse in.*/
    private FormLayout m_bundleValues;

    /** Cancel button. */
    private Button m_cancelButton;

    /** OK button .*/
    private Button m_okButton;

    /**
     * Public constructor.<p>
     *
     * @param okAction runnable for ok button.
     * @param cancelAction runnable for cancel button.
     * @param resource (folder) to resolve macros in.
     */
    public CmsMacroResolverDialog(Runnable okAction, Runnable cancelAction, CmsResource resource) {
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        m_okAction = okAction;

        m_cancelAction = cancelAction;

        m_okButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                if (m_okAction != null) {
                    m_okAction.run();
                }
            }
        });
        m_cancelButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                if (m_cancelAction != null) {
                    m_cancelAction.run();
                }

            }
        });
        CmsObject cms;
        try {
            cms = OpenCms.initCmsObject(A_CmsUI.getCmsObject());
            cms.getRequestContext().setSiteRoot("");

            //Read descriptor
            CmsResource descriptor = cms.readResource(
                resource.getRootPath() + CmsSiteManager.MACRO_FOLDER + "/" + CmsSiteManager.BUNDLE_NAME + "_desc");

            //Read related bundle
            Properties resourceBundle = getLocalizedBundle(cms, resource.getRootPath());
            Map<String, String[]> bundleKeyDescriptorMap = CmsMacroResolver.getBundleMapFromResources(
                resourceBundle,
                descriptor,
                cms);

            for (String key : bundleKeyDescriptorMap.keySet()) {

                //Create TextField
                TextField field = new TextField();
                field.setCaption(bundleKeyDescriptorMap.get(key)[0]);
                field.setValue(bundleKeyDescriptorMap.get(key)[1]);
                field.setWidth("100%");

                //Add vaadin component to UI and keep related key in HashMap
                m_bundleValues.addComponent(field);
                m_bundleComponentKeyMap.put(field, key);
            }

        } catch (CmsException | IOException e) {
            //
        }

    }

    /**
     * Reads the entered values for the macro and returns them together with their keys.<p>
     *
     * @return Maps
     */
    public Map<String, String> getMacroMap() {

        Map<String, String> map = new HashMap<String, String>();
        if (m_bundleComponentKeyMap != null) {
            Set<TextField> fields = m_bundleComponentKeyMap.keySet();

            for (TextField field : fields) {
                map.put(m_bundleComponentKeyMap.get(field), field.getValue());
            }
        }
        return map;
    }

    /**
     * Returns the correct variant of a resource name according to locale.<p>
     *
     * @param cms CmsObject
     * @param path where the considered resource is.
     * @param baseName of the resource
     * @return localized name of resource
     */
    private String getAvailableLocalVariant(CmsObject cms, String path, String baseName) {

        A_CmsUI.get();
        List<String> localVariations = CmsLocaleManager.getLocaleVariants(
            baseName,
            UI.getCurrent().getLocale(),
            false,
            true);

        for (String name : localVariations) {
            if (cms.existsResource(path + name)) {
                return name;
            }
        }
        return null;
    }

    /**
     * Gets localized property object.<p>
     *
     * @param cms CmsObject
     * @param path of resource
     * @return Properties object
     * @throws CmsException exception
     * @throws IOException exception
     */
    private Properties getLocalizedBundle(CmsObject cms, String path) throws CmsException, IOException {

        CmsResource bundleResource = cms.readResource(path
            + CmsSiteManager.MACRO_FOLDER
            + "/"
            + getAvailableLocalVariant(cms, path + CmsSiteManager.MACRO_FOLDER + "/", CmsSiteManager.BUNDLE_NAME));

        Properties ret = new Properties();
        InputStreamReader reader = new InputStreamReader(
            new ByteArrayInputStream(cms.readFile(bundleResource).getContents()),
            StandardCharsets.UTF_8);
        ret.load(reader);

        return ret;
    }
}
