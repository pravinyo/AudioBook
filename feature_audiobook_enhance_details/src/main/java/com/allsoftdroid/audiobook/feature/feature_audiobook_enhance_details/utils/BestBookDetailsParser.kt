package com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.utils

import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.model.WebDocument
import org.jsoup.Jsoup
import timber.log.Timber
import java.lang.IllegalArgumentException
import java.util.*

class BestBookDetailsParser {
    private var rank : List<Pair<Int,WebDocument>> = emptyList()
    private lateinit var titleKeyWords: List<String>

    fun getList(htmlResponse: String):List<WebDocument>{
        val list = getPageContent(htmlResponse)

        if(list.isEmpty()){
            Timber.d("Empty list item")
            return emptyList()
        }else{
            Timber.d("List is not empty: size -> ${list.size}")
        }

        return list
    }

    fun getListWithRanks(list: List<WebDocument>,bookTitle:String,bookAuthor:String):List<Pair<Int,WebDocument>>{

        if (list.isEmpty()) throw IllegalArgumentException("List cannot be empty")

        val title = formattedFilter(bookTitle)
        titleKeyWords = formattedList(title)
        Timber.d("Title keys=>$titleKeyWords")

        val author = formattedFilter(bookAuthor)
        val names =  formattedList(author)
        Timber.d("Author name keys=> $names")

        val newList = list.filter {
            hasWordInList(it.title.toLowerCase(Locale.ROOT), titleKeyWords) &&
                    hasWordInList(it.author.toLowerCase(Locale.ROOT), names)
        }.toSet()

        rank = getRankOfListItem(
            newList,
            titleKeyWords
        ).sortedByDescending {
            it.first
        }

        rank.forEach {
            Timber.d("Rank:${it.first}")
            Timber.d("Title: ${it.second.title}")
            Timber.d("Author: ${it.second.author}")
        }

        return rank
    }

    private fun hasWordInList(word:String, list:List<String>):Boolean{
        val newList = list.filter { word.contains(it) }
        return newList.isNotEmpty()
    }

    private fun formattedList(string: String) = string
        .split(" ")
        .map { it.toLowerCase(Locale.ROOT) }
        .filter { it.length>1 && it.toIntOrNull()==null}

    private fun formattedFilter(string: String) = string.toLowerCase(Locale.ROOT)
        .replace("(","")
        .replace(")","")
        .replace("-"," ")
        .replace(":"," ")
        .replace(","," ")

    private fun getPageContent(htmlResponse :String):List<WebDocument>{

        val htmlContent =
            processEscape(
                htmlResponse
            )

        val listOfDocument = mutableListOf<WebDocument>()

        Jsoup.parse(htmlContent).run {
            select("div.result-data").forEach {
                val title = it.select("h3 a").text()
                val author = it.select("p.book-author a").text()
                val link = it.select("h3 > a[href]").attr("href")
                val extra = it.select("p.book-meta").text().split("|")

                listOfDocument.add(
                    WebDocument(
                        title = title,
                        author = author,
                        url = link,
                        list = extra
                    )
                )
            }
        }

        return listOfDocument
    }

    private fun getRankOfListItem(newlist: Set<WebDocument>, titles:List<String>): List<Pair<Int, WebDocument>> {
        val list = mutableListOf<Pair<Int, WebDocument>>()
        newlist.forEach {document ->
            var count=0

            titles.forEach {
                if(document.title.toLowerCase(Locale.ROOT).contains(it)){
                    count++
                }
            }

            list.add(Pair(count,document))
        }
        return list
    }

    private fun processEscape(line: String): String {
        var temp = line.replace("\\\"","\"")

        temp = temp.replace("\\n","")
        temp = temp.replace("\\t","")
        return temp
    }
}