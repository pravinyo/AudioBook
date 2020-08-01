# AudioBook App
<table style="width:100%">
  <tr>
    <th>Details</th>
    <th>Store Link</td>
  </tr>
  <tr>
    <td> Current Version : 6.5.14 </td>
    <td rowspan="2">
      <a href='https://play.google.com/store/apps/details?id=com.allsoftdroid.audiobook&pcampaignid=pcampaignidMKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img      
      alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png' width="240px"/></a>
    </td>
  </tr>
  <tr>
    <td> Privacy Page of Published App: http://privacy.audiobooks.allsoftdroid.com/</td>
  </tr>
</table>

![AudioBook Feature UnitTest](https://github.com/pravinyo/AudioBook/workflows/AudioBook%20Feature%20UnitTest/badge.svg) ![AudioBook Dependency Graph Generator](https://github.com/pravinyo/AudioBook/workflows/AudioBook%20Dependency%20Graph%20Generator/badge.svg) ![Issues](https://img.shields.io/github/issues-closed/pravinyo/AudioBook)  ![Closed Pull Request](https://img.shields.io/github/issues-pr-closed/pravinyo/AudioBook) ![top language](https://img.shields.io/github/languages/top/pravinyo/AudioBook) ![license](https://img.shields.io/github/license/pravinyo/AudioBook)

A small AudioBook Project with the purpose of exploring multi module and new libraries in android Development. Some of this exploration are:
* Coroutines
* RxKotlin
* Jetpack testing
* Navigation pattern

### API used for backend data:
 - Archive API
 - LibriVox API

# Implementation techniques used:
This App is based on Single Activity Navigation. Each Fragment screen is developed as separate module which enable us to make app more modular and easy to reuse same componenet in other part of the application. Following are the independent modules in the project:

* `Database Module` - This module implements api to interact with the Database (Remote/Local).
* `Services Module` - This module contains background/foreground running services.
* `common Module` - This is core module which contains common code and it is shared with other dependent modules.
* `feature_book Module` - This module contains UI component which contains resource/code need to display book list. It is first screen that is displayed on the screen.
* `feature_book_details Module` - This module shows complete details of the selected Audio Book selected. It contains summary of the book and the playing tracks for the audio book.
* `feature_mini_player Module` - This module contains mini player UI/logic code and resource need to display this component. It shows the currently playing audio track.
* `feature_playerfullscreen Module` - This module contains full screen player UI/logic code and resource need to display this component. It shows the various progress and details for currently playing track
* `feature_downloader Module` - This module contains code save and download requested file locally
* `feature_audiobook_enhance_details Module` - This module provides additional api service to fetch  more details related to audiobook.
* `feature_settings Module` - This module enable user to change app default settings and other details.
* `feature_listen_later_ui Module` - This module contains UI, domain and data logic required to display bookmarked books. It displays data from the local database.

# Dependency Graph of the Project:
![AudioBook](/photos/dependencies_graph.png)

# Screenshots
<p float="left">
  <img src="/photos/mainscreen.png" width="200" />
  <img src="/photos/bookdetails.png" width="200" />
  <img src="/photos/listen_later.png" width="200" />
  <img src="/photos/mybooks.png" width="200" />
</p>

# Release: 6.5.14
## :star: New Features / Improvement
- user data export and import feature added

## :heart: Contributors

We'd like to thank all the contributors who worked on this release!

- [@pravinyo](https://github.com/pravinyo)


# Release: 6.5.13
## :star: New Features / Improvement
- Browser instance is used to load URL
- Cancel network request when parent is removed
- search books when keyboard entry pressed

## :beetle: Bug Fixes

- bug fixed for not playing book when loading first time.

## :heart: Contributors

We'd like to thank all the contributors who worked on this release!

- [@pravinyo](https://github.com/pravinyo)


# Release: 6.5.12
## :star: New Features / Improvement
- html code removed from track title

## :beetle: Bug Fixes

- app crash due to notification is fixed

## :heart: Contributors

We'd like to thank all the contributors who worked on this release!

- [@pravinyo](https://github.com/pravinyo)


# Release: 6.5.11
## :star: New Features / Improvement
- Refresh icon is replaced with Pull down to refresh swipe.
- Stroke added to album action in book details for better visibility
- Mini player slide up and down animation added
- Lock screen notification added for better UX

## :beetle: Bug Fixes

- Audio playing after brought from background in pause state fixed
- Book description popup screen dismiss when touch outside of the layout boundary fixed
- Notification partial display bug fixed
- Main Player screen white screen bug fixed

## :heart: Contributors

We'd like to thank all the contributors who worked on this release!

- [@pravinyo](https://github.com/pravinyo)


# Release: 6.5.8
## :star: New Features
- Play high quality tracks by default.

## :beetle: Bug Fixes

- Empty track bug fixed
- AudioService bug fixed

## :heart: Contributors

We'd like to thank all the contributors who worked on this release!

- [@pravinyo](https://github.com/pravinyo)


# Release: 6.5.7

## :beetle: Bug Fixes

* Fixed Downloader update
* Fixed Accessibility issues
* App Url fixed
* Previous button bug fixed
* Navigation bug fixed


## :heart: Contributors

We'd like to thank all the contributors who worked on this release!

- [@pravinyo](https://github.com/pravinyo)


# Release: 6.5.0
## :star: New Features

- Now downloader support resume and pause feature
- Downloads folder changed to Android/Data
- Implemented location preference for downloading media
- new notification look for downloading media

## :beetle: Bug Fixes

- fixed downloader crash on closing while downloading
- fixed play button bug
- fixed UI issue in book details screen
- fixed downloads screen bug
- fixed bug for downloads cancel button


## :heart: Contributors

We'd like to thank all the contributors who worked on this release!

- [@pravinyo](https://github.com/pravinyo)


# Release: 6.4.3
## :star: New Features

- launcher updated [#154](https://github.com/pravinyo/AudioBook/pull/154)
- Fix downloaded chapters ui bug [#153](https://github.com/pravinyo/AudioBook/pull/153)
- Player buffering state ui element [#152](https://github.com/pravinyo/AudioBook/pull/152)
- Change profile pic [#151](https://github.com/pravinyo/AudioBook/pull/151)
- Fix setting screen bug [#147](https://github.com/pravinyo/AudioBook/pull/147)
- profile icon change [#146](https://github.com/pravinyo/AudioBook/issues/146)
- Miniplayer swipe feature [#142](https://github.com/pravinyo/AudioBook/pull/142)
- url opening bug fixed [#141](https://github.com/pravinyo/AudioBook/pull/141)
- Fix time bug [#140](https://github.com/pravinyo/AudioBook/pull/140)

## :beetle: Bug Fixes

- fixed multiple download screen bug [#150](https://github.com/pravinyo/AudioBook/pull/150)
- Fix multi download screen [#148](https://github.com/pravinyo/AudioBook/pull/148)
- Multiple download screen is created [#145](https://github.com/pravinyo/AudioBook/issues/145)
- notification when clicked app is not appearing [#144](https://github.com/pravinyo/AudioBook/issues/144)
- settings screen back button not showing [#143](https://github.com/pravinyo/AudioBook/issues/143)
- Book details ui bug [#139](https://github.com/pravinyo/AudioBook/pull/139)

## :heart: Contributors

We'd like to thank all the contributors who worked on this release!

- [@pravinyo](https://github.com/pravinyo)

# Release: 6.2.2
## :beetle: Bug Fixes

- UI fixes and testing enhancement [#133](https://github.com/pravinyo/AudioBook/pull/133)

## :heart: Contributors

We'd like to thank all the contributors who worked on this release!

- [@pravinyo](https://github.com/pravinyo)

# Release: 6.2.0
## :star: New Features

- Added tooltip [#127](https://github.com/pravinyo/AudioBook/pull/127)
- Feature added for offline book playing and feature improvement [#116](https://github.com/pravinyo/AudioBook/pull/116)
- Crashlytics support [#113](https://github.com/pravinyo/AudioBook/pull/113), [#112](https://github.com/pravinyo/AudioBook/issues/112)
- My books UI added [#103](https://github.com/pravinyo/AudioBook/pull/103)
- Book details improved [#101](https://github.com/pravinyo/AudioBook/pull/101)
- Book management screen [#62](https://github.com/pravinyo/AudioBook/issues/62), [#57](https://github.com/pravinyo/AudioBook/issues/57), [#55](https://github.com/pravinyo/AudioBook/issues/55)

## :beetle: Bug Fixes

- multiple bug fixes [#126](https://github.com/pravinyo/AudioBook/pull/126)
- Fix downloads not clearing 120 issue [#130](https://github.com/pravinyo/AudioBook/pull/130)
- Fix booklist refresh [#129](https://github.com/pravinyo/AudioBook/pull/129)
- app is not loading list  [#128](https://github.com/pravinyo/AudioBook/issues/128)
- read button is not working [#125](https://github.com/pravinyo/AudioBook/issues/125)
- button clicks are not responding [#124](https://github.com/pravinyo/AudioBook/issues/124)
- no visual indication for opening player screen [#123](https://github.com/pravinyo/AudioBook/issues/123)
- book time text is blank in book details [#122](https://github.com/pravinyo/AudioBook/issues/122)
- information bottom sheet is floating upward [#121](https://github.com/pravinyo/AudioBook/issues/121)
- Downloads are not clearing from downloads screen [#120](https://github.com/pravinyo/AudioBook/issues/120)
- App crash when canceling multiple downloads  [#119](https://github.com/pravinyo/AudioBook/issues/119)
- replaced icon for listen later from book details [#118](https://github.com/pravinyo/AudioBook/issues/118)
- App state is lost when app is stopped by system [#114](https://github.com/pravinyo/AudioBook/issues/114)
- App crash on downloading [#108](https://github.com/pravinyo/AudioBook/issues/108)
- Main PLayer Ui bug [#107](https://github.com/pravinyo/AudioBook/issues/107)
- Downloads location issue [#106](https://github.com/pravinyo/AudioBook/issues/106), [#102](https://github.com/pravinyo/AudioBook/issues/102)
- UI bug in My Books screen [#105](https://github.com/pravinyo/AudioBook/issues/105)
- Fix remove download folder choice [#111](https://github.com/pravinyo/AudioBook/pull/111)
- fixed UI element bug [#110](https://github.com/pravinyo/AudioBook/pull/110)
- Fix main player ui issue [#109](https://github.com/pravinyo/AudioBook/pull/109)
- Fix issue 102 download location [#104](https://github.com/pravinyo/AudioBook/pull/104)
- Fix save state bug [#115](https://github.com/pravinyo/AudioBook/pull/115)

## :heart: Contributors

We'd like to thank all the contributors who worked on this release!

- [@pravinyo](https://github.com/pravinyo)

# Release: 5.5.2
## :star: New Features

- Main player ui redesigned and rewind, forward event added [#98](https://github.com/pravinyo/AudioBook/pull/98)
- Privacy and Feedback Option added [#97](https://github.com/pravinyo/AudioBook/pull/97)
- Listen later feature added  [#95](https://github.com/pravinyo/AudioBook/pull/95), [#96](https://github.com/pravinyo/AudioBook/pull/96), [#54](https://github.com/pravinyo/AudioBook/issues/54)
- Nav drawer improved  [#94](https://github.com/pravinyo/AudioBook/pull/94)
- Download Location settings included [#79](https://github.com/pravinyo/AudioBook/issues/79)
- Book Detail UI improvement [#64](https://github.com/pravinyo/AudioBook/issues/64)
- Main Player UI enhancement [#58](https://github.com/pravinyo/AudioBook/issues/58)
- Feature for checking downloads, help [#56](https://github.com/pravinyo/AudioBook/issues/56)

## :heart: Contributors

We'd like to thank all the contributors who worked on this release!

- [@pravinyo](https://github.com/pravinyo)

# Release: 4.3.0
## :star: New Features/Fixes

- Minor bug fixed for AudioManager
- Resume play bug fixed
- End to end test [#91](https://github.com/pravinyo/AudioBook/pull/91)
- Android test [#90](https://github.com/pravinyo/AudioBook/pull/90)
- Unit test added to modules [#89](https://github.com/pravinyo/AudioBook/pull/89)


# Release: 4.2.3-alpha
## :star: New Features
- Added Support to play offline downloaded files.
- Smartly create playlist from downloaded files and online files to save bandwidth [#75](https://github.com/pravinyo/AudioBook/issues/75)
- Downloader is improved [#53](https://github.com/pravinyo/AudioBook/issues/53), [#52](https://github.com/pravinyo/AudioBook/issues/52)

## :beetle: Bug Fixes

- Fix player connection [#83](https://github.com/pravinyo/AudioBook/pull/83)
- Player is not playing when app is reconnected to internet [#82](https://github.com/pravinyo/AudioBook/issues/82)
- Bug play downloaded file if there [#81](https://github.com/pravinyo/AudioBook/pull/81)
- Fix offline mode crash [#80](https://github.com/pravinyo/AudioBook/pull/80)
- Fix player delay bug [#78](https://github.com/pravinyo/AudioBook/pull/78)
- App crash when opening book in offline mode [#76](https://github.com/pravinyo/AudioBook/issues/76)
- Book Detail Screen show waiting indicator while player is fetching chapter [#72](https://github.com/pravinyo/AudioBook/issues/72)
- App crash [#51](https://github.com/pravinyo/AudioBook/issues/51)
- player is playing after in pause  [#36](https://github.com/pravinyo/AudioBook/issues/36)
- memory leak : service is still running after app closed [#34](https://github.com/pravinyo/AudioBook/issues/34)

## :heart: Contributors

We'd like to thank all the contributors who worked on this release!

- [@pravinyo](https://github.com/pravinyo)

## Special thanks
- Librivox for providing contents as in public domain
