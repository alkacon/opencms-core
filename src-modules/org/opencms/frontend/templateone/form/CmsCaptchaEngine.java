/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/templateone/form/CmsCaptchaEngine.java,v $
 * Date   : $Date: 2005/09/20 07:31:03 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

import java.awt.Color;
import java.awt.image.ImageFilter;
import java.io.File;
import java.util.Locale;

import com.jhlabs.image.WaterFilter;
import com.octo.captcha.component.image.backgroundgenerator.BackgroundGenerator;
import com.octo.captcha.component.image.backgroundgenerator.FileReaderRandomBackgroundGenerator;
import com.octo.captcha.component.image.deformation.ImageDeformation;
import com.octo.captcha.component.image.deformation.ImageDeformationByFilters;
import com.octo.captcha.component.image.fontgenerator.FontGenerator;
import com.octo.captcha.component.image.fontgenerator.RandomFontGenerator;
import com.octo.captcha.component.image.textpaster.BaffleRandomTextPaster;
import com.octo.captcha.component.image.textpaster.TextPaster;
import com.octo.captcha.component.image.wordtoimage.DeformedComposedWordToImage;
import com.octo.captcha.component.image.wordtoimage.WordToImage;
import com.octo.captcha.component.wordgenerator.RandomWordGenerator;
import com.octo.captcha.component.wordgenerator.WordGenerator;
import com.octo.captcha.engine.image.ImageCaptchaEngine;
import com.octo.captcha.image.ImageCaptcha;
import com.octo.captcha.image.ImageCaptchaFactory;
import com.octo.captcha.image.gimpy.GimpyFactory;

/**
 * A captcha engine using a Gimpy factory to create captchas.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.2 $
 */
public class CmsCaptchaEngine extends ImageCaptchaEngine {

    private CmsCaptchaSettings m_settings;
    private ImageCaptchaFactory m_factory;

    /**
     * Creates a new Captcha engine.<p>
     * 
     * @param captchaSettings the settings to render captcha images
     */
    public CmsCaptchaEngine(CmsCaptchaSettings captchaSettings) {

        super();
        
        m_settings = captchaSettings;
        initGimpyFactory();
    }

    /**
     * Initializes a Gimpy captcha factory.<p>
     */
    protected void initGimpyFactory() {

        /*
        WaterFilter water = new WaterFilter();
        water.setAmplitude(3d);
        water.setAntialias(true);
        water.setPhase(20d);
        water.setWavelength(70d);

        ImageDeformation backgroundDeformation = new ImageDeformationByFilters(new ImageFilter[] {});
        ImageDeformation textDeformation = new ImageDeformationByFilters(new ImageFilter[] {});
        ImageDeformation postDeformation = new ImageDeformationByFilters(new ImageFilter[] {water});

        WordGenerator dictionary = new ComposeDictionaryWordGenerator(new FileDictionnary("toddlist"));

        TextPaster paster = new BaffleRandomTextPaster(new Integer(m_settings.getMinPhraseLength()), new Integer(
            m_settings.getMaxPhraseLength()), m_settings.getFontColor(), new Integer(2), m_settings.getBackgroundColor());

        BackgroundGenerator background = new UniColorBackgroundGenerator(
            new Integer(m_settings.getImageWidth()),
            new Integer(m_settings.getImageHeight()),
            m_settings.getBackgroundColor());

        FontGenerator font = new RandomFontGenerator(new Integer(m_settings.getMinFontSize()), new Integer(m_settings.getMaxFontSize()));

        WordToImage wordToImage = new DeformedComposedWordToImage(
            font,
            background,
            paster,
            backgroundDeformation,
            textDeformation,
            postDeformation);

        m_factory = new GimpyFactory(dictionary, wordToImage);
        */
        
        if (m_settings != null) {
            // satisfies checkstyle...
        }
        
        WaterFilter water= new WaterFilter();
        water.setAmplitude(3d);
        water.setAntialias(true);
        water.setPhase(20d);
        water.setWavelength(70d);


        ImageDeformation backDef = new ImageDeformationByFilters(new ImageFilter[]{});
        ImageDeformation textDef = new ImageDeformationByFilters(new ImageFilter[]{});
        ImageDeformation postDef = new ImageDeformationByFilters(new ImageFilter[]{water});

        WordGenerator randomWords = new RandomWordGenerator("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");

        TextPaster randomPaster = new BaffleRandomTextPaster(new Integer(5), new Integer(5), Color.black, new Integer(0), Color.white);
        
        BackgroundGenerator back =  new FileReaderRandomBackgroundGenerator(new Integer(200), new Integer(100), OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf("gimpybackgrounds" + File.separatorChar));
        FontGenerator shearedFont = new RandomFontGenerator(new Integer(40), new Integer(40));

        WordToImage word2image = new DeformedComposedWordToImage(shearedFont, back, randomPaster,
                        backDef,
                        textDef,
                        postDef
                        );


        m_factory = new GimpyFactory(randomWords, word2image);
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

}
