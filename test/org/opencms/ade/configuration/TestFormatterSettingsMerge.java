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

package org.opencms.ade.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.opencms.xml.containerpage.I_CmsFormatterBean;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.content.CmsXmlContentProperty.Visibility;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Tests the merging of main and nested formatter settings in {@link CmsADEManager}.<p>
 */
public class TestFormatterSettingsMerge {

    /**
     * Creates a formatter stub that only provides a key and settings.<p>
     *
     * @param key the formatter key
     * @param settings the formatter settings
     *
     * @return the formatter stub
     */
    private static I_CmsFormatterBean formatter(String key, CmsXmlContentProperty... settings) {

        final Map<String, CmsXmlContentProperty> settingsMap = settingsMap(settings);
        return (I_CmsFormatterBean)Proxy.newProxyInstance(
            I_CmsFormatterBean.class.getClassLoader(),
            new Class<?>[] {I_CmsFormatterBean.class},
            (proxy, method, args) -> {
                switch (method.getName()) {
                    case "getKeyOrId":
                        return key;
                    case "getSettings":
                        return settingsMap;
                    default:
                        throw new UnsupportedOperationException(method.getName());
                }
            });
    }

    /**
     * Merges the given main settings with the nested formatters.<p>
     *
     * @param mainSettings the main formatter settings
     * @param nestedFormatters the nested formatters
     *
     * @return the merged settings
     */
    private static Map<String, CmsXmlContentProperty> merge(
        Map<String, CmsXmlContentProperty> mainSettings,
        I_CmsFormatterBean... nestedFormatters) {

        final List<I_CmsFormatterBean> nested = Arrays.asList(nestedFormatters);
        return CmsADEManager.mergeFormatterSettings(null, mainSettings, nested);
    }

    /**
     * Creates a setting definition.<p>
     *
     * @param name the setting name
     * @param visibility the visibility
     * @param widget the widget
     * @param defaultValue the default value
     *
     * @return the setting definition
     */
    private static CmsXmlContentProperty setting(
        String name,
        Visibility visibility,
        String widget,
        String defaultValue) {

        return new CmsXmlContentProperty(
            name,
            null,
            "string",
            visibility,
            widget,
            null,
            null,
            null,
            defaultValue,
            null,
            null,
            null,
            null,
            null,
            null);
    }

    /**
     * Creates a settings map keyed by setting name.<p>
     *
     * @param settings the settings
     *
     * @return the settings map
     */
    private static Map<String, CmsXmlContentProperty> settingsMap(CmsXmlContentProperty... settings) {

        final Map<String, CmsXmlContentProperty> result = new LinkedHashMap<>();
        for (CmsXmlContentProperty setting : settings) {
            result.put(setting.getName(), setting);
        }
        return result;
    }

    /**
     * Tests that a main formatter setting with visibility elementOverride does not affect nested individual settings.<p>
     */
    @Test
    public void testElementOverrideKeepsNestedIndividual() {

        final CmsXmlContentProperty mainSetting = setting("size", Visibility.elementOverride, "string", "small");
        final Map<String, CmsXmlContentProperty> result = merge(
            settingsMap(mainSetting),
            formatter("tile", setting("size", Visibility.parentIndividual, "string", "large")));
        assertSame(mainSetting, result.get("size"));
        assertEquals("tile_size", result.get("tile_size").getName());
        assertEquals("large", result.get("tile_size").getDefault());
    }

    /**
     * Tests that a main formatter setting with visibility elementOverride wins over nested shared settings.<p>
     */
    @Test
    public void testElementOverrideWinsOverNestedShared() {

        final CmsXmlContentProperty mainSetting = setting(
            "firstSquare",
            Visibility.elementOverride,
            "hidden",
            "allSmall");
        final Map<String, CmsXmlContentProperty> result = merge(
            settingsMap(mainSetting),
            formatter("tile1", setting("firstSquare", Visibility.parentShared, "select", "firstLarge")),
            formatter("tile2", setting("firstSquare", Visibility.elementAndParentShared, "select", "firstLarge")));
        assertEquals(1, result.size());
        assertSame(mainSetting, result.get("firstSquare"));
        assertEquals("allSmall", result.get("firstSquare").getDefault());
    }

    /**
     * Tests that a main formatter setting with visibility parentShared is not shown on the element.<p>
     */
    @Test
    public void testMainParentSharedSkipped() {

        final Map<String, CmsXmlContentProperty> result = merge(
            settingsMap(setting("onParent", Visibility.parentShared, "string", null)));
        assertFalse(result.containsKey("onParent"));
    }

    /**
     * Tests that a nested setting with visibility elementOverride does not appear on the parent.<p>
     */
    @Test
    public void testNestedElementOverrideNotOnParent() {

        final Map<String, CmsXmlContentProperty> result = merge(
            settingsMap(),
            formatter("tile", setting("own", Visibility.elementOverride, "string", null)));
        assertFalse(result.containsKey("own"));
        assertFalse(result.containsKey("tile_own"));
    }

    /**
     * Tests that a nested shared setting still replaces a main formatter setting with visibility element.<p>
     */
    @Test
    public void testNestedSharedReplacesMainElement() {

        final CmsXmlContentProperty nestedSetting = setting(
            "firstSquare",
            Visibility.parentShared,
            "select",
            "firstLarge");
        final Map<String, CmsXmlContentProperty> result = merge(
            settingsMap(setting("firstSquare", Visibility.element, "hidden", "allSmall")),
            formatter("tile", nestedSetting));
        assertSame(nestedSetting, result.get("firstSquare"));
    }
}
