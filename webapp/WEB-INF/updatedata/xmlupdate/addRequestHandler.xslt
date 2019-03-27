<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:strip-space elements="*"/>
 <xsl:output method="xml" encoding="UTF-8"
                doctype-system="http://www.opencms.org/dtd/6.0/opencms-system.dtd"
                indent="yes" />

<!--

Copies schema types from default opencms-vfs.xml if they don't already exist.

-->


<xsl:param name="configDir" />
<xsl:param name="opencmsSystem" select="document(concat($configDir, '/defaults/opencms-system.xml'))" />

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="requesthandlers">
        <xsl:variable name="ws" select="." />
        <requesthandlers>
            <xsl:apply-templates />
            <xsl:for-each select="$opencmsSystem//requesthandler">
                <xsl:variable name="cls" select="@class" />
                <xsl:if test="not($ws//requesthandler[@class=$cls])">
                    <xsl:copy-of select="." />
                </xsl:if>
            </xsl:for-each>
        </requesthandlers>
    </xsl:template>

</xsl:stylesheet>
