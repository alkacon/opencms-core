/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/util/CmsJspDeviceSelector.java,v $
 * Date   : $Date: 2009/12/15 15:24:39 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.jsp.util;

import org.opencms.util.CmsRequestUtil;

import javax.servlet.http.HttpServletRequest;

/**
 * This class provides the detection for different devices, so that the
 * <code>&lt;cms:device type="..."&gt;</code>-Tag can detect which device sends the HTTP request.<p>
 * 
 * @author Ruediger Kurz
 * 
 * @since 7.9.3
 * 
 * @version 1.0
 */
public class CmsJspDeviceSelector implements I_CmsJspDeviceSelector {

    /** Constant for console detection. */
    public static final String C_CONSOLE = "console";

    /** Constant for desktop detection. */
    public static final String C_DESKTOP = "desktop";

    /** Constant for mobile detection. */
    public static final String C_MOBILE = "mobile";

    /** A Smartphone name constant. */
    public static final String DEVICE_ANDROID = "android";

    /** Constant for other random devices and mobile browsers. */
    public static final String DEVICE_ARCHOS = "archos";

    /** A Smartphone name constant. */
    public static final String DEVICE_BB = "blackberry";

    /** Constant for other random devices and mobile browsers. */
    public static final String DEVICE_BREW = "brew";

    /** Constant for other random devices and mobile browsers. */
    public static final String DEVICE_DANGER = "danger";

    /** Constant for other random devices and mobile browsers. */
    public static final String DEVICE_HIPTOP = "hiptop";

    /** A Smartphone name constant. */
    public static final String DEVICE_IEMOBILE = "iemobile";

    /** A Smartphone name constant. */
    public static final String DEVICE_IPHONE = "iphone";

    /** A Smartphone name constant. */
    public static final String DEVICE_IPOD = "ipod";

    /** Constant for other random devices and mobile browsers. */
    public static final String DEVICE_MIDP = "midp"; //a mobile Java technology

    /** Constant for other random devices and mobile browsers. */
    public static final String DEVICE_NINTENDO = "nintendo";

    /** Constant for other random devices and mobile browsers. */
    public static final String DEVICE_NINTENDO_DS = "nitro";

    /** A Smartphone name constant. */
    public static final String DEVICE_PALM = "palm";

    /** Constant for other random devices and mobile browsers. */
    public static final String DEVICE_PDA = "pda"; //some devices report themselves as PDAs

    /** Constant for other random devices and mobile browsers. */
    public static final String DEVICE_PLAYSTATION = "playstation";

    /** A Smartphone name constant. */
    public static final String DEVICE_S60 = "series60";

    /** A Smartphone name constant. */
    public static final String DEVICE_S70 = "series70";

    /** A Smartphone name constant. */
    public static final String DEVICE_S80 = "series80";

    /** A Smartphone name constant. */
    public static final String DEVICE_S90 = "series90";

    /** A Smartphone name constant. */
    public static final String DEVICE_SYMBIAN = "symbian";

    /** Constant for other random devices and mobile browsers. */
    public static final String DEVICE_WII = "wii";

    /** A Smartphone name constant. */
    public static final String DEVICE_WIN_MOBILE = "windows ce";

    /** A Smartphone name constant. */
    public static final String DEVICE_WINDOWS = "windows";

    /** Constant for other random devices and mobile browsers. */
    public static final String DEVICE_XBOX = "xbox";

    /** A Smartphone name constant. */
    public static final String ENGINE_BLAZER = "blazer"; //Old Palm

    /** Constant for other random devices and mobile browsers. */
    public static final String ENGINE_NETFRONT = "netfront"; //Common embedded OS browser

    /** Constant for other random devices and mobile browsers. */
    public static final String ENGINE_OPEN_WEB = "openweb"; //Transcoding by OpenWave server

    /** Constant for other random devices and mobile browsers. */
    public static final String ENGINE_OPERA = "opera"; //Popular browser

    /** A Smartphone name constant. */
    public static final String ENGINE_PIE = "wm5 pie"; //An old Windows Mobile

    /** Constant for other random devices and mobile browsers. */
    public static final String ENGINE_UP_BROWSER = "up.browser"; //common on some phones

    /** A Smartphone name constant. */
    public static final String ENGINE_WEBKIT = "webkit";

    /** A Smartphone name constant. */
    public static final String ENGINE_XIINO = "xiino"; //Another old Palm

    /** Constant for other random devices and mobile browsers. */
    public static final String LINUX = "linux";

    /** Constant for other random devices and mobile browsers. */
    public static final String MAEMO = "maemo";

    /** Constant for other random devices and mobile browsers. */
    public static final String MAEMO_TABLET = "tablet";

