package com.v2ray.ang.dto

import com.v2ray.ang.util.V2rayConfigUtil


fun main(args: Array<String>) {
    println("your java version :" + System.getProperty("java.version") + "   recommended=java 8.0+")

    var mylink = "vmess://ew0KICAidiI6ICIyIiwNCiAgInBzIjogIllvdXR1YmUsIEluc3RhLCBUd2l0dGVyLCBXaGF0c2FwcCIsDQogICJhZGQiOiAiZ2Z3LnlvdXR1YmUuY29tIiwNCiAgInBvcnQiOiAiNDQzIiwNCiAgImlkIjogImUwODU1MTE5LTRiMTgtNDA0NS1iOTAzLThmNjk5YjBjNTQxNCIsDQogICJhaWQiOiAiMCIsDQogICJzY3kiOiAiYXV0byIsDQogICJuZXQiOiAidGNwIiwNCiAgInR5cGUiOiAibm9uZSIsDQogICJob3N0IjogIiIsDQogICJwYXRoIjogIiIsDQogICJ0bHMiOiAiIiwNCiAgInNuaSI6ICIiLA0KICAiYWxwbiI6ICIiDQp9"

    if(args.isNotEmpty()){
        val x = args[0]
        if(x.startsWith("vmess://") ||
            x.startsWith("vless://") ||
            x.startsWith("trojan://") ||
            x.startsWith("ss://") ){
            println("read link from arg:"+x)
            mylink = x
        }else{
            println("read link from file:"+x)
            mylink = V2rayConfigUtil.readTextFromLocalFile(x)
        }
    }else{
        println("arg is empty. use default link")
    }

    val myconfig = ConfigManager().GFW_make_Config_from_link(mylink,"")
    if(myconfig!=null) {
        val result = V2rayConfigUtil.getV2rayConfig(myconfig)
//        println(result.content)
        val status = V2rayConfigUtil.writeTextToLocalFile("config.json",result.content)
        println("write file to config.json")
        println("is file writed:"+status)
    }


}

