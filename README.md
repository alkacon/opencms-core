<h1 align="center">OpenCms</h1>

<p align="center">
  <a href="https://opencms.org/" alt="OpenCms">
    <img src="assets/opencms-logo.svg" alt="OpenCms logo" width="340" height="84">
  </a>
</p>

<p align="center">A powerful open source website content management system.</p>
<p align="center"><a href="https://opencms.org/">OpenCms</a> is built in Java and XML from open source components. OpenCms provides compelling features especially suited for large Internet or Intranet websites.</p>


## Table of contents

- [General OpenCms features](#general-opencms-features)
- [Online live demo](#online-live-demo)
- [Documentation](#documentation)
- [Community](#community)
- [Compatibility](#compatibility)
- [Installing OpenCms](#installing-opencms)
- [Source branches](#source-branches)
- [Bugs and feature requests](#bugs-and-feature-requests)
- [Contributing](#contributing)
- [Security issues](#security-issues)
- [Creators](#creators)
- [Copyright and license](#copyright-and-license)


## General OpenCms features

OpenCms features include:

- The page editor allows WYSIWYG inline editing of web pages and arrangement of content by drag & drop.
- The form based editor allows editing of structured content in a well defined form mask.
- The sitemap editor allows to create new pages and re-arrange the navigation tree by drag & drop.
- Responsive "Mercury" default template based on Bootstrap 4 with many features.
- Headless API for accessing content as JSON from external applications.
- Content creation for mobile devices with preview and device specific content control.
- Structured contents can be defined using a simple XML schema.
- Easy to use "Online / Offline" workflow, changes must be approved before they become visible.
- Link management for all internal resources with broken link detection.
- Integrated image scaling and cropping.
- SEO features with automatic sitemap.xml generation and page alias support.
- Full featured user management that supports the concept of "Organizational Units" (OUs).
- Allows management of multiple websites within one installation.
- Contents can be served dynamically or exported to static HTML files.
- File access to the OpenCms content repository over a shared network / SMB drive.
- CMIS and WebDAV access to the OpenCms content repository.
- Integrates Apache SOLR for powerful content searching and noSQL like queries.
- Full text search for web pages as well as in office documents like PDF, MS Office and Open Office.
- Extensions can be added through a flexible module system.
- The "time warp" feature allows to view resources which are expired or not yet released.
- JSP integration for dynamic functionality in templates, dynamic forms etc.
- ... and much more

See the [OpenCms website](https://opencms.org/) for further information.

## Online live demo

If you want to try out OpenCms, the easiest option is to use our [OpenCms Live Demo server](https://demo.opencms.org).

The live demo provides a personal OpenCms installation exclusively for you. All you need to do is register with a valid email address. We will not use the data you provide for anything else than setting up your personal demo installation!


## Documentation

The [OpenCms Documentation Central](https://documentation.opencms.org/) is the portal to all documentation regarding OpenCms related technology.


## Community

Receive updates about the OpenCms development and get in touch with the core developers and community members.

- Subscribe to the [OpenCms mailing list](https://www.opencms.org/en/community/mailinglist/) for all questions, comments and suggestions regarding OpenCms.
- Follow [@OpenCms on Twitter](https://twitter.com/opencms/) or [OpenCms on Facebook](https://www.facebook.com/opencms/).


## Compatibility

OpenCms is compatible with Java 21 and 25.

OpenCms 22 is available in two variants:

- The **Jakarta EE variant** requires a Jakarta Servlet 6.0 compatible web container. It works "out of the box" with Tomcat 10.1, Tomcat 11 or Jetty 12. This is the default variant and the recommended choice for new installations.
- The **legacy Java(x) EE variant** requires a Java Servlet 4.0 web container such as Tomcat 9 or Jetty 12 in EE 8 mode. This is for existing installations that still depend on `javax.*` code. *Please note: OpenCms 23 will be Jakarta-only.*

See the article [OpenCms goes Jakarta](https://www.opencms.org/en/article/OpenCms-goes-Jakarta) for the background of the Jakarta change and the upgrade path.

Others have reported deploying OpenCms successfully on other web servlet containers like WildFly, GlassFish, WebLogic, WebSphere and Resin.

On the database side, we provide support for MySQL, MariaDB, Oracle, PostgreSQL, MS SQL Server, DB2 and HSQLDB.


## Installing OpenCms

### Docker based installation

The easiest way to install OpenCms is to use our official docker image. See [alkacon/opencms-docker](https://hub.docker.com/r/alkacon/opencms-docker/) on Docker hub for full information about running the OpenCms docker image. Images are available for both variants: `alkacon/opencms-docker:22.0` for the Jakarta EE variant and `alkacon/opencms-docker:22.0-javax` for the legacy Java(x) EE variant.


### Manual installation

You can install OpenCms "from scratch" manually on your server as well. See [the OpenCms installation guide](https://github.com/alkacon/opencms-core/blob/main/assets/docs/INSTALL.md) for full information about manually installing OpenCms.


## Source branches

The OpenCms sources are maintained in two branches:

- `main` contains the Jakarta EE variant. All active development happens here.
- `master` contains the legacy Java(x) EE variant. It is generated from `main` for the release by converting the sources back to the `javax.*` namespace, so pull requests should always target `main`.

Which variant a build produces is controlled by the `eeTarget` property in `gradle.properties` (`jakarta` or `javax`), which also selects the matching dependency set.


## Bugs and feature requests

Please report issues and submit feature requests using our [GitHub OpenCms issue tracker](https://github.com/alkacon/opencms-core/issues).


## Contributing

Contributions to the OpenCms core development are most welcome. [Please send us your pull requests directly on GitHub.](https://github.com/alkacon/opencms-core/pulls).


## Security issues

Please report security issues in OpenCms using the [security issue report form](https://www.alkacon.com/security/) on the Alkacon website.


## Creators

OpenCms is developed by [Alkacon Software](https://www.alkacon.com) with the help of an international OpenCms community.


## Copyright and license

OpenCms is copyright by [Alkacon Software GmbH & Co. KG](https://www.alkacon.com). The OpenCms source code is released under the [GNU LGPL](https://github.com/alkacon/opencms-core/blob/main/LICENSE).
