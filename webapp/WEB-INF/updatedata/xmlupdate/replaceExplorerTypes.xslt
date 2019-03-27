<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:strip-space elements="*"/>
 <xsl:output method="xml" encoding="UTF-8"
                doctype-system="http://www.opencms.org/dtd/6.0/opencms-workplace.dtd"
                indent="yes" />

<!-- 

Replace explorer types with the default ones in opencms-workplace.

-->


<xsl:param name="configDir" />
<xsl:param name="opencmsWorkplace" select="document(concat($configDir, '/defaults/opencms-workplace.xml'))" />


    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="explorertypes">
        <xsl:copy-of select="$opencmsWorkplace//explorertypes" />
    </xsl:template>
</xsl:stylesheet>
