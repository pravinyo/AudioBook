package com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.repository

import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.model.BookDetails
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.model.Chapter
import org.jsoup.Jsoup

class BookDetailsParsingFromNetworkResponse {
    companion object{
        lateinit var bookDetails: BookDetails
        fun loadDetails(url:String):BookDetails{

            val chapterList = mutableListOf<Chapter>()

            Jsoup.connect(url).get().run {
                val runTime = this.select(".product-details > dd:nth-child(2)").text()

                val archiveLink = this.select("div.book-page-sidebar:nth-child(6) > p:nth-child(2) > a:nth-child(1)").attr("href")

                val textLink = this.select("div.book-page-sidebar:nth-child(6) > p:nth-child(3) > a:nth-child(1)").attr("href")

                val description = this.select(".description").text()

                var genre = this.select("p.book-page-genre:nth-child(5)").text()
                if(genre.contains(":")){
                    genre = genre.split(":").last()
                }

                var lang = this.select("p.book-page-genre:nth-child(6)").text()
                if(lang.contains(":")){
                    lang = lang.split(":").last()
                }

                this.select("table.chapter-download tr").forEach {
                    val chap = chapterList.size
                    val name = it.select("a.chapter-name").text()
                    val author = it.select("td:nth-child(3) a").text()
                    val reader = it.select("td:nth-child(5) > a:nth-child(1)").text()

                    chapterList.add(Chapter(number = chap, name = name,author = author,reader = reader))
                }

                bookDetails =  BookDetails(
                    runtime = runTime,
                    archiveUrl = archiveLink,
                    gutenbergUrl = textLink,
                    description = description,
                    genres = genre,
                    language = lang,
                    chapters = chapterList
                )

                return bookDetails
            }
        }
    }
}