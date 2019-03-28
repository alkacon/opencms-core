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

package org.opencms.setup.updater.dialogs;

import org.opencms.setup.CmsUpdateBean;
import org.opencms.setup.CmsUpdateUI;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;

import java.io.FileInputStream;
import java.io.InputStream;

import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;

/**
 * The abstract class for the update dialogs.<p>
 */
public abstract class A_CmsUpdateDialog extends CmsBasicDialog {

    /**vaadin serial id.*/
    private static final long serialVersionUID = 1L;

    /**Ui. */
    protected CmsUpdateUI m_ui;

    /**Continue button. */
    Button m_ok;

    /**Back button. */
    Button m_back;

    /**
     * Returns the back button.<p>
     *
     * @return Button
     */
    public Button getBackButton() {

        return new Button("Back");
    }

    /**
     * Returns the continue button.<p>
     *
     * @return Button
     */
    public Button getOkButton() {

        return new Button("Continue");
    }

    /**
     * Creates a new HTML-formatted label with the given content.
     *
     * @param html the label content
     * @return Label
     */
    public Label htmlLabel(String html) {

        Label label = new Label();
        label.setContentMode(ContentMode.HTML);
        label.setValue(html);
        return label;

    }

    /**
     * Inits the dialog.<p>
     * (The constructor is empty)
     *
     * @param ui UI
     * @return true if dialog should be displayed
     */
    public abstract boolean init(CmsUpdateUI ui);

    /**
     * Init called from implementations of this class.<p>
     *
     * @param ui ui
     * @param hasPrev has preview dialog
     * @param hasNext has next dialog
     */
    public void init(CmsUpdateUI ui, boolean hasPrev, boolean hasNext) {

        m_ui = ui;

        if (hasPrev) {
            m_back = getBackButton();
            m_back.addClickListener(new ClickListener() {

                private static final long serialVersionUID = 1L;

                public void buttonClick(ClickEvent event) {

                    ui.displayDialog(getPreviousDialog());
                }

            });
            addButton(m_back, true);
        }

        if (hasNext) {
            m_ok = getOkButton();
            m_ok.addClickListener(new ClickListener() {

                private static final long serialVersionUID = 1L;

                public void buttonClick(ClickEvent event) {

                    if (submitDialog()) {
                        ui.displayDialog(getNextDialog());
                    }
                }

            });
            addButton(m_ok, true);
        }
    }

    /**
     * Reads an HTML snipped with the given name.
     * @param name name of file
     *
     * @return the HTML data
     */
    public String readSnippet(String name) {

        String path = CmsStringUtil.joinPaths(
            m_ui.getUpdateBean().getWebAppRfsPath(),
            CmsUpdateBean.FOLDER_UPDATE,
            "html",
            name);
        try (InputStream stream = new FileInputStream(path)) {
            byte[] data = CmsFileUtil.readFully(stream, false);
            String result = new String(data, "UTF-8");
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * En / Disables the continue button.<p>
     *
     * @param enable boolean
     */
    protected void enableOK(boolean enable) {

        m_ok.setEnabled(enable);
    }

    /**
     * Submit method.<p>
     *
     * @return true if next dialog should be loaded
     */
    protected boolean submitDialog() {

        return true;
    }

    /**
     * Returns next Dialog (not initialized).<p>
     *
     * @return A_CmsUpdateDialog
     */
    abstract A_CmsUpdateDialog getNextDialog();

    /**
     * Returns previous Dialog (not initialized).<p>
     *
     * @return A_CmsUpdateDialog
     */
    abstract A_CmsUpdateDialog getPreviousDialog();

}
