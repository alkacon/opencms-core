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

package org.opencms.pdftools;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.wrapper.CmsWrappedResource;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsResourceInitException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.I_CmsResourceInit;
import org.opencms.main.Messages;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsSecurityException;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

/**
 * This resource handler handles URLs of the form /pdflink/{locale}/{formatter-id}/{detailname} and format
 * the content identified by detailname using the JSP identified by formatter-id to generate XHTML which is then
 * converted to PDF and returned directly by this handler.<p>
 *
 * In Online mode, the generated PDFs are cached on the real file system, while in Offline mode, the PDF data is always
 * generated on-the-fly.<p>
 */
public class CmsPdfResourceHandler implements I_CmsResourceInit {

    /** Mime type data for different file extensions. */
    public static final String IMAGE_MIMETYPECONFIG = "png:image/png|gif:image/gif|jpg:image/jpeg";

    /** Map of mime types for different file extensions. */
    public static final Map<String, String> IMAGE_MIMETYPES = Collections.unmodifiableMap(
        CmsStringUtil.splitAsMap(IMAGE_MIMETYPECONFIG, "|", ":"));

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsPdfResourceHandler.class);
    /** The cache for the generated PDFs. */
    private CmsPdfCache m_pdfCache;

    /** The converter used to generate the PDFs. */
    private CmsPdfConverter m_pdfConverter = new CmsPdfConverter();

    /** Cache for thumbnails. */
    private CmsPdfThumbnailCache m_thumbnailCache = new CmsPdfThumbnailCache();

    /**
     * Creates a new instance.<p>
     */
    public CmsPdfResourceHandler() {

        m_pdfCache = new CmsPdfCache();
    }

    /**
     * @see org.opencms.main.I_CmsResourceInit#initResource(org.opencms.file.CmsResource, org.opencms.file.CmsObject, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public CmsResource initResource(
        CmsResource resource,
        CmsObject cms,
        HttpServletRequest request,
        HttpServletResponse response) throws CmsResourceInitException, CmsSecurityException {

        // check if the resource was already found or the path starts with '/system/'
        boolean abort = (resource != null) || cms.getRequestContext().getUri().startsWith(CmsWorkplace.VFS_PATH_SYSTEM);
        if (abort) {
            // skip in all cases above
            return resource;
        }
        if (response != null) {
            String uri = cms.getRequestContext().getUri();

            try {
                if (uri.contains(CmsPdfLink.PDF_LINK_PREFIX)) {
                    handlePdfLink(cms, request, response, uri);
                    return null; // this will not be reached because the previous call will throw an exception
                } else if (uri.contains(CmsPdfThumbnailLink.MARKER)) {
                    handleThumbnailLink(cms, request, response, uri);
                    return null; // this will not be reached because the previous call will throw an exception
                } else {
                    return null;
                }
            } catch (CmsResourceInitException e) {
                throw e;
            } catch (CmsSecurityException e) {
                LOG.warn(e.getLocalizedMessage(), e);
                throw e;
            } catch (CmsPdfLink.CmsPdfLinkParseException e) {
                // not a valid PDF link, just continue with the resource init chain
                LOG.warn(e.getLocalizedMessage(), e);
                return null;
            } catch (CmsPdfThumbnailLink.ParseException e) {
                LOG.warn(e.getLocalizedMessage(), e);
                return null;
            } catch (Exception e) {
                // don't just return null, because we want a useful error message to be displayed
                LOG.error(e.getLocalizedMessage(), e);
                throw new CmsRuntimeException(
                    Messages.get().container(
                        Messages.ERR_RESOURCE_INIT_ABORTED_1,
                        CmsPdfResourceHandler.class.getName()),
                    e);
            }
        } else {
            return null;
        }
    }

    /**
     * Handles a link for generating a PDF.<p>
     *
     * @param cms the current CMS context
     * @param request the servlet request
     * @param response the servlet response
     * @param uri the current uri
     *
     * @throws Exception if something goes wrong
     * @throws CmsResourceInitException if the resource initialization is cancelled
     */
    protected void handlePdfLink(CmsObject cms, HttpServletRequest request, HttpServletResponse response, String uri)
    throws Exception {

        CmsPdfLink linkObj = new CmsPdfLink(cms, uri);
        CmsResource formatter = linkObj.getFormatter();
        CmsResource content = linkObj.getContent();
        LOG.info("Trying to render " + content.getRootPath() + " using " + formatter.getRootPath());
        Locale locale = linkObj.getLocale();
        CmsObject cmsForJspExecution = OpenCms.initCmsObject(cms);
        cmsForJspExecution.getRequestContext().setLocale(locale);
        cmsForJspExecution.getRequestContext().setSiteRoot("");
        byte[] result = null;
        String cacheParams = formatter.getStructureId() + ";" + formatter.getDateLastModified() + ";" + locale;
        String cacheName = m_pdfCache.getCacheName(content, cacheParams);
        if (cms.getRequestContext().getCurrentProject().isOnlineProject()) {
            result = m_pdfCache.getCacheContent(cacheName);
        }
        if (result == null) {
            cmsForJspExecution.getRequestContext().setUri(content.getRootPath());
            byte[] xhtmlData = CmsPdfFormatterUtils.executeJsp(
                cmsForJspExecution,
                request,
                response,
                formatter,
                content);

            LOG.info("Rendered XHTML from " + content.getRootPath() + " using " + formatter.getRootPath());
            if (LOG.isDebugEnabled()) {
                logXhtmlOutput(formatter, content, xhtmlData);
            }
            // Use the same CmsObject we used for executing the JSP, because the same site root is needed to resolve external resources like images
            result = m_pdfConverter.convertXhtmlToPdf(cmsForJspExecution, xhtmlData, "opencms://" + uri);
            LOG.info("Converted XHTML to PDF, size=" + result.length);
            m_pdfCache.saveCacheFile(cacheName, result);
        } else {
            LOG.info(
                "Retrieved PDF data from cache for content "
                    + content.getRootPath()
                    + " and formatter "
                    + formatter.getRootPath());
        }
        response.setContentType("application/pdf");
        response.getOutputStream().write(result);
        CmsResourceInitException initEx = new CmsResourceInitException(CmsPdfResourceHandler.class);
        initEx.setClearErrors(true);
        throw initEx;
    }

    /**
     * Logs the XHTML output.<p>
     *
     * @param formatter the formatter
     * @param content the content resource
     * @param xhtmlData the XHTML data
     */
    protected void logXhtmlOutput(CmsResource formatter, CmsResource content, byte[] xhtmlData) {

        try {
            String xhtmlString = new String(xhtmlData, "UTF-8");
            LOG.debug(
                "(PDF generation) The formatter "
                    + formatter.getRootPath()
                    + " generated the following XHTML source from "
                    + content.getRootPath()
                    + ":");
            LOG.debug(xhtmlString);
        } catch (Exception e) {
            LOG.debug(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Handles a request for a PDF thumbnail.<p>
     *
     * @param cms the current CMS context
     * @param request the servlet request
     * @param response the servlet response
     * @param uri the current uri
     *
     *  @throws Exception if something goes wrong
     */
    private void handleThumbnailLink(
        CmsObject cms,
        HttpServletRequest request,
        HttpServletResponse response,
        String uri) throws Exception {

        String options = request.getParameter(CmsPdfThumbnailLink.PARAM_OPTIONS);
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(options)) {
            options = "w:64";
        }
        CmsPdfThumbnailLink linkObj = new CmsPdfThumbnailLink(cms, uri, options);
        CmsResource pdf = linkObj.getPdfResource();
        CmsFile pdfFile = cms.readFile(pdf);
        CmsPdfThumbnailGenerator thumbnailGenerator = new CmsPdfThumbnailGenerator();
        // use a wrapped resource because we want the cache to store files with the correct (image file) extensions
        CmsWrappedResource wrapperWithImageExtension = new CmsWrappedResource(pdfFile);
        wrapperWithImageExtension.setRootPath(pdfFile.getRootPath() + "." + linkObj.getFormat());
        String cacheName = m_thumbnailCache.getCacheName(
            wrapperWithImageExtension.getResource(),
            options + ";" + linkObj.getFormat());
        byte[] imageData = m_thumbnailCache.getCacheContent(cacheName);
        if (imageData == null) {
            imageData = thumbnailGenerator.generateThumbnail(
                new ByteArrayInputStream(pdfFile.getContents()),
                linkObj.getWidth(),
                linkObj.getHeight(),
                linkObj.getFormat(),
                linkObj.getPage());
            m_thumbnailCache.saveCacheFile(cacheName, imageData);
        }
        response.setContentType(IMAGE_MIMETYPES.get(linkObj.getFormat()));
        response.getOutputStream().write(imageData);
        CmsResourceInitException initEx = new CmsResourceInitException(CmsPdfResourceHandler.class);
        initEx.setClearErrors(true);
        throw initEx;

    }

}
