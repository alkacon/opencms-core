<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:strip-space elements="*"/>
 <xsl:output method="xml" encoding="UTF-8"
                doctype-system="http://www.opencms.org/dtd/6.0/opencms-vfs.dtd"
                indent="yes" />

<!-- 

Copies schema types from default opencms-vfs.xml if they don't already exist.

-->


<xsl:param name="configDir" />
<xsl:param name="opencmsVfs" select="document(concat($configDir, '/defaults/opencms-vfs.xml'))" />

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="schematypes">
        <xsl:variable name="ws" select="." />
        <schematypes>
            <xsl:apply-templates />
            <xsl:for-each select="$opencmsVfs//schematype">
                <xsl:variable name="cls" select="@class" />
                <xsl:if test="not($ws//schematype[@class=$cls])">
                    <xsl:copy-of select="." />
                </xsl:if>
            </xsl:for-each>
        </schematypes>
    </xsl:template>
    
</xsl:stylesheet>
