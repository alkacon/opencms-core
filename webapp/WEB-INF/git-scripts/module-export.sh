#!/bin/bash
# Simplified version of module-checkin.sh meant for a manual Git workflow using the OpenCms JLAN share as a source of exported modules.

getExportedModule(){
	echo $( ls | grep "${module}.zip" | head -n1 )
}

testGitRepository(){
	__pwd=$(pwd)
	cd $REPOSITORY_HOME
	if [[ $? != 0 ]]; then
		echo "ERROR: The GIT repository's main folder \"$REPOSITORY_HOME\" does not exist."
		exit 5
	fi
	echo "Status of the git repository:"
	git status
	echo
	if [[ $? != 0 ]]; then
		echo "ERROR: You have not specified a GIT repository's main folder via\
			\"REPOSITORY_HOME\" ($REPOSITORY_HOME)"
		exit 6
	fi
	if [[ -n $(git status --porcelain) ]]; then
		if [[ $resetHead == 1 || $resetRemoteHead == 1 || $ignoreUnclean == 1 ]]; then
			echo " WARN: found unclean git repository, but continue."
		else
			echo " ERROR: found unclean git repository."
			exit 10
		fi
	else
		echo " * Test ok: found clean git repository at \"$(pwd)\"."
	fi
	cd $__pwd
}

testModuleSourcePath(){
	__pwd=$(pwd)
	cd $moduleSourcePath
	if [[ $? != 0 ]]; then
		echo "ERROR: The specified module source path \"$moduleSourcePath\" does not exist."
		exit 7
	fi
	echo " * Test ok: Module export folder \"$(pwd)\" exists."
	cd $_pwd
}

testModuleTargetPath(){
	__pwd=$(pwd)
	cd $MODULE_TARGET_PATH
	if [[ $? != 0 ]]; then
		echo "ERROR: The specified module target path \"$MODULE_TARGET_PATH\" does not exist."
		exit 8
	fi
	echo " * Test ok: Module main folder \"$(pwd)\" exists."
	cd $__pwd
}


echo
echo "Started automatic export of OpenCms modules over an existing GIT repository."
echo "-------------------------------------------------------------------------------"
echo
echo "Reading command line arguments ..."

#read commandline arguments
while [ "$1" != "" ]; do
	case $1 in
		-m | --modules )       	shift
								modulesToExport=$1
								echo " * Read modules to export: \"$modulesToExport\"."
								;;
		--no-exclude-libs )		excludeLibs=0;
								echo " * Read no-exclude-libs option."
								;;
		--exclude-libs )		excludeLibs=1;
								echo " * Read exclude-libs option."
								;;
		--export-folder )		shift
								moduleSourcePath=$1
								echo " * Read module source path: \"$moduleSourcePath\"."
								;;
		--ignore-unclean )      ignoreUnclean=1
								echo " * Read ignore-unclean option."
								;;
		--no-ignore-unclean )   ignoreUnclean=0
								echo " * Read no-ignore-unclean option."
								;;
		--copy-and-unzip )      copyAndUnzip=1
								echo " * Read copy-and-unzip option."
								;;
		--no-copy-and-unzip )   copyAndUnzip=0
								echo " * Read no-copy-and-unzip option."
								;;
		* )             		configfile=$1
								echo " * Read config file: \"$configfile\"."
	esac
	shift
done

echo
echo "Reading configuration file ..."
if [[ -z "$configfile" ]]; then
	echo " * ERROR: No config file provided."
	exit 3;

fi
source $configfile
echo " * Read file \"$configfile\":"
cat $configfile | awk '$0="   * "$0'

# see http://wiki.bash-hackers.org/syntax/pe#use_a_default_value
MODULES_TO_EXPORT=${MODULES_TO_EXPORT:-$DEFAULT_MODULES_TO_EXPORT}
MODULE_RESOURCES_SUBFOLDER=${MODULE_RESOURCES_SUBFOLDER:-resources/}
MODULE_SOURCE_PATH=${MODULE_SOURCE_PATH:-$MODULE_EXPORT_FOLDER}
MODULE_TARGET_PATH=${MODULE_TARGET_PATH:-$MODULE_PATH}
REPOSITORY_HOME=${REPOSITORY_HOME:-$MODULE_TARGET_PATH}

echo
echo "Values read from configuration:"
echo
echo "* Modules to export : ${MODULES_TO_EXPORT}"
echo "* Module source path: ${MODULE_SOURCE_PATH}"
echo "* Module target path: ${MODULE_TARGET_PATH}"

echo
echo "Setting parameters ..."

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
echo " * Set modules to export: \"$modulesToExport\"."

## set export mode
if [[ -z "$exportMode" ]]; then
	exportMode=$MODULE_EXPORT_MODE
fi
case $exportMode in
	1 )	;;
	* ) exportMode=0
esac
echo " * Set export mode: $exportMode."

## set module export folder
if [[ -z "$moduleSourcePath" ]]; then
	moduleSourcePath=$MODULE_SOURCE_PATH
fi
echo " * Set module export folder: \"$moduleSourcePath\"."

echo
echo "Testing module export folder ..."
testModuleSourcePath


echo
echo "Setting parameters (continued) ..."

## set ignore-unclean
if [[ -z "$ignoreUnclean" ]]; then
	if [[ -z "$GIT_IGNORE_UNCLEAN" ]]; then
		ignoreUnclean=1
	else
		ignoreUnclean=$GIT_IGNORE_UNCLEAN
	fi
