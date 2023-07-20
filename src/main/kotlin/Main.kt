package com.v2ray.ang.dto

import com.v2ray.ang.util.V2rayConfigUtil


fun main(args: Array<String>) {
    //println("your java version :" + System.getProperty("java.version") + "   recommended=java 8.0+")

//    var mylink = "vmess://ew0KICAidiI6ICIyIiwNCiAgInBzIjogInRlc3QxIiwNCiAgImFkZCI6ICJ3ZWIuZ29vZ2xlLmNvbSIsDQogICJwb3J0IjogIjQ0MyIsDQogICJpZCI6ICI2MjBjNjAzMS03MDE4LTQ4ODAtOGI3Ny0wOGY4NDY5ZDlmNmQiLA0KICAiYWlkIjogIjAiLA0KICAic2N5IjogImF1dG8iLA0KICAibmV0IjogInRjcCIsDQogICJ0eXBlIjogIm5vbmUiLA0KICAiaG9zdCI6ICJnb29nbGUuY29tIiwNCiAgInBhdGgiOiAiIiwNCiAgInRscyI6ICJ0bHMiLA0KICAic25pIjogInNuaS5nb29nbGUuY29tIiwNCiAgImFscG4iOiAiaDIiLA0KICAiZnAiOiAiYW5kcm9pZCINCn0="
    var mylink = ""
    var myfile = ""

    if(args.isNotEmpty()){

        var x = ""
        if(args.size==1) {
            myfile = "config.json"
            x = args[0]
        }else if(args.size==2){
            myfile = args[0]
            x = args[1]
        }else{
            println("ERR : too much args. we need 1 or 2 args at most")
        }


        if(x.startsWith("vmess://") ||
            x.startsWith("vless://") ||
            x.startsWith("trojan://") ||
            x.startsWith("ss://") ||
            x.startsWith("socks://") ||
            x.startsWith("wireguard://") ) {

            println("read link from arg")
            mylink = x
        }else if(x.isNotEmpty()){
            println("read link from file:"+x)
            mylink = V2rayConfigUtil.readOneLineFromLocalFile(x)
        }else{
            println("invalid protocol link")
        }
    }else{
        println("Link2Json V1.2\r\narg is empty.\r\nusage:\r\njava -jar Link2Json.jar [\"output.json\"] \"v2raylink\"\r\njava -jar Link2Json.jar [\"output.json\"] \"file_contain_v2rayLink.txt\"")
    }

    if( mylink.isEmpty() ){
        println("v2rayLink is empty")
        return
    }
    if( myfile.isEmpty() ){
        println("filename is empty")
        return
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
//        println(result.content)

        if(result.content.isNotEmpty()){
            println("parsing successfull")
            status = V2rayConfigUtil.writeTextToLocalFile(myfile,result.content)
            if(status){
                println("successfully write file ==> "+myfile)
            }else{
                println("failed to write file")
            }
        }else{
            println("parsing failed")
            status = V2rayConfigUtil.writeTextToLocalFile(myfile,"")
            if(status){
                println("write \"Empty String\" to ==> "+myfile)
            }else{
                println("failed to write file")
            }
        }


    }else{
        println("link is corrupt")
        status = V2rayConfigUtil.writeTextToLocalFile(myfile,"")
        if(status){
            println("write \"Empty String\" to ==> "+myfile)
        }else{
            println("failed to write file")
        }
    }


}




