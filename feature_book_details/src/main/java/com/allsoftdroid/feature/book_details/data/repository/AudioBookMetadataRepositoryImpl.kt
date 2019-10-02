//package com.allsoftdroid.feature.book_details.data.repository
//
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.Transformations
//import com.allsoftdroid.feature.book_details.domain.model.AudioBookMetadataDomainModel
//import com.allsoftdroid.feature.book_details.domain.repository.AudioBookMetadataRepository
//
//class AudioBookMetadataRepositoryImpl : AudioBookMetadataRepository{
//
//    /**
//     * Books type live data is fetched from the database and notify observer for any change in data.
//     * Books count at restricted to {@link BOOK_LIMIT}.
//     * Data are converted to Domain model type instance
//     */
//    private var _audioBookMetadata : LiveData<AudioBookMetadataDomainModel> = Transformations.map(
//
//    ){
//        it.asBookDomainModel()
//    }
//
//    private val audioBookMetadata : LiveData<AudioBookMetadataDomainModel>
//        get() = _audioBookMetadata
//
//
//    override suspend fun loadMetadataForBookId(bookId: String) {
//
//    }
//
//    override suspend fun getMetadata() = audioBookMetadata
//
//}