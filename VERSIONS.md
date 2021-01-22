
# Version: 0.4.8-alpha

Finished the app settings screen, including the options to change tile colours

Added haptic feedback to buttons and sliders in the settings screen

Swipe down from the home screen to open notification panel

***currently the notification panel will not open if the home screen cannot be overscrolled (eg. having too few tiles)


# Version 0.4.79
Added more customization options

The next release (0.4.8) will include color picker

# Version 0.4.75
Added a blur effect toggle. More customization options incoming

better context references

resolved a memory leak when reloading home fragment on Android 8 and below

(Widget hosting is delayed. The settings screen is currently being worked on first.)

# Version 0.4.73
Migrated ChipsLayoutManager to Androidx

# Version 0.4.71

Fixed a memory leak when changing system wallpaper

# Version 0.4.7

Support Android 7.0

# Version 0.4.6

Removed widget fragment. In an upcoming version, widgets will be implemented on the home screen.

Hide status bar on Android 11

Navigation bar is now displayed on top of the launcher UI on Android 11.

# Version 0.4.5
added animations on tiles/app touched

retrieve currently displayed notifications at launcher start

optimized app drawer scroll

# Version 0.4.4
unregister broadcast receivers in fragments onDestroyView.

better usage of memory

# Version 0.4.3
test changed recyclerview cache size to increase performance

# Version 0.4.2 
added permission to query all packages due to Android 11's limitation on package visibility

# Version 0.4.1
fixed an issue that causes the app to crash when loading tiles on the home screen

# Version 0.4
1. basic functionality of the app drawer and home screen, app pinning and removing, and resizing tiles
2. app searching
3. app renaming
4. app hiding/unhiding
5. listen for notifications
6. app drawer indexing (fast scroller)
7. option to change the system wallpaper
8. a simple welcome page