    /** Constant for other random devices and mobile browsers. */
    public static final String MANU_ERICSSON = "ericsson";

    /** Constant for other random devices and mobile browsers. */
    public static final String MANU_SAMSUNG1 = "sec-sgh";

    /** Constant for other random devices and mobile browsers. */
    public static final String MANU_SONY = "sony";

    /** Constant for other random devices and mobile browsers. */
    public static final String MANU_SONY_ERICSSON = "sonyericsson";

    /** Constant for other random devices and mobile browsers. */
    public static final String MINI = "mini"; //Some mobile browsers put "mini" in their names.

    /** Constant for other random devices and mobile browsers. */
    public static final String MOBI = "mobi"; //Some mobile browsers put "mobi" in their user agent strings.

    /** Constant for other random devices and mobile browsers. */
    public static final String MOBILE = "mobile"; //Some mobile browsers put "mobile" in their user agent strings.

    /** Constant for other random devices and mobile browsers. */
    public static final String MYLO_COM2 = "com2"; //for Sony Mylo also

    /** Constant for other random devices and mobile browsers. */
    public static final String QT_EMBEDDED = "qt embedded"; //for Sony Mylo

    /** Constant for other random devices and mobile browsers. */
    public static final String SVC_DOCOMO = "docomo";

    /** Constant for other random devices and mobile browsers. */
    public static final String SVC_KDDI = "kddi";

    /** Constant for other random devices and mobile browsers. */
    public static final String SVC_VODAFONE = "vodafone";

    /** Constant for other random devices and mobile browsers. */
    public static final String UPLINK = "up.link";

    /** A Smartphone name constant. */
    public static final String VND_RIM = "vnd.rim"; //Detectable when BB devices emulate IE or Firefox

    /** Constant for mobile-specific content. */
    public static final String VND_WAP = "vnd.wap";

    /** Constant for mobile-specific content. */
    public static final String WML = "wml";

    /** Accept HTTP request header. */
    protected String m_httpAccept = "";

    /** User-Agent HTTP request header. */
    protected String m_userAgent = "";

    /**
     * @see org.opencms.jsp.util.I_CmsJspDeviceSelector#getDeviceType(javax.servlet.http.HttpServletRequest)
     */
    public String getDeviceType(HttpServletRequest req) {

        init(req.getHeader(CmsRequestUtil.HEADER_USER_AGENT), req.getHeader(CmsRequestUtil.HEADER_ACCEPT));

        if (detectMobileQuick()) {
            return C_MOBILE;
        } else if (detectGameConsole()) {
            return C_CONSOLE;
        } else {
            return C_DESKTOP;
        }
    }

    /**
     * @see org.opencms.jsp.util.I_CmsJspDeviceSelector#getDeviceTypes()
     */
    public String[] getDeviceTypes() {

        String[] devices = {C_MOBILE, C_CONSOLE, C_DESKTOP};
        return devices;
    }

    /**
     * Return the lower case <code>HTTP_ACCEPT</code>.<p>
     * 
     * @return the HTTP HEADER Accept information
     */
    public String getHttpAccept() {

        return m_httpAccept;
    }

    /**
     * Return the lower case <code>HTTP_USER_AGENT</code>.<p>
     * 
     * @return the user agent
     */
    public String getUserAgent() {

        return m_userAgent;
    }

    /**
     * Initialize the userAgent and httpAccept variables.<p>
     *
     * @param userAgent the User-Agent header
     * @param httpAccept the Accept header
     */
    public void init(String userAgent, String httpAccept) {

        if (userAgent != null) {
            this.m_userAgent = userAgent.toLowerCase();
        }
        if (httpAccept != null) {
            this.m_httpAccept = httpAccept.toLowerCase();
        }
    }

    /**
     * Detects if the current device is an Android OS-based device.<p>
     * 
     * @return <code>true</code> if the device is an Android, <code>false</code> otherwise
     */
    protected boolean detectAndroid() {

        if (m_userAgent.indexOf(DEVICE_ANDROID) != -1) {
            return true;
        }
        return false;
    }

    /**
     * Detects if the current device is an Android OS-based device and
     * the browser is based on WebKit.<p>
     * 
     * @return <code>true</code> if the device is an Android OS-based, <code>false</code> otherwise
     */
    protected boolean detectAndroidWebKit() {

        if (detectAndroid() && detectWebkit()) {
            return true;
        }
        return false;
    }

    /**
     * Detects if the current device is an Archos media player/Internet tablet.<p>
     * 
     * @return <code>true</code> if the current device is an Archos media player/Internet tablet, <code>false</code> otherwise
     */
    protected boolean detectArchos() {

        if (m_userAgent.indexOf(DEVICE_ARCHOS) != -1) {
            return true;
        }
        return false;
    }

