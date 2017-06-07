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

package org.opencms.ade.sitemap.client.hoverbar;

import org.opencms.ade.sitemap.client.CmsSitemapView;
import org.opencms.ade.sitemap.client.Messages;
import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.client.edit.CmsEditEntryHandler;
import org.opencms.ade.sitemap.client.edit.CmsNavModePropertyEditor;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.property.A_CmsPropertyEditor;
import org.opencms.gwt.client.property.CmsPropertySubmitHandler;
import org.opencms.gwt.client.property.CmsVfsModePropertyEditor;
import org.opencms.gwt.client.property.I_CmsPropertyEditorHandler;
import org.opencms.gwt.client.property.definition.CmsPropertyDefinitionButton;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.input.form.CmsDialogFormHandler;
import org.opencms.gwt.client.ui.input.form.CmsFormDialog;
import org.opencms.gwt.client.ui.input.form.I_CmsFormSubmitHandler;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.util.CmsUUID;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.dom.client.Style;

/**
 * Sitemap context menu edit entry.<p>
 *
 * @since 8.0.0
 */
public class CmsEditMenuEntry extends A_CmsSitemapMenuEntry {

    /**
     * Constructor.<p>
     *
     * @param hoverbar the hoverbar
     */
    public CmsEditMenuEntry(CmsSitemapHoverbar hoverbar) {

        super(hoverbar);
        setLabel(Messages.get().key(Messages.GUI_HOVERBAR_EDIT_0));
        setActive(true);
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry#execute()
     */
    public void execute() {

        final CmsSitemapController controller = getHoverbar().getController();
        final CmsClientSitemapEntry entry = getHoverbar().getEntry();

        final CmsUUID infoId;

        if ((entry.getDefaultFileId() != null) && CmsSitemapView.getInstance().isNavigationMode()) {
            infoId = entry.getDefaultFileId();
        } else {
            infoId = entry.getId();
        }
        Set<CmsUUID> idsForPropertyConfig = new HashSet<CmsUUID>();
        if (entry.getDefaultFileId() != null) {
            idsForPropertyConfig.add(entry.getDefaultFileId());
        }
        if (entry.getId() != null) {
            idsForPropertyConfig.add(entry.getId());
        }
        final List<CmsUUID> propertyConfigIds = new ArrayList<CmsUUID>(idsForPropertyConfig);

        CmsRpcAction<CmsListInfoBean> action = new CmsRpcAction<CmsListInfoBean>() {

            @Override
            public void execute() {

                start(0, true);
                CmsCoreProvider.getVfsService().getPageInfo(infoId, this);
            }

            @Override
            protected void onResponse(CmsListInfoBean infoResult) {

                stop(false);
                final CmsEditEntryHandler handler = new CmsEditEntryHandler(
                    controller,
                    entry,
                    CmsSitemapView.getInstance().isNavigationMode());
                handler.setPageInfo(infoResult);

                CmsRpcAction<Map<CmsUUID, Map<String, CmsXmlContentProperty>>> propertyAction = new CmsRpcAction<Map<CmsUUID, Map<String, CmsXmlContentProperty>>>() {

                    @Override
                    public void execute() {

                        start(0, true);
                        CmsCoreProvider.getVfsService().getDefaultProperties(propertyConfigIds, this);
                    }

                    @Override
                    protected void onResponse(Map<CmsUUID, Map<String, CmsXmlContentProperty>> propertyResult) {

                        stop(false);
                        Map<String, CmsXmlContentProperty> propConfig = new LinkedHashMap<String, CmsXmlContentProperty>();
                        for (Map<String, CmsXmlContentProperty> defaultProps : propertyResult.values()) {
                            propConfig.putAll(defaultProps);
                        }
                        propConfig.putAll(CmsSitemapView.getInstance().getController().getData().getProperties());
                        A_CmsPropertyEditor editor = createEntryEditor(handler, propConfig);
                        editor.setPropertyNames(
                            CmsSitemapView.getInstance().getController().getData().getAllPropertyNames());
                        final CmsFormDialog dialog = new CmsFormDialog(handler.getDialogTitle(), editor.getForm());
                        CmsPropertyDefinitionButton defButton = new CmsPropertyDefinitionButton() {

                            /**
                             * @see org.opencms.gwt.client.property.definition.CmsPropertyDefinitionButton#onBeforeEditPropertyDefinition()
                             */
                            @Override
                            public void onBeforeEditPropertyDefinition() {

                                dialog.hide();
                            }

                        };
                        defButton.getElement().getStyle().setFloat(Style.Float.LEFT);
                        defButton.installOnDialog(dialog);
                        CmsDialogFormHandler formHandler = new CmsDialogFormHandler();
                        formHandler.setDialog(dialog);
                        I_CmsFormSubmitHandler submitHandler = new CmsPropertySubmitHandler(handler);
                        formHandler.setSubmitHandler(submitHandler);
                        dialog.setFormHandler(formHandler);
                        editor.initializeWidgets(dialog);
                        dialog.centerHorizontally(50);
                        dialog.catchNotifications();
                        String noEditReason = controller.getNoEditReason(entry);
                        if (noEditReason != null) {
                            editor.disableInput(noEditReason);
                            dialog.getOkButton().disable(noEditReason);
                        }

                    }
                };
                propertyAction.execute();
            }

        };
        action.execute();
    }

    /**
     * @see org.opencms.ade.sitemap.client.hoverbar.A_CmsSitemapMenuEntry#onShow()
     */
    @Override
    public void onShow() {

        CmsClientSitemapEntry entry = getHoverbar().getEntry();
        boolean show = getHoverbar().getController().isEditable()
            && (entry != null)
            && (!CmsSitemapView.getInstance().isGalleryMode()
                || getHoverbar().getController().getData().isGalleryManager());
        setVisible(show);
    }

    /**
     * Creates the right sitemap entry editor for the current mode.<p>
     *
     * @param handler the entry editor handler
     * @param  propConfig the property configuration to use
     *
     * @return a sitemap entry editor instance
     */
    protected A_CmsPropertyEditor createEntryEditor(
        I_CmsPropertyEditorHandler handler,
        Map<String, CmsXmlContentProperty> propConfig) {

        if (CmsSitemapView.getInstance().isNavigationMode()) {
            return new CmsNavModePropertyEditor(propConfig, handler);
        } else {
            boolean isFolder = handler.isFolder();
            CmsVfsModePropertyEditor result = new CmsVfsModePropertyEditor(propConfig, handler);
            result.setShowResourceProperties(!isFolder);
            return result;
        }
    }
}
