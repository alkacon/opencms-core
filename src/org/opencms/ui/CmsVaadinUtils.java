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

package org.opencms.ui;

import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsMessages;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsMacroResolver;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.vaadin.ui.Component;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.declarative.Design;

/**
 * Vaadin utility functions.<p>
 * 
 */
public final class CmsVaadinUtils {

    /**
     * Hidden default constructor for utility class.<p>
     */
    private CmsVaadinUtils() {

    }

    public static String getDefaultDesignPath(Component component) {

        String className = component.getClass().getName();
        String designPath = className.replace(".", "/") + ".html";
        return designPath;
    }

    /** 
     * Reads the declarative design for a component and localizes it using a messages object.<p>
     * 
     * The design will need to be located in the same directory as the component's class and have '.html' as a file extension.
     *       
     * @param component the component for which to read the design  
     * @param messages the message bundle to use for localization 
     */
    @SuppressWarnings("resource")
    public static void readAndLocalizeDesign(Component component, CmsMessages messages, Map<String, String> macros) {

        String designPath = getDefaultDesignPath(component);
        readAndLocalizeDesign(component, designPath, messages, macros);
    }

    /**
     * Visits all descendants of a given component (including the component itself) and applies a predicate
     * to each.<p>
     * 
     * If the predicate returns false for a component, no further descendants will be processed.<p>
     * 
     * @param component the component 
     * @param handler the predicate 
     */
    public static void visitDescendants(Component component, Predicate<Component> handler) {

        List<Component> stack = Lists.newArrayList();
        stack.add(component);
        while (!stack.isEmpty()) {
            Component currentComponent = stack.get(stack.size() - 1);
            stack.remove(stack.size() - 1);
            if (!handler.apply(currentComponent)) {
                return;
            }
            if (currentComponent instanceof HasComponents) {
                List<Component> children = Lists.newArrayList((HasComponents)currentComponent);
                Collections.reverse(children);
                stack.addAll(children);
            }
        }
    }

    protected static void readAndLocalizeDesign(
        Component component,
        String designPath,
        CmsMessages messages,
        Map<String, String> macros) {

        InputStream designStream = CmsVaadinUtils.class.getClassLoader().getResourceAsStream(designPath);
        if (designStream == null) {
            throw new IllegalArgumentException("Design not found: " + designPath);
        }
        try {
            byte[] designBytes = CmsFileUtil.readFully(designStream);
            final String encoding = "UTF-8";
            String design = new String(designBytes, encoding);
            CmsMacroResolver resolver = new CmsMacroResolver() {

                @Override
                public String getMacroValue(String macro) {

                    String result = super.getMacroValue(macro);
                    // The macro may contain quotes or angle brackets, so we need to escape the values for insertion into the design file 
                    return CmsEncoder.escapeXml(result);

                }
            };
            for (Map.Entry<String, String> entry : macros.entrySet()) {
                resolver.addMacro(entry.getKey(), entry.getValue());
            }
            resolver.setMessages(messages);
            String resolvedDesign = resolver.resolveMacros(design);
            Design.read(new ByteArrayInputStream(resolvedDesign.getBytes(encoding)), component);
        } catch (IOException e) {
            throw new RuntimeException("Could not read design: " + designPath, e);
        }
    }

}