    /**
     * Detects if the current browser is a BlackBerry of some sort.<p>
     * 
     * @return <code>true</code> if the current browser is a BlackBerry of some sort, <code>false</code> otherwise
     */
    protected boolean detectBlackBerry() {

        if ((m_userAgent.indexOf(DEVICE_BB) != -1) || (m_httpAccept.indexOf(VND_RIM) != -1)) {
            return true;
        }
        return false;
    }

    /**
     * Detects whether the device is a Brew-powered device.<p>
     * 
     * @return <code>true</code> if the device is a Brew-powered device, <code>false</code> otherwise
     */
    protected boolean detectBrewDevice() {

        if (m_userAgent.indexOf(DEVICE_BREW) != -1) {
            return true;
        }
        return false;
    }

    /**
     * Detects the Danger Hiptop device.<p>
     * 
     * @return <code>true</code> if the device is a Danger Hiptop device, <code>false</code> otherwise
     */
    protected boolean detectDangerHiptop() {

        if ((m_userAgent.indexOf(DEVICE_DANGER) != -1) || (m_userAgent.indexOf(DEVICE_HIPTOP) != -1)) {
            return true;
        }
        return false;
    }

    /**
     * Detects if the current device is an Internet-capable game console.<p>
     * 
     * @return <code>true</code> if the device is an Internet-capable game console, <code>false</code> otherwise
     */
    protected boolean detectGameConsole() {

        if (detectSonyPlaystation() || detectNintendo() || detectXbox()) {
            return true;
        }

        return false;
    }

    /**
     * Detects if the current device is an iPhone.<p>
     * 
     * @return <code>true</code> if the device is an iPhone, <code>false</code> otherwise
     */
    protected boolean detectIphone() {

        // The iPod touch says it's an iPhone! So let's disambiguate.
        if ((m_userAgent.indexOf(DEVICE_IPHONE) != -1) && !detectIpod()) {
            return true;
        }

        return false;
    }

    /**
     * Detects if the current device is an iPhone or iPod Touch.<p>
     * 
     * @return <code>true</code> if the device is an iPhone or iPod Touch, <code>false</code> otherwise
     */
    protected boolean detectIphoneOrIpod() {

        //We repeat the searches here because some iPods may report themselves as an iPhone, which would be okay.
        if ((m_userAgent.indexOf(DEVICE_IPHONE) != -1) || (m_userAgent.indexOf(DEVICE_IPOD) != -1)) {
            return true;
        }
        return false;
    }

    /**
     * Detects if the current device is an iPod Touch.<p>
     * 
     * @return <code>true</code> if the device is an iPod Touch, <code>false</code> otherwise
     */
    protected boolean detectIpod() {

        if (m_userAgent.indexOf(DEVICE_IPOD) != -1) {
            return true;
        }
        return false;
    }

    /**
     * Detects if the current device is on one of the Maemo-based Nokia Internet Tablets.<p>
     * 
     * @return <code>true</code> if the current device is on one of the Maemo-based Nokia Internet Tablets, <code>false</code> otherwise
     */
    protected boolean detectMaemoTablet() {

        if (m_userAgent.indexOf(MAEMO) != -1) {
            return true;
        }
        return false;
    }

    /**
     * Detects if the current device supports MIDP, a mobile Java technology.<p>
     * 
     * @return <code>true</code> if the current device supports MIDP, a mobile Java technology, <code>false</code> otherwise
     */
    protected boolean detectMidpCapable() {

        if ((m_userAgent.indexOf(DEVICE_MIDP) != -1) || (m_httpAccept.indexOf(DEVICE_MIDP) != -1)) {
            return true;
        }
        return false;
    }

    /**
     * The longer and more thorough way to detect for a mobile device.
     * Will probably detect most feature phones,
     * smartphone-class devices, Internet Tablets,
     * Internet-enabled game consoles, etc.
     * This ought to catch a lot of the more obscure and older devices, also --
     * but no promises on thoroughness!<p>
     *   
     * @return <code>true</code> if the current device is a mobile, a tablet or a gameconsole, <code>false</code> otherwise
     */
    protected boolean detectMobileLong() {

        if (detectMobileQuick() || detectMaemoTablet() || detectGameConsole()) {
            return true;
        }
        return false;
    }

