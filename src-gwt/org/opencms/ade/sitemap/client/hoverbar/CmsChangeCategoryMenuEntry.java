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

import org.opencms.ade.sitemap.client.Messages;
import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.client.hoverbar.CmsCreateCategoryMenuEntry.CmsCategoryTitleAndName;
import org.opencms.file.CmsResource;
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
 * Menu entry for changing categories.<p>
 */
public class CmsChangeCategoryMenuEntry extends A_CmsSitemapMenuEntry {

    /**
     * Constructor.<p>
     *
     * @param hoverbar the hoverbar
     */
    public CmsChangeCategoryMenuEntry(CmsSitemapHoverbar hoverbar) {

        super(hoverbar);
        setLabel(Messages.get().key(Messages.GUI_CONTEXTMENU_EDIT_CATEGORY_0));
        setActive(true);
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry#execute()
     */
    public void execute() {

        final CmsSitemapController controller = getHoverbar().getController();

        final CmsUUID id = getHoverbar().getId();
        CmsCategoryTreeEntry entry = controller.getCategoryData().getEntryById(id);
        if (entry != null) {
            String title = entry.getTitle();
            String name = CmsResource.getName(entry.getPath()).replaceAll("/", "");
            String caption = Messages.get().key(Messages.GUI_EDIT_CATEGORY_CAPTION_1, entry.getTitleOrName());

            askForCategoryInfo(caption, title, name, new AsyncCallback<CmsCategoryTitleAndName>() {

                public void onFailure(Throwable caught) {

                    // do nothing
                }

                public void onSuccess(CmsCategoryTitleAndName result) {

                    controller.changeCategory(id, result.getTitle(), result.getName());
                }
            });

        }

    }

    /**
     * @see org.opencms.ade.sitemap.client.hoverbar.A_CmsSitemapMenuEntry#onShow()
     */
    @Override
    public void onShow() {

        boolean visible = getHoverbar().getController().isEditable()
            && (getHoverbar().getId() != null)
            && !getHoverbar().getId().isNullUUID();
        setVisible(visible);
    }

    /**
     * Creates a property configuration for a simple named string field.<p>
     *
     * @param name the name of the field
     * @param niceName the display name of the field
     *
     * @return the property configuration
     */
    CmsXmlContentProperty createBasicStringProperty(String name, String niceName) {

        CmsXmlContentProperty prop = new CmsXmlContentProperty(
            name, //name
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
     * Asks the user for the changed category info.<p>
     *
     * @param caption the dialog caption
     * @param title the old title
     * @param name the old name
     * @param callback the callback to call when done
     *
     */
    private void askForCategoryInfo(
        String caption,
        String title,
        String name,
        final AsyncCallback<CmsCategoryTitleAndName> callback) {

        CmsFormDialog dlg = new CmsFormDialog(caption, new CmsForm(true));
        dlg.setWidth(CmsPopup.DEFAULT_WIDTH);
        CmsDialogFormHandler fh = new CmsDialogFormHandler();
        fh.setSubmitHandler(new I_CmsFormSubmitHandler() {

            public void onSubmitForm(CmsForm form, Map<String, String> fieldValues, Set<String> editedFields) {

                String formtitle = fieldValues.get("title");
                String formname = fieldValues.get("name");
                callback.onSuccess(new CmsCategoryTitleAndName(formtitle, formname));

            }
        });

        dlg.setFormHandler(fh);
        fh.setDialog(dlg);
        String titleLabel = Messages.get().key(Messages.GUI_CATEGORY_TITLE_LABEL_0);
        dlg.getForm().addField(CmsBasicFormField.createField(createBasicStringProperty("title", titleLabel)), title);
        String nameLabel = Messages.get().key(Messages.GUI_CATEGORY_NAME_LABEL_0);
        dlg.getForm().addField(CmsBasicFormField.createField(createBasicStringProperty("name", nameLabel)), name);
        dlg.getForm().render();
        dlg.center();

    }
}
