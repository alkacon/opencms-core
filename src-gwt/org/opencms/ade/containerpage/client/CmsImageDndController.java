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

package org.opencms.ade.containerpage.client;

import org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel;
import org.opencms.ade.containerpage.shared.CmsContainerElement;
import org.opencms.ade.contenteditor.client.CmsContentEditor;
import org.opencms.ade.galleries.client.ui.CmsResultListItem;
import org.opencms.gwt.client.dnd.CmsDNDHandler;
import org.opencms.gwt.client.dnd.CmsDNDHandler.Orientation;
import org.opencms.gwt.client.dnd.I_CmsDNDController;
import org.opencms.gwt.client.dnd.I_CmsDraggable;
import org.opencms.gwt.client.dnd.I_CmsDropTarget;
import org.opencms.gwt.client.ui.CmsHighlightingBorder;
import org.opencms.gwt.client.ui.CmsHighlightingBorder.BorderColor;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsPositionBean;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * DND controller for drag/drop of images from the gallery menu.<p>
 *
 * Since the image drag and drop logic is mostly separate from the container drag and drop logic, the container page DND controller
 * delegates most of the drag and drop logic to this class if it detects that an image is being dragged.<p>
 */
public class CmsImageDndController implements I_CmsDNDController {

    /**
     * Drop target for an image.<p>
     */
    class ImageDropTarget implements I_CmsDropTarget {

        /** The border to display around the drop target. */
        private CmsHighlightingBorder m_border;

        /** The container element to which the drop target belongs (optional). */
        private Optional<CmsContainerPageElementPanel> m_containerElement;

        /** The element which acts as the drop target. */
        private Element m_element;

        /**
         * Creates a new instance.<p>
         *
         * @param element the element to use as the drop target
         *
         * @param containerElement the optional container element to which the drop target belongs
         */
        public ImageDropTarget(Element element, Optional<CmsContainerPageElementPanel> containerElement) {

            m_element = element;
            m_border = new CmsHighlightingBorder(CmsPositionBean.getBoundingClientRect(m_element), BorderColor.red);
            m_containerElement = containerElement;
            RootPanel.get().add(m_border);

        }

        /**
         * @see org.opencms.gwt.client.dnd.I_CmsDropTarget#checkPosition(int, int, org.opencms.gwt.client.dnd.CmsDNDHandler.Orientation)
         */
        public boolean checkPosition(int x, int y, Orientation orientation) {

            CmsPositionBean position = getPosition();
            boolean result = position.containsPoint(x, y);
            return result;
        }

        /**
         * Cleans up widgets which are part of this drop target and are no longer needed.<p>
         */
        public void cleanup() {

            m_border.removeFromParent();
        }

        /**
         * @see org.opencms.gwt.client.dnd.I_CmsDropTarget#getElement()
         */
        public Element getElement() {

            return m_element;
        }

        /**
         * @see org.opencms.gwt.client.dnd.I_CmsDropTarget#getPlaceholderIndex()
         */
        public int getPlaceholderIndex() {

            return 0;
        }

        /**
         * @see org.opencms.gwt.client.dnd.I_CmsDropTarget#insertPlaceholder(com.google.gwt.dom.client.Element, int, int, org.opencms.gwt.client.dnd.CmsDNDHandler.Orientation)
         */
        public void insertPlaceholder(Element placeholder, int x, int y, Orientation orientation) {

            // do nothing

        }

        /**
         * @see org.opencms.gwt.client.dnd.I_CmsDropTarget#onDrop(org.opencms.gwt.client.dnd.I_CmsDraggable)
         */
        public void onDrop(I_CmsDraggable draggable) {

            if (draggable instanceof CmsResultListItem) {
                CmsResultListItem result = (CmsResultListItem)draggable;
                String path = result.getResult().getPath();
                saveImage(path);
            }
        }

        /**
         * @see org.opencms.gwt.client.dnd.I_CmsDropTarget#removePlaceholder()
         */
        public void removePlaceholder() {

            // do nothing
        }

        /**
         * @see org.opencms.gwt.client.dnd.I_CmsDropTarget#repositionPlaceholder(int, int, org.opencms.gwt.client.dnd.CmsDNDHandler.Orientation)
         */
        public void repositionPlaceholder(int x, int y, Orientation orientation) {

            // do nothing
        }

