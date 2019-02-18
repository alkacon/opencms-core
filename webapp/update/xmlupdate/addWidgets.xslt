<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:strip-space elements="*" />
    <xsl:output method="xml" encoding="UTF-8" doctype-system="http://www.opencms.org/dtd/6.0/opencms-vfs.dtd" indent="yes" />

<!-- 

Copies widgets from default opencms-vfs.xml if they don't already exist.

-->


    <xsl:param name="configDir" />
    <xsl:param name="opencmsVfs" select="document(concat($configDir, '/defaults/opencms-vfs.xml'))" />

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()" />
        </xsl:copy>
    </xsl:template>


    <xsl:template match="widgets">
        <xsl:variable name="ws" select="." />
        <widgets>
            <xsl:apply-templates />
            <xsl:for-each select="$opencmsVfs//widget">
                <xsl:variable name="cls" select="@class" />
                <xsl:if test="not($ws//widget[@class=$cls])">
                    <xsl:copy-of select="." />
                </xsl:if>
            </xsl:for-each>
        </widgets>
    </xsl:template>

</xsl:stylesheet>
