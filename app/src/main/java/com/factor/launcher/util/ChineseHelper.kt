package com.factor.launcher.util

import net.sourceforge.pinyin4j.PinyinHelper
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination


// helper class to convert Chinese to pinyin string
object ChineseHelper
{
    private fun getCharacterPinYin(c: Char): String?
    {
        val format = HanyuPinyinOutputFormat()
        var pinyin: Array<String?>? = null
        format.toneType = HanyuPinyinToneType.WITHOUT_TONE
        try
        {
            pinyin = PinyinHelper.toHanyuPinyinStringArray(c, format)
        }
        catch (e: BadHanyuPinyinOutputFormatCombination)
        {
            e.printStackTrace()
        }
        return pinyin?.get(0)
    }

    fun getStringPinYin(str: String): String
    {
        val sb = StringBuilder()
        var tempPinyin: String?
        for (i in str.indices)
        {
            tempPinyin = getCharacterPinYin(str[i])
            if (tempPinyin == null)
            {
                sb.append(str[i])
            }
            else
            {
                sb.append(tempPinyin)
            }
        }
        return sb.toString()
    }
}