        /**
         * Saves the image with the given path to the content to which this drop zone belongs.<p>
         *
         * @param path the path of the image to save
         */
        @SuppressWarnings("synthetic-access")
        public void saveImage(final String path) {

            String attribute = m_element.getAttribute(ATTR_DATA_IMAGEDND);
            if (attribute != null) {
                List<String> tokens = CmsStringUtil.splitAsList(attribute, "|");
                if (tokens.size() == 3) {
                    final String contentId = tokens.get(0);
                    final String contentPath = tokens.get(1);
                    final String locale = tokens.get(2);
                    if ((new CmsUUID(contentId)).isNullUUID()) {
                        if (m_containerElement.isPresent() && m_containerElement.get().isNew()) {
                            m_pageController.createNewElement(
                                m_containerElement.get(),
                                new AsyncCallback<CmsContainerElement>() {

                                    public void onFailure(Throwable caught) {

                                        // do nothing
                                    }

                                    public void onSuccess(final CmsContainerElement result) {

                                        m_containerElement.get().setNewType(null);
                                        m_containerElement.get().setId(result.getClientId());
                                        m_containerElement.get().setSitePath(result.getSitePath());

                                        m_pageController.setPageChanged(new Runnable() {

                                            public void run() {

                                                String serverId = CmsContainerpageController.getServerId(
                                                    result.getClientId());
                                                saveAndReloadElement(serverId, contentPath, locale, path);
                                            }
                                        });
                                    }
                                });
                        } else {
                            return;
                        }
                    } else {
                        saveAndReloadElement(contentId, contentPath, locale, path);
                    }
                }

            }

        }

        /**
         * Sets the border color.<p>
         *
         * @param color the border color
         */
        public void setBorderColor(BorderColor color) {

            m_border.setColor(color);
        }

        /**
         * Saves the value to the content and reloads the corresponding container element.<p>
         *
         * @param contentId the content structure id
         * @param contentPath the content xpath
         * @param locale the locale
         * @param value the value to save
         */
        protected void saveAndReloadElement(String contentId, String contentPath, String locale, String value) {

            CmsContentEditor.getInstance().saveValue(
                contentId,
                contentPath,
                locale,
                value,
                new AsyncCallback<String>() {

                    public void onFailure(Throwable caught) {

                        // TODO Auto-generated method stub

                    }

                    @SuppressWarnings("synthetic-access")
                    public void onSuccess(String result) {

                        if (m_containerElement.isPresent()) {
                            String clientId = m_containerElement.get().getId();
                            m_pageController.reloadElements(new String[] {clientId});
                        } else {
                            Window.Location.reload();
                        }
                    }
                });
        }

        /**
         * Gets the position.<p>
         *
         * @return the position
         */
        CmsPositionBean getPosition() {

            return CmsPositionBean.getBoundingClientRect(m_element, false);
        }
    }

    /** The attribute used to mark image drop zones. */
    public static final String ATTR_DATA_IMAGEDND = "data-imagednd";

    /** The current list of image drop targets. */
    private List<ImageDropTarget> m_imageDropTargets = Lists.newArrayList();

    /** The container page controller. */
    private CmsContainerpageController m_pageController;

