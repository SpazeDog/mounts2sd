#!/system/bin/sh
#####
# @id 2013110101
# @version 6.0.20
#####
# This file is part of the Mounts2SD Project: https://github.com/spazedog/mounts2sd
#  
# Copyright (c) 2013 Daniel Bergløv
#
# Mounts2SD is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# Mounts2SD is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with Mounts2SD. If not, see <http://www.gnu.org/licenses/>
#####

export iShell="$1"
export iBusybox="$2"

export iLogName=Mounts2SD
export iDirTmp=/mounts2sd-tmp
export iDirSdext=/sd-ext
export iDirProperty=/data/property
export iTimestamp=$(date "+%s")

export iEnviroment=false
export iSimpleExport=false
export iLogReset=false

# ===========================================================================================
# -------------------------------------------------------------------------------------------
# Returns a list of all property names and default values
# 
ListProperties() {
$_cat <<EOF > $iDirTmp/function.list_properties.tmp
move_apps 1
move_sysapps 0
move_dalvik 0
move_data 0
move_libs 0
move_media 0
move_system 0
enable_cache 0
enable_swap 0
set_swap_level 0
enable_sdext_journal $($_test -z "$_tune2fs" && $_echo 0 || $_echo -1)
enable_debug 0
set_sdext_fstype $($_grep ext4 /proc/filesystems | $_grep -q -v 'nodev' && $_echo ext4 || $_echo auto)
run_sdext_fschk $($_test -z "$_e2fsck" && $_echo 0 || $_echo 1)
set_storage_threshold $($_test -z "$_sqlite3" && $_echo 0 || $_echo 1)
set_zram_compression $($_test -e /proc/swaps && ( $_test -e /system/lib/modules/ramzswap.ko || $_test -e /system/lib/modules/zram.ko || $_test -e /sys/block/zram0 ) && $_echo 18 || $_echo 0)
set_emmc_readahead 512
set_emmc_scheduler cfq
set_immc_readahead $($_test -e /proc/mtd && $_echo 4 || $_echo 128)
set_immc_scheduler $($_test -e /proc/mtd && $_echo deadline || $_echo cfq)
disable_safemode 1
EOF

# Some shell do not support "< <$(ListProperties)"
# This is a work-around which can be used with "< $(ListProperties)"
$_echo $iDirTmp/function.list_properties.tmp
}

# ===========================================================================================
# -------------------------------------------------------------------------------------------
# Returns a list of all commands needed by the script.
#
# chmod and chown will use toolbox whenever possible. Not all toolbox versions include these commands.
# The reason for using toolbox is that some busybox versions can't set for an example ownership based on
# names, only ID's. And since we often in this script use infomation from init.rc, which uses names most of the time,
# we need to make sure that this will work.
# 
ListCommands() {
$_cat <<'EOF' > $iDirTmp/function.list_commands.tmp
cat
cut
echo
tee
test
sleep
sed
grep
awk
chmod toolbox
chown toolbox
pgrep
find
ls
ln
df
du
mount
umount
tr
id
tail
kill
killall
basename
rm
cp
mv
mkdir
touch
seq
date
swapon
mkswap
insmod
readlink
blkid
e2fsck binary
tune2fs binary
sqlite3 binary
rzscontrol binary
EOF

$_echo $iDirTmp/function.list_commands.tmp
}

