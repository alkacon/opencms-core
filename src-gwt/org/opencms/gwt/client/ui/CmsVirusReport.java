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

package org.opencms.gwt.client.ui;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.I_CmsUploadConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * Shows which viruses were found in files the user tried to upload.
 */
public class CmsVirusReport extends Composite {

    /** The UiBinder interface for this widget. */
    protected interface I_CmsVirusReportUiBinder extends UiBinder<Widget, CmsVirusReport> {
        // empty
    }

    /** The UIBinder for this class. */
    static final I_CmsVirusReportUiBinder UI_BINDER = GWT.create(I_CmsVirusReportUiBinder.class);

    /** Displays the warning message to the user. */
    @UiField
    protected CmsMessageWidget m_message;

    /** Contains the resource boxes with the virus information. */
    @UiField
    protected CmsFieldSet m_fieldset;

    /**
     * Creates a new virus report widget.
     *
     * @param viruses a map from file names to lists of virus names found in the corresponding files
     */
    public CmsVirusReport(Map<String, List<String>> viruses) {

        Widget content = UI_BINDER.createAndBindUi(this);
        initWidget(content);
        CmsScrollPanel scroll = new CmsScrollPanel();
        scroll.getElement().getStyle().setProperty("maxHeight", 500, Unit.PX);
        CmsList<?> list = new CmsList<>();
        m_fieldset.add(scroll);
        scroll.add(list);
        m_message.setMessageText(
            org.opencms.gwt.client.Messages.get().key(
                org.opencms.gwt.client.Messages.GUI_UPLOAD_VIRUSES_FOUND_WARNING_0));

        for (Map.Entry<String, List<String>> entry : viruses.entrySet()) {
            String commaSeparatedViruses = Joiner.on(", ").join(entry.getValue());
            CmsListInfoBean infoBean = new CmsListInfoBean(entry.getKey(), commaSeparatedViruses, null);
            CmsListItemWidget listItemWidget = new CmsListItemWidget(infoBean);
            listItemWidget.setIcon(CmsCoreProvider.get().getResourceTypeIcon(entry.getKey()));
            list.add(new CmsListItem(listItemWidget));
        }
    }

    /**
     * Creates a popup containing a virus report.
     *
     * @param viruses a map from file names to lists of viruses found in the corresponding files
     * @param callback the callback to executing after closing the dialog
     * @return
     */
    public static CmsPopup createPopup(Map<String, List<String>> viruses, Runnable callback) {

        CmsPopup popup = new CmsPopup();
        popup.setModal(true);
        popup.setGlassEnabled(true);
        popup.setMainContent(new CmsVirusReport(viruses));
        popup.setCaption(
            org.opencms.gwt.client.Messages.get().key(
                org.opencms.gwt.client.Messages.GUI_UPLOAD_VIRUSES_FOUND_TITLE_0));
        CmsPushButton ok = new CmsPushButton();
        ok.setText(org.opencms.gwt.client.Messages.get().key(org.opencms.gwt.client.Messages.GUI_OK_0));
        ok.addClickHandler(event -> {
            popup.hide();
            callback.run();
        });
        popup.addButton(ok);
        return popup;
    }

    /**
     * Extracts virus warnings from a JSON object.
     *
     * @param jsonObject the JSON object
     * @return the map of virus warnings, which maps file names to lists of viruses found in the corresponding file
     */
    public static Map<String, List<String>> getVirusWarnings(JSONObject jsonObject) {

        Map<String, List<String>> viruses = new HashMap<>();
        JSONValue virusWarnings = jsonObject.get(I_CmsUploadConstants.ATTR_VIRUS_WARNINGS);
        if (virusWarnings != null) {
            JSONObject virusWarningsObj = (JSONObject)virusWarnings;
            for (String key : virusWarningsObj.keySet()) {
                List<String> virusList = new ArrayList<>();
                viruses.put(key, virusList);
                JSONArray virusesForFile = (JSONArray)virusWarningsObj.get(key);
                for (int i = 0; i < virusesForFile.size(); i++) {
                    JSONString jsv = (JSONString)virusesForFile.get(i);
                    virusList.add(jsv.stringValue());
                }
            }
        }
        return viruses;
    }

}
