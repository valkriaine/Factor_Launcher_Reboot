# Version 0.62-beta

- added support for Chinese pinyin sorting in the app drawer
- added drop shadow to recent apps
- bug fixes

# Version 0.61-beta

- added quick access to 3 recent apps
- removed the animation when deleting tiles due to a layout animation issue

# Version 0.59-beta

- better logic when loading apps, handle apps with illegal names

# Version 0.58-beta

- Optimized touch area in the app drawer

# Version 0.57-beta

- handle loading app shortcuts with no valid icon

# Version 0.56-beta

- fixed some stability issues 

# Version 0.55-beta

- Adjusted tile element sizes based on percentage 

- Scroll to access more shortcuts on large tiles

- Minor bug fixes

- Better MVVM logic

# Version 0.54-beta

- Better MVVM architecture

- Minor layout adjustments

- Bug fixes

# Version 0.52-beta

- Bug fixes


# Version 0.52-alpha

- Better first launch logic

- Resolved an issue that causes the app the crash when opening the options menu on Android 8.0 devices 

# Version 0.51

- Bug fixes

# Version 0.5

- Initial early access release on Play Store

- Dropped support for 5.0 due to [this issue](https://github.com/Valkriaine/Factor_Launcher_Reboot/issues/44).

# Version: 0.4.8-alpha

- Finished the app settings screen, including the options to change tile colours

- Added haptic feedback to buttons and sliders in the settings screen

- Swipe down from the home screen to open notification panel

***currently the notification panel will not open if the home screen cannot be overscrolled (eg. having too few tiles)


# Version 0.4.79
- Added more customization options

- The next release (0.4.8) will include color picker

# Version 0.4.75
- Added a blur effect toggle. More customization options incoming

- better context references

- resolved a memory leak when reloading home fragment on Android 8 and below

- (Widget hosting is delayed. The settings screen is currently being worked on first.)

# Version 0.4.73
- Migrated ChipsLayoutManager to Androidx

# Version 0.4.71

- Fixed a memory leak when changing system wallpaper

# Version 0.4.7

- Support Android 7.0

# Version 0.4.6

- Removed widget fragment. In an upcoming version, widgets will be implemented on the home screen.

- Hide status bar on Android 11

- Navigation bar is now displayed on top of the launcher UI on Android 11.

# Version 0.4.5
- added animations on tiles/app touched

- retrieve currently displayed notifications at launcher start

- optimized app drawer scroll

# Version 0.4.4
- unregister broadcast receivers in fragments onDestroyView.

- better usage of memory

# Version 0.4.3
- test changed recyclerview cache size to increase performance

# Version 0.4.2 
- added permission to query all packages due to Android 11's limitation on package visibility

# Version 0.4.1
- fixed an issue that causes the app to crash when loading tiles on the home screen

# Version 0.4
- basic functionality of the app drawer and home screen, app pinning and removing, and resizing tiles
- app searching
- app renaming
- app hiding/unhiding
- listen for notifications
- app drawer indexing (fast scroller)
- option to change the system wallpaper
- a simple welcome page