# ===========================================================================================
# -------------------------------------------------------------------------------------------
# This function will return all the mkdir lines from every init.rc file available.
# Each line will contain the folder path, user, group and permission.
# 
ListFolderInfo() {
    if ! $_test -f $iDirTmp/function.list_folder_info.tmp; then
        local lFile
        local lFilePartType
        local lFilePartPath
        local lFilePartPerm
        local lFilePartUser
        local lFilePartGroup

        for lFile in /*.rc; do
            $_grep -e '^[ \t]*mkdir[ \t]' $lFile | $_tr -s ' ' | while read lFilePartType lFilePartPath lFilePartPerm lFilePartUser lFilePartGroup; do
                if ! $_test -z "$lFilePartUser" && ! $_test -z "$lFilePartGroup"; then
                    $_echo "$lFilePartPath $lFilePartPerm $lFilePartUser $lFilePartGroup" >> $iDirTmp/function.list_folder_info.tmp.1
                fi
            done
        done

        # Remove dupplicate paths
        $_awk '!_[$1]++' $iDirTmp/function.list_folder_info.tmp.1 >> $iDirTmp/function.list_folder_info.tmp
        $_rm -rf $iDirTmp/function.list_folder_info.tmp.1
    fi

    $_echo $iDirTmp/function.list_folder_info.tmp
}

# ===========================================================================================
# -------------------------------------------------------------------------------------------
# This function will return all init defined folders from every init.rc file available.
# 
ListFolders() {
    local lFolderPath
    local lFolderRest
    local lFolders

    if ! $_test -f $iDirTmp/function.list_folders.tmp; then
        while read lFolderPath lFolderRest; do
            $_echo $lFolderPath >> $iDirTmp/function.list_folders.tmp

        done < $(ListFolderInfo)
    fi

    $_echo $iDirTmp/function.list_folders.tmp
}

# ===========================================================================================
# -------------------------------------------------------------------------------------------
# This function returns all export variable lines from all init.rc files.
# This is used to setup the script enviroment with all global variables needed by different binaries
# 
ListExports() {
    local lFile
    local lFilePartExport
    local lFilePartName
    local lFilePartValue

    if ! $_test -f $iDirTmp/function.list_exports.tmp; then
        for lFile in /*.rc; do
            $_grep -e '^[ \t]*export[ \t]' $lFile | $_tr -s ' ' | while read lFilePartExport lFilePartName lFilePartValue; do
                if ! $_test -z "$lFilePartExport"; then
                    $_echo "$lFilePartName $lFilePartValue" >> $iDirTmp/function.list_exports.tmp
                fi
            done
        done
    fi

    $_echo $iDirTmp/function.list_exports.tmp
}

# ===========================================================================================
# -------------------------------------------------------------------------------------------
# This function is used prepare the enviroment for the script. It will locate the busybox
# binary, check SuperUser rights, create command variables that links to busybox, toolbox or a separate binary,
# prepare the script properties and more... 
# 
ProcessEnviroment() {
    local lMessage

    case "$iBusybox" in
        "")  
            lMessage="You need to have busybox installed on this device in order to run Mounts2SD!"
        ;;

        *) 
            for tmpBusybox in $iBusybox busybox; do
                iBusybox=$tmpBusybox

                if $iBusybox test true > /dev/null 2>&1; then
                    # This is a compatibillity test which will check to see if the current busybox will work with this script. This test is based on a lot of different reported issues that has been gattered over a long period of time, and it has proven to fetch any busybox version that will cause problems.
                    if $iBusybox test "52223abccbcf00eb3c81300545d63126" != "`( $iBusybox [ 1 -eq 0 ] || $iBusybox [ 0 -eq 1 ] ) && $iBusybox echo no || $iBusybox echo 'remove this part okay:bla=no-4' | $iBusybox grep -e '.*bla=no-[1-9]*' | $iBusybox sed -e 's/^remove //' | $iBusybox awk '{print $3}' | $iBusybox cut -d ':' -f1 | $iBusybox md5sum | $iBusybox awk '{print $1}'`"; then
                        # Do NOT break here. We might have more busybox paths to search
                        lMessage="The current busybox is outdated!"

                    elif $iBusybox test "`$iBusybox id | $iBusybox sed -ne "s/^uid=\([0-9]*\)[^0-9].*$/\1/p"`" != "0"; then
                        lMessage="The script has been invoked without super user permissions!"; break

                    else
                        # This has not yet been created when we call 'ListCommands'
                        export _cat="$iBusybox cat"
                        export _echo="$iBusybox echo"

                        # This has not yet been created when we call 'ExportVar' and 'Session'
                        export _test="$iBusybox test"

                        # Check whether we need eval or not to export variables
                        export $(echo iSimpleExport)=true

                        Session set test_session test

                        if ! $iBusybox test "$(Session get test_session)" = "test"; then
                            lMessage="The device shell environment does not support dynamic variables!"

                        else
                            # We need to save the current state for later usage. We will add them to our session storage later
                            local lModRoot=$($iBusybox grep ' / ' /proc/mounts | $iBusybox sed 's/.*[ ,]\(r[ow]\)[ ,].*/\1/') && $iBusybox mount -o remount,rw /
                            local lModSystem=$($iBusybox grep ' /system ' /proc/mounts | $iBusybox sed 's/.*[ ,]\(r[ow]\)[ ,].*/\1/') && $iBusybox mount -o remount,rw /system

                            if ! $iBusybox grep ' /system ' /proc/mounts | $iBusybox grep -q 'rw'; then
                                $iBusybox blockdev --setrw $($iBusybox grep ' /system ' /proc/mounts | $iBusybox cut -d ' ' -f 0)
                                $iBusybox mount -o remount,rw /system
                            fi

                            # This is needed by devices including 'mksh'.
                            # If this is missing, the below error will be thrown when piping output to 'read' or 'cat'
                            # Error = can't create temporary file /sqlite_stmt_journals/mksh.(random): No such file or directory
                            $iBusybox test ! -d /sqlite_stmt_journals && $iBusybox mkdir /sqlite_stmt_journals

                            # We need this directory before preparing anything
                            $iBusybox test ! -d $iDirTmp && ( $iBusybox mkdir -p $iDirTmp || $iBusybox mkdir $iDirTmp )

                            local lCommand
                            local lType

                            while read lCommand lType; do
                                case "$lType" in
                                    "toolbox")
                                        # The -h will make sure that the command does not end up with an un-ending sub-process
                                        /system/bin/toolbox "$lCommand" -h > /dev/null 2>&1

                                        if $iBusybox test $? -ne 255; then
                                            ExportVar _$lCommand "/system/bin/toolbox $lCommand"

                                        elif $iBusybox which "$lCommand" > /dev/null; then
                                            ExportVar _$lCommand $($iBusybox which $lCommand)

                                        else
                                            ExportVar _$lCommand "$iBusybox $lCommand"
                                        fi
                                    ;;

                                    "binary")
                                        if $iBusybox which "$lCommand" > /dev/null; then
                                            ExportVar _$lCommand $($iBusybox which $lCommand)

                                        else
                                            if $iBusybox test ! -z "`$iBusybox --list | $iBusybox grep -e "^$lCommand$"`"; then
                                                ExportVar _$lCommand "$iBusybox $lCommand"
                                            fi
                                        fi
                                    ;;

                                    *)
                                        ExportVar _$lCommand "$iBusybox $lCommand"
                                    ;;
                                esac

                            done < $(ListCommands)

                            local lExportName
                            local lExportValue

                            while read lExportName lExportValue; do
                                # Some binaries like sqlite3 and so, needs a path variable to it's library files in order to work.
                                # Some init.d implementations like CM's internal sysinit does not provide anything other than PATH in /system/bin/sysinit, 
                                # and Android's init system does not parse these along when using the "exec" from within init.rc
                                ExportVar $lExportName "$lExportValue"

                            done < $(ListExports)

                            # Make sure that this was created by init
                            TouchDir --skip-existing --user 0 --group 0 --mod 0700 $iDirProperty

                            # We will also be needing this
                            TouchDir --skip-existing --user 1000 --group 1000 --mod 0771 $iDirSdext

                            Session set modRoot $lModRoot
                            Session set modSystem $lModSystem

                            # This should be at the top, but for obvious reasons it can't
                            Log v "Setting up the script enviroment"
                            Log v "Using the shell environment '$iShell'"
                            Log v "Using the busybox binary located at '$iBusybox'"

                            $_rm -rf /data/m2sd.fallback.log 2> /dev/null

                            local lPropName
                            local lPropValue

                            # Since script version 6.x, R-Mount is no longer available. This below code is used as backward compatibility with updating from older versions
                            if $_test -e $iDirProperty/m2sd.enable_reversed_mount && $_test "`$_cat $iDirProperty/m2sd.enable_reversed_mount`" = "1"; then
                                Log i "Found old reversed mount option which is no longer supported. Correcting an conflicts"

                                $_rm -rf $iDirProperty/m2sd.enable_reversed_mount

                                for lPropName in move_apps move_dalvik move_data; do
                                    if $_test -e $iDirProperty/m2sd.$lPropName; then
                                        lPropValue=$($_test "`$_cat $iDirProperty/m2sd.$lPropName`" = "1" && $_echo 0 || $_echo 1)
                                        $_echo $lPropValue > $iDirProperty/m2sd.$lPropName
                                    fi
                                done
                            fi

                            # Make sure that all the properties exist and add them all to our session system so that we don't have to re-open files all the time
                            while read lPropName lPropValue; do
                                if ! $_test -e $iDirProperty/m2sd.$lPropName; then
                                    Log v "Creating missing property '$iDirProperty/m2sd.$lPropName'"
                                    $_echo $lPropValue > $iDirProperty/m2sd.$lPropName

                                elif $_test -z "`$_cat $iDirProperty/m2sd.$lPropName`"; then
                                    Log i "The property '$iDirProperty/m2sd.$lPropName' contains empty value. Resetting it to default value '$lPropValue'"
                                    $_echo $lPropValue > $iDirProperty/m2sd.$lPropName
                                fi

                                Session set prop_$lPropName $($_cat $iDirProperty/m2sd.$lPropName)

                            done < $(ListProperties)

                            export iEnviroment=true && return 0
                        fi

                        break
                    fi
                fi
            done
        ;;
    esac

    log -p e -t Mounts2SD "$lMessage"
    echo "E/$lMessage" >> /data/m2sd.fallback.log

    return 1
}

