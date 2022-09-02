<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:strip-space elements="*"/>
  <xsl:output method="xml" 
                doctype-system="http://www.opencms.org/dtd/6.0/opencms-system.dtd"
                indent="yes" />


<!--

Disables 2FA for the module update step.

-->


    <xsl:param name="configDir" />
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="//two-factor-authentication/enabled">
        <enabled>false</enabled>
    </xsl:template>
</xsl:stylesheet>
