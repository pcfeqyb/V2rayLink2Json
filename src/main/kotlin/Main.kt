package com.v2ray.ang.dto

import com.v2ray.ang.util.V2rayConfigUtil


fun main(args: Array<String>) {
    //println("your java version :" + System.getProperty("java.version") + "   recommended=java 8.0+")

    var mylink = ""

    if(args.isNotEmpty()){
        val x = args[0]
        if(x.startsWith("vmess://") ||
            x.startsWith("vless://") ||
            x.startsWith("trojan://") ||
            x.startsWith("ss://") ){
            println("read link from arg")
            mylink = x
        }else{
            println("read link from file:"+x)
            mylink = V2rayConfigUtil.readOneLineFromLocalFile(x)
        }
    }else{
        println("arg is empty\r\nusage:\r\njava -jar Link2Json.jar \"v2raylink\"\r\njava -jar Link2Json.jar \"file_contain_v2rayLink.txt\"")
    }

    println("link ==> "+mylink)

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
//        println(result.content)
        status = V2rayConfigUtil.writeTextToLocalFile("config.json",result.content)
        if(status){
            println("successfully write file ==> config.json")
        }else{
            println("failed to write file")
        }
    }else{
        println("link is corrupt")
        status = V2rayConfigUtil.writeTextToLocalFile("config.json","")
        if(status){
            println("write \"Empty String\" to ==> config.json")
        }else{
            println("failed to write file")
        }
    }





}

