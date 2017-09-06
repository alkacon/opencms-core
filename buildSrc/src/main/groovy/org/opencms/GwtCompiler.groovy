package org.opencms

import org.gradle.api.tasks.*

/**
 * Invokes the GWT compiler on the OpenCms-core sources.
 * <p>
 * It requires the gradle source sets:
 * <ul>
 *     <li>{@link #sourceSetMain main}</li>
 *     <li>{@link #sourceSetModules modules}</li>
 *     <li>{@link #sourceSetGwt gwt}</li>
 * </ul>
 * During development, use {@link #draft} mode to improve compilation speed.
 *
 * @since 2017-07
 */
class GwtCompiler extends JavaExec {
    @Input
    String opencmsModuleName

    /**
     * GWT module name (e.g.: "{@code org.opencms.ugc.Ugc}").
     * <p>
     * In GWT Jargon, a module is an individual unit of GWT configuration. It bundles
     * together all the configuration settings that the GWT project needs (e.g.: inherited
     * modules, entry point application class name, source path entries, etc. ).
     * <p>
     * Modules are defined in XML and placed into the project package hierarchy. It is
     * recommended that modules appear in the root package of <a href="http://www.gwtproject.org/doc/latest/DevGuideOrganizingProjects.html#DevGuideDirectoriesPackageConventions">the standard project layout</a>.
     */
    @Input
    String gwtModuleName

    /**
     * Create only a draft version. Use only during development to achieve maximum compilation
     * speed.
     * <p>
     * Default value: {@code false}
     */
    @Input
    boolean draft = false

    private SourceSet sourceSetMain

    private SourceSet sourceSetModules

    private SourceSet sourceSetGwt

    /** The directory into which deployable output files will be written (defaults to 'war') */
    private String gwtWarDir

    /** The directory into which extra files, not intended for deployment, will be written */
    private String gwtExtraDir

    GwtCompiler() {
        group = 'gwt'
        description = 'Compiles Java into JavaScript using the GWT compiler'
        main = 'com.google.gwt.dev.Compiler'
        jvmArgs = ['-Dgwt.jjs.permutationWorkerFactory=com.google.gwt.dev.ThreadedPermutationWorkerFactory']
    }

    /**
     * Sets the name of the OpenCms module (e.g.: "{@code org.opencms.ugc}").
     * <p>
     * There should be a corresponding module with manifest under
     * "{@code modules/$opencmsModuleName/resources/manifest.xml}".
     *
     * @param opencmsModuleName name of the OpenCms module
     * @return this
     */
    GwtCompiler setOpencmsModuleName(String opencmsModuleName) {
        this.opencmsModuleName = opencmsModuleName

        gwtWarDir = project.buildDir.toString() + "/gwt/$opencmsModuleName"
        gwtExtraDir = project.buildDir.toString() + "/extra/$opencmsModuleName"
        return this
    }

    @OutputDirectories
    Set<File> getOutputDirs() {
        [new File(gwtWarDir), new File(gwtExtraDir)]
    }

    @InputFiles
    Set<File> getGwtFiles() {
        sourceSetGwt.java.srcDirs
    }

    @InputDirectory
    File getGwtResourceDir() {
        sourceSetGwt.output.resourcesDir
    }

    /**
     * Sets the 'OpenCms Core' source set.
     *
     * @param sourceSetMain the main source set
     * return this
     */
    GwtCompiler sourceSetMain(SourceSet sourceSetMain) {
        this.sourceSetMain = sourceSetMain
        return this
    }

    /**
     * Sets the 'OpenCms Modules' source set.
     *
     * @param sourceSetModules the modules source set
     * return this
     */
    GwtCompiler sourceSetModules(SourceSet sourceSetModules) {
        this.sourceSetModules = sourceSetModules
        return this
    }

    /**
     * Sets the OpenCms GWT source set.
     *
     * @param sourceSetGwt the gwt source set
     * return this
     */
    GwtCompiler sourceSetGwt(SourceSet sourceSetGwt) {
        this.sourceSetGwt = sourceSetGwt
        return this
    }

    @Override
    @TaskAction
    void exec() {
        println '====================================================================================================='
        println "Building GWT resources for opencms module '$opencmsModuleName' (GWT module '$gwtModuleName')"
        println '====================================================================================================='

        // Do not clean output if in draft mode, to allow for incremental compilation
        if (draft) {
            logger.warn "GWT-Compiling DRAFT version. Do not use in production!"
        } else {
            // Clean the output directories
            [gwtWarDir, gwtExtraDir].each { dirName ->
                def dir = new File(dirName)
                if (dir.exists()) {
                    dir.delete()
                    logger.info "dir $dir deleted"
                }
                dir.mkdirs()
            }
        }
        logger.debug "war dir: $gwtWarDir"
        logger.debug "extra dir: $gwtExtraDir"

        if (logger.debugEnabled) {
            def inputFiles = ""
            inputs.files.each { f -> inputFiles += "$f " }
            logger.debug "input files: $inputFiles"
            def outputFiles = ""
            outputs.files.each { f -> outputFiles += "$f " }
            logger.debug "output files: $outputFiles"
        }

        args = [
                '-war', gwtWarDir,
                '-extra', gwtExtraDir,
                '-logLevel', 'ERROR',   // The level of logging detail: ERROR, WARN, INFO, TRACE, DEBUG, SPAM or ALL (defaults to INFO)
                '-localWorkers', 2,    // The number of local workers to use when compiling permutations
                '-style', 'OBFUSCATED',  // Script output style: DETAILED, OBFUSCATED or PRETTY (defaults to OBFUSCATED)
        ]

        if (draft) {
            // Compile quickly with minimal optimizations. (defaults to OFF)
            // Speeds up compile up to 25%
            args '-draftCompile'
            // Specifies the name(s) of the module(s) to compile
            // Compile only the draft version of the module
            args "${gwtModuleName}_Draft"
        } else {
            // Sets the optimization level used by the compiler.  0=none 9=maximum.
            args '-optimize', 9
            // Compile a report that tells the "Story of Your Compile". (defaults to OFF)
            // args '-compileReport'
            // Specifies the name(s) of the module(s) to compile
            // Compile the production version of the module
            args gwtModuleName
        }

        classpath([
                // Java source core
                sourceSetMain.java.srcDirs,
                // Java source modules
                sourceSetModules.java.srcDirs,
                // Java source gwt
                sourceSetGwt.java.srcDirs,
                // Generated resources
                sourceSetGwt.output.resourcesDir,
                // Generated classes
                sourceSetGwt.java.outputDir,
                // Dependecies
                sourceSetGwt.compileClasspath,
        ])

        super.exec()
    }
}
