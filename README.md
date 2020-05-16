# AudioBook App
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
