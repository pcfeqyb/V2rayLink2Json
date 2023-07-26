package com.v2ray.ang.util

import java.util.*

import com.v2ray.ang.AppConfig
import java.net.*
//import sun.net.util.URLUtil
import java.io.IOException

import java.math.BigInteger
import java.security.MessageDigest


object Utils {

    /**
     * convert string to editalbe for kotlin
     *
     * @param text
     * @return
     */


    fun md5_hash(input:String): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
    }

    fun my_base64(input:String): String {
        return Base64.getEncoder().encodeToString(input.toByteArray());
    }

    /**
     * parseInt
     */
    fun parseInt(str: String): Int {
        return parseInt(str, 0)
    }

    fun parseInt(str: String?, default: Int): Int {
        str ?: return default
        return try {
            Integer.parseInt(str)
        } catch (e: Exception) {
            e.printStackTrace()
            default
        }
    }


    /**
     * base64 decode
     */
    fun decode(text: String): String {
        tryDecodeBase64(text)?.let { return it }
        if (text.endsWith('=')) {
            // try again for some loosely formatted base64
            tryDecodeBase64(text.trimEnd('='))?.let { return it }
        }
        return ""
    }

    fun tryDecodeBase64(text: String): String? {
        try {
            return Base64.getDecoder().decode(text.toByteArray(Charsets.UTF_8)).toString(charset("UTF-8"))
        } catch (e: Exception) {
            println("Parse base64 standard failed $e")
        }
        try {
            return Base64.getDecoder().decode(text.toByteArray(Charsets.UTF_8)).toString(charset("UTF-8"))
        } catch (e: Exception) {
            println("Parse base64 url safe failed $e")
        }
        return null
    }



    fun encode(text: String): String {
        return try {
            Base64.getEncoder().encodeToString(text.toByteArray(charset("UTF-8")) )
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }



    /**
     * is ip address
     */
    fun isIpAddress(value: String): Boolean {
        try {
            var addr = value
            if (addr.isEmpty() || addr.isBlank()) {
                return false
            }
            //CIDR
            if (addr.indexOf("/") > 0) {
                val arr = addr.split("/")
                if (arr.count() == 2 && Integer.parseInt(arr[1]) > 0) {
                    addr = arr[0]
                }
            }

            // "::ffff:192.168.173.22"
            // "[::ffff:192.168.173.22]:80"
            if (addr.startsWith("::ffff:") && '.' in addr) {
                addr = addr.drop(7)
            } else if (addr.startsWith("[::ffff:") && '.' in addr) {
                addr = addr.drop(8).replace("]", "")
            }

            // addr = addr.toLowerCase()
            val octets = addr.split('.').toTypedArray()
            if (octets.size == 4) {
                if (octets[3].indexOf(":") > 0) {
                    addr = addr.substring(0, addr.indexOf(":"))
                }
                return isIpv4Address(addr)
            }

            // Ipv6addr [2001:abc::123]:8080
            return isIpv6Address(addr)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun isPureIpAddress(value: String): Boolean {
        return (isIpv4Address(value) || isIpv6Address(value))
    }

    fun isIpv4Address(value: String): Boolean {
        val regV4 = Regex("^([01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])\\.([01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])\\.([01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])\\.([01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])$")
        return regV4.matches(value)
    }

    fun isIpv6Address(value: String): Boolean {
        var addr = value
        if (addr.indexOf("[") == 0 && addr.lastIndexOf("]") > 0) {
            addr = addr.drop(1)
            addr = addr.dropLast(addr.count() - addr.lastIndexOf("]"))
        }
        val regV6 = Regex("^((?:[0-9A-Fa-f]{1,4}))?((?::[0-9A-Fa-f]{1,4}))*::((?:[0-9A-Fa-f]{1,4}))?((?::[0-9A-Fa-f]{1,4}))*|((?:[0-9A-Fa-f]{1,4}))((?::[0-9A-Fa-f]{1,4})){7}$")
        return regV6.matches(addr)
    }

    private fun isCoreDNSAddress(s: String): Boolean {
        return s.startsWith("https") || s.startsWith("tcp") || s.startsWith("quic")
    }


    //--------------------------------------------

    /**
     * uuid
     */
    fun getUuid(): String {
        return try {
            UUID.randomUUID().toString().replace("-", "")
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun urlDecode(url: String): String {
        return try {
            URLDecoder.decode(URLDecoder.decode(url), "utf-8")
        } catch (e: Exception) {
            e.printStackTrace()
            url
        }
    }

    fun urlEncode(url: String): String {
        return try {
            URLEncoder.encode(url, "UTF-8")
        } catch (e: Exception) {
            e.printStackTrace()
            url
        }
    }



    fun getUrlContext(url: String, timeout: Int): String {
        var result: String
        var conn: HttpURLConnection? = null

        try {
            conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = timeout
            conn.readTimeout = timeout
            conn.setRequestProperty("Connection", "close")
            conn.instanceFollowRedirects = false
            conn.useCaches = false
            //val code = conn.responseCode
            result = conn.inputStream.bufferedReader().readText()
        } catch (e: Exception) {
            result = ""
        } finally {
            conn?.disconnect()
        }
        return result
    }



    fun getIpv6Address(address: String): String {
        return if (isIpv6Address(address)) {
            String.format("[%s]", address)
        } else {
            address
        }
    }

    fun fixIllegalUrl(str: String): String {
        return str
            .replace(" ","%20")
            .replace("|","%7C")
    }

    fun removeWhiteSpace(str: String?): String? {
        return str?.replace(" ", "")
    }

    fun idnToASCII(str: String): String {
        val url = URL(str)
        return URL(url.protocol, IDN.toASCII(url.host, IDN.ALLOW_UNASSIGNED), url.port, url.file)
            .toExternalForm()
    }

}

