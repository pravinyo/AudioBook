# AudioBook App
A small AudioBook Project with the purpose of exploring multi module and new libraries in android Development. Some of this exploration are:
* Coroutines
* RxKotlin
* Jetpack testing
* Navigation pattern

> API Used: Made using Archive.org API

# Implementation techniques used
This App is based on Single Activity Navigation. Each Fragment screen is developed as separate module which enable us to make app more modular and easy to reuse same componenet in other part of the application. Following are the independent modules in the project:

* Database Module 
> This module implements api to interact with the Database (Remote/Local).
* Services Module
> This module contains background/foreground running services.
* common Module
> This is core module which contains common code and it is shared with other dependent modules.
* feature_book Module
> This module contains UI component which contains resource/code need to display book list. It is first screen that is displayed on the screen.
* feature_book_details Module
> This module shows complete details of the selected Audio Book selected. It contains summary of the book and the playing tracks for the audio book.
* feature_mini_player Module
> This module contains mini player UI/logic code and resource need to display this component. It shows the currently playing audio track.


## Special thanks
- Librivox for providing contents as in public domain