    /**
     * Creates a new instance.<p>
     *
     * @param controller the container page controller
     */
    public CmsImageDndController(CmsContainerpageController controller) {

        m_pageController = controller;
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDNDController#onAnimationStart(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget, org.opencms.gwt.client.dnd.CmsDNDHandler)
     */
    public void onAnimationStart(I_CmsDraggable draggable, I_CmsDropTarget target, CmsDNDHandler handler) {

        // do nothing
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDNDController#onBeforeDrop(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget, org.opencms.gwt.client.dnd.CmsDNDHandler)
     */
    public boolean onBeforeDrop(I_CmsDraggable draggable, I_CmsDropTarget target, CmsDNDHandler handler) {

        return true;
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDNDController#onDragCancel(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget, org.opencms.gwt.client.dnd.CmsDNDHandler)
     */
    public void onDragCancel(I_CmsDraggable draggable, I_CmsDropTarget target, CmsDNDHandler handler) {

        cleanupTargets();
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDNDController#onDragStart(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget, org.opencms.gwt.client.dnd.CmsDNDHandler)
     */
    public boolean onDragStart(I_CmsDraggable draggable, I_CmsDropTarget target, CmsDNDHandler handler) {

        handler.clearTargets();
        m_imageDropTargets.clear();
        m_imageDropTargets.addAll(findImageTargets());
        CmsContainerpageController.get().getHandler().hideMenu();
        for (ImageDropTarget target1 : m_imageDropTargets) {
            handler.addTarget(target1);
        }
        Optional<int[]> offsetDelta = handler.getDraggable().getCursorOffsetDelta();
        if (offsetDelta.isPresent()) {
            handler.setCursorOffsetX(handler.getCursorOffsetX() + offsetDelta.get()[0]);
            handler.setCursorOffsetY(handler.getCursorOffsetY() + offsetDelta.get()[1]);
        }
        return true;
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDNDController#onDrop(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget, org.opencms.gwt.client.dnd.CmsDNDHandler)
     */
    public void onDrop(I_CmsDraggable draggable, I_CmsDropTarget target, CmsDNDHandler handler) {

        // actual drop logic is handled by the ImageDropTarget class, we only do some cleanup here
        cleanupTargets();
        handler.clearTargets();
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDNDController#onPositionedPlaceholder(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget, org.opencms.gwt.client.dnd.CmsDNDHandler)
     */
    public void onPositionedPlaceholder(I_CmsDraggable draggable, I_CmsDropTarget target, CmsDNDHandler handler) {

        // do nothing

    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDNDController#onTargetEnter(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget, org.opencms.gwt.client.dnd.CmsDNDHandler)
     */
    public boolean onTargetEnter(I_CmsDraggable draggable, I_CmsDropTarget target, CmsDNDHandler handler) {

        if (target instanceof ImageDropTarget) {
            ImageDropTarget imageTarget = (ImageDropTarget)target;
            imageTarget.setBorderColor(BorderColor.blue);
        }
        return true;
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDNDController#onTargetLeave(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget, org.opencms.gwt.client.dnd.CmsDNDHandler)
     */
    public void onTargetLeave(I_CmsDraggable draggable, I_CmsDropTarget target, CmsDNDHandler handler) {

        if (target instanceof ImageDropTarget) {
            ImageDropTarget imageTarget = (ImageDropTarget)target;
            imageTarget.setBorderColor(BorderColor.red);

        }
    }

    /**
     * Collects the valid drop targets for images from the page and initializes them.<p>
     *
     * @return the list of drop targets
     */
    protected List<ImageDropTarget> findImageTargets() {

        List<ImageDropTarget> result = Lists.newArrayList();
        List<CmsContainerPageElementPanel> modelGroups = CmsContainerpageController.get().getModelGroups();
        elementLoop: for (Element element : CmsDomUtil.nodeListToList(
            CmsDomUtil.querySelectorAll("*[" + ATTR_DATA_IMAGEDND + "]", RootPanel.getBodyElement()))) {
            Optional<CmsContainerPageElementPanel> optElemWidget = CmsContainerpageController.get().getContainerElementWidgetForElement(
                element);
            if (optElemWidget.isPresent()) {
                CmsContainerPageElementPanel elemWidget = optElemWidget.get();
                if (!elemWidget.hasViewPermission()) {
                    continue elementLoop;
                }
                String noEditReason = elemWidget.getNoEditReason();
                if ((noEditReason != null) && !elemWidget.hasWritePermission()) {
                    continue elementLoop;
                }
            }
            if (!CmsContainerpageController.get().getData().isModelGroup()) {
                // Don't make images in model groups into drop targets, except when we are in model group editing mode
                for (CmsContainerPageElementPanel modelGroup : modelGroups) {
                    if (modelGroup.getElement().isOrHasChild(element)) {
                        continue elementLoop;
                    }
                }
            }
            ImageDropTarget target = new ImageDropTarget(element, optElemWidget);
            result.add(target);
        }
        return result;

    }

    /**
     * Cleans up all the drop targets.<p>
     */
    private void cleanupTargets() {

        for (ImageDropTarget target : m_imageDropTargets) {
            target.cleanup();
        }
        m_imageDropTargets.clear();
    }

}
