/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.xml.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.types.CmsXmlStringValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.jupiter.api.Test;

/** Tests configuration location navigation and path construction costs. */
public class TestCmsXmlContentValueLocation {

    /** A real XML value which counts path reconstruction. */
    private static class CountingValue extends CmsXmlStringValue {

        /** Number of path requests. */
        private int m_pathCalls;

        /**
         * Creates a value backed by a DOM element.
         *
         * @param document the document
         * @param element the element
         * @param type the schema type
         */
        CountingValue(TestContent document, Element element, CmsXmlStringValue type) {

            super(document, element, Locale.ENGLISH, type);
        }

        @Override
        public String getPath() {

            m_pathCalls++;
            return super.getPath();
        }
    }

    /** A small document using the real bookmark lookup and choice handling. */
    private static class TestContent extends CmsXmlContent {

        /** The locale element. */
        private final Element m_locale;

        /** Values in registration order. */
        private final List<CountingValue> m_values = new ArrayList<>();

        /** Creates an empty configuration document. */
        TestContent() {

            m_document = DocumentHelper.createDocument();
            m_locale = m_document.addElement("Configs").addElement("Config").addAttribute("language", "en");
            m_elementLocales = new HashMap<>();
            m_elementNames = new HashMap<>();
        }

        /**
         * Adds a value to the DOM and bookmarks.
         *
         * @param parent the parent element
         * @param name the element name
         * @param choice whether the value belongs to a repeating choice
         * @return the value
         */
        CountingValue add(Element parent, String name, boolean choice) {

            CmsXmlContentDefinition definition = new CmsXmlContentDefinition("Config", "test://config.xsd") {

                @Override
                public int getChoiceMaxOccurs() {

                    return choice ? Integer.MAX_VALUE : 0;
                }
            };
            CmsXmlStringValue type = new CmsXmlStringValue(name, "0", "unbounded");
            type.setContentDefinition(definition);
            CountingValue value = new CountingValue(this, parent.addElement(name), type);
            m_values.add(value);
            addBookmark(value.getPath(), Locale.ENGLISH, true, value);
            value.m_pathCalls = 0;
            return value;
        }

        /**
         * Rebuilds bookmarks after edits, optionally in reverse order to exercise choice sorting.
         *
         * @param reverse whether to reverse bookmark insertion order
         */
        void reindex(boolean reverse) {

            clearBookmarks();
            List<CountingValue> values = new ArrayList<>(m_values);
            if (reverse) {
                Collections.reverse(values);
            }
            for (CountingValue value : values) {
                addBookmark(value.getPath(), Locale.ENGLISH, true, value);
                value.m_pathCalls = 0;
            }
        }
    }

    /** Choice lists retain numeric ordering and reuse each path for sorting and child reads. */
    @Test
    public void testChoiceFilteringAndNumericOrder() {

        for (boolean cachePaths : new boolean[] {false, true}) {
            TestContent content = new TestContent();
            CountingValue settings = content.add(content.m_locale, "Settings", false);
            List<CountingValue> expected = new ArrayList<>();
            for (int i = 0; i < 12; i++) {
                CountingValue setting = content.add(settings.getElement(), "Setting", true);
                expected.add(setting);
                content.add(setting.getElement(), "Name", false);
                content.add(settings.getElement(), "Other", true);
            }
            content.reindex(true);
            I_CmsXmlContentValueLocation parent = new CmsXmlContentRootLocation(
                content,
                Locale.ENGLISH,
                cachePaths).getSubValue("Settings");
            List<I_CmsXmlContentValueLocation> locations = parent.getSubValues("Setting");
            assertEquals(12, locations.size());
            for (int i = 0; i < locations.size(); i++) {
                assertSame(expected.get(i), locations.get(i).getValue());
                assertEquals(1, expected.get(i).m_pathCalls);
                assertEquals("Name", locations.get(i).getSubValue("Name").getValue().getName());
                assertEquals(cachePaths ? 1 : 2, expected.get(i).m_pathCalls);
            }
            assertEquals(1, settings.m_pathCalls);
            assertEquals(Collections.emptyList(), parent.getSubValues("Missing"));
        }
    }

    /** Repeated field reads reuse paths through both root navigation methods. */
    @Test
    public void testReadOnlyPathsAreLazyAndPropagate() {

        for (boolean useRootList : new boolean[] {false, true}) {
            TestContent content = new TestContent();
            CountingValue setting = content.add(content.m_locale, "Setting", false);
            CountingValue nested = content.add(setting.getElement(), "Nested", false);
            CountingValue name = content.add(nested.getElement(), "Name", false);
            CountingValue defaultValue = content.add(nested.getElement(), "Default", false);
            CmsXmlContentRootLocation root = new CmsXmlContentRootLocation(content, Locale.ENGLISH, true);
            I_CmsXmlContentValueLocation location = useRootList
            ? root.getSubValues("Setting").get(0)
            : root.getSubValue("Setting");
            assertEquals(0, setting.m_pathCalls);
            I_CmsXmlContentValueLocation nestedLocation = location.getSubValue("Nested");
            assertNull(location.getSubValue("Missing"));
            assertSame(name, nestedLocation.getSubValue("Name").getValue());
            assertSame(defaultValue, nestedLocation.getSubValue("Default").getValue());
            assertEquals(1, setting.m_pathCalls);
            assertEquals(1, nested.m_pathCalls);
            assertEquals(0, name.m_pathCalls);
        }
    }

    /** Existing constructors continue to resolve the current path after sibling reordering. */
    @Test
    public void testUncachedLocationsFollowMovedElements() {

        TestContent content = new TestContent();
        CountingValue first = content.add(content.m_locale, "Setting", false);
        CountingValue firstName = content.add(first.getElement(), "Name", false);
        CountingValue second = content.add(content.m_locale, "Setting", false);
        content.add(second.getElement(), "Name", false);
        CmsXmlContentRootLocation root = new CmsXmlContentRootLocation(content, Locale.ENGLISH);
        List<I_CmsXmlContentValueLocation> locations = new ArrayList<>();
        locations.add(root.getSubValue("Setting"));
        locations.add(root.getSubValues("Setting").get(0));
        locations.add(new CmsXmlContentValueLocation(first));
        for (I_CmsXmlContentValueLocation location : locations) {
            assertSame(firstName, location.getSubValue("Name").getValue());
        }
        first.getElement().detach();
        content.m_locale.add(first.getElement());
        content.reindex(false);
        for (I_CmsXmlContentValueLocation location : locations) {
            assertSame(firstName, location.getSubValue("Name").getValue());
            assertSame(firstName, location.getSubValues("Name").get(0).getValue());
        }
    }
}