# ===========================================================================================
# -------------------------------------------------------------------------------------------
# There are two ways of implementing init.d to a ROM. One is to do an internal execution
# of the init.d folder from init while the other is adding a service to do the same. 
# Adding it as a service has it's problems when it comes to sd-ext script because the
# phone will continue it's boot process while init.d is running in a sub-process. 
# This hack will help with this type of implementation by killing the system_server 
# every time it is started and thereby holding off on the services we do not want running
# while handling the content. It is not pretty, but it is the only way as we cannot change
# the ramdisk at runtime. 
#
ProcessBoot() {
    if $_pgrep /system/bin/servicemanager; then
        $_echo service > $iDirTmp/init.type

        Log v "The Android process /system/bin/servicemanager has been detected"

        if $_test $(Session get prop_disable_safemode 0) -eq 1; then
            Log v "Stopping Android's System Server process"

            # Start the sub-process which will stall all system services
            (
                while :; do
                    if $_test -f $iDirTmp/script.result; then
                        Log v "Restarting Android's System Server process"
                        start zygote
                        break

                    elif $_pgrep system_server; then
                        stop zygote
                    fi

                    $_sleep 1
                done
            ) & 

        else
            local lPropName

            Log i "The script has entered into safe-mode"

            # These options is known to cause issues when core services are running.
            # So we will disable these in safe-mode
            for lPropName in move_dalvik move_data move_libs move_media move_system; do
                $_echo 0 > $iDirProperty/m2sd.$lPropName
                Session set prop_$lPropName 0
            done

            $_echo 1 > $iDirTmp/safemode.result
            Session set status_safemode 1
        fi

    else
        $_echo internal > $iDirTmp/init.type
    fi

    Session set init_type $($_cat $iDirTmp/init.type)
}

# ===========================================================================================
# -------------------------------------------------------------------------------------------
# This function will locate the IMMC and EMMC devices along with sd-ext, swap, system, cache and data.
# It will optimize the system, data and cache mount parameters, set the readahead on IMMC and EMMC
# along with setting prefered Scheduler on both.
#
ProcessMMC() {
    local mmcType
    local mmcPartition
    local mmcDevice
    local mmcDeviceMM
    local mmcLegacy=false
    local i

    Log v "Collecting MMC partition information"

    for mmcNum in `$_seq 0 9`; do
        if $_test -e /sys/block/mmcblk$mmcNum/device/type; then
            mmcType=$($_cat /sys/block/mmcblk$mmcNum/device/type)
            mmcDevice=/dev/block/mmcblk$mmcNum
            mmcDeviceMM=$($_ls -l $mmcDevice | $_tr -s ' ' | $_sed -ne "s/^.*[ ]\([0-9]*\),[ ]\([0-9]*\)[ ].*$/\1:\2/p")

            case "$mmcType" in
                "SD")
                    Log v "The external MMC was located at '$mmcDevice'"

                    Session set device_emmc $mmcDevice
                    Session set device_emmc_mm $mmcDeviceMM

                    for i in `$_seq 2 3`; do
                        if $_test -b ${mmcDevice}p$i; then
                            case "$i" in
                                "2")
                                    Log v "Located the sd-ext partition at '${mmcDevice}p$i'"
                                    Session set device_sdext ${mmcDevice}p$i
                                ;;

                                "3")
                                    Log v "Located the SWAP partition at '${mmcDevice}p$i'"
                                    Session set device_swap ${mmcDevice}p$i
                                ;;
                            esac
                        fi
                    done
                ;;

                "MMC")
                    Log v "The internal MMC was located at '$mmcDevice'"

                    Session set device_immc $mmcDevice
                    Session set device_immc_mm $mmcDeviceMM
                ;;
            esac
        fi
    done

    if ! Session check device_immc; then
        # The main MTD entry resides in /dev/block/mtdblock0
        if $_test -e /dev/block/mtdblock0; then
            Log v "The internal MMC was located at '/dev/block/mtdblock0'"
            Session set device_immc /dev/block/mtdblock0
            Session set device_immc_mm $($_ls -l /dev/block/mtdblock0 | $_tr -s ' ' | $_sed -ne "s/^.*[ ]\([0-9]*\),[ ]\([0-9]*\)[ ].*$/\1:\2/p")

        elif $_test -e /dev/block/bml0!c; then
            Log v "The internal MMC was located at '/dev/block/bml0!c'"
            Session set device_immc /dev/block/bml0!c
            Session set device_immc_mm $($_ls -l /dev/block/bml0!c | $_tr -s ' ' | $_sed -ne "s/^.*[ ]\([0-9]*\),[ ]\([0-9]*\)[ ].*$/\1:\2/p")
        fi

        mmcLegacy=true
    fi

    for mmcPartition in system data cache; do
        mmcDevice=$($_grep '/dev/' /proc/mounts | $_grep " /$mmcPartition " | $_awk '{print $1}')

        if $_test -b $mmcDevice; then
            Log v "Located the '$mmcPartition' partition at '$mmcDevice'"
            Session set device_$mmcPartition $mmcDevice
        fi
    done

    Log v "Optimizing MMC devices and partitions"

    local lReadahead
    local lScheduler
    local lDeviceScheduler
    local lDevice

    for mmcDevice in immc emmc system data cache; do
        lDevice=$(Session get device_$mmcDevice)

        if Session check device_${mmcDevice}_mm; then
            mmcDeviceMM=/sys/devices/virtual/bdi/$(Session get device_${mmcDevice}_mm)/read_ahead_kb

            if $_test -e $mmcDeviceMM; then
                lReadahead=$(Session get prop_set_${mmcDevice}_readahead)

                Log v "Setting readahead on '$lDevice' to '${lReadahead}kb'"
                $_echo $lReadahead > $mmcDeviceMM
                $_chmod 0444 $mmcDeviceMM

            else
                Log w "Could not change readahead on '$lDevice'. Device was not found!"
            fi

            lDeviceScheduler=/sys/block/$($_basename $lDevice)/queue/scheduler

            if $_test -e $lDeviceScheduler; then
                lScheduler=$(Session get prop_set_${mmcDevice}_scheduler)

                if $_grep -q $lScheduler $lDeviceScheduler; then
                    Log v "Setting scheduler on '$lDevice' to '$lScheduler'"
                    $_echo $lScheduler > $lDeviceScheduler
                    $_chmod 0444 $lDeviceScheduler

                else
                    Log w "Could not change scheduler on '$lDevice'. The scheduler type '$lScheduler' is not supported!"
                fi

            else
                Log w "Could not change scheduler on '$lDevice'. Device was not found!"
            fi
        fi

        if $_test "$mmcDevice" != "immc" && $_test "$mmcDevice" != "emmc"; then
            if ! mmcLegacy; then
                Log v "Setting optimized mount parameters on '$lDevice'"
                $_mount -o remount,noatime,nodiratime,relatime /$mmcDevice
            fi
        fi
    done
}

