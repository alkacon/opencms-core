/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/templateone/form/CmsCaptchaEngine.java,v $
 * Date   : $Date: 2008/02/27 12:05:22 $
 * Version: $Revision: 1.8 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2008 Alkacon Software GmbH (http://www.alkacon.com)
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
package org.opencms.frontend.templateone.form;

import org.opencms.main.OpenCms;

import java.awt.image.ImageFilter;
import java.util.Locale;

import com.jhlabs.image.WaterFilter;
import com.octo.captcha.CaptchaFactory;
import com.octo.captcha.component.image.backgroundgenerator.BackgroundGenerator;
import com.octo.captcha.component.image.backgroundgenerator.FileReaderRandomBackgroundGenerator;
import com.octo.captcha.component.image.backgroundgenerator.UniColorBackgroundGenerator;
import com.octo.captcha.component.image.color.ColorGenerator;
import com.octo.captcha.component.image.color.SingleColorGenerator;
import com.octo.captcha.component.image.deformation.ImageDeformation;
import com.octo.captcha.component.image.deformation.ImageDeformationByFilters;
import com.octo.captcha.component.image.fontgenerator.FontGenerator;
import com.octo.captcha.component.image.fontgenerator.RandomFontGenerator;
import com.octo.captcha.component.image.textpaster.DecoratedRandomTextPaster;
import com.octo.captcha.component.image.textpaster.TextPaster;
import com.octo.captcha.component.image.textpaster.textdecorator.BaffleTextDecorator;
import com.octo.captcha.component.image.textpaster.textdecorator.TextDecorator;
import com.octo.captcha.component.image.wordtoimage.DeformedComposedWordToImage;
import com.octo.captcha.component.image.wordtoimage.WordToImage;
import com.octo.captcha.component.word.wordgenerator.RandomWordGenerator;
import com.octo.captcha.component.word.wordgenerator.WordGenerator;
import com.octo.captcha.engine.CaptchaEngineException;
import com.octo.captcha.engine.image.ImageCaptchaEngine;
import com.octo.captcha.image.ImageCaptcha;
import com.octo.captcha.image.ImageCaptchaFactory;
import com.octo.captcha.image.gimpy.GimpyFactory;

/**
 * A captcha engine using a Gimpy factory to create captchas.
 * <p>
 * 
 * @author Thomas Weckert
 * 
 * @author Achim Westermann
 * 
 * @version $Revision: 1.8 $
 */
public class CmsCaptchaEngine extends ImageCaptchaEngine {

    /** The configured image captcha factory. */
    private ImageCaptchaFactory m_factory;

    /** The settings for this captcha engine. */
    private CmsCaptchaSettings m_settings;

    /**
     * Creates a new Captcha engine.
     * <p>
     * 
     * @param captchaSettings the settings to render captcha images
     */
    public CmsCaptchaEngine(CmsCaptchaSettings captchaSettings) {

        super();

        m_settings = captchaSettings;
        initGimpyFactory();
    }

    /**
     * @see com.octo.captcha.engine.image.ImageCaptchaEngine#getNextImageCaptcha()
     */
    public ImageCaptcha getNextImageCaptcha() {

        return m_factory.getImageCaptcha();
    }

    /**
     * @see com.octo.captcha.engine.image.ImageCaptchaEngine#getNextImageCaptcha(java.util.Locale)
     */
    public ImageCaptcha getNextImageCaptcha(Locale locale) {

        return m_factory.getImageCaptcha(locale);
    }

    /**
     * Sets the settings.
     * <p>
     * 
     * @param settings the settings to set
     */
    public void setSettings(CmsCaptchaSettings settings) {

        m_settings = settings;
        initGimpyFactory();
    }

    /**
     * Initializes a Gimpy captcha factory.
     * <p>
     */
    protected void initGimpyFactory() {

        WaterFilter water = new WaterFilter();
        water.setAmplitude(m_settings.getFilterAmplitude());
        water.setAntialias(true);
        water.setPhase(0);
        water.setWavelength(m_settings.getFilterWaveLength());

        ImageDeformation backgroundDeformation = new ImageDeformationByFilters(new ImageFilter[] {});
        ImageDeformation textDeformation = new ImageDeformationByFilters(new ImageFilter[] {});
        ImageDeformation postDeformation = new ImageDeformationByFilters(new ImageFilter[] {water});

        // FileDictionnary will be renamed correctly in next release! The argument denotes a
        // java.util.ResourceBundle properies file: toddlist.properties e.g. in root of jcaptcha jar
        // WordGenerator dictionary = new ComposeDictionaryWordGenerator(new
        // FileDictionnary("toddlist"));
        WordGenerator randomWords = new RandomWordGenerator(m_settings.getCharacterPool());
        // creates holes into image
        BaffleTextDecorator textDecorator = new BaffleTextDecorator(
            m_settings.getHolesPerGlyph(),
            m_settings.getFontColor());
        ColorGenerator colorGenerator = new SingleColorGenerator(m_settings.getFontColor());

        TextPaster paster = new DecoratedRandomTextPaster(new Integer(m_settings.getMinPhraseLength()), new Integer(
            m_settings.getMaxPhraseLength()), colorGenerator, new TextDecorator[] {textDecorator});

        BackgroundGenerator background;
        if (m_settings.isUseBackgroundImage()) {
            background = new FileReaderRandomBackgroundGenerator(new Integer(m_settings.getImageWidth()), new Integer(
                m_settings.getImageHeight()), OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebApplication(
                "resources/captchabackgrounds/"));

        } else {
            background = new UniColorBackgroundGenerator(new Integer(m_settings.getImageWidth()), new Integer(
                m_settings.getImageHeight()), m_settings.getBackgroundColor());
        }

        FontGenerator font = new RandomFontGenerator(new Integer(m_settings.getMinFontSize()), new Integer(
            m_settings.getMaxFontSize()));

        WordToImage wordToImage = new DeformedComposedWordToImage(
            font,
            background,
            paster,
            backgroundDeformation,
            textDeformation,
            postDeformation);

        m_factory = new GimpyFactory(randomWords, wordToImage);
    }

    /**
     * Returns the hardcoded factory (array of length 1) that is used.
     * <p>
     * 
     * @return the hardcoded factory (array of length 1) that is used
     * 
     * @see com.octo.captcha.engine.CaptchaEngine#getFactories()
     */
    public CaptchaFactory[] getFactories() {

        return new CaptchaFactory[]{m_factory};
    }

    /**
     * This does nothing. <p>
     * 
     * A hardcored factory for deformation is used. 
     * <p>
     * 
     * @see com.octo.captcha.engine.CaptchaEngine#setFactories(com.octo.captcha.CaptchaFactory[])
     */
    public void setFactories(CaptchaFactory[] arg0) throws CaptchaEngineException {

        // TODO Auto-generated method stub

    }

}
