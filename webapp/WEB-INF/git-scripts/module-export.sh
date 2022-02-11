#!/bin/bash
#
# Script to export modules from a mounted OpenCms.
# To be used for a manual Git workflow.
# The modules are transfered as ZIP files and copied over an existing directory structure.
#

##################
#
# Display error message ${1} and then exit the script with code ${2}.
# If ${2} is not provided then do not exit.
#
echoError() {
	echo ""
	echo -e "${red}ERROR: ${bold}${1}${normal}"
	if [ -n "${2}" ]; then
		echo ""
		exit ${2}
	fi
}

##################
#
# Display message ${1} only if --verbose is enabled.
#
echoVerbose() {
	if [ -n "${OPT_VERBOSE}" ]; then
		echo "${1}${normal}"
	fi
}

##################
#
# Set env variable for options.
#
setOptions() {

	# check if stdout is a terminal...
	if test -t 1; then
		# see if it supports colors...
		NCOLORS=$(tput colors)
		if test -n "${NCOLORS}" && test ${NCOLORS} -ge 8; then
			bold="$(tput bold)"
			underline="$(tput smul)"
			standout="$(tput smso)"
			normal="$(tput sgr0)"
			black="$(tput setaf 0)"
			red="$(tput setaf 1)"
			green="$(tput setaf 2)"
			yellow="$(tput setaf 3)"
			blue="$(tput setaf 4)"
			magenta="$(tput setaf 5)"
			cyan="$(tput setaf 6)"
			white="$(tput setaf 7)"
		fi
	fi

	#set config file default name
	configfile="module-export.conf"

	#read commandline arguments
	while [ "$1" != "" ]; do
		case $1 in
			-v | --verbose )		OPT_VERBOSE="true"
									echoVerbose "* Activated option: --verbose"
									;;
			-m | --modules )		shift
									modulesToExport=$1
									echoVerbose "* Modules to export: \"$modulesToExport\""
									;;
			-mo )					shift
									modulesExportVar=$1
									echoVerbose "* Modules to export defined by variable: \"$modulesExportVar\""
									;;
			--export-folder )		shift
									moduleSourcePath=$1
									echoVerbose "* Module source path: \"$moduleSourcePath\""
									;;
			--no-exclude-libs )		excludeLibs=0;
									echoVerbose "* Activated option: --no-exclude-libs"
									;;
			--exclude-libs )		excludeLibs=1;
									echoVerbose "* Activated option: --exclude-libs"
									;;
			--ignore-unclean )		ignoreUnclean=1
									echoVerbose "* Activated option: --ignore-unclean"
									;;
			--no-ignore-unclean )	ignoreUnclean=0
									echoVerbose "* Activated option: --no-ignore-unclean"
									;;
			--copy-and-unzip )		copyAndUnzip=1
									echoVerbose "* Activated option: --copy-and-unzip"
									;;
			-t | --no-copy-and-unzip )	copyAndUnzip=0
									echoVerbose "* Activated option: --no-copy-and-unzip (test mode)"
									;;
			* ) 				 	configfile=$1
									echoVerbose "* Configuration file: \"$configfile\"."
		esac
		shift
	done
}

getExportedModule(){
	echo $( ls | grep "${module}.zip" | head -n1 )
}

testGitRepository(){
	# test git repository
	echoVerbose
	echoVerbose "Testing git repository and adjusting user information"
	__pwd=$(pwd)
	cd $REPOSITORY_HOME
	if [[ $? != 0 ]]; then
		echoError "The git repository's main folder \"$REPOSITORY_HOME\" does not exist." 5
	fi
	echoVerbose "Status of the git repository:"
	git status > /dev/null
	echoVerbose
	if [[ $? != 0 ]]; then
		echoError "You have not specified a git repository's main folder via\
			\"REPOSITORY_HOME\" ($REPOSITORY_HOME)" 6
	fi
	if [[ -n $(git status --porcelain) ]]; then
		if [[ $resetHead == 1 || $resetRemoteHead == 1 || $ignoreUnclean == 1 ]]; then
			echo "${yellow}${bold}* WARNING:${normal}${yellow} Unclean local git repository detected - continuing anyway!${normal}"
		else
			echoError "Unclean git repository." 10
		fi
	else
		echoVerbose "* Test ok: found clean git repository at \"$(pwd)\"."
	fi
	cd $__pwd
}

testModuleSourcePath(){
	__pwd=$(pwd)
	cd $moduleSourcePath
	if [[ $? != 0 ]]; then
		echoError "The specified module source path \"$moduleSourcePath\" does not exist!" 7
	fi
	echoVerbose
	echo "* Modules exported from: ${cyan}$(pwd)${normal}"
	cd $_pwd
}

testModuleTargetPath(){
	__pwd=$(pwd)
	cd $MODULE_TARGET_PATH
	if [[ $? != 0 ]]; then
		echoError "The specified module target path \"$MODULE_TARGET_PATH\" does not exist!" 8
		exit 8
	fi
	echo "* Modules exported to  : ${cyan}$(pwd)${normal}"
	cd $__pwd
}

