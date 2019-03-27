<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:strip-space elements="*"/>
 <xsl:output method="xml" encoding="UTF-8"
                doctype-system="http://www.opencms.org/dtd/6.0/opencms-modules.dtd"
                indent="yes" />

<!--

Removes export point for old Russian localization module lib folder so that the JAR isn't removed
during the module update, causing problems with the classloader.

-->


<xsl:param name="configDir" />

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="exportpoint[@uri='/system/workplace/locales/ru/lib/']" />
    <xsl:template match="exportpoint[@uri='/system/modules/org.opencms.editors.codemirror/lib/']" />
    <xsl:template match="exportpoint[@uri='/system/modules/org.opencms.editors.tinymce/lib/']" />


<!--

Removes export point for old workplace modules

-->

	<xsl:template match="exportpoint[@uri='/system/modules/org.opencms.workplace.administration/lib']" />
	<xsl:template match="exportpoint[@uri='/system/modules/org.opencms.workplace.tools.accounts/lib']" />
	<xsl:template match="exportpoint[@uri='/system/modules/org.opencms.workplace.tools.cache/lib']" />
	<xsl:template match="exportpoint[@uri='/system/modules/org.opencms.workplace.tools.content/lib']" />
	<xsl:template match="exportpoint[@uri='/system/modules/org.opencms.workplace.tools.database/lib']" />
	<xsl:template match="exportpoint[@uri='/system/modules/org.opencms.workplace.tools.galleryoverview/lib']" />
	<xsl:template match="exportpoint[@uri='/system/modules/org.opencms.workplace.tools.history/lib']" />
	<xsl:template match="exportpoint[@uri='/system/modules/org.opencms.workplace.tools.link/lib']" />
	<xsl:template match="exportpoint[@uri='/system/modules/org.opencms.workplace.tools.modules/lib']" />
	<xsl:template match="exportpoint[@uri='/system/modules/org.opencms.workplace.tools.projects/lib']" />
	<xsl:template match="exportpoint[@uri='/system/modules/org.opencms.workplace.tools.publishqueue/lib']" />
	<xsl:template match="exportpoint[@uri='/system/modules/org.opencms.workplace.tools.scheduler/lib']" />
	<xsl:template match="exportpoint[@uri='/system/modules/org.opencms.workplace.tools.searchindex/lib']" />
	<xsl:template match="exportpoint[@uri='/system/modules/org.opencms.workplace.tools.sites/lib']" />
	<xsl:template match="exportpoint[@uri='/system/modules/org.opencms.workplace.tools.workplace/lib']" />

	<xsl:template match="exportpoint[@uri='/system/modules/org.opencms.workplace.administration/lib/']" />
	<xsl:template match="exportpoint[@uri='/system/modules/org.opencms.workplace.tools.accounts/lib/']" />
	<xsl:template match="exportpoint[@uri='/system/modules/org.opencms.workplace.tools.cache/lib/']" />
	<xsl:template match="exportpoint[@uri='/system/modules/org.opencms.workplace.tools.content/lib/']" />
	<xsl:template match="exportpoint[@uri='/system/modules/org.opencms.workplace.tools.database/lib/']" />
	<xsl:template match="exportpoint[@uri='/system/modules/org.opencms.workplace.tools.galleryoverview/lib/']" />
	<xsl:template match="exportpoint[@uri='/system/modules/org.opencms.workplace.tools.history/lib/']" />
	<xsl:template match="exportpoint[@uri='/system/modules/org.opencms.workplace.tools.link/lib/']" />
	<xsl:template match="exportpoint[@uri='/system/modules/org.opencms.workplace.tools.modules/lib/']" />
	<xsl:template match="exportpoint[@uri='/system/modules/org.opencms.workplace.tools.projects/lib/']" />
	<xsl:template match="exportpoint[@uri='/system/modules/org.opencms.workplace.tools.publishqueue/lib/']" />
	<xsl:template match="exportpoint[@uri='/system/modules/org.opencms.workplace.tools.scheduler/lib/']" />
	<xsl:template match="exportpoint[@uri='/system/modules/org.opencms.workplace.tools.searchindex/lib/']" />
	<xsl:template match="exportpoint[@uri='/system/modules/org.opencms.workplace.tools.sites/lib/']" />
	<xsl:template match="exportpoint[@uri='/system/modules/org.opencms.workplace.tools.workplace/lib/']" />



</xsl:stylesheet>