# ===========================================================================================
# -------------------------------------------------------------------------------------------
# This function handles the sd-ext partition. It run a file system check, handle file system
# journal and mount the partition
# 
ProcessStorage() {
    if Session check device_sdext; then
        local lDevice=$(Session get device_sdext)
        local lFsType=$(Session get prop_set_sdext_fstype)
        local lMountParamsFull="noatime,nodiratime,relatime,noauto_da_alloc,data=writeback,commit=15,barrier=1,nouser_xattr,errors=continue,nosuid,nodev"
        local lMountParamsLimited="noatime,nodiratime,relatime,nosuid,nodev"
        local lStatus

        if $_test "`$_grep "$lDevice" /proc/mounts | $_tr -s ' ' | $_cut -d ':' -f1`" = "/data"; then
            Log e "The device '$lDevice' is beeing used as the /data partition. Skipping sd-ext handling to avoid conflicts!"

        else
            if $_grep -q "$lDevice" /proc/mounts; then
                Log i "The device '$lDevice' has been mounted by some other process. Unmounting in order to continue"
                DetachMount "$lDevice"
            fi

            if $_test $(Session get prop_run_sdext_fschk 0) -eq 1; then
                if ! $_test -z "$_e2fsck"; then
                    Log v "Running a file system check on '$lDevice'"
                    lShellError=$($_e2fsck -y -D $lDevice 2>&1); lStatus=$?

                    if $_test $lStatus -gt 0 && $_test $lStatus -lt 4; then
                        Log i "Error detected while checking '$lDevice'. Auto correction was performed!"

                    elif $_test $lStatus -gt 0; then
                        Log w "Error detected while checking '$lDevice'. Everything was left uncorrected!"
                    fi

                    $_echo $lStatus > $iDirTmp/e2fsck.result

                else
                    Log w "Could not run a file system check on '$lDevice'. Missing the e2fsck binary!"
                fi

            else
                Log d "File system check is disabled. Skipping test on '$lDevice'"
            fi

            if $_test $(Session get prop_enable_sdext_journal 0) -ne 2 && $_blkid $lDevice | $_grep -q 'TYPE="ext4"'; then
                if ! $_test -z "$_tune2fs"; then
                    local lHasJournal=$($_tune2fs -l $lDevice | $_grep features | $_grep -q has_journal && $_echo 1 || $_echo 0)
                    local lShellError

                    Log v "Checking current journal status on '$lDevice'"

                    if $_test "`Session get prop_enable_sdext_journal 0`" != "$lHasJournal"; then
                        Log v "Changing journal settings on '$lDevice'"
                        lShellError=$($_tune2fs -O $($_test $lHasJournal -eq 1 && $_echo "^has_journal" || $_echo "has_journal") $lDevice 2>&1); lStatus=$?

                        if $_test $? -gt 0; then
                            Log e "Failed while trying to change journal settings on '$lDevice'" "$lShellError"
                        fi

                    else
                        Log v "The journal status on $lDevice already matches the configuration"
                    fi

                    $_echo $lStatus > $iDirTmp/tune2fs.result

                else
                    Log w "Could not change journal settings on '$lDevice'. Missing the tune2fs binary!"
                fi

            elif $_test $(Session get prop_enable_sdext_journal 0) -ne 2; then
                Log d "The file system type on '$lDevice' is not ext4. Disabling journal handling on this device"

            else
                Log d "Journal handeling is disabled. Leaving journal on '$lDevice' as it is"
            fi

            if AttachMount --fstype $lFsType --options $lMountParamsFull --options $lMountParamsLimited $lDevice $iDirSdext; then
                Log v "Checking writable state on '$lDevice'"
                $_echo 1 > $iDirSdext/WriteTest; sync

                # Samsung has created some pretty strange file systems which has been used on their older devices,
                # and has been encountered a few times on the sd-ext partition (Don't know why). This is how to detect them.
                if $_df $iDirSdext > /dev/null 2>&1 && $_test -f $iDirSdext/WriteTest; then
                    $_rm -rf $iDirSdext/WriteTest

                    Session set device_sdext_status 1

                else
                    Log e "The '$lDevice' file system cannot be mounted with write permissions. Please change your file system type to something like ext(2/3/4)!"
                    DetachMount $iDirSdext
                fi
            fi
        fi
    fi
}

