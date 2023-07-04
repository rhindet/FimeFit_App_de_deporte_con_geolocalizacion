package com.arrap.fimefit

import android.text.TextUtils
import java.util.regex.Matcher
import java.util.regex.Pattern

class ValidatePassword {

    companion object{
        var pat: Pattern?= null
        var mat: Matcher?= null

        //valida si no es vacio, si tiene mas de 6 caracteres y es numerico
        fun isPassword(password: String): Boolean{
            if(TextUtils.isEmpty(password) || password.length < 6)
                return false
            else
            {
                pat = Pattern.compile("[0-9]+")
                mat = pat!!.matcher(password)

                return mat!!.find()
            }
        }
    }
}