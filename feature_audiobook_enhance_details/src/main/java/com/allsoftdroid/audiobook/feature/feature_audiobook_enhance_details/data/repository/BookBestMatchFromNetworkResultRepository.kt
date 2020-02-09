package com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.repository

import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.model.WebDocument
import org.jsoup.Jsoup

class BookBestMatchFromNetworkResultRepository {
    companion object{
        lateinit var rank : List<Pair<Int,WebDocument>>

        fun getList(htmlResponse: String):List<WebDocument>{
            val list =
                getPageContent(
                    htmlResponse
                )

            if(list.isEmpty()){
                println("Empty list item")
                return emptyList()
            }

            val title =
                formattedFilter(
                    "Selected poems by Thomas Chatterton (in Library of the World's Best Literature, Ancient and Modern, volume 9)"
                )
            val titles =
                formattedList(
                    title
                )
            println(titles)

            val author =
                formattedFilter(
                    "VARIOUS ( - )"
                )
            val names =
                formattedList(
                    author
                )
            println(names)

            val newlist = list.filter {
                hasWordInList(
                    it.title.toLowerCase(),
                    titles
                ) && hasWordInList(
                    it.author.toLowerCase(),
                    names
                )
            }.toSet()

            rank = getRankOfListItem(
                newlist,
                titles
            ).sortedByDescending {
                it.first
            }


            rank.forEach {
                println("Rank:${it.first}")
                println("Title: ${it.second.title}")
                println("Author: ${it.second.author}")
            }

            println("Best match is: ${rank[0]}")

            return list
        }

        private fun hasWordInList(word:String, list:List<String>):Boolean{
            val newList = list.filter { word.contains(it) }
            return newList.isNotEmpty()
        }

        private fun formattedList(string: String) = string.split(" ").map { it.toLowerCase()}.filter { it.length>1 && it.toIntOrNull()==null}

        private fun formattedFilter(string: String) = string.toLowerCase()
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
                    if(document.title.toLowerCase().contains(it)){
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

}