package com.allsoftdroid.feature.book_details.presentation.viewModel

//
//@Suppress("UNCHECKED_CAST")
///**
// * Check for the Specified ViewModel existence and return it's instance if already implemented
// * Otherwise return exception
// */
//class BookDetailsViewModelFactory(private val application: Application,private val bookId:String): ViewModelProvider.Factory {
//
//    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(BookDetailsViewModel::class.java)) {
//
//            //database
//            val database = AudioBookDatabase.getDatabase(application)
//            //repository reference
//            val metadataRepository = AudioBookMetadataRepositoryImpl(database.metadataDao(),bookId)
//
//
//            //Book metadata use case
//            val getMetadataUsecase = GetMetadataUsecase(metadataRepository)
//
//
//            //Book Track list usecase
//            val getTrackListUsecase = GetTrackListUsecase(metadataRepository)
//
//            //Use case handler
//            val useCaseHandler  = UseCaseHandler.getInstance()
//
////            //sharedPref Dependency
////            val sharedPreferences by lazy {
////                BookDetailsSharedPreferencesRepositoryImpl.create(application)
////            }
//
////            val modelState = BookDetailsStateModel(
////                isPlaying = sharedPreferences.isPlaying(),
////                title = sharedPreferences.trackTitle(),
////                trackPlaying = sharedPreferences.trackPosition())
//
//            return BookDetailsViewModel(
//                application = application,
////                state = modelState,
//                useCaseHandler = useCaseHandler,
//                getMetadataUsecase = getMetadataUsecase,
//                getTrackListUsecase = getTrackListUsecase) as T
//        }
//
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//
//}