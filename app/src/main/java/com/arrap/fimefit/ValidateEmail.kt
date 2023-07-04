package com.arrap.fimefit

import java.util.regex.Matcher
import java.util.regex.Pattern

class ValidateEmail {
    companion object{
        var pat: Pattern?= null
        var mat: Matcher?= null



        fun isEmail(email:String): Boolean{
            pat = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")
            mat = pat!!.matcher(email)
            return  mat!!.find()
        }

    }
}