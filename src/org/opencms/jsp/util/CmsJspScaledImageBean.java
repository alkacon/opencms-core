
package org.opencms.jsp.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Bean extending the CmsJspImageBean with an additional Map containing hi-DPI variants of the same image.
 */
public class CmsJspScaledImageBean extends CmsJspImageBean {

    /**
     * Internal Map used to store the hi-DPI variants of the image.
     * <ul>
     *   <li>key: the variant multiplier, e.g. "2x" (the common retina multiplier)</li>
     *   <li>value: a CmsJspImageBean representing the hi-DPI variant</li>
     * </ul>
     */
    private Map<String, CmsJspImageBean> m_hiDpiImages;

    /**
     * adds a CmsJspImageBean as hi-DPI variant to this image
     * @param factor the variant multiplier, e.g. "2x" (the common retina multiplier)
     * @param image the image to be used for this variant
     */
    public void addHiDpiImage(String factor, CmsJspImageBean image) {

        if (m_hiDpiImages == null) {
            m_hiDpiImages = new HashMap<>();
        }
        m_hiDpiImages.put(factor, image);
    }

    /**
     * Returns the map containing all hi-DPI variants of this image.
     * @return Map containing the hi-DPI variants of the image.
     * <ul>
     *   <li>key: the variant multiplier, e.g. "2x" (the common retina multiplier)</li>
     *   <li>value: a CmsJspImageBean representing the hi-DPI variant</li>
     * </ul>
     */
    public Map<String, CmsJspImageBean> getHiDpiImages() {

        return m_hiDpiImages;
    }

}