##################
#
# Main Script starts here.
#

# Initialize command line parameters
setOptions "${@}"

echo
echo "${green}${bold}Exporting modules from OpenCms to local git repository.${normal}"
echo

if [[ -z "$configfile" ]]; then
	echoError "No config file provided!" 3
fi
if [[ ! -f "$configfile" ]]; then
	echoError "Config file '${configfile}' does not exit!" 3
fi

source $configfile
echoVerbose "* Contents of configuration file \"$configfile\":"

if [ -n "${OPT_VERBOSE}" ]; then
	cat $configfile | awk '$0="   * "$0'
fi

if [[ ! -z "$modulesExportVar" ]]; then
	MODULES_TO_EXPORT=${!modulesExportVar}
	if [[ -z "$MODULES_TO_EXPORT" ]]; then
		echoError "No modules defined by variable \"$modulesExportVar\"!" 3
	fi
fi

# see http://wiki.bash-hackers.org/syntax/pe#use_a_default_value
MODULES_TO_EXPORT=${MODULES_TO_EXPORT:-$DEFAULT_MODULES_TO_EXPORT}
MODULE_RESOURCES_SUBFOLDER=${MODULE_RESOURCES_SUBFOLDER:-resources/}
MODULE_SOURCE_PATH=${MODULE_SOURCE_PATH:-$MODULE_EXPORT_FOLDER}
MODULE_TARGET_PATH=${MODULE_TARGET_PATH:-$MODULE_PATH}
REPOSITORY_HOME=${REPOSITORY_HOME:-$MODULE_TARGET_PATH}

echoVerbose
echoVerbose "Values read from configuration:"
echoVerbose
echoVerbose "* Modules to export : ${MODULES_TO_EXPORT}"
echoVerbose "* Module source path: ${MODULE_SOURCE_PATH}"
echoVerbose "* Module target path: ${MODULE_TARGET_PATH}"

echoVerbose
echoVerbose "Setting parameters ..."

## set modules to export
if [[ -z "$modulesToExport" ]]; then
	modulesToExport=$MODULES_TO_EXPORT
else
	newModules=""
	for module in $modulesToExport; do
		if [[ ! ($MODULES_TO_EXPORT =~ (^| )$module($| ) ) ]]; then
			newModules="${newModules}${module} "
		fi
	done
	if [[ $newModules != "" ]]; then
		sed -i "/^MODULES_TO_EXPORT/s/=\"/=\"$newModules/" $configfile
		echo
		echo " * Added new modules \"$newModules\" to the config file."
		echo
	fi
fi
echoVerbose "* Set modules to export: \"$modulesToExport\"."

## set export mode
if [[ -z "$exportMode" ]]; then
	exportMode=$MODULE_EXPORT_MODE
fi
case $exportMode in
	1 )	;;
	* ) exportMode=0
esac
echoVerbose "* Set export mode: $exportMode."

## set module export folder
if [[ -z "$moduleSourcePath" ]]; then
	moduleSourcePath=$MODULE_SOURCE_PATH
fi
echoVerbose "* Set module export folder: \"$moduleSourcePath\"."

testModuleSourcePath

echoVerbose
echoVerbose "Setting parameters (continued) ..."

## set ignore-unclean
if [[ -z "$ignoreUnclean" ]]; then
	if [[ -z "$GIT_IGNORE_UNCLEAN" ]]; then
		ignoreUnclean=1
	else
		ignoreUnclean=$GIT_IGNORE_UNCLEAN
	fi
fi
echoVerbose "* Set ignore-unclean: $ignoreUnclean."

## set copy-and-unzip flag
if [[ -z "$copyAndUnzip" ]]; then
	if [[ -z "$COPY_AND_UNZIP" ]]; then
		copyAndUnzip=1
	else
		copyAndUnzip=$COPY_AND_UNZIP
	fi
fi
echoVerbose "* Set copy-and-unzip: $copyAndUnzip."


## set export libs flag
if [[ -z "$excludeLibs" ]]; then
	if [[ -z "$DEFAULT_EXCLUDE_LIBS" ]]; then
		excludeLibs=1
	else
		excludeLibs=$DEFAULT_EXCLUDE_LIBS
	fi
fi
echoVerbose "* Set exclude libs flag: $excludeLibs."

# Read associative array from variable MODULE_MAPPINGS, which should have the form "key1=value1 key2=value2..."

declare -A moduleMappings
if [ ! -z "$MODULE_MAPPINGS" ] ; then
	for MAPPING in $MODULE_MAPPINGS ; do
		IFS="=" read k v <<< "$MAPPING"
		moduleMappings[$k]="$v"
	done
	command -v perl || {
		echoError "Perl is required when using module mappings." 15
	}
fi

testGitRepository

testModuleTargetPath

echo
echo "Modules to export:"
for module in $modulesToExport; do
	echo "- ${module}"
done

