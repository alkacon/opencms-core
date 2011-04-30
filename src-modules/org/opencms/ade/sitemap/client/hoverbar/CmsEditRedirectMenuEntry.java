/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/hoverbar/Attic/CmsEditRedirectMenuEntry.java,v $
 * Date   : $Date: 2011/04/30 15:28:20 $
 * Version: $Revision: 1.3 $
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

package org.opencms.ade.sitemap.client.hoverbar;

import org.opencms.ade.sitemap.client.Messages;
import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry.EntryType;
import org.opencms.gwt.client.ui.contenteditor.CmsContentEditorDialog;
import org.opencms.gwt.client.ui.contenteditor.I_CmsContentEditorHandler;
import org.opencms.gwt.client.ui.css.I_CmsImageBundle;

import com.google.gwt.user.client.Command;

/**
 * Sitemap context menu new entry.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.3 $
 * 
 * @since 8.0.0
 */
public class CmsEditRedirectMenuEntry extends A_CmsSitemapMenuEntry {

    /** The sitemap entry. */
    private CmsClientSitemapEntry m_entry;

    /**
     * Constructor.<p>
     * 
     * @param hoverbar the hoverbar 
     */
    public CmsEditRedirectMenuEntry(CmsSitemapHoverbar hoverbar) {

        super(hoverbar);
        setImageClass(I_CmsImageBundle.INSTANCE.contextMenuIcons().newElement());
        setLabel(Messages.get().key(Messages.GUI_HOVERBAR_EDIT_REDIRECT_0));
        setActive(true);
        setCommand(new Command() {

            /**
             * @see com.google.gwt.user.client.Command#execute()
             */
            public void execute() {

                CmsContentEditorDialog.get().openEditDialog(
                    getEntry().getId(),
                    getHoverbar().getSitePath(),
                    false,
                    new I_CmsContentEditorHandler() {

                        /**
                         * @see org.opencms.gwt.client.ui.contenteditor.I_CmsContentEditorHandler#onClose(java.lang.String, boolean)
                         */
                        public void onClose(String sitePath, boolean isNew) {

                            getHoverbar().getController().updateEntry(sitePath);

                        }
                    });
            }
        });
    }

    /**
     * @see org.opencms.ade.sitemap.client.hoverbar.A_CmsSitemapMenuEntry#onShow(org.opencms.ade.sitemap.client.hoverbar.CmsHoverbarShowEvent)
     */
    @Override
    public void onShow(CmsHoverbarShowEvent event) {

        String sitePath = getHoverbar().getSitePath();
        CmsSitemapController controller = getHoverbar().getController();
        m_entry = controller.getEntry(sitePath);
        boolean show = (m_entry != null) && (m_entry.getEntryType() == EntryType.redirect);
        setVisible(show);
    }

    /**
     * Returns the entry to edit.<p>
     * 
     * @return the entry
     */
    protected CmsClientSitemapEntry getEntry() {

        return m_entry;
    }
}
