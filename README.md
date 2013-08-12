Mounts2SD
=========

Mounts2SD is a customizable sd-ext control script that can be used to move content to and from the second sdcard partition known in Android as sd-ext. 
In addition to moving content, Mounts2SD comes with other features and is also the only script that has a bunch of safe guards build-in for almost any situation, making sure that when things go wrong, the script will adapt and try keeping your phone running 100% and try to make sure that data is not lost or corrupted. And it comes with an app to configure the script and monitor the options current status in real time.

Table of content
----------------

* [Script Features](https://github.com/SpazeDog/mounts2sd#script-features)
* [Backup, backup and backup](https://github.com/SpazeDog/mounts2sd#backup-backup-and-backup)
* [Installation Instructions](https://github.com/SpazeDog/mounts2sd#installation-instructions)
* [Bug Reporting](https://github.com/SpazeDog/mounts2sd#bug-reporting)
* [What is SD-EXT](https://github.com/SpazeDog/mounts2sd#what-is-sd-ext)
* [File System Types](https://github.com/SpazeDog/mounts2sd#file-system-types)
* [Partition Table](https://github.com/SpazeDog/mounts2sd#partition-table)
* [What is init.d](https://github.com/SpazeDog/mounts2sd#what-is-initd)
* [Safe Mode](https://github.com/SpazeDog/mounts2sd#safe-mode)
* [System Applications](https://github.com/SpazeDog/mounts2sd#system-applications)
* [Script Properties](https://github.com/SpazeDog/mounts2sd#script-properties)
* [Root (SuperUser) permissions](https://github.com/SpazeDog/mounts2sd#root-superuser-permissions)
* [Screen Shots](https://github.com/SpazeDog/mounts2sd#screen-shots)

Script Features
---------------

* SWAP Support (Needs kernel support)
* ZRAM Support (Needs kernel support)
* Move and Revert content between /data and /sd-ext
    * /data/dalvik-cache
    * /data/app and /data/app-private
    * /data/app-asec (Used by Android 4.1+)
    * /data/app-lib (Used by Android 4.2+)
    * /data/data and /data/user (Android 4.2+)
    * /data/media (Used by some Samsung devices as internal storage)
* Optimizing internal partitions
* Checks/Fixes the sd-ext partition during boot (Needs e2fsck)
* Auto Clean /sd-ext/Lost+Found to avoid having it take up to much disk space
* Disable/Enable Ext4 Journal (Needs tune2fs) on sd-ext
* Let's you use whatever linux file system for sd-ext which is supported by the kernel
* Change Storage Threshold to fix Market issue 'Not enough space'
* Comparing destination partition to folder sizes before moving content
* Change Internal and External Readahead and Scheduler

Backup, backup and backup
-------------------------

**For the love of god, make sure to do a full recovery backup before attempting to install something like this**

If not, I do not want to hear any complaints about your loss of data if something went wrong

Installation Instructions
-------------------------

**Make sure to backup your device before attempting this**

1. You will need a rooted device
    * You can find the root procedure for your device at the [XDA Forum](http://forum.xda-developers.com/)
2. You will need init.d support
    * Init.d enables the feature of executing custom shell scripts during boot. The sd-ext script is what does all the work. If you do not have this feature in your ROM, check out [Init.d Injector](http://forum.xda-developers.com/showthread.php?t=2256647)
3. You will need a second partition on your external sdcard
    * The second sdcard partition is the one that will be used as a second /data partition. It needs to be formated with a Linux File system like Ext2, Ext3, Ext4, XFS etc. Windows people can use [MiniTool](http://download.cnet.com/MiniTool-Partition-Wizard-Home-Edition/3000-2094_4-10962200.html) to format partitions with these file systems
4. Download the Application from the [XDA Thread](http://forum.xda-developers.com/showthread.php?t=1342387) or from [Google Play](https://play.google.com/store/apps/details?id=com.spazedog.mounts2sd)
    * If you are updating a custom ROM which previously had Mounts2SD installed, you will need the recovery package from the [XDA Thread](http://forum.xda-developers.com/showthread.php?t=1342387) to restore the script after flashing the updated ROM
5. **Only if you are not updating your ROM and restoring the script using the recovery package**
    1. Install the application
    2. Go to Application Settings and install the script
    3. Reboot the device

Bug Reporting
-------------

If you encounter any issues with Mounts2SD, there are several ways to get help

1. Create a [new bug](https://github.com/SpazeDog/mounts2sd/issues/new) report on this page
    * I will then get an email from github announcing your report and will write to you on the bug page
2. Write a post in the [XDA Thread](http://forum.xda-developers.com/showthread.php?t=1342387) 
    * Here you will not have to wait until I get the time to help you out, as you would also receive help from other users of XDA
3. Write an email directly to me on [d.bergloev At gmail.com](mailto:d.bergloev@gmail.com)
    * More private, but might take longer for me to respond

While creating your bug report, there are some information that you will need to provide, otherwise no one will be able to help and your report is a waste of time

* Information about what device you are using, link or name of the ROM you are running and possibly the kernel (if you do not use the bundled version)
* The version of Mounts2SD, where you got it and how you installed it
* A proper description of your issue, what happens and how/when/where did it occur
* A copy of the Mounts2SD Log, if possible
    * If you can open the application, you can go to the log viewer, press menu and save it to the sdcard in a text file
* Some logcat output from the beginning of the boot process, if possible
    * You can get more information about logcat in this [XDA Thread](http://forum.xda-developers.com/showthread.php?t=1726238)

What is SD-EXT
--------------

Normal operating systems like Linux, Windows etc. allows you to store different types of applications and also wherever you'd like. Mobile operating systems like Android however, uses APK's (Special ZIP Files) to pack applications and it stores both the application (APK) and all of it's data like libraries and user data (Settings and such) on the /data partition in specific sub-folders. The OS is not built to look for applications and data elsewhere, so if you run out of space on this partition, you will be out of luck. Especially since you cannot upgrade the internal nand drive. 

The conept of sd-ext was created for this very reason. It enables you to move some of the content from /data and place it on an additional partition on your external sdcard (sd-ext), something that you are able to upgrade. The problem however is that Android, like mentioned above, is not able to look for applications and other types of data outside the /data partition, so whatever you move to sd-ext, will not be located by Android. This is where the sd-ext scripts comes in to play. The trick is to make Android think that everything is still located on the /data partition, while in fact it is stored on the sd-ext partition. The way this works is that the sd-ext script moves one or more sub-folders from /data to sd-ext, folders like `app` which stores the APK files or `data` which stores the user specific app configurations. Once moved to sd-ext, the script will then mount (link) each sub-folder from sd-ext and back to /data where Android can find it. Now every time Android accesses one of these moved folders to read, write or store files, Android will in fact be using the files from sd-ext, even though it accesses them via /data. 

One thing to note here is that Android is not aware of the sd-ext partition. This means that you will not be able to see this partition from within the Android Settings. You will however be able to see the extra space on your /data partition after moving some of the content to sd-ext. 

Also note that sd-ext is NOT your external sdcard storage partition (/sdcard, /extSdCard, /mnt/sdcard etc.). It is a SECOND partition on your external sdcard which the script handles during boot.

File System Types
-----------------

Android, like any Linux based operating system, uses POSIX-compliant permission scheme to administer files and folder permissions. Android uses these permissions to restrict access to files and folders for specific processes. For an example, any file and folder owned by a specific application, can only be accesses by this application (Unless it was created world wide). Also many Android system files and folders can not be accessed by any application at all. All of these permissions is written in a header on each file and folder, which means that in order to work, it will need a file system that supports this type of permission scheme. The Fat32 file system for an example, was created by Microsoft and therefore does not support this scheme. This means that the sd-ext partition needs to be formated using a Linux File system like Ext2, Ext3, Ext4, XFS etc. Otherwise Android will not be able to set permissions on any content within sd-ext, and will in most cases refuse to boot the device. 

Partition Table
---------------

Android is pre-configured to use the first sdcard partition as the external storage device (Where you save your Music, Photos etc.). Therefor, the sd-ext partition needs to be added as the second partition on the sdcard. 

You can also add a SWAP partition to the sdcard. This should be the third partition in the table. 

1. Storage partition (Fat32 or exFat, depending on kernel support)
2. SD-EXT partition (Ext2, Ext3, Ext4, XFS or another Linux file system type)
3. SWAP (Optional)

What is init.d
--------------

When Android boots a device, it executes the init binary which is located in the device's boot partition. By using multiple init.rc files (customizable configurations for init), it handles all of the pre-boot configurations like creating missing folders, setting specific permissions on important files and folders, setting up shell environment variables, starting all of the services needed for Android to run and much more. Basically anything that should be handled before entering into the actual Android OS. 

Some times you might want to add additional jobs to be handled during boot, like changing partition mount arguments, changing sdcard readahead, setting up custom kernel settings like CPU OC or in this case, handle the sd-ext partition and move content to and from the original /data partition. The problem with init's configuration files, is that they are located on the boot partition and everything here is packed into a boot.img file, so it cannot be altered easily. In order to add custom jobs to init, you will need to reboot into the recovery, extract the boot.img, unpack it, add your custom work, repack it and flash it back to the boot partition. To make this a lot easier, custom ROM's often comes pre-configured with an init.d feature, which is a small change to the boot.img that during boot, will execute any script located in /system/etc/init.d/. This makes it easier to add custom work for init as you will only need to add new scripts to this folder. 

If you are not running a custom ROM, but just rooted your stock version, you can use [Init.d Injector](http://forum.xda-developers.com/showthread.php?t=2256647) to add init.d support to it. 

Safe Mode
---------

There are two types of init.d support. One which executes scripts in the background while the OS is booting and one which executes scripts in the foreground while waiting for them to finish before booting the OS.

Sd-ext scripts works better with the second option. Once the OS is booted, it will start using the content placed on the data partition, in which case it is not smart to start moving it around. To avoid any issues, a safe mode has been added to Mounts2SD which will auto 
disable certain options that are known to cause damage under these circumstances whenever this type of init.d support is detected. 

Mounts2SD also has a work-around for this type of init.d support, which is used whenever safe mode has been disabled. By default, this is used. However, it is still possible to enable safe-mode if the work-around whould cause any problems on certain devices. 

You also have the option of adding the better type of support by using [Init.d Injector](http://forum.xda-developers.com/showthread.php?t=2256647)

System Applications
-------------------

Mounts2SD enabled a feature which allows you to store system files on /data or sd-ext (Files normally found in /system). The first added support was for system applications (/system/app), which could be placed on /data/app-system and then moved to sd-ext by the script depending on your application options. In Mounts2SD version 3+ (Script version 6+), a new feature was added which allows you to store anything system related on /data and sd-ext. By adding a folder to /data named [NAME]_s, for an example /data/app_s or /data/lib_s, Mounts2SD will link any content in these folders to it's original system folder, for an example /system/app and /system/lib. 

Unlike the old /data/app-system feature, using /data/[NAME]_s provides an additional option for moving the content to sd-ext, which enables you to keep system files on /data while moving regular applications to sd-ext (The app option will move both regular APK's and the once in /data/app-system). 

Script Properties
-----------------

In order to configure the script, some sort of configuration system is needed to store custom setups which the script can then read during boot. Mounts2SD uses property files located in /data/property. It keeps one files for each available option, and each file is named /data/property/m2sd.[OPTION]

* m2sd.move_apps `1 = enabled`, `0 = disabled`
    * If enabled, the script will move everything from /data/app, /data/app-private, /data/app-asec (Android 4.1+) and /data/app-system to equal folders in /sd-ext
* m2sd.move_dalvik `1 = enabled`, `0 = disabled`
    * If enabled, the script will move everything from /data/dalvik-cache to /sd-ext/dalvik-cache
* m2sd.move_data `1 = enabled`, `0 = disabled`
    * If enabled, the script will move everything from /data/data and /data/user (Android 4.2+) to equal folders in /sd-ext
* m2sd.move_libs `1 = enabled`, `0 = disabled`
    * If enabled, the script will move everything from /data/app-lib (Android 4.2+) to /sd-ext/app-lib
* m2sd.move_media `1 = enabled`, `0 = disabled`
    * If enabled, the script will move everything from /data/media (Used by some Samsung devices as internal storage) to /sd-ext/media
* m2sd.move_system `1 = enabled`, `0 = disabled`
    * If enabled, the script will move everything from /data/[NAME]_s to equal folders in /sd-ext
* m2sd.enable_cache `2 = auto`, `1 = enabled`, `0 = disabled`
    * If enabled, the script will move /cache to /sd-ext/cache. If set to auto, the script will use the partition with most available space, whether that being the original cache, sd-ext or /data
* m2sd.enable_swap `1 = enabled`, `0 = disabled`
    * If enabled, the script will enable the SWAP partition on the external sdcard. This needs a third partition on the sdcard formated as SWAP and it needs SWAP support in the kernel
* m2sd.enable_sdext_journal `2 = don't change`, `1 = enable journal`, `0 = disable journal`
    * If set to 1, the script will make sure that the sd-ext partition journal is enabled, if set to 0, it will make sure that it is disabled. If this is set to 2, the script will not change the journal settings and the current state will be kept as is. Note that this only works on Ext4 file systems and it needs the tune2fs binary
* m2sd.enable_debug `1 = enabled`, `0 = disabled`
    * If enabled, the script will provide a more detailed log file. Useful to locate issues
* m2sd.set_swap_level `Integer from 0 to 100`
    * This will set the swappiness value used by the SWAP and ZRAM option to determine the balance between swapping. A low value means the kernel will try to avoid swapping as much as possible where a higher value instead will make the kernel aggressively try to use swap space
* m2sd.set_sdext_fstype `Type like ext2 or xfs`, `auto = Auto Detection`
    * This should contain the file system driver in which the script should use when mounting the sd-ext partition
* m2sd.run_sdext_fschk `1 = enabled`, `0 = disabled`
    * If enabled, the script will run a file system check on sd-ext before mounting it. It will also try to repair any problems if possible. Note that this needs the e2fsck binary
* m2sd.set_storage_threshold `Integer in percentage, like 10 for 10%`, `0 = disable feature`
    * This should contain the storage threshold in percentage. When Android is installing applications, it has a rule that at least 10% of the /data partition size needs to be free in order to install. This means that if your /data partition is 1GB in size, you need to have at least 100Mb left on this partition in order to install new applications. This option allows you to change this value to for an example 1% (10Mb). If this is set to 0, the threshold will not be changed. Note that this needs the sqlite3 binary
* m2sd.set_zram_compression `Integer in percentage, like 10 for 10%`, `0 = disabled`
    * This will enable ZRAM on your device. The size of the ZRAM partition is calculated using the value of this property as percentage against the available memory on the device. So 18 means create a ZRAM partition which uses 18% of the available memory. If set to 0, ZRAM will be disabled. Note that needs ZRAM support in the kernel
* m2sd.set_emmc_readahead `Integer in kilobytes`
    * This should contain the readahead value which will be set on the external sdcard
* m2sd.set_emmc_scheduler `Scheduler type like row and cfq`
    * This should contain the scheduler type which will be set on the external sdcard
* m2sd.set_immc_readahead `Integer in kilobytes`
    * This should contain the readahead value which will be set on the internal nand
* m2sd.set_immc_scheduler `Scheduler type like row and cfq`
    * This should contain the scheduler type which will be set on the internal nand
* m2sd.disable_safemode `1 = disable safe mode`, `0 = use safe mode`
    * If safe-mode get's disabled, it will instead enable a work-around for service implemented init.d methods. Note that this property have no affect on inline init.d methods (when init.d is executed directly from init, and not from an underlaying service), as they don't use safe-mode nor the work-around

Root (SuperUser) permissions
----------------------------

Having root enabled on your device can be a great thing. It enables applications to provide much more features that Android would normally not allow applications to provide. However, it can also be very risky as applications which are granted root permissions can do anything that they like. There is no restrictions enabled for root. For this reason, no one should ever grand any closed source applications root permissions, as they could hide anything in their code. Mounts2SD is full Open Sourced, so anyone can look trough the code to see what both the application and the script is using root for. To make it easier for people that cannot read programming code, I will explain the usage of the root permissions.

Normal processes like applications and such, are not allowed to view or change any file or folder stored in /data which is not owned by the process itself. This means that in order for the script to move content not owned by Mounts2SD from /data to sd-ext, it needs to do so using root. 

Most of the application can do without root, however, calculating how much space each /data folder uses (Displayed in the app), needs root permissions as the app is not allowed access to any /data sub folder, not even to calculate the size of the content within. It also needs root to list the current value of the storge threshold, as this value is stored in a database file which is not accessable for regular applications. And last, it needs root in order to write configurations to the script property files located in /data/property, as this folder also is not accessable for regular applications. So most of the application root usage, is used to collect specific information. The only changes made as root, is to the scripts own property files. 

Screen Shots
------------

![Applications](https://raw.github.com/SpazeDog/mounts2sd/3.x/screenshots/overview_dark_port.png) : 
![Applications](https://raw.github.com/SpazeDog/mounts2sd/3.x/screenshots/configure_dark_port.png) : 
![Applications](https://raw.github.com/SpazeDog/mounts2sd/3.x/screenshots/configure_dark_port_dialog.png) : 
![Applications](https://raw.github.com/SpazeDog/mounts2sd/3.x/screenshots/log_dark_port.png) : 
![Applications](https://raw.github.com/SpazeDog/mounts2sd/3.x/screenshots/settings_dark_port.png) : 
![Applications](https://raw.github.com/SpazeDog/mounts2sd/3.x/screenshots/overview_light_port.png) : 
![Applications](https://raw.github.com/SpazeDog/mounts2sd/3.x/screenshots/configure_light_port_dialog.png) : 
![Applications](https://raw.github.com/SpazeDog/mounts2sd/3.x/screenshots/log_light_port.png) : 

![Applications](https://raw.github.com/SpazeDog/mounts2sd/3.x/screenshots/overview_dark_land.png) : 
![Applications](https://raw.github.com/SpazeDog/mounts2sd/3.x/screenshots/configure_dark_land_dialog.png) 