    /**
     * The quick way to detect for a mobile device.<p>
     * 
     * Will probably detect most recent/current mid-tier Feature Phones
     * as well as smartphone-class devices.<p>
     *  
     * @return <code>true</code> if the device is mobile, <code>false</code> otherwise
     */
    protected boolean detectMobileQuick() {

        if (detectIphone()) {
            return true;
        }
        //Ordered roughly by market share, WAP/XML > Brew > Smartphone.
        if (detectWapWml()) {
            return true;
        }
        if (detectBrewDevice()) {
            // Updated by AHand
            if (detectOperaMobile()) {
                return true;
            }
        }
        if (m_userAgent.indexOf(ENGINE_UP_BROWSER) != -1) {
            if (m_userAgent.indexOf(ENGINE_OPEN_WEB) != -1) {
                return true;
            }
        }
        if (m_userAgent.indexOf(DEVICE_MIDP) != -1) {
            if (detectSmartphone()) {
                return true;
            }
        }
        if (detectDangerHiptop()) {
            if (detectMidpCapable()) {
                return true;
            }
        }
        if (m_userAgent.indexOf(DEVICE_PDA) != -1) {
            if (m_userAgent.indexOf(MOBILE) != -1) {
                return true;
            }
        }

        //detect older phones from certain manufacturers and operators.
        if (m_userAgent.indexOf(UPLINK) != -1) {
            if (m_userAgent.indexOf(MANU_SONY_ERICSSON) != -1) {
                return true;
            }
        }
        if (m_userAgent.indexOf(MANU_ERICSSON) != -1) {
            if (m_userAgent.indexOf(MANU_SAMSUNG1) != -1) {
                return true;
            }
        }
        if (m_userAgent.indexOf(SVC_DOCOMO) != -1) {
            if (m_userAgent.indexOf(SVC_KDDI) != -1) {
                return true;
            }
        }
        if (m_userAgent.indexOf(SVC_VODAFONE) != -1) {
            return false;
        }
        return false;
    }

    /**
     * Detects if the current device is a Nintendo game device.<p>
     * 
     * @return <code>true</code> if the device is a Nintendo game, <code>false</code> otherwise
     */
    protected boolean detectNintendo() {

        if ((m_userAgent.indexOf(DEVICE_NINTENDO) != -1)
            || (m_userAgent.indexOf(DEVICE_WII) != -1)
            || (m_userAgent.indexOf(DEVICE_NINTENDO_DS) != -1)) {
            return true;
        }

        return false;
    }

    /**
     * Detects Opera Mobile or Opera Mini.<p>
     * 
     * @return <code>true</code> if the browser is a Opera Mobile or Opera Mini, <code>false</code> otherwise
     */
    protected boolean detectOperaMobile() {

        if ((m_userAgent.indexOf(ENGINE_OPERA) != -1)
            && ((m_userAgent.indexOf(MINI) != -1) || (m_userAgent.indexOf(MOBI) != -1))) {
            return true;
        }
        return false;
    }

    /**
     * Detects if the current browser is on a PalmOS device.<p>
     * 
     * @return <code>true</code> if the current browser is on a PalmOS device, <code>false</code> otherwise
     */
    protected boolean detectPalmOS() {

        //Most devices nowadays report as 'Palm', but some older ones reported as Blazer or Xiino.
        if ((m_userAgent.indexOf(DEVICE_PALM) != -1)
            || (m_userAgent.indexOf(ENGINE_BLAZER) != -1)
            || (m_userAgent.indexOf(ENGINE_XIINO) != -1)) {
            return true;
        }

        return false;
    }

    /**
     * Detects if the current browser is the S60 Open Source Browser.<p>
     * 
     * @return <code>true</code> if the current browser is the S60 Open Source Browser, <code>false</code> otherwise
     */
    protected boolean detectS60OssBrowser() {

        //First, test for WebKit, then make sure it's either Symbian or S60.
        if (detectWebkit() && ((m_userAgent.indexOf(DEVICE_SYMBIAN) != -1) || (m_userAgent.indexOf(DEVICE_S60) != -1))) {
            return true;
        }
        return false;
    }

    /**
     * Check to see whether the device is any device in the 'smartphone' category.<p>
     * 
     * @return <code>true</code> if the device is any device in the 'smartphone', <code>false</code> otherwise
     */
    protected boolean detectSmartphone() {

        return (detectIphoneOrIpod()
            || detectS60OssBrowser()
            || detectSymbianOS()
            || detectWindowsMobile()
            || detectBlackBerry()
            || detectPalmOS() || detectAndroid());
    }

    /**
     * Detects if the current browser is a Sony Mylo device.<p>
     * 
     * @return <code>true</code> if the current device is a Sony Mylo device, <code>false</code> otherwise 
     */
    protected boolean detectSonyMylo() {

        if ((m_userAgent.indexOf(MANU_SONY) != -1)
            && ((m_userAgent.indexOf(QT_EMBEDDED) != -1) || (m_userAgent.indexOf(MYLO_COM2) != -1))) {
            return true;
        }
        return false;
    }

