/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/Attic/CmsSitemapHoverbar.java,v $
 * Date   : $Date: 2010/04/21 14:29:20 $
 * Version: $Revision: 1.2 $
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

package org.opencms.ade.sitemap.client;

import org.opencms.ade.sitemap.client.ui.css.I_CmsImageBundle;
import org.opencms.gwt.client.ui.CmsImageButton;
import org.opencms.gwt.client.ui.CmsToolbar;

/**
 * Sitemap toolbar.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 8.0.0
 */
public class CmsSitemapHoverbar extends CmsToolbar {

    /** The delete button. */
    private CmsImageButton m_deleteButton;

    /** The edit button. */
    private CmsImageButton m_editButton;

    /** The go-to button. */
    private CmsImageButton m_gotoButton;

    /** The move button. */
    private CmsImageButton m_moveButton;

    /** The new button. */
    private CmsImageButton m_newButton;

    /** The subsitemap button. */
    private CmsImageButton m_subsitemapButton;

    /**
     * Constructor.<p>
     * 
     * @param handler the handler
     */
    public CmsSitemapHoverbar(CmsSitemapHoverbarHandler handler) {

        handler.setHoverbar(this);

        m_moveButton = new CmsImageButton(I_CmsImageBundle.INSTANCE.buttonCss().hoverbarMove(), false);
        m_moveButton.setTitle(Messages.get().key(Messages.GUI_HOVERBAR_MOVE_0));
        m_moveButton.addClickHandler(handler);

        m_newButton = new CmsImageButton(I_CmsImageBundle.INSTANCE.buttonCss().hoverbarNew(), false);
        m_newButton.setTitle(Messages.get().key(Messages.GUI_HOVERBAR_NEW_0));
        m_newButton.addClickHandler(handler);

        m_editButton = new CmsImageButton(I_CmsImageBundle.INSTANCE.buttonCss().hoverbarEdit(), false);
        m_editButton.setTitle(Messages.get().key(Messages.GUI_HOVERBAR_EDIT_0));
        m_editButton.addClickHandler(handler);

        m_deleteButton = new CmsImageButton(I_CmsImageBundle.INSTANCE.buttonCss().hoverbarDelete(), false);
        m_deleteButton.setTitle(Messages.get().key(Messages.GUI_HOVERBAR_DELETE_0));
        m_deleteButton.addClickHandler(handler);

        m_subsitemapButton = new CmsImageButton(I_CmsImageBundle.INSTANCE.buttonCss().hoverbarSubsitemap(), false);
        m_subsitemapButton.setTitle(Messages.get().key(Messages.GUI_HOVERBAR_SUBSITEMAP_0));
        m_subsitemapButton.addClickHandler(handler);

        // TODO: this should be a link so it can be opened in a new window or tab by the user
        m_gotoButton = new CmsImageButton(I_CmsImageBundle.INSTANCE.buttonCss().hoverbarGoto(), false);
        m_gotoButton.setTitle(Messages.get().key(Messages.GUI_HOVERBAR_GOTO_0));
        m_gotoButton.addClickHandler(handler);
    }

    /**
     * Returns the delete Button.<p>
     *
     * @return the delete Button
     */
    public CmsImageButton getDeleteButton() {

        return m_deleteButton;
    }

    /**
     * Returns the edit Button.<p>
     *
     * @return the edit Button
     */
    public CmsImageButton getEditButton() {

        return m_editButton;
    }

    /**
     * Returns the goto Button.<p>
     *
     * @return the goto Button
     */
    public CmsImageButton getGotoButton() {

        return m_gotoButton;
    }

    /**
     * Returns the move Button.<p>
     *
     * @return the move Button
     */
    public CmsImageButton getMoveButton() {

        return m_moveButton;
    }

    /**
     * Returns the new Button.<p>
     *
     * @return the new Button
     */
    public CmsImageButton getNewButton() {

        return m_newButton;
    }

    /**
     * Returns the subsitemap Button.<p>
     *
     * @return the subsitemap Button
     */
    public CmsImageButton getSubsitemapButton() {

        return m_subsitemapButton;
    }

}
