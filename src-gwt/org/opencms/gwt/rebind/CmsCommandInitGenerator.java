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

package org.opencms.gwt.rebind;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;

/**
 * Context menu command init generator.<p>
 *
 * @since version 8.0.1
 */
public class CmsCommandInitGenerator extends Generator {

    /** The name of the class to generate. */
    private static final String CLASS_NAME = "CmsContextMenuCommandInitializer";

    /** The name of the interface passed into GWT.create(). */
    private static final String INIT_INTERFACE_NAME = "org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuCommandInitializer";

    /** The name of the marker interface for the generator. */
    private static final String MARKER_INTERFACE_NAME = "org.opencms.gwt.client.ui.contextmenu.I_CmsHasContextMenuCommand";

    /** The name of the context menu command interface. */
    private static final String COMMAND_INTERFACE = "org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuCommand";

    /** The name of the get context menu command method. */
    private static final String GET_COMMAND_METHOD = "getContextMenuCommand";

    /** The package of the class to generate. */
    private static final String PACKAGE_NAME = "org.opencms.gwt.client";

    /**
     * @see com.google.gwt.core.ext.Generator#generate(com.google.gwt.core.ext.TreeLogger, com.google.gwt.core.ext.GeneratorContext, java.lang.String)
     */
    @Override
    public String generate(TreeLogger logger, GeneratorContext context, String typeName)
    throws UnableToCompleteException {

        TypeOracle oracle = context.getTypeOracle();
        JClassType initClass = oracle.findType(MARKER_INTERFACE_NAME);
        List<JClassType> initTypes = new ArrayList<JClassType>();
        for (JClassType subtype : initClass.getSubtypes()) {
            try {
                JMethod method = subtype.getMethod(GET_COMMAND_METHOD, new JType[] {});
                if (!method.isStatic()) {
                    throw new NotFoundException();
                }
                initTypes.add(subtype);
            } catch (NotFoundException e) {
                logger.log(
                    TreeLogger.ERROR,
                    "Could not find " + GET_COMMAND_METHOD + "() method in class " + subtype.getQualifiedSourceName());
                throw new UnableToCompleteException();
            }
        }
        generateClass(logger, context, initTypes);
        return PACKAGE_NAME + "." + CLASS_NAME;
    }

    /**
     * This method generates the source code for the class initializer class.<p>
     *
     * @param logger the logger to be used
     * @param context the generator context
     * @param subclasses the classes for which the generated code should the initClass() method
     */
    public void generateClass(TreeLogger logger, GeneratorContext context, List<JClassType> subclasses) {

        PrintWriter printWriter = context.tryCreate(logger, PACKAGE_NAME, CLASS_NAME);
        if (printWriter == null) {
            return;
        }
        ClassSourceFileComposerFactory composer = new ClassSourceFileComposerFactory(PACKAGE_NAME, CLASS_NAME);
        composer.addImplementedInterface(INIT_INTERFACE_NAME);
        SourceWriter sourceWriter = composer.createSourceWriter(context, printWriter);
        sourceWriter.println("public java.util.Map<String, " + COMMAND_INTERFACE + "> initCommands() {");
        sourceWriter.indent();
        sourceWriter.println(
            "java.util.Map<String, "
                + COMMAND_INTERFACE
                + "> result=new java.util.HashMap<String, "
                + COMMAND_INTERFACE
                + ">();");
        for (JClassType type : subclasses) {
            sourceWriter.println(
                "result.put(\""
                    + type.getQualifiedSourceName()
                    + "\","
                    + type.getQualifiedSourceName()
                    + "."
                    + GET_COMMAND_METHOD
                    + "());");
        }
        sourceWriter.println("return result;");
        sourceWriter.outdent();
        sourceWriter.println("}");
        sourceWriter.outdent();
        sourceWriter.println("}");
        context.commit(logger, printWriter);
    }
}
