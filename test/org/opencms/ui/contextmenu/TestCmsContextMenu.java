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

package org.opencms.ui.contextmenu;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.util.CmsTreeNode;
import org.opencms.workplace.explorer.menu.CmsMenuItemVisibilityMode;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Tests context menu construction.<p>
 */
public class TestCmsContextMenu extends OpenCmsTestCase {

    /**
     * Tests building the context menu tree.<p>
     */
    public void testBuildTree() {

        List<I_CmsContextMenuItem> items = Arrays.asList(
            entry("foo", null, 0, 0),
            entry("bar", null, 0, 1),
            entry("baz", "foo", 0, 2),
            entry("qux", "foo", 0, 3));

        CmsContextMenuTreeBuilder builder = new CmsContextMenuTreeBuilder(null);
        CmsTreeNode<I_CmsContextMenuItem> root = builder.buildTree(items);
        List<CmsTreeNode<I_CmsContextMenuItem>> topLevel = root.getChildren();
        assertEquals(2, topLevel.size());
        assertEquals("foo", topLevel.get(0).getData().getId());
        assertEquals("bar", topLevel.get(1).getData().getId());
        assertEquals(2, topLevel.get(0).getChildren().size());
        assertEquals(0, topLevel.get(1).getChildren().size());
        assertEquals("baz", topLevel.get(0).getChildren().get(0).getData().getId());
        assertEquals("qux", topLevel.get(0).getChildren().get(1).getData().getId());
    }

    /**
     * Tests that context menu items with higher priorities override those  with lower ones.<p>
     */
    public void testOverride() {

        List<I_CmsContextMenuItem> items = Arrays.asList(
            entry("foo", null, 0, 0),
            entry("foo", null, 100, 100),
            entry("foo", null, 1, 1));
        CmsContextMenuTreeBuilder builder = new CmsContextMenuTreeBuilder(null);
        CmsTreeNode<I_CmsContextMenuItem> root = builder.buildTree(items);
        List<CmsTreeNode<I_CmsContextMenuItem>> topLevel = root.getChildren();
        assertEquals(1, topLevel.size());
        assertEquals(100, topLevel.get(0).getData().getPriority());

    }

    /**
     * Tests that items cyclically referring to each other via the parent id are not included in the context menu tree.<p>
     */
    public void testRemoveCycles() {

        List<I_CmsContextMenuItem> items = Arrays.asList(
            entry("foo", null, 0, 0),
            entry("bar", "baz", 0, 1),
            entry("baz", "bar", 0, 2));

        CmsContextMenuTreeBuilder builder = new CmsContextMenuTreeBuilder(null);
        CmsTreeNode<I_CmsContextMenuItem> root = builder.buildTree(items);
        List<CmsTreeNode<I_CmsContextMenuItem>> topLevel = root.getChildren();
        assertEquals(1, topLevel.size());
        assertEquals("foo", topLevel.get(0).getData().getId());

    }

    /**
     * Helper method to construct a context menu item.<p>
     *
     * @param id the id
     * @param parentId the parent id
     * @param priority the priority
     * @param order the order
     *
     * @return the context menu item
     */
    private I_CmsContextMenuItem entry(final String id, final String parentId, final int priority, final float order) {

        return new I_CmsContextMenuItem() {

            public void executeAction(I_CmsDialogContext context) {

                // TODO Auto-generated method stub

            }

            public String getId() {

                return id;
            }

            public float getOrder() {

                return order;
            }

            public String getParentId() {

                return parentId;
            }

            public int getPriority() {

                return priority;
            }

            public String getTitle(Locale locale) {

                return id;
            }

            public CmsMenuItemVisibilityMode getVisibility(CmsObject cms, List<CmsResource> resources) {

                return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
            }

            public CmsMenuItemVisibilityMode getVisibility(I_CmsDialogContext context) {

                return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
            }

            public boolean isLeafItem() {

                return false;
            }

        };
    }

}