# ===========================================================================================
# -------------------------------------------------------------------------------------------
# This function handles all of the content. It is used to move content between /data and /sd-ext
# 
ProcessContent() {
    if Session check device_sdext_status; then
        local lDataPath
        local lSdextPath
        local lContentMove
        local lContentRevert
        local lContentCurrent
        local lSystemContent
        local lPath
        local lFolder

        # Don't delete newly created content. Wait until next boot to allow users to restore the content if needed
        if $_test -d $iDirSdext/lost+found && $_test -f $iDirSdext/lost+found/delete; then
            Log v "Deleting old content in '$iDirSdext/lost+found'"
            $_rm -rf $iDirSdext/lost+found/*

        elif $_ls -v $iDirSdext/lost+found | $_grep -q ""; then
            Log i "Marking content in $iDirSdext/lost+found for deletion on next boot"
            $_echo 1 > $iDirSdext/lost+found/delete
        fi

        # Revert Link2SD app placement
        for lPath in /data $iDirSdext; do
            if $_ls -v $lPath | $_grep -qe "\.apk$"; then
                Log i "Located APK's in the root of '$lPath'. Moving them into '$lPath/app'"
                $_mv -f $lPath/*.apk $lPath/app/
            fi
        done

        Log v "Preparing to move content between '/data' and '$iDirSdext'"

        # This is a fix for fast booting devices using the init.d service implementation. They some times boot up before an sd-ext script is done moving the content and attaching the folders
        if $_test $(Session get status_safemode 0) -eq 1; then
            for lSdextPath in $iDirSdext/app/* $iDirSdext/app-private/* $iDirSdext/app-asec/* $iDirSdext/app-system/*; do
                lDataPath=$($_echo $lSdextPath | $_sed -e "s/^$($_echo $iDirSdext | $_sed 's/\//\\\//')\//\/data\//")

                if ! $_test -e $lDataPath && $_test -f $lSdextPath; then
                    $_ln -s $lSdextPath $lDataPath
                fi
            done
        fi

        for lPath in /system/*; do
            lFolder=$($_basename $lPath)_s

            if $_test -d $iDirSdext/$lFolder || $_test -d /data/$lFolder; then
                lSystemContent="$lSystemContent $lFolder"
            fi
        done

        $_test $(Session get prop_move_apps 0) -eq 1 && lContentMove="$lContentMove app app-private" || lContentRevert="$lContentRevert app app-private"
        $_test $(Session get prop_move_sysapps 0) -eq 1 && lContentMove="$lContentMove app-system" || lContentRevert="$lContentRevert app-system"
        $_test $(Session get prop_move_data 0) -eq 1 && lContentMove="$lContentMove data" || lContentRevert="$lContentRevert data"
        $_test $(Session get prop_move_dalvik 0) -eq 1 && lContentMove="$lContentMove dalvik-cache" || lContentRevert="$lContentRevert dalvik-cache"

        if CheckDir /data/app-asec; then
            $_test $(Session get prop_move_apps 0) -eq 1 && lContentMove="$lContentMove app-asec" || lContentRevert="$lContentRevert app-asec"
        fi

        if CheckDir /data/user; then
            $_test $(Session get prop_move_data 0) -eq 1 && lContentMove="$lContentMove user" || lContentRevert="$lContentRevert user"
        fi

        if CheckDir /data/media; then
            $_test $(Session get prop_move_media 0) -eq 1 && lContentMove="$lContentMove media" || lContentRevert="$lContentRevert media"
        fi

        if CheckDir /data/app-lib; then
            $_test $(Session get prop_move_libs 0) -eq 1 && lContentMove="$lContentMove app-lib" || lContentRevert="$lContentRevert app-lib"
        fi

        if ! $_test -z "$lSystemContent"; then
            $_test $(Session get prop_move_system 0) -eq 1 && lContentMove="$lContentMove $lSystemContent" || lContentRevert="$lContentRevert $lSystemContent"
        fi

        # Make sure to leave some extra room
        local lExtSize=$(($(PartitionSize $iDirSdext) - 25))
        local lIntSize=$(($(PartitionSize /data) - 25))

        local lSrcPath
        local lDestPath
        local lDestSize
        local lSrcSize
        local lContSize
        local lContinue
        local lAction
        local lActionRevert
        local lFolder
        local lSub
        local lSubName

        for lContinue in true false; do
            for lAction in revert move; do
                $_test "$lAction" = "revert" && lActionRevert=true || lActionRevert=false

                if $lActionRevert; then
                    lSrcPath=$iDirSdext
                    lDestPath=/data
                    lDestSize=$lIntSize
                    lSrcSize=$lExtSize
                    lContentCurrent="$lContentRevert"
                    lContentRevert=
                else
                    lSrcPath=/data
                    lDestPath=$iDirSdext
                    lDestSize=$lExtSize
                    lSrcSize=$lIntSize
                    lContentCurrent="$lContentMove"
                    lContentMove=
                fi

                if ! $_test -z "$lContentCurrent"; then
                    for lFolder in $lContentCurrent; do
                        Log v "Checking '$lSrcPath/$lFolder' to see if something should be moved to '$lDestPath/$lFolder'"

                        if $lActionRevert; then
                            TouchDir --skip-existing /data/$lFolder
                        else
                            TouchDir --skip-existing /data/$lFolder --parent $iDirSdext/$lFolder
                        fi

                        if $_test -d $lSrcPath/$lFolder && $_ls -v $lSrcPath/$lFolder | $_grep -q "" && ( ! $_echo $lFolder | $_grep -q -e "^app" || $_find $lSrcPath/$lFolder -type f | $_grep -q "" ); then
                            lContSize="`$_echo $($_du -s -m $lSrcPath/$lFolder) | $_awk '{print $1}'`"

                            Log d "Comparing '$lSrcPath/$lFolder' size of '${lContSize}MB' to remaining spaze on '$lDestPath' of '${lDestSize}MB'"

                            if $_test $lDestSize -gt $lContSize; then
                                Log v "Moving '$lSrcPath/$lFolder' to '$lDestPath/$lFolder'"

                                if $_test "$lFolder" = "dalvik-cache"; then
                                    $_rm -rf $lSrcPath/$lFolder/*

                                else
                                    for lSub in $lSrcPath/$lFolder/* $lSrcPath/$lFolder/.*; do
                                        if $_test -e $lSub; then
                                            lSubName=$($_basename $lSub)

                                            if $_test "$lSubName" != "." && $_test "$lSubName" != ".." && ( ! $_test -L $lSub || ! $_echo $lFolder | $_grep -q -e "^app" ); then
                                                $_mv -f $lSrcPath/$lFolder/$lSubName $lDestPath/$lFolder/$lSubName

                                                # This is a fix for fast booting devices using a service implemented init.d method
                                                if ! $lActionRevert && $_echo $lFolder | $_grep -q -e "^app" && $_test $(Session get status_safemode 0) -eq 1; then
                                                    $_ln -sf $lDestPath/$lFolder/$lSubName $lSrcPath/$lFolder/$lSubName
                                                fi
                                            fi
                                        fi
                                    done

                                    lContSize="`$_echo $($_du -s -m $lDestPath/$lFolder) | $_awk '{print $1}'`"
                                fi

                                lSrcSize=$(($lSrcSize + $lContSize))
                                lDestSize=$(($lDestSize - $lContSize))

                                if $_ls -v $lSrcPath/$lFolder | $_grep -q "" && ( ! $_echo $lFolder | $_grep -q -e "^app" || $_find $lSrcPath/$lFolder -type f | $_grep -q "" ); then
                                    Log w "Not everything from '$lSrcPath/$lFolder' could be moved to '$lDestPath/$lFolder'"

                                    # We do not delete the sd-ext folder when there is still content in it, but we do not want it to be attached to the data folder either
                                    if $lActionRevert; then
                                        continue
                                    fi

                                elif $lActionRevert; then
                                    $_rm -rf $lSrcPath/$lFolder
                                fi

                            else
                                if $lContinue; then
                                    # Try again during round two
                                    Log i "Could not move '$lSrcPath/$lFolder' due to low storage on '$lDestPath', trying again later!"
                                    $lActionRevert && lContentRevert="$lContentRevert $lFolder" || lContentMove="$lContentMove $lFolder"

                                    continue

                                else
                                    Log i "Could not move '$lSrcPath/$lFolder' due to low storage on '$lDestPath', giving up!"
                                    if ! $lActionRevert; then
                                        $_rm -rf $lSrcPath/$lFolder
                                    fi
                                fi
                            fi

                        elif $lActionRevert && $_test -e $lSrcPath/$lFolder; then
                            $_rm -rf $lSrcPath/$lFolder
                        fi

                        if $_test -d $iDirSdext/$lFolder; then
                            # Cleanup old links
                            if $_test $(Session get status_safemode 0) -eq 1; then
                                if $_echo $lFolder | $_grep -q -e "^app"; then
                                    for lDataPath in /data/$lFolder/*; do
                                        if $_test -L $lDataPath; then
                                            lSdextPath=$($_echo $lDataPath | $_sed -e "s/^\/data\//$($_echo $iDirSdext | $_sed 's/\//\\\//')\//")

                                            if ! $_test -e $lSdextPath; then
                                                $_rm -rf $lDataPath
                                            fi
                                        fi
                                    done
                                fi
                            fi

                            AttachMount $iDirSdext/$lFolder /data/$lFolder
                        fi
                    done
                fi

                if $lActionRevert; then
                    lIntSize=$lDestSize
                    lExtSize=$lSrcSize
                else
                    lIntSize=$lSrcSize
                    lExtSize=$lDestSize
                fi
            done
        done
    fi
}

# ===========================================================================================
# -------------------------------------------------------------------------------------------
# This function is used to create links between /data/app-system and /system/app.
# It is a way of keeping system apps on the data/sd-ext partitions.
# 
ProcessLinks() {
    local lFile
    local lDirs="/data/app-system"
    local lFolder
    local lPath
    local lSystemLocation
    local lDataLocation

    for lPath in /system/*; do
        lFolder=$($_basename $lPath)_s

        if $_test -d /data/$lFolder; then
            lDirs="$lDirs /data/$lFolder"
        fi
    done

    # Make sure that the device has not got the HTC S-On flag on the /system partition
    # --------------------------------------------------------------------------------
    # Some devices has a tendency to hang on this test, why I don't know.
    # But to be sure that does not become a problem, we handle this in a sub-process
    # which can later be killed and let the script continue.
    # --------------------------------------------------------------------------------
    Log d "Running an S-On protection test on '/system'"
    ( $_echo 1 > /system/s-off 2> /dev/null; sync ) & 

    pid=$!
    $_sleep 1
    $_kill "$pid" 2> /dev/null

    if $_test -e /system/s-off; then
        $_rm -rf /system/s-off

        Log v "Linking system content to the '/system' location"

        for lDataLocation in $lDirs; do
            lSystemLocation="$($_echo "$lDataLocation" | $_sed 's/\/data\/\(.*\)\(_s\|-system\)/\/system\/\1/')"

            Log d "Linking content from '$lDataLocation' to '$lSystemLocation'"

            for lFile in $lSystemLocation/*; do
                if $_test -L $lFile && $_test -z "`$_readlink $lFile`"; then
                    Log d "Removing unused link '$lFile'"
                    $_rm -rf $lFile
                fi
            done

            if $_test -d $lDataLocation; then
                for lFile in $lDataLocation/*; do
                    if $_test -f $lFile && ! $_test -e $lSystemLocation/$($_basename $lFile); then
                        Log d "Creating a link for '$lFile' in '$lSystemLocation'"
                        $_ln -s $lFile $lSystemLocation/
                    fi
                done
            fi
        done

    else
        Log i "The system partition is S-On protected and it is therefore not possible to link system content to the '/system' location!"
    fi
}

# ===========================================================================================
# -------------------------------------------------------------------------------------------
# This function controlls ZRam and SWAP
# 
ProcessMemory() {
    local lType
    local lDevice
    local lShellError

    for lType in ZRAM SWAP; do
        lDevice= 

        if ( $_test "$lType" = "ZRAM" && $_test $(Session get prop_set_zram_compression 0) -gt 0 ) || ( $_test "$lType" = "SWAP" && $_test $(Session get prop_enable_swap 0) -eq 1 && Session check device_swap ); then
            if $_test -e /proc/swaps; then
                if $_test "$lType" = "ZRAM"; then
                    local lMemory=$($_cat /proc/meminfo | $_awk '{ if ($1 eq "MemTotal:") print $2; exit }')
                    local lSize=$(Session get prop_set_zram_compression)

                    if $_test -e /system/lib/modules/ramzswap.ko || $_test -b /dev/block/ramzswap0; then
                        if ! $_test -z "$_rzscontrol" && ( $_test -b /dev/block/ramzswap0 || $_insmod /system/lib/modules/ramzswap.ko ); then
                            lDevice=/dev/block/ramzswap0 && $_rzscontrol $lDevice --disksize_kb=$(($(($lMemory * $lSize)) / 100)) --init
                        
                        elif $_test -z "$_rzscontrol"; then
                            Log e "Could not enable '$lType'. Missing the rzscontrol binary!"

                        else
                            Log e "Could not enable '$lType'. Missing the ramzswap module!"
                        fi

                    elif $_test -b /dev/block/zram0 || ( $_test -e /system/lib/modules/zram.ko && $_insmod /system/lib/modules/zram.ko ); then
                        $_echo "$(($(($(($lMemory * $lSize)) / 100)) * 1024))" > /sys/block/zram0/disksize
                        lDevice=/dev/block/zram0 && $_mkswap $lDevice >/dev/null

                    else
                        Log e "Could not enable '$lType' due to lack of kernel support!"
                    fi

                else
                    lDevice=$(Session get device_swap)
                fi

                if ! $_test -z "$lDevice"; then
                    Log v "Enabling '$lType' on '$lDevice'"
                    lShellError=$($_swapon $lDevice 2>&1)

                    if $_test -e /system/bin/compcache; then
                        Log d "Disabling CM's comcache"
                        $_chmod 0644 /system/bin/compcache
                    fi

                    if $_test $? -ne 0 || ! $_grep -q $lDevice /proc/swaps; then
                        Log e "Failed while enabling '$lType'!" "'$lShellError'"
                    fi
                fi

            else
                Log e "Could not enable '$lType' due to lack of kernel support!"
            fi
        fi
    done

    if $_test -e /proc/swaps && $_grep -q -e '^\/dev\/' /proc/swaps; then
        local lSwappiness=$(Session get prop_set_swap_level 0)

        Log v "Setting swappiness to '$lSwappiness'"
        $_echo $lSwappiness > /proc/sys/vm/swappiness
    fi
}

# ===========================================================================================
# -------------------------------------------------------------------------------------------
# This function handles the cache partition. If this is set to auto (2), the cache
# will be placed on the partition containing most free space, whether this being /cache, /data or /sd-ext.
# If this is set to on (1), it will be forced on to /sd-ext.
# 
# When placed elsewhere than the original /cache partition, then /cache/recovery and (If exists) /cache/dalvik-cache
# will be placed on /cache-int which is the original cache partition. 
# 
ProcessCache() {
    if $_test $(Session get prop_enable_cache 0) -eq 1 || $_test $(Session get prop_enable_cache 0) -eq 2; then
        local lDataSize=$(PartitionSize /data)
        local lCacheSize=$(PartitionSize /cache)
        local lSdextSize=0
        local lCMDalvik=false
        local lCachePath=/cache

        Log v "Handling the cache partition"

        Session check device_sdext_status && lSdextSize=$(PartitionSize $iDirSdext)

        if $_test $(Session get prop_enable_cache 0) -eq 1 || ( $_test $lDataSize -gt $lCacheSize || $_test $lSdextSize -gt $lCacheSize ); then
            lCachePath=$iDirSdext/cache

            if ( $_test $(Session get prop_enable_cache 0) -eq 2 && $_test $lDataSize -gt $lSdextSize ) || $_test $lSdextSize -eq 0; then
                if $_test -e $iDirSdext/cache; then
                    $_rm -rf $iDirSdext/cache
                fi

                lCachePath=/data/cache
            fi

            TouchDir --skip-existing --user 1000 --group 2001 --mod 0770 /cache --parent $lCachePath
            TouchDir --skip-existing --user 1000 --group 2001 --mod 0770 /cache --parent /cache-int

            if MoveMount /cache /cache-int; then
                if AttachMount $lCachePath /cache; then
                    # If the ROM uses CM's /cache/dalvik-cache option, make sure that the cache is placed on the internal cache partition
                    if CheckDir /cache/dalvik-cache && ! $_grep dalvik.vm.dexopt-data-only=1 /system/build.prop | $_grep -q -v "#"; then
                        TouchDir --skip-existing --user 1000 --group 1000 --mod 0771 /cache/dalvik-cache --parent /cache-int/dalvik-cache
                        AttachMount /cache-int/dalvik-cache /cache/dalvik-cache
                    fi

                    # /cache/recovery is used to communicate with the recovery. Make sure that it uses the real cache partition
                    TouchDir --skip-existing --user 1000 --group 2001 --mod 0770 /cache/recovery --parent /cache-int/recovery
                    AttachMount /cache-int/recovery /cache/recovery

                    local lCacheFolder

                    for lCacheFolder in `$_cat $(ListFolders) | $_grep -e '^\/cache\/'`; do
                        TouchDir --skip-existing $lCacheFolder
                    done
                fi
            fi

        else
            local lPaths

            Log d "Internal cache (${lCacheSize}MB) is grater than both '/data' (${lDataSize}MB) and '/sd-ext' (${lSdextSize}MB). Leaving '/cache' as is!"

            for lPaths in $iDirSdext/cache /data/cache; do
                if $_test -e $lPaths; then
                    $_rm -rf $lPaths
                fi
            done
        fi

    else
        for lPaths in $iDirSdext/cache /data/cache; do
            if $_test -e $lPaths; then
                $_rm -rf $lPaths
            fi
        done
    fi
}

# ===========================================================================================
# -------------------------------------------------------------------------------------------
# This function handles things where the phone might need to boot completly before anything is done.
# The function will start a sub-process which will wait until work can start, while the rest of the
# script completes. 
# 
ProcessSubShell() {
    (
        while :; do
            $_sleep 1

            if $_test -f $iDirTmp/script.result; then
                Log d "The finalizing sub-process has been started"

                if $_test $(Session get prop_set_storage_threshold 0) -gt 0; then
                    if ! $_test -z "$_sqlite3"; then
                        while :; do
                            # After a clean wipe of the phone, this might not exist until boot is almost complete. So we wait!
                            if $_test -e /data/data/com.android.providers.settings/databases/settings.db; then
                                lValue=$($_sqlite3 /data/data/com.android.providers.settings/databases/settings.db "select value from secure where name = 'sys_storage_threshold_percentage'" 2>&1); lStatus=$?

                                if $_test $lStatus -eq 0; then
                                    lNewValue=$(Session get prop_set_storage_threshold)

                                    if $_test -z "$lValue" || $_test "$lValue" != "$lNewValue"; then
                                        Log v "Changing storage threshold to '${lNewValue}%'"
                                        lShellError=$($_sqlite3 /data/data/com.android.providers.settings/databases/settings.db "insert into secure (name, value) VALUES('sys_storage_threshold_percentage','1')" 2>&1); lStatus=$?

                                        if $_test $lStatus -ne 0; then
                                            Log e "Storage threshold could not be changed. The sqlite3 binary returned result code '$lStatus'!" "$lShellError"
                                        fi

                                    else
                                        Log v "Storage threshold is already set for '${lValue}%'. Leaving it as is"
                                    fi

                                else
                                    Log e "Storage threshold could not be changed. The sqlite3 binary returned result code '$lStatus'!" "$lValue"
                                fi

                                break
                            fi

                            $_sleep 1
                        done

                    else
                        Log e "Storage threshold could not be changed. Missing the 'sqlite3' binary!"
                    fi

                else
                    Log d "The storage threshold option is disabled, skipping"
                fi

                Log d "The finalizing sub-process has been stopped"
                Log d "The script was executed in $(($(date '+%s') - $iTimestamp)) seconds"

                # Cleanup script tmp files
                $_rm -rf $iDirTmp/*.tmp

                $_test "`Session get modRoot`" != "rw" && $_mount -o remount,rw /
                $_test "`Session get modSystem`" != "rw" && $_mount -o remount,rw /system

                break
            fi
        done
    ) & 

    # Logwrapper has been made to stall until the current process along with all sub-processes has been stopped.
    # We need this sub-process to continue after this script, so we will force the script out of logwrapper.
    if $_pgrep logwrapper; then
        $_killall logwrapper
    fi
}

# ===========================================================================================
# -------------------------------------------------------------------------------------------
# Some shells will not except a simple (export $lName="$lValue"), while other shells
# has problems exporting variables using eval. 
# 
ExportVar() {
    local lName=$1
    local lValue="$2"

    if $iSimpleExport; then
        export $lName="$lValue"

        if $_test -z "$lValue"; then
            unset $lName
        fi

    else
        eval export "$lName=\"$lValue\""

        if $_test -z "$lValue"; then
            eval unset \$lName
        fi
    fi
}

# ===========================================================================================
# -------------------------------------------------------------------------------------------
# This function can check whether a folder is defined in one of the init.rc files. 
# It is used to check certain options which is not available in all Android versions or custom ROM's.
# Things like CM's /cache/dalvik-cache, Android 4.x's /data/app-lib or Android 4.2+ /data/user
# 
CheckDir() {
    local lPath="$1"

    if $_test -d "$lPath" || $_grep -q -e "^$lPath\(\/.*\)*$" "$(ListFolders)"; then
        return 0
    fi

    return 1
}

# ===========================================================================================
# -------------------------------------------------------------------------------------------
# Different busybox versions print's different output from the df command. This function
# is used to locate the part of available space.
# 
PartitionSize() {
    local lPath="$1"
    local lPart
    local i=1

    for lPart in `$_df -m "$lPath" | $_tail -n1 | $_tr -s ' '`; do
        if ! $_echo -n $lPart | $_sed -e 's/[0-9]*//' | $_grep -q ""; then
            if $_test $i -eq 3; then
                $_echo $lPart; return 0
            fi

            i=$(($i + 1))
        fi
    done

    $_echo 0; return 1
}

