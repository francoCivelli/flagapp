package com.example.introduccionkotlin.util

import java.util.Locale

object SearchHelper {
    fun removeSpecialCharacters(input: String) : String {
        val original = "áàäéèëíìïóòöúùuñÁÀÄÉÈËÍÌÏÓÒÖÚÙÜÑçÇ"
        val ascii = "aaaeeeiiiooouuunAAAEEEIIIOOOUUUNcC"
        var output = input
        for (i in original.indices) {
            output = output.replace(original[i], ascii[i])
        }
        return output.replace("[^A-Za-z0-9 ]".toRegex(), "")
            .trim { it <= ' ' || it == '.' }.lowercase(Locale.getDefault())
    }
}