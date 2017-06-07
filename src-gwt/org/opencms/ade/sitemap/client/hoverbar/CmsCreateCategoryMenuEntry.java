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
import org.opencms.ade.sitemap.shared.CmsSitemapCategoryData;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.input.form.CmsBasicFormField;
import org.opencms.gwt.client.ui.input.form.CmsDialogFormHandler;
import org.opencms.gwt.client.ui.input.form.CmsForm;
import org.opencms.gwt.client.ui.input.form.CmsFormDialog;
import org.opencms.gwt.client.ui.input.form.I_CmsFormSubmitHandler;
import org.opencms.gwt.shared.CmsCategoryTreeEntry;
import org.opencms.util.CmsUUID;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Menu entry for creating new categories.<p>
 */
public class CmsCreateCategoryMenuEntry extends A_CmsSitemapMenuEntry {

    /**
     * Bean containing the title and name of a category.<p>
     */
    public static class CmsCategoryTitleAndName {

        /** The name of the category. */
        private String m_name;

        /** The title of the category. */
        private String m_title;

        /**
         * Creates a new instance.<p>
         *
         * @param title the title of the category
         * @param name the name of the category
         */
        public CmsCategoryTitleAndName(String title, String name) {

            m_name = name;
            m_title = title;
        }

        /**
         * Gets the name of the category.<p>
         *
         * @return the category name
         */
        public String getName() {

            return m_name;
        }

        /**
         * Gets the title of the category.<p>
         *
         * @return the category title
         */
        public String getTitle() {

            return m_title;
        }
    }

    /**
     * Constructor.<p>
     *
     * @param hoverbar the hoverbar
     */
    public CmsCreateCategoryMenuEntry(CmsSitemapHoverbar hoverbar) {

        super(hoverbar);
        setLabel(Messages.get().key(Messages.GUI_SITEMAP_CONTEXT_MENU_CREATE_CATEGORY_0));
        setActive(true);
    }

    /**
     * Asks the user for a new category's name and title.<p>
     *
     * @param parentId the parent category
     * @param callback the callback to call with the user-supplied information
     */
    public static void askForNewCategoryInfo(CmsUUID parentId, final AsyncCallback<CmsCategoryTitleAndName> callback) {

        CmsSitemapCategoryData categoryData = CmsSitemapView.getInstance().getController().getCategoryData();

        CmsCategoryTreeEntry entry = categoryData.getEntryById(parentId);
        String caption;
        if (entry == null) {
            caption = Messages.get().key(Messages.GUI_SITEMAP_CREATE_CATEGORY_TITLE_0);
        } else {
            caption = Messages.get().key(Messages.GUI_SITEMAP_CREATE_SUBCATEGORY_TITLE_1, entry.getPath());
        }
        CmsFormDialog dlg = new CmsFormDialog(caption, new CmsForm(true));
        dlg.setWidth(CmsPopup.DEFAULT_WIDTH);
        CmsDialogFormHandler fh = new CmsDialogFormHandler();
        fh.setSubmitHandler(new I_CmsFormSubmitHandler() {

            public void onSubmitForm(CmsForm form, Map<String, String> fieldValues, Set<String> editedFields) {

                String title = fieldValues.get("title");
                String name = fieldValues.get("name");
                callback.onSuccess(new CmsCategoryTitleAndName(title, name));

            }
        });

        dlg.setFormHandler(fh);
        fh.setDialog(dlg);
        String nameLabel = Messages.get().key(Messages.GUI_CATEGORY_NAME_LABEL_0);
        String titleLabel = Messages.get().key(Messages.GUI_CATEGORY_TITLE_LABEL_0);
        dlg.getForm().addField(CmsBasicFormField.createField(createBasicStringProperty("title", titleLabel)), "");
        dlg.getForm().addField(CmsBasicFormField.createField(createBasicStringProperty("name", nameLabel)), "");
        dlg.getForm().render();
        dlg.center();

    }

    /**
     * Creates a property configuration for a simple named string field.<p>
     *
     * @param name the name of the field
     * @param niceName the display name of the field
     *
     * @return the property configuration
     */
    public static CmsXmlContentProperty createBasicStringProperty(String name, String niceName) {

        CmsXmlContentProperty prop = new CmsXmlContentProperty(name, //name
            "string", // type
            "string", // widget
            "", // widgetconfig
            null, //regex
            null, //ruletype
            null, //default
            niceName, //nicename
            null, //description
            null, //error
            null //preferfolder
        );
        return prop;
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry#execute()
     */
    public void execute() {

        final CmsSitemapController controller = getHoverbar().getController();
        final CmsUUID id = getHoverbar().getId();
        askForNewCategoryInfo(id, new AsyncCallback<CmsCategoryTitleAndName>() {

            public void onFailure(Throwable caught) {

                // do nothing
            }

            public void onSuccess(CmsCategoryTitleAndName result) {

                controller.createCategory(id, result.getTitle(), result.getName());
            }
        });

    }

    /**
     * @see org.opencms.ade.sitemap.client.hoverbar.A_CmsSitemapMenuEntry#onShow()
     */
    @Override
    public void onShow() {

        setVisible(getHoverbar().getController().isEditable());
    }
}
