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

package org.opencms.ui.components.categoryselect;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.relations.CmsCategory;
import org.opencms.relations.CmsCategoryService;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsBasicDialog.DialogWidth;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.components.fileselect.I_CmsSelectionHandler;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.v7.ui.CustomField;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.ui.Window;

/**
 * The category select field.<p>
 */
public class CmsCategorySelectField extends CustomField<String>
implements I_CmsSelectionHandler<Collection<CmsCategory>> {

    /** The serial version id. */
    private static final long serialVersionUID = -3080639027333425153L;

    /** The select dialog. */
    private CmsCategorySelectDialog m_dialog;

    /** The select dialog window. */
    private Window m_dialogWindow;

    /** The tree to display the selected categories. */
    private CmsCategoryTree m_tree;

    /**
     * @see com.vaadin.ui.AbstractField#getType()
     */
    @Override
    public Class<? extends String> getType() {

        return String.class;
    }

    /**
     * @see org.opencms.ui.components.fileselect.I_CmsSelectionHandler#onSelection(java.lang.Object)
     */
    public void onSelection(Collection<CmsCategory> selected) {

        setValue(getStringValue(selected));
        m_dialogWindow.close();
    }

    /**
     * @see com.vaadin.ui.AbstractField#getInternalValue()
     */
    @Override
    protected String getInternalValue() {

        if (m_tree == null) {
            String result = super.getInternalValue();
            return result != null ? result : "";
        } else {
            CmsObject cms = A_CmsUI.getCmsObject();
            String result = "";
            for (CmsCategory cat : m_tree.getSelectedCategories()) {
                result += cms.getRequestContext().removeSiteRoot(cat.getRootPath()) + ",";
            }
            if (result.length() > 0) {
                result = result.substring(0, result.length() - 1);
            }
            return getStringValue(m_tree.getSelectedCategories());
        }
    }

    /**
     * @see com.vaadin.ui.CustomField#initContent()
     */
    @Override
    protected Component initContent() {

        HorizontalLayout main = new HorizontalLayout();
        main.setWidth("100%");
        main.setSpacing(true);

        m_tree = new CmsCategoryTree();
        m_tree.setWidth("100%");
        m_tree.setHeight("34px");
        m_tree.setDisplayOnly(true);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(super.getInternalValue())) {
            setInternalValue(super.getInternalValue());
        }
        main.addComponent(m_tree);
        main.setExpandRatio(m_tree, 2);
        Button open = new Button("");
        open.addStyleName(OpenCmsTheme.BUTTON_ICON);
        open.setIcon(FontOpenCms.GALLERY);

        open.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                openWindow();
            }
        });
        main.addComponent(open);
        return main;
    }

    /**
     * @see com.vaadin.ui.AbstractField#setInternalValue(java.lang.Object)
     */
    @Override
    protected void setInternalValue(String newValue) {

        if (m_tree != null) {
            List<CmsCategory> categories = new ArrayList<CmsCategory>();
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(newValue)) {
                CmsObject cms = A_CmsUI.getCmsObject();
                CmsCategoryService catService = CmsCategoryService.getInstance();
                for (String path : newValue.split(",")) {
                    try {
                        CmsCategory cat = catService.getCategory(cms, path);
                        categories.add(cat);
                    } catch (CmsException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            m_tree.setCategories(categories);
            int height = (categories.size() * 33) + 1;
            if (height > 200) {
                height = 200;
            }
            m_tree.setHeight(height + "px");
        }
        super.setInternalValue(newValue);
    }

    /**
     * Opens the select window.<p>
     */
    void openWindow() {

        if (m_dialogWindow == null) {
            m_dialogWindow = CmsBasicDialog.prepareWindow(DialogWidth.wide);
            m_dialogWindow.setCaption("Select categories");
        }
        if (m_dialog == null) {
            m_dialog = new CmsCategorySelectDialog("/");
            m_dialogWindow.setContent(m_dialog);
            m_dialog.addSelectionHandler(this);
        }
        A_CmsUI.get().addWindow(m_dialogWindow);
        m_dialogWindow.center();
        m_dialog.setSelectedCategories(m_tree.getSelectedCategories());
    }

    /**
     * Returns the string representation of the selected categories.<p>
     *
     * @param categories the selected categories
     *
     * @return the string representation of the selected categories
     */
    private String getStringValue(Collection<CmsCategory> categories) {

        CmsObject cms = A_CmsUI.getCmsObject();
        String result = "";
        for (CmsCategory cat : categories) {
            result += cms.getRequestContext().removeSiteRoot(cat.getRootPath()) + ",";
        }
        if (result.length() > 0) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

}
