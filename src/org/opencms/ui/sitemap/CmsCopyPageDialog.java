/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ui.sitemap;

import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.tools.CmsContainerPageCopier;
import org.opencms.i18n.tools.CmsContainerPageCopier.NoCustomReplacementException;
import org.opencms.main.CmsException;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.components.fileselect.CmsResourceSelectField;
import org.opencms.ui.components.fileselect.CmsSitemapSelectField;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Arrays;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;

/**
 * Dialog used to copy container pages including their elements.<p>
 */
public class CmsCopyPageDialog extends CmsBasicDialog {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The cancel button. */
    private Button m_cancelButton;

    /** The dialog context. */
    private I_CmsDialogContext m_context;

    /** The copy mode selection field. */
    private ComboBox m_copyMode = new ComboBox();

    /** The OK button. */
    private Button m_okButton;

    /** The field for selecting the target folder. */
    private CmsResourceSelectField m_targetSelect;

    /**
     * Creates a new instance.<p>
     *
     * @param context the dialog context
     */
    public CmsCopyPageDialog(I_CmsDialogContext context) {
        m_context = context;
        displayResourceInfo(context.getResources());
        initButtons();
        m_copyMode.setNullSelectionAllowed(false);
        setContent(initContent());
        m_okButton.setEnabled(false);
        m_targetSelect.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 1L;

            @SuppressWarnings("synthetic-access")
            public void valueChange(ValueChangeEvent event) {

                m_okButton.setEnabled(true);
            }
        });

    }

    /**
     * Initializes the button bar.<p>
     */
    void initButtons() {

        m_okButton = new Button(CmsVaadinUtils.getMessageText(org.opencms.workplace.Messages.GUI_DIALOG_BUTTON_OK_0));
        m_okButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                onClickOk();
            }
        });
        addButton(m_okButton);
        m_cancelButton = new Button(
            CmsVaadinUtils.getMessageText(org.opencms.workplace.Messages.GUI_DIALOG_BUTTON_CANCEL_0));
        m_cancelButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            @SuppressWarnings("synthetic-access")
            public void buttonClick(ClickEvent event) {

                m_context.finish(new ArrayList<CmsUUID>());
            }
        });

        addButton(m_cancelButton);
    }

    /**
     * Method that is called when the OK button is clicked.<p>
     */
    void onClickOk() {

        CmsContainerPageCopier copier = new CmsContainerPageCopier(m_context.getCms());
        try {
            CmsContainerPageCopier.CopyMode mode = (CmsContainerPageCopier.CopyMode)(m_copyMode.getValue());
            copier.setCopyMode(mode);
            copier.run(m_context.getResources().get(0), m_targetSelect.getValue());
            m_context.finish(
                Arrays.asList(
                    copier.getTargetFolder().getStructureId(),
                    copier.getCopiedFolderOrPage().getStructureId()));
        } catch (CmsException e) {
            m_context.error(e);
        } catch (NoCustomReplacementException e) {
            String errorMessage = CmsVaadinUtils.getMessageText(
                Messages.GUI_COPYPAGE_NO_REPLACEMENT_FOUND_1,
                e.getResource().getRootPath());
            CmsErrorDialog.showErrorDialog(errorMessage, e);
        }

    }

    /**
     * Initializes the content panel.<p>
     *
     * @return the content panel
     */
    private FormLayout initContent() {

        FormLayout form = new FormLayout();
        CmsResourceSelectField field = new CmsSitemapSelectField(m_context.getResources().get(0));
        field.setStartWithSitempaView(true);
        field.setResourceFilter(CmsResourceFilter.IGNORE_EXPIRATION.addRequireFolder());
        field.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_TARGET_FOLDER_0));
        form.addComponent(field);
        m_targetSelect = field;
        m_copyMode.addItem(CmsContainerPageCopier.CopyMode.automatic);
        m_copyMode.setItemCaption(
            CmsContainerPageCopier.CopyMode.automatic,
            CmsVaadinUtils.getMessageText(Messages.GUI_COPYPAGE_MODE_AUTO_0));

        m_copyMode.addItem(CmsContainerPageCopier.CopyMode.smartCopyAndChangeLocale);
        m_copyMode.setItemCaption(
            CmsContainerPageCopier.CopyMode.smartCopyAndChangeLocale,
            CmsVaadinUtils.getMessageText(Messages.GUI_COPYPAGE_MODE_SMART_0));
        m_copyMode.addItem(CmsContainerPageCopier.CopyMode.reuse);
        m_copyMode.setItemCaption(
            CmsContainerPageCopier.CopyMode.reuse,
            CmsVaadinUtils.getMessageText(Messages.GUI_COPYPAGE_MODE_REUSE_0));
        m_copyMode.setValue(CmsContainerPageCopier.CopyMode.automatic);
        form.addComponent(m_copyMode);
        m_copyMode.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_COPYPAGE_COPY_MODE_0));
        return form;
    }

}
