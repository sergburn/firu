/*******************************************************************************
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Sergey Burnevsky
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *******************************************************************************/

package com.burnevsky.firu.model;

import android.util.Log;

public class LangUtil
{
    public static int lang2Int(String lang)
    {
        int code = 0;
        byte[] bytes = null;
        try
        {
            bytes = lang.getBytes("UTF-8");
            Log.d("firu.model", String.format("LangUtil.lang2Int: '%s' -> %d", lang, code));
        }
        catch (Exception e)
        {
            Log.d("firu.model", "LangUtil.lang2Int: exception " + e.getMessage());
            e.printStackTrace();
            return 0;
        }

        for (int i = 0; i < Math.min(4, bytes.length); i++)
        {
            code |= bytes[i];
            code <<= 8;
        }
        return code;
    }

    public static String int2Lang(int code)
    {
        byte[] bytes = new byte[4];
        for (int i = 3; i >= 0; --i)
        {
            bytes[i] = (byte) code;
            code >>= 8;
        }
        try
        {
            String lang = new String(bytes, "UTF-8");
            Log.d("firu.model", String.format("LangUtil.int2Lang: %d -> '%s'", code, lang));
            return lang;
        }
        catch (Exception e)
        {
            Log.d("firu.model", "LangUtil.lang2Int: exception " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }
}