# ===========================================================================================
# -------------------------------------------------------------------------------------------
# This function acts as an internal session storage that other fuctions can use to share
# data with one another in an easy way. 
# 
Session() {
    local lAction=$1
    local lName=$2
    local lValue="$3"

    case "$lAction" in
        "set") 
            ExportVar mem_${lName} "$lValue"
        ;;

        "get")
            if $_test -z "$lValue" || Session check $lName; then
                echo $(eval echo \$mem_${lName})

            else
                $_echo $lValue
            fi
        ;;

        "unset")
            ExportVar mem_${lName}
        ;;

        "check") 
            if eval $_test \${mem_${lName}+defined}; then
                return 0

            else
                return 1
            fi
        ;;
    esac
}

# ===========================================================================================
# -------------------------------------------------------------------------------------------
# This function is used to create directories and/or set permissions and ownership.
# The magic with this function is that it can collect permission and ownership information
# from the init.rc files and thereby add the correct once to the folder 
# 
TouchDir() {
    local lSkipCheck=false
    local lSkipExisting=false
    local lUser=1000
    local lGroup=1000
    local lMod=0771
    local lParent

    while $_test $# -gt 0; do
        case "$1" in
            "--skip-check") lSkipCheck=true ;;
            "--skip-existing") lSkipExisting=true ;;
            "--user") shift && lUser=$1 ;;
            "--group") shift && lGroup=$1 ;;
            "--mod") shift && lMod=$1 ;;
            "--parent") shift && lParent=$1 ;;
            *) local lFolder=$1 ;;
        esac

        shift
    done

    if ! $lSkipExisting || ! $_test -d $lFolder || ( ! $_test -z "$lParent" && ! $_test -d $lParent ); then
        if ! $lSkipCheck; then
            local lLinePerm
            local lLinePerm
            local lLineUser
            local lLineGroup

            # We cannot use grep in the redirection below. It will fail in some shells because we don't have a static reference to the folder we are looking for
            while read lLinePath lLinePerm lLineUser lLineGroup; do
                if $_test "$lLinePath" = "$lFolder"; then
                    ! $_test -z "$lLinePerm" && lMod=$lLinePerm
                    ! $_test -z "$lLineUser" && lUser=$lLineUser
                    ! $_test -z "$lLineGroup" && lGroup=$lLineGroup
                fi

            done < $(ListFolderInfo)
        fi

        # Parent folder is a folder that should contain the same permissions
        # and ownership as our main target. For an example /data/app and /sd-ext/app
        ! $_test -z "$lParent" && lFolder="$lFolder $lParent"

        local lDirectory

        for lDirectory in $lFolder; do
            if ! $_test -d $lDirectory; then
                if $_test -L $lDirectory; then
                    $_rm -rf $lDirectory
                fi

                $_mkdir -p $lDirectory || $_mkdir $lDirectory

            elif $lSkipExisting; then
                continue
            fi

            Log d "Retouching the directory '$lDirectory' with permissions '$lMod' and ownership '${lUser}.${lGroup}'"

            $_chmod $lMod $lDirectory
            $_chown ${lUser}.${lGroup} $lDirectory
        done
    fi
}

