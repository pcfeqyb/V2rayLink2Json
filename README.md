## V2rayLink2Json
parse vmess/vless/trojan/ss/...  links.<br>
generate config.json to be used with xray-core<br>
it use v2ray_config.json as a template so you can customize it.<br>
the link2json is part of project [xray_config_tester](https://github.com/GFW-knocker/xray_config_tester)<br>

## Usage example
java -jar Link2Json.jar [-p xray_port] [-o output.json] "v2ray_link"<br>
java -jar Link2Json.jar [-p xray_port] [-o output.json] "file_contain_a_v2ray_link.txt"<br>

## parsing technology
the parsing code is exactly copied from v2rayNG 1.8.5<br>
and is compatible with almost all protocol untill today (July 2023) including reality & so on.<br>

## compile
use Intellij -> {kotlin + Maven}

## build executable jar 
project structure -> artifact -> jar -> ...<br>
build menu -> build artifact<br>

## to test v2ray link with xray-core
use this project<br>
https://github.com/GFW-knocker/xray_config_tester<br>
