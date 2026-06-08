<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:strip-space elements="*"/>
 <xsl:output method="xml" encoding="UTF-8"
                doctype-system="http://www.opencms.org/dtd/6.0/opencms-workplace.dtd"
                indent="yes" />

<!-- 

Adds the checkReuseWarning preference

-->


<xsl:param name="configDir" />
<xsl:param name="opencmsWorkplace" select="document(concat($configDir, '/defaults/opencms-workplace.xml'))" />


    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="preference-tab[@name='basic']">
        <preference-tab name="basic">
            <xsl:apply-templates />
            <xsl:if test="not(../preference-tab/preference[@name='checkReuseWarning'])">
                <xsl:copy-of select="$opencmsWorkplace//preference[@name='checkReuseWarning']" />
            </xsl:if>
        </preference-tab>
    </xsl:template>
</xsl:stylesheet>
