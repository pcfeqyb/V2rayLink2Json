package com.v2ray.ang.dto

import com.v2ray.ang.AppConfig
import com.v2ray.ang.util.Utils
import com.v2ray.ang.util.V2rayConfigUtil


fun main(args: Array<String>) {
    //println("your java version :" + System.getProperty("java.version") + "   recommended=java 8.0+")

//    var mylink = "vmess://ew0KICAidiI6ICIyIiwNCiAgInBzIjogInRlc3QxIiwNCiAgImFkZCI6ICJ3ZWIuZ29vZ2xlLmNvbSIsDQogICJwb3J0IjogIjQ0MyIsDQogICJpZCI6ICI2MjBjNjAzMS03MDE4LTQ4ODAtOGI3Ny0wOGY4NDY5ZDlmNmQiLA0KICAiYWlkIjogIjAiLA0KICAic2N5IjogImF1dG8iLA0KICAibmV0IjogInRjcCIsDQogICJ0eXBlIjogIm5vbmUiLA0KICAiaG9zdCI6ICJnb29nbGUuY29tIiwNCiAgInBhdGgiOiAiIiwNCiAgInRscyI6ICJ0bHMiLA0KICAic25pIjogInNuaS5nb29nbGUuY29tIiwNCiAgImFscG4iOiAiaDIiLA0KICAiZnAiOiAiYW5kcm9pZCINCn0="
    var mylink = ""
    var myfile = ""
    var myport = ""
    var LNK = ""
    var is_only_hash = false


    if(args.isNotEmpty()){


        if(args.size==1) {
            myfile = "config.json"
            LNK = args[0]

        }else if(args.size==2){
            val z1 = args[0]
            if(z1=="-H"){
                myfile = "config.json"
                LNK = args[1]
                is_only_hash = true
            }else{
                println("ERR : invalid option flag")
            }

        }else if(args.size==3){
            val z = args[0]
            if(z=="-p"){
                myfile = "config.json"
                myport = args[1]
                LNK = args[2]
            }else if(z=="-o"){
                myfile = args[1]
                LNK = args[2]
            }else{
                println("ERR : invalid option flag")
            }

        }else if(args.size==5){
            val z1 = args[0]
            val z2 = args[2]
            if( (z1=="-p") && (z2=="-o") ){
                myport = args[1]
                myfile = args[3]
                LNK = args[4]
            }else if( (z1=="-o") && (z2=="-p") ){
                myfile = args[1]
                myport = args[3]
                LNK = args[4]
            }else{
                println("ERR : invalid option flag")
            }

        }else {
            println("ERR : invalid number of args")
        }



        if(LNK.startsWith("vmess://") ||
            LNK.startsWith("vless://") ||
            LNK.startsWith("trojan://") ||
            LNK.startsWith("ss://") ||
            LNK.startsWith("socks://") ||
            LNK.startsWith("wireguard://") ) {

            println("read link from arg")
            mylink = LNK
        }else if(LNK.isNotEmpty()){
            println("read link from file:"+LNK)
            mylink = V2rayConfigUtil.readOneLineFromLocalFile(LNK)
        }else{
            println("invalid protocol link")
        }
    }else{
        println("Link2Json V1.5\r\narg is empty.\r\nusage:\r\njava -jar Link2Json.jar -H \"v2raylink\"  ==> only hash returned\r\njava -jar Link2Json.jar [-p xray_port] [-o output.json] \"v2raylink\"\r\njava -jar Link2Json.jar [-p xray_port] [-o output.json] \"file_contain_v2rayLink.txt\"")
    }

    if( mylink.isEmpty() ){
        println("v2rayLink is empty")
        return
    }
    if( myfile.isEmpty() ){
        println("filename is empty")
        return
    }

    if( myport.isNotEmpty() ){
        var inbound_http_port = Utils.parseInt(myport,0)
        if( (inbound_http_port > 65535) || (inbound_http_port<1000) ){
            inbound_http_port = 10809
            println("port in not in valid range, we set to default 10809")
        }
        AppConfig.PORT_SOCKS = (inbound_http_port-1).toString()
        AppConfig.PORT_HTTP = inbound_http_port.toString()
    }else{
        AppConfig.PORT_SOCKS = "10808"
        AppConfig.PORT_HTTP = "10809"
    }


//    println("link ==> "+mylink)

    var status = false
    var myconfig:ServerConfig? = null

    try{
        myconfig = ConfigManager().GFW_make_Config_from_link(mylink,"")
    }catch (e : Exception){
        println(e.toString())
    }


    if(myconfig!=null) {
        println("link is valid")
        val result = V2rayConfigUtil.getV2rayConfig(myconfig)

        if(result.content.isNotEmpty()){
            println("parsing successfull")
            val s87 = myconfig.remarks
            val s88 = Utils.my_base64(myconfig.remarks)
            val s89 = Utils.md5_hash(myconfig.outboundBean.toString())
            println("alias__of_config-------->$$$$$$$s87$$$$$$$")
            println("b64_of_alias_config----->$$$$$$$s88$$$$$$$")
            println("hash_of_outbnd_config--->$$$$$$$s89$$$$$$$")


            if(!is_only_hash) {
                status = V2rayConfigUtil.writeTextToLocalFile(myfile, result.content)
                if(status){
                    val listen_HTTP = AppConfig.PORT_HTTP
                    val listen_Socks = AppConfig.PORT_SOCKS
                    println("successfully write ==> file=$myfile  xray_HTTP=$listen_HTTP  xray_Socks=$listen_Socks")
                }else{
                    println("failed to write file")
                }
            }

        }else{
            println("parsing failed")
            if(!is_only_hash) {
                status = V2rayConfigUtil.writeTextToLocalFile(myfile, "")
                if (status) {
                    println("write \"Empty String\" to ==> " + myfile)
                } else {
                    println("failed to write file")
                }
            }
        }


    }else{
        println("link is corrupt")
        if(!is_only_hash) {
            status = V2rayConfigUtil.writeTextToLocalFile(myfile, "")
            if (status) {
                println("write \"Empty String\" to ==> " + myfile)
            } else {
                println("failed to write file")
            }
        }
    }


}