# ===========================================================================================
# -------------------------------------------------------------------------------------------
# This function is used to mount devices and bind folders together. 
#
AttachMount() {
    local lOptions
    local lFstype
    local lDevice
    local lLocation
    local lShellError

    while $_test $# -gt 0; do
        case "$1" in
            "--options") shift && lOptions="$lOptions $1" ;;
            "--fstype") shift && lFstype=$1 ;;
            *) $_test -z "$lDevice" && lDevice=$1 || lLocation=$1 ;;
        esac

        shift
    done

    Log v "Attaching '$lDevice' to '$lLocation'"

    if $_test -b $lDevice; then
        if $_test "$lFstype" != "auto" && ! $_grep $lFstype /proc/filesystems | $_grep -q -v 'nodev'; then
            Log w "The file system type '$lFstype' is not supported by the kernel. Will try to attach '$lDevice' using auto detection!"
            lFstype=auto
        fi

        $_test "$lFstype" != "auto" && lFstype="$lFstype auto"
        $_test "$lOptions" != "none" && lOptions="$lOptions none"

        local i
        local x

        # Mount has a lot of different support depending on the busybox available.
        # We need to make a great deal of safe-guards in order to make sure that we get a successfull mount
        for x in $lOptions; do
            for i in $lFstype; do
                Log d "Mounting '$lDevice' to '$lLocation' as '$i' with options '$x'"

                if $_test "$x" != "none"; then
                    lShellError=$($_mount -t $i -o $x $lDevice $lLocation 2>&1)
                else
                    if $_test "$i" != "auto"; then
                        lShellError=$($_mount -t $i $lDevice $lLocation 2>&1)

                    else
                        lShellError=$($_mount $lDevice $lLocation 2>&1)
                    fi
                fi

                if $_test $? -gt 0; then
                    Log d "Failed to mount '$lDevice' to '$lLocation'" "$lShellError"

                else
                    return 0
                fi
            done
        done

    elif $_test -d $lDevice; then
        lShellError=$($_mount --bind $lDevice $lLocation 2>/dev/null || $_mount -o 'bind' $lDevice $lLocation 2>&1)

        if $_test $? -gt 0; then
            Log d "Failed to mount '$lDevice' to '$lLocation'" "$lShellError"

        else
            return 0
        fi
    fi

    Log e "It was not possible to attach '$lDevice' to '$lLocation'"

    return 1
}