    /**
     * Detects if the current device is a Sony Playstation.<p>
     * 
     * @return <code>true</code> if the device is a Sony Playstation, <code>false</code> otherwise
     */
    protected boolean detectSonyPlaystation() {

        if (m_userAgent.indexOf(DEVICE_PLAYSTATION) != -1) {
            return true;
        }
        return false;
    }

    /**
     *
     * Detects if the current device is any Symbian OS-based device,
     * including older S60, Series 70, Series 80, Series 90, and UIQ,
     * or other browsers running on these devices.<p>
     *   
     * @return <code>true</code> if the current device is any Symbian OS-based device, <code>false</code> otherwise
     */
    protected boolean detectSymbianOS() {

        if ((m_userAgent.indexOf(DEVICE_SYMBIAN) != -1)
            || (m_userAgent.indexOf(DEVICE_S60) != -1)
            || (m_userAgent.indexOf(DEVICE_S70) != -1)
            || (m_userAgent.indexOf(DEVICE_S80) != -1)
            || (m_userAgent.indexOf(DEVICE_S90) != -1)) {
            return true;
        }
        return false;
    }

    /**
     * The quick way to detect for a tier of devices.<p>
     * 
     * This method detects for devices which can
     * display iPhone-optimized web content.
     * Includes iPhone, iPod Touch, Android, etc.<p>
     *   
     * @return <code>true</code> if the current device from the iPhone tier, <code>false</code> otherwise 
     */
    protected boolean detectTierIphone() {

        if (detectIphoneOrIpod() || detectAndroid() || detectAndroidWebKit()) {
            return true;
        }
        return false;
    }

    /**
     * The quick way to detect for a tier of devices.<p>
     * 
     * This method detects for all other types of phones,
     * but excludes the iPhone and Smartphone Tier devices.<p>
     *   
     * @return <code>true<code> if the current device is mobile but not a iPhone and not a smartphone, <code>false</code> otherwise
     */
    protected boolean detectTierOtherPhones() {

        if (detectMobileQuick() && (!detectTierIphone()) && (!detectTierSmartphones())) {
            return true;
        }
        return false;
    }

    /**
     * The quick way to detect for a tier of devices.<p>
     * 
     * This method detects for all smartphones, but
     * excludes the iPhone Tier devices.<p>
     *   
     * @return <code>true</code> if the current device is a smartphone but not a iPhone, <code>false</code> otherwise
     */
    protected boolean detectTierSmartphones() {

        if (detectSmartphone() && (!detectTierIphone())) {
            return true;
        }

        return false;
    }

    /**
     * Detects whether the device supports WAP or WML.<p>
     * 
     * @return <code>true</code> if the device supports WAP or WML, <code>false</code> otherwise
     */
    protected boolean detectWapWml() {

        if ((m_httpAccept.indexOf(VND_WAP) != -1) || (m_httpAccept.indexOf(WML) != -1)) {
            return true;
        }

        return false;
    }

    /**
     * Detects if the current browser is based on WebKit.<p>
     * 
     * @return <code>true</code> if the current browser is based on WebKit, <code>false</code> otherwise
     */
    protected boolean detectWebkit() {

        if (m_userAgent.indexOf(ENGINE_WEBKIT) != -1) {
            return true;
        }
        return false;
    }

    /**
     * Detects if the current browser is a Windows Mobile device.<p>
     * 
     * @return <code>true</code> if the current browser is a Windows Mobile device, <code>flase</code> otherwise
     */
    protected boolean detectWindowsMobile() {

        //Most devices use 'Windows CE', but some report 'iemobile'
        //  and some older ones report as 'PIE' for Pocket IE.
        if ((m_userAgent.indexOf(DEVICE_WIN_MOBILE) != -1)
            || (m_userAgent.indexOf(DEVICE_IEMOBILE) != -1)
            || (m_userAgent.indexOf(ENGINE_PIE) != -1)
            || (detectWapWml() && (m_userAgent.indexOf(DEVICE_WINDOWS) != -1))) {
            return true;
        }
        return false;
    }

    /**
     * Detects if the current device is a Microsoft Xbox.<p>
     * 
     * @return <code>true</code> if the device is a Microsoft Xbox, <code>false</code> otherwise
     */
    protected boolean detectXbox() {

        if (m_userAgent.indexOf(DEVICE_XBOX) != -1) {
            return true;
        }
        return false;
    }
}
