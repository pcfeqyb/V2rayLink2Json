package com.v2ray.ang.dto

import com.google.gson.Gson
import com.v2ray.ang.dto.EConfigType
import com.v2ray.ang.dto.ServerConfig
import com.v2ray.ang.dto.V2rayConfig
import com.v2ray.ang.dto.V2rayConfig.Companion.DEFAULT_SECURITY
import com.v2ray.ang.dto.V2rayConfig.Companion.TLS
import com.v2ray.ang.dto.VmessQRCode
import com.v2ray.ang.util.Utils
import java.net.URI
import com.v2ray.ang.extension.idnHost


class ConfigManager {

    // -------------------------- GFW knocker -----------------------------------------
    fun GFW_make_Config_from_link(
        str: String?,
        subid: String
    ): ServerConfig? {
        try {
            if (str.isNullOrEmpty()) {
                return null
            }


            var config: ServerConfig? = null
            val allowInsecure = true // GFW-knocker: just disable root CA check by default that cause many configs to fail
            if (str.startsWith(EConfigType.VMESS.protocolScheme)) {
                config = ServerConfig.create(EConfigType.VMESS)
                val streamSetting = config.outboundBean?.streamSettings ?: return null


                if (!tryParseNewVmess(str, config, allowInsecure)) {
                    if (str.indexOf("?") > 0) {
                        if (!tryResolveVmess4Kitsunebi(str, config)) {
                            return null
                        }
                    } else {
                        var result = str.replace(EConfigType.VMESS.protocolScheme, "")
                        result = Utils.decode(result)
                        if (result.isEmpty()) {
                            return null
                        }
                        val vmessQRCode = Gson().fromJson(result, VmessQRCode::class.java)
                        // Although VmessQRCode fields are non null, looks like Gson may still create null fields
                        if (   (vmessQRCode.add.isEmpty())
                            || (vmessQRCode.port.isEmpty())
                            || (vmessQRCode.id.isEmpty())
                            || (vmessQRCode.net.isEmpty())
                        ) {
                            return null
                        }

                        config.remarks = vmessQRCode.ps
                        config.outboundBean?.settings?.vnext?.get(0)?.let { vnext ->
                            vnext.address = vmessQRCode.add
                            vnext.port = Utils.parseInt(vmessQRCode.port)
                            vnext.users[0].id = vmessQRCode.id
                            vnext.users[0].security =
                                if ((vmessQRCode.scy.isEmpty())) DEFAULT_SECURITY else vmessQRCode.scy
                            vnext.users[0].alterId = Utils.parseInt(vmessQRCode.aid)
                        }
                        val sni = streamSetting.populateTransportSettings(
                            vmessQRCode.net,
                            vmessQRCode.type,
                            vmessQRCode.host,
                            vmessQRCode.path,
                            vmessQRCode.path,
                            vmessQRCode.host,
                            vmessQRCode.path,
                            vmessQRCode.type,
                            vmessQRCode.path
                        )

                        val fingerprint = vmessQRCode.fp ?: streamSetting.tlsSettings?.fingerprint
                        streamSetting.populateTlsSettings(
                            vmessQRCode.tls, allowInsecure,
                            if ((vmessQRCode.sni.isEmpty())) sni else vmessQRCode.sni,
                            fingerprint, vmessQRCode.alpn, null, null, null
                        )
                    }
                }
            } else if (str.startsWith(EConfigType.SHADOWSOCKS.protocolScheme)) {
                config = ServerConfig.create(EConfigType.SHADOWSOCKS)
                if (!tryResolveResolveSip002(str, config)) {
                    var result = str.replace(EConfigType.SHADOWSOCKS.protocolScheme, "")
                    val indexSplit = result.indexOf("#")
                    if (indexSplit > 0) {
                        try {
                            config.remarks =
                                Utils.urlDecode(result.substring(indexSplit + 1, result.length))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        result = result.substring(0, indexSplit)
                    }

                    //part decode
                    val indexS = result.indexOf("@")
                    result = if (indexS > 0) {
                        Utils.decode(result.substring(0, indexS)) + result.substring(
                            indexS,
                            result.length
                        )
                    } else {
                        Utils.decode(result)
                    }

                    val legacyPattern = "^(.+?):(.*)@(.+?):(\\d+?)/?$".toRegex()
                    val match = legacyPattern.matchEntire(result)
                        ?: return null

                    config.outboundBean?.settings?.servers?.get(0)?.let { server ->
                        server.address = match.groupValues[3].removeSurrounding("[", "]")
                        server.port = match.groupValues[4].toInt()
                        server.password = match.groupValues[2]
                        server.method = match.groupValues[1].lowercase()
                    }
                }
            } else if (str.startsWith(EConfigType.SOCKS.protocolScheme)) {
                var result = str.replace(EConfigType.SOCKS.protocolScheme, "")
                val indexSplit = result.indexOf("#")
                config = ServerConfig.create(EConfigType.SOCKS)
                if (indexSplit > 0) {
                    try {
                        config.remarks =
                            Utils.urlDecode(result.substring(indexSplit + 1, result.length))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    result = result.substring(0, indexSplit)
                }

                //part decode
                val indexS = result.indexOf("@")
                if (indexS > 0) {
                    result = Utils.decode(result.substring(0, indexS)) + result.substring(
                        indexS,
                        result.length
                    )
                } else {
                    result = Utils.decode(result)
                }

                val legacyPattern = "^(.*):(.*)@(.+?):(\\d+?)$".toRegex()
                val match =
                    legacyPattern.matchEntire(result) ?: return null

                config.outboundBean?.settings?.servers?.get(0)?.let { server ->
                    server.address = match.groupValues[3].removeSurrounding("[", "]")
                    server.port = match.groupValues[4].toInt()
                    val socksUsersBean =
                        V2rayConfig.OutboundBean.OutSettingsBean.ServersBean.SocksUsersBean()
                    socksUsersBean.user = match.groupValues[1].lowercase()
                    socksUsersBean.pass = match.groupValues[2]
                    server.users = listOf(socksUsersBean)
                }
            } else if (str.startsWith(EConfigType.TROJAN.protocolScheme)) {
                val uri = URI(Utils.fixIllegalUrl(str))
                config = ServerConfig.create(EConfigType.TROJAN)
                config.remarks = Utils.urlDecode(uri.fragment ?: "")

                var flow = ""
                var fingerprint = config.outboundBean?.streamSettings?.tlsSettings?.fingerprint
                if (uri.rawQuery != null) {
                    val queryParam = uri.rawQuery.split("&")
                        .associate { it.split("=").let { (k, v) -> k to Utils.urlDecode(v) } }

                    val sni = config.outboundBean?.streamSettings?.populateTransportSettings(
                        queryParam["type"] ?: "tcp",
                        queryParam["headerType"],
                        queryParam["host"],
                        queryParam["path"],
                        queryParam["seed"],
                        queryParam["quicSecurity"],
                        queryParam["key"],
                        queryParam["mode"],
                        queryParam["serviceName"]
                    )
                    fingerprint = queryParam["fp"] ?: ""
                    config.outboundBean?.streamSettings?.populateTlsSettings(
                        queryParam["security"] ?: TLS,
                        allowInsecure, queryParam["sni"] ?: sni!!, fingerprint, queryParam["alpn"],
                        null, null, null
                    )
                    flow = queryParam["flow"] ?: ""
                } else {
                    config.outboundBean?.streamSettings?.populateTlsSettings(
                        TLS, allowInsecure, "",
                        fingerprint, null, null, null, null
                    )
                }

                config.outboundBean?.settings?.servers?.get(0)?.let { server ->
                    server.address = uri.idnHost
                    server.port = uri.port
                    server.password = uri.userInfo
                    server.flow = flow
                }
            } else if (str.startsWith(EConfigType.VLESS.protocolScheme)) {
                val uri = URI(Utils.fixIllegalUrl(str))
                val queryParam = uri.rawQuery.split("&")
                    .associate { it.split("=").let { (k, v) -> k to Utils.urlDecode(v) } }
                config = ServerConfig.create(EConfigType.VLESS)
                val streamSetting = config.outboundBean?.streamSettings ?: return null
                var fingerprint = streamSetting.tlsSettings?.fingerprint

                config.remarks = Utils.urlDecode(uri.fragment ?: "")
                config.outboundBean?.settings?.vnext?.get(0)?.let { vnext ->
                    vnext.address = uri.idnHost
                    vnext.port = uri.port
                    vnext.users[0].id = uri.userInfo
                    vnext.users[0].encryption = queryParam["encryption"] ?: "none"
                    vnext.users[0].flow = queryParam["flow"] ?: ""
                }

                val sni = streamSetting.populateTransportSettings(
                    queryParam["type"] ?: "tcp",
                    queryParam["headerType"],
                    queryParam["host"],
                    queryParam["path"],
                    queryParam["seed"],
                    queryParam["quicSecurity"],
                    queryParam["key"],
                    queryParam["mode"],
                    queryParam["serviceName"]
                )
                fingerprint = queryParam["fp"] ?: ""
                val pbk = queryParam["pbk"] ?: ""
                val sid = queryParam["sid"] ?: ""
                val spx = Utils.urlDecode(queryParam["spx"] ?: "")
                streamSetting.populateTlsSettings(
                    queryParam["security"] ?: "", allowInsecure,
                    queryParam["sni"] ?: sni, fingerprint, queryParam["alpn"], pbk, sid, spx
                )
            }
            if (config == null) {
                return null
            }
            config.subscriptionId = subid

            /*
            val guid = MmkvManager.encodeServerConfig("", config)
            if (removedSelectedServer != null &&
                config.getProxyOutbound()
                    ?.getServerAddress() == removedSelectedServer.getProxyOutbound()
                    ?.getServerAddress() &&
                config.getProxyOutbound()
                    ?.getServerPort() == removedSelectedServer.getProxyOutbound()?.getServerPort()
            ) {
                mainStorage?.encode(KEY_SELECTED_SERVER, guid)
            }
            return guid

             */
//            println(config)
            return config
        } catch (e: Exception) {
//            e.printStackTrace()
            println("Parsing Problem => "+e.toString())
            return null
        }
        return null
    }
    // --------------------------------------------------------------------------------


/*
    fun encodeServerConfig(guid: String, config: ServerConfig): String {
        val key = guid.ifBlank { Utils.getUuid() }
        serverStorage?.encode(key, Gson().toJson(config))
        val serverList = decodeServerList()
        if (!serverList.contains(key)) {
            serverList.add(0, key)
            mainStorage?.encode(KEY_ANG_CONFIGS, Gson().toJson(serverList))
            if (mainStorage?.decodeString(KEY_SELECTED_SERVER).isNullOrBlank()) {
                mainStorage?.encode(KEY_SELECTED_SERVER, key)
            }
        }
        return key
    }
 */




    private fun tryParseNewVmess(
        uriString: String,
        config: ServerConfig,
        allowInsecure: Boolean
    ): Boolean {
        return runCatching {
            val uri = URI(uriString)
            check(uri.scheme == "vmess")
            val (_, protocol, tlsStr, uuid, alterId) =
                Regex("(tcp|http|ws|kcp|quic|grpc)(\\+tls)?:([0-9a-z]{8}-[0-9a-z]{4}-[0-9a-z]{4}-[0-9a-z]{4}-[0-9a-z]{12})")
                    .matchEntire(uri.userInfo)?.groupValues
                    ?: error("parse user info fail.")
            val tls = tlsStr.isNotBlank()
            val queryParam = uri.rawQuery.split("&")
                .associate { it.split("=").let { (k, v) -> k to Utils.urlDecode(v) } }

            val streamSetting = config.outboundBean?.streamSettings ?: return false
            config.remarks = Utils.urlDecode(uri.fragment ?: "")
            config.outboundBean.settings?.vnext?.get(0)?.let { vnext ->
                vnext.address = uri.idnHost
                vnext.port = uri.port
                vnext.users[0].id = uuid
                vnext.users[0].security = DEFAULT_SECURITY
                vnext.users[0].alterId = alterId.toInt()
            }
            var fingerprint = streamSetting.tlsSettings?.fingerprint
            val sni = streamSetting.populateTransportSettings(protocol,
                queryParam["type"],
                queryParam["host"]?.split("|")?.get(0) ?: "",
                queryParam["path"]?.takeIf { it.trim() != "/" } ?: "",
                queryParam["seed"],
                queryParam["security"],
                queryParam["key"],
                queryParam["mode"],
                queryParam["serviceName"])
            streamSetting.populateTlsSettings(
                if (tls) TLS else "", allowInsecure, sni, fingerprint, null,
                null, null, null
            )
            true
        }.getOrElse { false }
    }






    private fun tryResolveVmess4Kitsunebi(server: String, config: ServerConfig): Boolean {

        var result = server.replace(EConfigType.VMESS.protocolScheme, "")
        val indexSplit = result.indexOf("?")
        if (indexSplit > 0) {
            result = result.substring(0, indexSplit)
        }
        result = Utils.decode(result)

        val arr1 = result.split('@')
        if (arr1.count() != 2) {
            return false
        }
        val arr21 = arr1[0].split(':')
        val arr22 = arr1[1].split(':')
        if (arr21.count() != 2) {
            return false
        }

        config.remarks = "Alien"
        config.outboundBean?.settings?.vnext?.get(0)?.let { vnext ->
            vnext.address = arr22[0]
            vnext.port = Utils.parseInt(arr22[1])
            vnext.users[0].id = arr21[1]
            vnext.users[0].security = arr21[0]
            vnext.users[0].alterId = 0
        }
        return true
    }




    private fun tryResolveResolveSip002(str: String, config: ServerConfig): Boolean {
        try {
            val uri = URI(Utils.fixIllegalUrl(str))
            config.remarks = Utils.urlDecode(uri.fragment ?: "")

            val method: String
            val password: String
            if (uri.userInfo.contains(":")) {
                val arrUserInfo = uri.userInfo.split(":").map { it.trim() }
                if (arrUserInfo.count() != 2) {
                    return false
                }
                method = arrUserInfo[0]
                password = Utils.urlDecode(arrUserInfo[1])
            } else {
                val base64Decode = Utils.decode(uri.userInfo)
                val arrUserInfo = base64Decode.split(":").map { it.trim() }
                if (arrUserInfo.count() < 2) {
                    return false
                }
                method = arrUserInfo[0]
                password = base64Decode.substringAfter(":")
            }

            config.outboundBean?.settings?.servers?.get(0)?.let { server ->
                server.address = uri.idnHost
                server.port = uri.port
                server.password = password
                server.method = method
            }
            return true
        } catch (e: Exception) {
            //Log.d(AppConfig.ANG_PACKAGE, e.toString())
            println(e.toString())
            return false
        }
    }







}