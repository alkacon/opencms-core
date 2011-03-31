/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsInfoLoadingListItemWidget.java,v $
 * Date   : $Date: 2011/03/31 17:39:52 $
 * Version: $Revision: 1.6 $
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

package org.opencms.gwt.client.ui;

import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.css.I_CmsImageBundle;
import org.opencms.gwt.client.util.I_CmsAdditionalInfoLoader;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.util.CmsPair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * This class extends the basic list item widget with the ability to load additional info items
 * asnchronously via RPC.<p>
 * 
 * The loaded additional info items will be displayed after the additional info items contained in the 
 * bean which is passed into the constructor.<p>  
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.6 $
 * 
 * @since 8.0.0
 */
public class CmsInfoLoadingListItemWidget extends CmsListItemWidget {

    /**
     * Creates a new list item widget from an info bean.<p>
     * 
     * @param infoBean the bean containing the information to display 
     */
    public CmsInfoLoadingListItemWidget(CmsListInfoBean infoBean) {

        super(infoBean);
    }

    /** Flag which keeps track of whether the additional info panel is shown. */
    protected boolean m_additionalInfoOpen;

    /** Flag which keeps track of whether additional info items are currently being loaded. */
    protected boolean m_loading;

    /**
     * Sets the loader for additional info items.<p>
     * 
     * @param loader the loader for additional info items 
     */
    public void setAdditionalInfoLoader(I_CmsAdditionalInfoLoader loader) {

        m_additionalInfoLoader = loader;
    }

    /** The loader for additional info items. */
    protected I_CmsAdditionalInfoLoader m_additionalInfoLoader = new DummyAdditionalInfoLoader();

    /**
     * The default loader for additional info items, which does nothing.
     */
    public class DummyAdditionalInfoLoader implements I_CmsAdditionalInfoLoader {

        /**
         * @see org.opencms.gwt.client.util.I_CmsAdditionalInfoLoader#load(com.google.gwt.user.client.rpc.AsyncCallback)
         */
        public void load(AsyncCallback<List<AdditionalInfoItem>> callback) {

            callback.onSuccess(Collections.<AdditionalInfoItem> emptyList());
        }
    }

    /** The dynamically loaded additional info items. */
    private List<AdditionalInfoItem> m_dynamicInfo = new ArrayList<AdditionalInfoItem>();

    /**
     * Sets the dynamically loaded additional info items.<p>
     * 
     * @param info the dynamically loaded additional info items 
     */
    protected void setDynamicInfo(List<AdditionalInfoItem> info) {

        for (AdditionalInfoItem item : m_dynamicInfo) {
            item.removeFromParent();
        }
        for (AdditionalInfoItem item : info) {
            m_dynamicInfo.add(item);
            m_additionalInfo.add(item);
        }
    }

    /**
     * Sets the icon to the open or closed state.<p>
     * 
     * @param open if true, sets the icon to the open state, else to the closed state 
     */
    protected void setIcon(boolean open) {

        String imageClass = open
        ? I_CmsImageBundle.INSTANCE.style().triangleDown()
        : I_CmsImageBundle.INSTANCE.style().triangleRight();
        m_openClose.setDownImageClass(imageClass);
        m_openClose.setImageClass(imageClass);
    }

    /**
     * Constructor.<p>
     * 
     * @param infoBean bean holding the item information
     */
    @Override
    protected void init(final CmsListInfoBean infoBean) {

        m_iconPanel.setVisible(false);
        m_title.setText(infoBean.getTitle());
        m_subtitle.setText(infoBean.getSubTitle());
        if ((infoBean.getAdditionalInfo() != null) && (infoBean.getAdditionalInfo().size() > 0)) {
            m_openClose = new CmsPushButton(
                I_CmsImageBundle.INSTANCE.style().triangleRight(),
                I_CmsImageBundle.INSTANCE.style().triangleDown());
            setIcon(false);
            m_openClose.setButtonStyle(ButtonStyle.TRANSPARENT, null);
            m_titleRow.insert(m_openClose, 0);
            m_openClose.addClickHandler(new ClickHandler() {

                /**
                 * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
                 */
                public void onClick(ClickEvent event) {

                    if (m_additionalInfoOpen) {
                        setAdditionalInfoVisible(false);
                        m_additionalInfoOpen = false;
                        setIcon(false);
                    } else {
                        if (!m_loading) {
                            m_loading = true;
                            setIcon(true);
                            m_additionalInfoLoader.load(new AsyncCallback<List<AdditionalInfoItem>>() {

                                /**
                                 * @see com.google.gwt.user.client.rpc.AsyncCallback#onFailure(java.lang.Throwable)
                                 */
                                public void onFailure(Throwable caught) {

                                    m_loading = false;
                                    setIcon(false);
                                }

                                /**
                                 * @see com.google.gwt.user.client.rpc.AsyncCallback
                                 */
                                public void onSuccess(List<AdditionalInfoItem> result) {

                                    setDynamicInfo(result);
                                    m_loading = false;
                                    m_additionalInfoOpen = true;
                                    setDynamicInfo(result);
                                    setIcon(true);
                                    setAdditionalInfoVisible(true);
                                }
                            });
                        }
                    }
                }
            });
            m_additionalInfo.clear();
            for (Entry<String, CmsPair<String, String>> entry : infoBean.getAdditionalInfo().entrySet()) {
                CmsPair<String, String> values = entry.getValue();
                AdditionalInfoItem info = new AdditionalInfoItem(entry.getKey(), values.getFirst(), values.getSecond());
                m_additionalInfo.add(info);
            }

        }
    }

}
