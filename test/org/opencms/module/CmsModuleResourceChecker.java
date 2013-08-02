/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.module;

import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsFileUtil.FileWalkState;
import org.opencms.util.CmsStringUtil;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.Closure;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * Helper class to list files in modules which are missing from the modules' manifests.<p>
 */
public class CmsModuleResourceChecker {

    /** Default module path . */
    public static final String DEFAULT_MODULE_PATH = "C:/dev/workspace/opencms/modules";

    /**
     * Main method.<p>
     * @param args first argument should be the path of the modules folder
     * 
     * @throws Exception if something goes wrong 
     */
    public static void main(String[] args) throws Exception {

        String path = args.length > 0 ? args[0] : DEFAULT_MODULE_PATH;
        (new CmsModuleResourceChecker()).checkAllModules(path);

    }

    /**
     * Checks all modules for missing manifest entries.<p>
     * 
     * @param modulesPath the path of the module folder 
     * @throws Exception if something goes wrong 
     */
    public void checkAllModules(String modulesPath) throws Exception {

        File modules = new File(modulesPath);
        for (File file : modules.listFiles()) {
            if (file.isDirectory()) {
                checkModule(modules.getAbsolutePath(), file.getName());
            }
        }
    }

    /**
     * Checks a single module for missing manifest entries.<p>
     * 
     * @param basePath the modules folder path 
     * @param module the module name 
     * @throws Exception
     */
    public void checkModule(String basePath, String module) throws Exception {

        String manifest = CmsStringUtil.joinPaths(basePath, module, "resources/manifest.xml");
        String resourcePath = CmsStringUtil.joinPaths(basePath, module, "/resources");
        Set<String> missing = getMissingPaths(manifest, resourcePath);
        if (!missing.isEmpty()) {
            System.out.println(module + " lacks manifest entries for: ");
            for (String path : missing) {
                System.out.println("     " + path);
            }
        }
    }

    /**
     * Finds the missing paths for a single module.<p>
     * 
     * @param manifest the path of the manifest 
     * @param baseFolder the path of the module resources folder 
     * @return the set of missing paths 
     * 
     * @throws Exception if something goes wrong 
     */
    public Set<String> getMissingPaths(String manifest, final String baseFolder) throws Exception {

        final Set<String> manifestPaths = new HashSet<String>();
        final Set<String> filePaths = new HashSet<String>();
        final String basePath = new File(baseFolder).getAbsolutePath();

        SAXReader reader = new SAXReader();
        Document doc = reader.read(new File(manifest));
        List<?> nodes = doc.selectNodes("//export/files/file/source");
        for (Object node : nodes) {
            Element elem = (Element)node;
            String path = elem.getText().trim();
            path = CmsStringUtil.joinPaths("/", path);
            manifestPaths.add(path);
        }
        CmsFileUtil.walkFileSystem(new File(baseFolder), new Closure() {

            public void execute(Object obj) {

                FileWalkState state = (FileWalkState)obj;
                for (File file : state.getFiles()) {
                    String path = file.getAbsolutePath().substring(basePath.length());
                    path = path.replace('\\', '/');
                    filePaths.add(path);
                }
            }
        });
        Set<String> diff = new HashSet<String>(filePaths);
        diff.removeAll(manifestPaths);
        diff.remove("/manifest.xml");
        return diff;
    }

}
