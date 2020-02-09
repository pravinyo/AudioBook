package com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.repository

import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.model.BookDetails
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.model.Chapter
import org.jsoup.Jsoup

class BookDetailsParsingFromNetworkResponseRespository {
    companion object{
        lateinit var bookDetails: BookDetails
        fun loadDetails(url:String):BookDetails{

            val chapterList = mutableListOf<Chapter>()

            Jsoup.connect(url).get().run {
                val runTime = this.select(".product-details > dd:nth-child(2)").text()
                println("Runtime: $runTime")

                val archiveLink = this.select("div.book-page-sidebar:nth-child(6) > p:nth-child(2) > a:nth-child(1)").attr("href")
                println("Archive:$archiveLink")
                val textLink = this.select("div.book-page-sidebar:nth-child(6) > p:nth-child(3) > a:nth-child(1)").attr("href")
                println("Text: $textLink")

                val description = this.select(".description").text()
                println(description)

                val genre = this.select("p.book-page-genre:nth-child(5)").text()
                println(genre)

                val lang = this.select("p.book-page-genre:nth-child(6)").text()
                println(lang)

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