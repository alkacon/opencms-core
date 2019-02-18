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

</xsl:stylesheet>