echo
if [ $copyAndUnzip == 1 ]; then
	## copy and unzip modules
	unzipOptions=""
	if [ ! -n "${OPT_VERBOSE}" ]; then
		unzipOptions="-qq"
	fi
	for module in $modulesToExport; do
		echoVerbose
		echo "* Exporting module: ${cyan}${module}${normal}"
		echoVerbose
		cd $moduleSourcePath
		fileName=$(getExportedModule)
		if [[ ! -z "$fileName" ]]; then
			targetModule=${moduleMappings[$module]}
			if [ -z "$targetModule" ] ; then
				targetModule=$module
			fi
			echoVerbose "   * Found zip file ${fileName}."
			#switch to project's module path
			cd "${MODULE_TARGET_PATH}"
			#check if a subdirectory for the module exists - if not add it
			if [ ! -d "$targetModule" ]; then
				echoVerbose "   * Creating missing module directory \"$targetModule\" under \"$(pwd)\"."
				mkdir $targetModule
			fi
			#go to the modules' subfolder in the project
			cd $targetModule
			#remove leading "/" from MODULE_RESOURCES_SUBFOLDER, if necessary
			if [[ (! -z "$MODULE_RESOURCES_SUBFOLDER") && (${MODULE_RESOURCES_SUBFOLDER:0:1} == "/") ]]; then
				MODULE_RESOURCES_SUBFOLDER=${MODULE_RESOURCES_SUBFOLDER:1}
			fi
			#if necessary, add the resources' subfolder of the module
			if [[ (! -z "$MODULE_RESOURCES_SUBFOLDER") && (! -d "$MODULE_RESOURCES_SUBFOLDER") ]]; then
				echoVerbose "   * Creating missing resources subfolder \"$MODULE_RESOURCES_SUBFOLDER\"\
					under $(pwd)."
				mkdir $MODULE_RESOURCES_SUBFOLDER
			fi
			#if there's a resources subfolder, switch to it
			if [[ -d "$MODULE_RESOURCES_SUBFOLDER" ]]; then
				cd $MODULE_RESOURCES_SUBFOLDER
			fi
			#delete all resources currently checked in in the project
			if [[ "$(pwd)" == "${MODULE_TARGET_PATH}"* ]]; then
				echoVerbose "   * Removing old version of the module resources under $(pwd)."
				rm -fr ./{.[^.],}*
				if [[ $? != 0 ]]; then
					echoError "Failed to remove all resources under $(pwd)." 13
				fi
			else
				echoError "   * Something went wrong the current directory \($(pwd)\) is not a\
				subdirectory of the repository's configured modules main folder (${MODULE_TARGET_PATH})." 4
			fi
			echoVerbose "   * Copying "${moduleSourcePath}/${fileName}" to $(pwd) ..."
			#copy the new module .zip
			cp "${moduleSourcePath}/${fileName}" ./
			if [[ $? != 0 ]]; then
				echoError "Failed to copy \"${moduleSourcePath}/${fileName}\" to $(pwd)." 13
			fi
			echoVerbose "   * Unzipping copied file."
			#unzip it
			unzip -o ${unzipOptions} "${fileName}" | awk '$0="     "$0'
			if [[ $? != 0 ]]; then
				echoError "Failed to unzip \"${fileName}\" in $(pwd)." 13
			fi
			if [ ! "$module" == "$targetModule" ] ; then
				echoVerbose "Adjusting module name from $module to $targetModule in manifest"
				manifest=$(find -type f -name manifest.xml | head -1)
				echoVerbose "CWD=$(pwd)"
				echoVerbose "Manifest path: $manifest"
				if [ ! -z "$manifest" ] ; then
					export targetModule
					perl -ne 'if (/<module>/../<\/module>/) { s#<name>.*?</name>#<name>$ENV{"targetModule"}</name>#; } ; print;' < $manifest > "${manifest}.tmp"
					tmpContent=$(cat "${manifest}.tmp")
					if [ ! -z "$tmpContent" ] ; then
						rm "$manifest"
						mv "${manifest}.tmp" "$manifest"
					fi
				fi
			fi

			echoVerbose "   * Deleting copy of the .zip file."
			#remove the .zip file
			rm "${fileName}"
			#remove lib/ subfolder if necessary
			echoVerbose "   * Removing lib folder ..."
			if [[ $excludeLibs == 1 ]]; then
				libFolder="system/modules/${module}/lib"
				if [[ -d "$libFolder" ]]; then
					rm -fr "$libFolder"
					echoVerbose "     * ... lib/ folder \"$(pwd)/$libFolder\" removed."
				else
					echoVerbose "     * ... lib/ folder \"$(pwd)/$libFolder\" does not exist. Do nothing."
							fi
			else
				echoVerbose "     * ... lib folder shall not be removed. Do nothing."
			fi
		else
			echo "${yellow}${bold}* WARNING:${normal}${yellow} Skipped module $module - module / zip file not found!${normal}"
		fi
	done

else
	echo "${yellow}Test mode - Module copy and unzip is disabled.${normal}"
fi

# commit changes
cd $REPOSITORY_HOME

echo
echo "${bold}Export completed successfully!${normal}"
echo

exit 0
