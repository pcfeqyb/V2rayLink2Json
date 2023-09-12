
import subprocess
import os
import base64


# v1.5

def extract_config_alias_and_hash(txt=""):
    txt_list = txt.split("\r\n")
    config_alias = ""
    config_hash = ""
    for x in txt_list:
        if(x.startswith("b64_of_alias_config----->$$$$$$") and x.endswith("$$$$$$$")):
            config_alias = base64.b64decode(x[31:-7]).decode("utf-8")
        if(x.startswith("hash_of_outbnd_config--->$$$$$$") and x.endswith("$$$$$$$")):
            config_hash = x[31:-7]
    return (config_alias , config_hash)





def link_to_hash(config_link=""):    

    if( config_link.startswith("vmess://") or
        config_link.startswith("vless://") or
        config_link.startswith("trojan://") or
        config_link.startswith("ss://") or
        config_link.startswith("socks://") or
        config_link.startswith("wireguard://") 
        ):        
        pass
    else:
        print("invalid argument in calling do_test()")
        return (False, -1, -1) 


    try:
        # Run the JAR file as a subprocess
        process_java = subprocess.Popen(["java", "-jar", "Link2Json.jar", "-H", config_link] , stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        # Wait for the process to finish and get the output
        stdout, stderr = process_java.communicate()
        
        config_alias = ""
        config_hash = ""
        if(len(stdout)!=0):
            output = stdout.decode("utf-8")
        else:
            output = stderr.decode("utf-8")                
        # print(output)  # print the output of Link2Json which include config alias and config hash
        (config_alias , config_hash) = extract_config_alias_and_hash(output)
        

        if(len(config_hash)>16):
            config_hash = config_hash[:16]

        
        process_java.kill()
    except Exception as e:
        print("failed to start Link2json "+str(e))

    print(f"alias={config_alias}    hash={config_hash}" )
    return (config_alias , config_hash)

    


def check_working_directory():
    current_dir = os.getcwd()
    actual_file_dir = os.path.dirname(os.path.realpath(__file__))
    if(current_dir != actual_file_dir):
        os.chdir(actual_file_dir)




if __name__ == '__main__':
    check_working_directory()    

    link_to_hash("vless://fa0e6e80-7ede-4c01-b9aa-aa2f43e0afe8@web.yahoo.com:2087?encryption=none&flow=xtls-rprx-vision&security=reality&sni=sni.yahoo.com&fp=firefox&pbk=sCCsXQbQP5Dcw8Ab3Yv-G5wdLZIT0qYCJdU3hSk-mQk&sid=385efdba&type=grpc#salam")
    link_to_hash("vmess://ew0KICAidiI6ICIyIiwNCiAgInBzIjogInRlc3QxIiwNCiAgImFkZCI6ICJ3ZWIuZ29vZ2xlLmNvbSIsDQogICJwb3J0IjogIjQ0MyIsDQogICJpZCI6ICI2MjBjNjAzMS03MDE4LTQ4ODAtOGI3Ny0wOGY4NDY5ZDlmNmQiLA0KICAiYWlkIjogIjAiLA0KICAic2N5IjogImF1dG8iLA0KICAibmV0IjogInRjcCIsDQogICJ0eXBlIjogIm5vbmUiLA0KICAiaG9zdCI6ICJnb29nbGUuY29tIiwNCiAgInBhdGgiOiAiIiwNCiAgInRscyI6ICJ0bHMiLA0KICAic25pIjogInNuaS5nb29nbGUuY29tIiwNCiAgImFscG4iOiAiaDIiLA0KICAiZnAiOiAiYW5kcm9pZCINCn0=")
    link_to_hash("vmess://ew0KICAidiI6ICIyIiwNCiAgInBzIjogInRlc3QxIiwNCiAgImFkZCI6ICJ3ZWIuZ29vZ2xlLmNvbSIsDQogICJwb3J0IjogIjQ0MyIsDQogICJpZCI6ICI2MjBjNjAzMS03MDE4LTQ4ODAtOGI3Ny0wOGY4NDY5ZDlmNmQiLA0KICAiYWlkIjogIjAiLA0KICAic2N5IjogImF1dG8iLA0KICAibmV0IjogInRjcCIsDQogICJ0eXBlIjogIm5vbmUiLA0KICAiaG9zdCI6ICJnb29nbGUuY29tIiwNCiAgInBhdGgiOiAiIiwNCiAgInRscyI6ICJ0bHMiLA0KICAic25pIjogInNuaS5nb29nbGUuY29tIiwNCiAgImFscG4iOiAiaDIiLA0KICAiZnAiOiAiYW5kcm9pZCINCn0=")
    link_to_hash("vless://fa0e6e80-7ede-4c01-b9aa-aa2f43e0afe8@web.yahoo.com:2087?encryption=none&flow=xtls-rprx-vision&security=reality&sni=sni.yahoo.com&fp=firefox&pbk=sCCsXQbQP5Dcw8Ab3Yv-G5wdLZIT0qYCJdU3hSk-mQk&sid=385efdba&type=grpc#test2")    
    link_to_hash("vless://fa0e6e80-7ede-4c01-b9aa-aa2f43e0afe8@web.yahoo.com:2087?encryption=none&flow=xtls-rprx-vision&security=reality&sni=sni.yahoo.com&fp=firefox&pbk=sCCsXQbQP5Dcw8Ab3Yv-G5wdLZIT0qYCJdU3hSk-mQk&sid=385efdba&type=grpc#test2")    
    link_to_hash("vless://fa0e6e80-7ede-4c01-b9aa-aa2f43e0afe8@web.yahoo.com:2087?encryption=none&flow=xtls-rprx-vision&security=reality&sni=sni.yahoo.com&fp=firefox&pbk=sCCsXQbQP5Dcw8Ab3Yv-G5wdLZIT0qYCJdU3hSk-mQk&sid=385efdba&type=grpc#test2")
    link_to_hash("vless://fa0e6e80-7ede-4c01-b9aa-aa2f43e0afe8@web.yahoo.com:2087?encryption=none&flow=xtls-rprx-vision&security=reality&sni=sni.yahoo.com&fp=firefox&pbk=sCCsXQbQP5Dcw8Ab3Yv-G5wdLZIT0qYCJdU3hSk-mQk&sid=385efdba&type=grpc#salam")
    link_to_hash("vless://fa0e6e80-7ede-4c01-b9aa-aa2f43e0afe8@web.yahoo.com:2087?encryption=none&flow=xtls-rprx-vision&security=reality&sni=sni.yahoo.com&fp=firefox&pbk=sCCsXQbQP5Dcw8Ab3Yv-G5wdLZIT0qYCJdU3hSk-mQk&sid=385efdba&type=grpc#khubi")
    link_to_hash("vless://fa0e6e80-7ede-4c01-b9aa-aa2f43e0afe8@web.yahoo.com:2087?encryption=none&flow=xtls-rprx-vision&security=reality&sni=sni.yahoo.com&fp=firefox&pbk=sCCsXQbQP5Dcw8Ab3Yv-G5wdLZIT0qYCJdU3hSk-mQk&sid=385efdba&type=grpc#salam")
    link_to_hash("vless://fa0e6e80-7ede-4c01-b9aa-aa2f43e0afe8@web.yahoo.com:2087?encryption=none&flow=xtls-rprx-vision&security=reality&sni=sni.yahoo.com&fp=firefox&pbk=sCCsXQbQP5Dcw8Ab3Yv-G5wdLZIT0qYCJdU3hSk-mQk&sid=385efdba&type=grpc#nmtnm")




    