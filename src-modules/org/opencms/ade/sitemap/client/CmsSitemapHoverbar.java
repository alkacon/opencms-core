/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/Attic/CmsSitemapHoverbar.java,v $
 * Date   : $Date: 2010/05/03 14:33:05 $
 * Version: $Revision: 1.10 $
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
import org.opencms.gwt.client.ui.A_CmsHoverHandler;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.util.CmsDomUtil;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * Sitemap tree item hover-bar.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.10 $ 
 * 
 * @since 8.0.0
 */
public class CmsSitemapHoverbar extends FlowPanel {

    /** The delete button. */
    private CmsPushButton m_deleteButton;

    /** The edit button. */
    private CmsPushButton m_editButton;

    /** The go-to button. */
    private CmsPushButton m_gotoButton;

    /** The move button. */
    private CmsPushButton m_moveButton;

    /** The new button. */
    private CmsPushButton m_newButton;

    /** The subsitemap button. */
    private CmsPushButton m_subsitemapButton;

    /** The parent sitemap button. */
    private CmsPushButton m_parentSitemapButton;

    /**
     * Constructor.<p>
     * 
     * @param handler the handler
     */
    public CmsSitemapHoverbar(final CmsSitemapHoverbarHandler handler) {

        setStyleName(I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().buttonPanel());
        ClickHandler clickHandler = new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                String sitePath = getSitePath();
                deattach();
                if (event.getSource().equals(getMoveButton())) {
                    handler.onMove(sitePath);
                } else if (event.getSource().equals(getNewButton())) {
                    handler.onNew(sitePath);
                } else if (event.getSource().equals(getDeleteButton())) {
                    CmsDomUtil.ensureMouseOut(getDeleteButton().getElement());
                    handler.onDelete(sitePath);
                } else if (event.getSource().equals(getEditButton())) {
                    handler.onEdit(sitePath);
                } else if (event.getSource().equals(getSubsitemapButton())) {
                    handler.onSubsitemap(sitePath);
                } else if (event.getSource().equals(getGotoButton())) {
                    handler.onGoto(sitePath);
                } else if (event.getSource().equals(getParentSitemapButton())) {
                    handler.onParent(sitePath);
                }
            }
        };

        // TODO: this should be a link so it can be opened in a new window or tab by the user
        m_gotoButton = new CmsPushButton();
        m_gotoButton.setImageClass(I_CmsImageBundle.INSTANCE.buttonCss().hoverbarGoto());
        m_gotoButton.setShowBorder(false);
        m_gotoButton.setTitle(Messages.get().key(Messages.GUI_HOVERBAR_GOTO_0));
        m_gotoButton.addClickHandler(clickHandler);
        add(m_gotoButton);

        m_subsitemapButton = new CmsPushButton();
        m_subsitemapButton.setImageClass(I_CmsImageBundle.INSTANCE.buttonCss().hoverbarSubsitemap());
        m_subsitemapButton.setShowBorder(false);
        m_subsitemapButton.setTitle(Messages.get().key(Messages.GUI_HOVERBAR_SUBSITEMAP_0));
        m_subsitemapButton.addClickHandler(clickHandler);
        add(m_subsitemapButton);

        m_deleteButton = new CmsPushButton();
        m_deleteButton.setImageClass(I_CmsImageBundle.INSTANCE.buttonCss().hoverbarDelete());
        m_deleteButton.setShowBorder(false);
        m_deleteButton.setTitle(Messages.get().key(Messages.GUI_HOVERBAR_DELETE_0));
        m_deleteButton.addClickHandler(clickHandler);
        add(m_deleteButton);

        m_editButton = new CmsPushButton();
        m_editButton.setImageClass(I_CmsImageBundle.INSTANCE.buttonCss().hoverbarEdit());
        m_editButton.setShowBorder(false);
        m_editButton.setTitle(Messages.get().key(Messages.GUI_HOVERBAR_EDIT_0));
        m_editButton.addClickHandler(clickHandler);
        add(m_editButton);

        m_newButton = new CmsPushButton();
        m_newButton.setImageClass(I_CmsImageBundle.INSTANCE.buttonCss().hoverbarNew());
        m_newButton.setShowBorder(false);
        m_newButton.setTitle(Messages.get().key(Messages.GUI_HOVERBAR_NEW_0));
        m_newButton.addClickHandler(clickHandler);
        add(m_newButton);

        m_moveButton = new CmsPushButton();
        m_moveButton.setImageClass(I_CmsImageBundle.INSTANCE.buttonCss().hoverbarMove());
        m_moveButton.setShowBorder(false);
        m_moveButton.setTitle(Messages.get().key(Messages.GUI_HOVERBAR_MOVE_0));
        m_moveButton.addClickHandler(clickHandler);
        add(m_moveButton);

        // TODO: this should be a link so it can be opened in a new window or tab by the user
        m_parentSitemapButton = new CmsPushButton();
        m_parentSitemapButton.setImageClass(I_CmsImageBundle.INSTANCE.buttonCss().hoverbarParent());
        m_parentSitemapButton.setShowBorder(false);
        m_parentSitemapButton.setTitle(Messages.get().key(Messages.GUI_HOVERBAR_PARENT_0));
        m_parentSitemapButton.addClickHandler(clickHandler);
        m_parentSitemapButton.setVisible(false);
        add(m_parentSitemapButton);
    }

    /**
     * Returns the delete Button.<p>
     *
     * @return the delete Button
     */
    public CmsPushButton getDeleteButton() {

        return m_deleteButton;
    }

    /**
     * Returns the edit Button.<p>
     *
     * @return the edit Button
     */
    public CmsPushButton getEditButton() {

        return m_editButton;
    }

    /**
     * Returns the goto Button.<p>
     *
     * @return the goto Button
     */
    public CmsPushButton getGotoButton() {

        return m_gotoButton;
    }

    /**
     * Returns the move Button.<p>
     *
     * @return the move Button
     */
    public CmsPushButton getMoveButton() {

        return m_moveButton;
    }

    /**
     * Returns the new Button.<p>
     *
     * @return the new Button
     */
    public CmsPushButton getNewButton() {

        return m_newButton;
    }

    /**
     * Returns the subsitemap Button.<p>
     *
     * @return the subsitemap Button
     */
    public CmsPushButton getSubsitemapButton() {

        return m_subsitemapButton;
    }

    /**
     * Returns the parent sitemap Button.<p>
     *
     * @return the parent sitemap Button
     */
    public CmsPushButton getParentSitemapButton() {

        return m_parentSitemapButton;
    }

    /**
     * Installs this hoverbar for the given item widget.<p>
     * 
     * @param controller the controller 
     * @param itemWidget the item widget to hover
     */
    public void installOn(final CmsSitemapController controller, final CmsListItemWidget itemWidget) {

        A_CmsHoverHandler handler = new A_CmsHoverHandler() {

            /**
             * @see org.opencms.gwt.client.ui.A_CmsHoverHandler#onHoverIn(com.google.gwt.event.dom.client.MouseOverEvent)
             */
            @SuppressWarnings("synthetic-access")
            @Override
            protected void onHoverIn(MouseOverEvent event) {

                itemWidget.getContentPanel().getElement().appendChild(getElement());
                if (controller.isRoot(getSitePath())) {
                    getDeleteButton().disable(Messages.get().key(Messages.GUI_DISABLED_ROOT_ITEM_0));
                    getMoveButton().disable(Messages.get().key(Messages.GUI_DISABLED_ROOT_ITEM_0));
                    getSubsitemapButton().disable(Messages.get().key(Messages.GUI_DISABLED_ROOT_ITEM_0));
                    if (CmsSitemapProvider.get().getParentSitemap() != null) {
                        getParentSitemapButton().setVisible(true);
                        getEditButton().disable(Messages.get().key(Messages.GUI_DISABLED_PARENT_SITEMAP_0));
                    }
                }
                onAttach();
            }

            /**
             * @see org.opencms.gwt.client.ui.A_CmsHoverHandler#onHoverOut(com.google.gwt.event.dom.client.MouseOutEvent)
             */
            @Override
            protected void onHoverOut(MouseOutEvent event) {

                deattach();
            }

        };
        itemWidget.addMouseOutHandler(handler);
        itemWidget.addMouseOverHandler(handler);
    }

    /**
     * Deattaches the hoverbar.<p>
     */
    protected void deattach() {

        getDeleteButton().enable();
        getMoveButton().enable();
        getSubsitemapButton().enable();
        getEditButton().enable();
        getParentSitemapButton().setVisible(false);
        getElement().removeFromParent();
        onDetach();
    }

    /**
     * Returns the site path of the hovered item.<p>
     * 
     * @return the site path of the hovered item
     */
    protected String getSitePath() {

        Element anscestor = CmsDomUtil.getAncestor(
            getElement(),
            I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().itemContent());
        return anscestor.getAttribute(CmsSitemapTreeItem.ATTR_SITEPATH);
    }
}
