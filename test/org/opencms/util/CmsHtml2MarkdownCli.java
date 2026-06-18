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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

/**
 * Command line tool that renders a single OpenCms page element through the local "ocmcp" MCP server
 * and prints its HTML converted to Markdown.<p>
 *
 * The page url is remembered between calls, so it has to be provided only once; the element instance
 * id is passed as the shorter <code>id</code> parameter. The optional flags <code>--ignore-images</code>
 * and <code>--remove-links</code> are passed through to <code>{@link CmsHtml2MarkdownConverter}</code>:<p>
 *
 * <pre>
 * java ... org.opencms.util.CmsHtml2MarkdownCli url=http://host/page.html id=&lt;instanceId&gt;
 * java ... org.opencms.util.CmsHtml2MarkdownCli id=&lt;otherInstanceId&gt; --ignore-images --remove-links
 * </pre>
 *
 * It runs <code>ocmcp page_render_element url=&lt;url&gt; instanceId=&lt;id&gt; --raw</code>, reads the
 * <code>html</code> field from the returned JSON and converts it with
 * <code>{@link CmsHtml2MarkdownConverter}</code>.<p>
 *
 * @since 22.0.0
 */
public final class CmsHtml2MarkdownCli {

    /** The MCP tool used to render a single page element. */
    private static final String OCMCP_TOOL = "page_render_element";

    /** Name of the file (in the temp directory) that remembers the last used url between calls. */
    private static final String URL_STATE_FILE = "ocmcp-md-cli.url";

    /**
     * Hides the public constructor.<p>
     */
    private CmsHtml2MarkdownCli() {

        // hides the public constructor
    }

    /**
     * CLI entry point.<p>
     *
     * @param args the command line arguments: <code>url=...</code>, <code>id=...</code> and the optional
     *      flags <code>--ignore-images</code> and <code>--remove-links</code>
     *
     * @throws Exception if rendering the element or converting the HTML fails
     */
    public static void main(String[] args) throws Exception {

        String url = null;
        String id = null;
        boolean ignoreImages = false;
        boolean removeLinks = false;
        for (String arg : args) {
            if ("--ignore-images".equals(arg)) {
                ignoreImages = true;
                continue;
            }
            if ("--remove-links".equals(arg)) {
                removeLinks = true;
                continue;
            }
            int eq = arg.indexOf('=');
            if (eq < 0) {
                continue;
            }
            String key = arg.substring(0, eq);
            String value = arg.substring(eq + 1);
            if ("url".equals(key)) {
                url = value;
            } else if ("id".equals(key)) {
                id = value;
            }
        }

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(id)) {
            System.err.println("Missing required parameter: id=<instanceId>");
            System.exit(2);
        }

        url = resolveUrl(url);
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(url)) {
            System.err.println("Missing url. Provide url=<pageUrl> once; it is remembered for later calls.");
            System.exit(2);
        }

        String json = renderElement(url, id);
        String html = new JSONObject(json).getString("html");
        System.out.println(CmsHtml2MarkdownConverter.html2markdown(html, ignoreImages, removeLinks));
    }

    /**
     * Calls the local "ocmcp" MCP server to render the given element and returns the raw JSON result.<p>
     *
     * @param url the page url
     * @param instanceId the element instance id
     *
     * @return the raw JSON result printed by ocmcp
     *
     * @throws IOException if the ocmcp process cannot be run or exits with an error
     * @throws InterruptedException if waiting for the ocmcp process is interrupted
     */
    private static String renderElement(String url, String instanceId) throws IOException, InterruptedException {

        List<String> command = new ArrayList<String>();
        command.add("ocmcp");
        command.add(OCMCP_TOOL);
        command.add("url=" + url);
        command.add("instanceId=" + instanceId);
        command.add("--raw");

        ProcessBuilder builder = new ProcessBuilder(command);
        // let ocmcp diagnostics flow straight to our stderr; only its stdout (the JSON) is captured
        builder.redirectError(ProcessBuilder.Redirect.INHERIT);
        Process process = builder.start();
        String out = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int exit = process.waitFor();
        if (exit != 0) {
            throw new IOException("ocmcp exited with code " + exit);
        }
        return out.trim();
    }

    /**
     * Returns the url to use, remembering a newly provided one and otherwise reading the last used url.<p>
     *
     * @param url the url provided on the command line, or <code>null</code>
     *
     * @return the url to use, or <code>null</code> if none is available
     *
     * @throws IOException if reading or writing the state file fails
     */
    private static String resolveUrl(String url) throws IOException {

        Path stateFile = Paths.get(System.getProperty("java.io.tmpdir"), URL_STATE_FILE);
        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(url)) {
            Files.writeString(stateFile, url.trim(), StandardCharsets.UTF_8);
            return url.trim();
        }
        if (Files.exists(stateFile)) {
            return Files.readString(stateFile, StandardCharsets.UTF_8).trim();
        }
        return null;
    }
}