fi
echo " * Set ignore-unclean: $ignoreUnclean."

## set copy-and-unzip flag
if [[ -z "$copyAndUnzip" ]]; then
	if [[ -z "$COPY_AND_UNZIP" ]]; then
		copyAndUnzip=1
	else
		copyAndUnzip=$COPY_AND_UNZIP
	fi
fi
echo " * Set copy-and-unzip: $copyAndUnzip."


## set export libs flag
if [[ -z "$excludeLibs" ]]; then
	if [[ -z "$DEFAULT_EXCLUDE_LIBS" ]]; then
		excludeLibs=1
	else
		excludeLibs=$DEFAULT_EXCLUDE_LIBS
	fi
fi
echo " * Set exclude libs flag: $excludeLibs."

# Read associative array from variable MODULE_MAPPINGS, which should have the form "key1=value1 key2=value2..."

declare -A moduleMappings
if [ ! -z "$MODULE_MAPPINGS" ] ; then
	for MAPPING in $MODULE_MAPPINGS ; do
		IFS="=" read k v <<< "$MAPPING"
		moduleMappings[$k]="$v"
	done
	command -v perl || {
		echo "Perl is required when using module mappings."
		exit 15
	}
fi


# test git repository
echo
echo "Testing Git repository and adjusting user information"
testGitRepository

echo
echo "Testing module target path ..."
testModuleTargetPath

echo "Copy and unzip modules ..."
if [ $copyAndUnzip == 1 ]; then

## copy and unzip modules
	for module in $modulesToExport; do
		echo
		echo " * Handling module ${module} ..."
		echo
		cd $moduleSourcePath
		fileName=$(getExportedModule)
		if [[ ! -z "$fileName" ]]; then
			targetModule=${moduleMappings[$module]}
			if [ -z "$targetModule" ] ; then
				targetModule=$module
			fi
			echo "   * Found zip file ${fileName}."
			#switch to project's module path
			cd "${MODULE_TARGET_PATH}"
			#check if a subdirectory for the module exists - if not add it
			if [ ! -d "$targetModule" ]; then
				echo "   * Creating missing module directory \"$targetModule\" under \"$(pwd)\"."
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
				echo "   * Creating missing resources subfolder \"$MODULE_RESOURCES_SUBFOLDER\"\
					under $(pwd)."
				mkdir $MODULE_RESOURCES_SUBFOLDER
			fi
			#if there's a resources subfolder, switch to it
			if [[ -d "$MODULE_RESOURCES_SUBFOLDER" ]]; then
				cd $MODULE_RESOURCES_SUBFOLDER
			fi
			#delete all resources currently checked in in the project
			if [[ "$(pwd)" == "${MODULE_TARGET_PATH}"* ]]; then
				echo "   * Removing old version of the module resources under $(pwd)."
				rm -fr ./{.[^.],}*
				if [[ $? != 0 ]]; then
					echo
					echo "ERROR: Failed to remove all resources under $(pwd)."
					echo "Exit script with exit code 13."
					echo
					exit 13
				fi
			else
				echo "   * ERROR: Something went wrong the current directory \($(pwd)\) is not a\
				subdirectory of the repository's configured modules main folder (${MODULE_TARGET_PATH})."
				exit 4
			fi
			echo "   * Copying "${moduleSourcePath}/${fileName}" to $(pwd) ..."
			#copy the new module .zip
			cp "${moduleSourcePath}/${fileName}" ./
			if [[ $? != 0 ]]; then
				echo
				echo "ERROR: Failed to copy \"${moduleSourcePath}/${fileName}\" to $(pwd)."
				echo "Exit script with exit code 13."
				echo
				exit 13
			fi
			echo "   * Unzipping copied file."
			#unzip it
			unzip -o "${fileName}" | awk '$0="     "$0'
			if [[ $? != 0 ]]; then
				echo
				echo "ERROR: Failed to unzip \"${fileName}\" in $(pwd)."
				echo "Exit script with exit code 13."
				echo
				exit 13
			fi
			if [ ! "$module" == "$targetModule" ] ; then
				echo "Adjusting module name from $module to $targetModule in manifest"
				manifest=$(find -type f -name manifest.xml | head -1)
				echo "CWD=$(pwd)"
				echo "Manifest path: $manifest"
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

			echo "   * Deleting copy of the .zip file."
			#remove the .zip file
			rm "${fileName}"
			#remove lib/ subfolder if necessary
			echo "   * Removing lib folder ..."
			if [[ $excludeLibs == 1 ]]; then
				libFolder="system/modules/${module}/lib"
				if [[ -d "$libFolder" ]]; then
					rm -fr "$libFolder"
					echo "     * ... lib/ folder \"$(pwd)/$libFolder\" removed."
				else
					echo "     * ... lib/ folder \"$(pwd)/$libFolder\" does not exist. Do nothing."
							fi
			else
				echo "     * ... lib folder shall not be removed. Do nothing."
			fi
		else
			echo "   ! WARN: Skipped module $module because the zip file was not found."
		fi
	done

else
	echo " * Copy and unzip is disabled. Not copying and unzipping modules."
fi

echo
# commit changes
cd $REPOSITORY_HOME

echo
echo
echo "Script completed successfully."
echo "------------------------------"
echo

exit 0