# ===========================================================================================
# -------------------------------------------------------------------------------------------
# This function is used to unmount a device or folder
# 
DetachMount() {
    local lDevice=$1
    local lShellError

    Log v "Detaching '$lDevice'"
    lShellError=$($_umount "$lDevice" 2>&1)

    if $_test $? -gt 0; then
        Log d "Could not detach '$lDevice', trying with force" "$lShellError"
        lShellError=$($_umount -f "$lDevice" 2>&1)

        if $_test $? -gt 0; then
            Log d "Could not detach '$lDevice', trying lazy unmount instead" "$lShellError"
            lShellError=$($_umount -l "$lDevice" 2>&1)

            if $_test $? -gt 0; then
                Log e "It was not possible to detach '$lDevice'!" "$lShellError"
                
                return 1
            fi
        fi
    fi

    return 0
}

# ===========================================================================================
# -------------------------------------------------------------------------------------------
# This function is used to move a mount point. Not all busybox versions support the --move options.
# This will provide a different move procedure in those cases. 
# 
MoveMount() {
    local lSrcPath=$1
    local lDestPath=$2
    local lShellError

    Log v "Moving the mount point '$lSrcPath' to '$lDestPath'"

    lShellError=$($_mount --move $lSrcPath $lDestPath 2>&1)

    # Not all busybox versions support the move option
    if $_test $? -gt 0; then
        Log i "Failed while trying to move the mount point '$lSrcPath' to '$lDestPath'. Trying manual procedure!" "$lShellError"

        local lMounts="`$_grep -e '^\/dev\/' /proc/mounts | $_grep " $lSrcPath " | $_tail -n1`"

        if ! $_test -z "$lMounts"; then
            local lDevice=$($_echo "$lMounts" | $_awk '{print $1}')
            local lType=$($_echo "$lMounts" | $_awk '{print $3}')
            local lOptions=$($_echo "$lMounts" | $_awk '{print $4}')

            if ! DetachMount $lSrcPath || ! AttachMount $lDevice $lDestPath --fstype $lType --options $lOptions; then
                return 1
            fi

        else
            Log e "Could not collect mount information on '$lSrcPath'!"; return 1
        fi
    fi

    return 0
}

# ===========================================================================================
# -------------------------------------------------------------------------------------------
# This function handles all log entries.  
# 
Log() {
    local lLevel
    local lType
    local lMessage
    local lShellError

    if $_test -z "$1"; then
        read lMessage
        lType="w"

        if ! $_test -z "$lMessage"; then
            lMessage="Shell Error [$($_echo "$lMessage" | $_awk '{print substr($0, 0, 150)}') ...]"
        fi

    else
        lType=$1
        lMessage="$2"
        lShellError="$3"
    fi

    if $_test -n "$lMessage"; then
        if $_test "$lType" != "d" || $_test $(Session get prop_enable_debug 0) -eq 1; then

            log -p $lType -t $iLogName "$lMessage"

            case "$lType" in 
                "e") lLevel=3; lType=E ;;
                "w") lLevel=2; lType=W ;;
                "i") lLevel=1; lType=I ;;
                "d") lLevel=0; lType=D ;;
                "v") lLevel=0; lType=V ;;
            esac

            if ! $iLogReset; then
                if $_test -f /data/local/mounts2sd.log; then
                    $_mv -f /data/local/mounts2sd.log /data/local/mounts2sd.log.old
                fi

                export iLogReset=true
            fi

            $_echo "$lType/$lMessage" | $_tee -a $iDirTmp/log.txt >> /data/local/mounts2sd.log

            if $_test $lLevel -gt 0; then
                if ! $_test -e $iDirTmp/log.level || $_test $($_cat $iDirTmp/log.level) -lt $lLevel; then
                    $_echo $lLevel > $iDirTmp/log.level
                fi
            fi

            if ! $_test -z "$lShellError"; then
                Log d "Shell Error [$($_echo "$lShellError" | $_awk '{print substr($0, 0, 150)}') ...]"
            fi
        fi
    fi
}

# ===========================================================================================
# -------------------------------------------------------------------------------------------
# This is where the script starts it work
# 

echo -n "" > /data/m2sd.fallback.log

# In bash, we could use 'exec 2> >(Log)', but this will raise an redirect error in sh. So we use a small sub process hack instead
## exec 3>&1 

## ProcessEnviroment 2>&1 >&3 3>&- >> /data/m2sd.fallback.log
ProcessEnviroment

if $iEnviroment; then
    ## (
        ProcessBoot
        ProcessMMC
        ProcessStorage
        ProcessContent
        ProcessLinks
        ProcessMemory
        ProcessCache
        ProcessSubShell

        $_echo 0 > $iDirTmp/script.result

    ## ) 2>&1 >&3 3>&- | Log
fi

exit 0
