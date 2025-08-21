#!/usr/bin/env python3
# -*- coding: utf-8 -*-


from __future__ import print_function
import socket
import boto3
from warrant.aws_srp import AWSSRP
from warrant import Cognito as warrantCognito
from pycognito import Cognito
import os
import random
import base64
from cryptography.fernet import Fernet
from cryptography.hazmat.primitives import hashes, hmac
from cryptography.hazmat.primitives.kdf.pbkdf2 import PBKDF2HMAC
import mysql.connector
from mysql.connector import Error
from mysql.connector import errorcode
import urllib
import sys
import json
import time
import traceback
#new changes on 18/8/2022
import datetime
import maya
from datetime import datetime
#########################



def get_json_resources():
    page = urllib.request.urlopen("https://locproof.s3.eu-west-2.amazonaws.com/index3.html").read()
    #words = page.decode().split("<p>")
    text = find_between(page,b'<p>',b'</p>')
    text = text.replace(b'\n',b'')
    text = text.replace(b' ', b'')

    res = json.loads(text)
    return res


def find_between( inputStr, firstSubstr, lastSubstr ):
    start, end = (-1,-1)
    try:
        start = inputStr.index( firstSubstr ) + len( firstSubstr )
    except ValueError:
        print('    ValueError: '),
        print("firstSubstr=%s  -  "%( firstSubstr )),
        print(sys.exc_info()[1])

    try:
        end = inputStr.index( lastSubstr, start )
    except ValueError:
        print('    ValueError: '),
        print("lastSubstr=%s  -  "%( lastSubstr )),
        print(sys.exc_info()[1])

    return inputStr[start:end]

def offlineShares(key, j, deviceType): #This happens on redbox and offline RPI
    # Generate offline share
    
    #new changes on 18/8/2022

    #digest = hmac.HMAC(bytes(key), hashes.SHA256())
    #digest.update(bytes(j))
    j1 = int(j/100000)
    j2 = j%100000
    print("j1, j2: ",j1,j2)
    digest = hmac.HMAC(bytes(key),hashes.SHA256())
    digest.update(bytes(j1))
    digest.update(bytes(j2))

    S_j = int.from_bytes(digest.finalize(), byteorder='big')

    if deviceType == "BLE":
        #return int(str(S_j)[51:])
        return int(str(S_j)[57:])
        ########################
    else:
        return S_j

def getAdditiveShares(secret, N, fieldSize, off_shares):
    # N = amount of shares needed -1 = amount of devices used in policy
    # Generate n-1 shares randomly
    shares = [system_random.randrange(fieldSize) for i in range(N - len(off_shares))]
    shares.extend(off_shares)
    shares.append((secret - sum(shares)) % fieldSize)
    return shares




def Main():
    s.settimeout(None) #make timeout blocking
    encrypted, addr = s.recvfrom(2048)
    try:
        res = get_json_resources()
        decrypted=encrypted
        decrypted = decrypted.decode('utf-8')
        print("Login/Register Message from: " + str(addr))
        print("Login/Register Message : " + decrypted)

        message_type = decrypted.split('&')[0]
        if message_type == "1": #then login message

            s.settimeout(timeout) ### set timeout

            #splitting arriving messsage from client
            username=decrypted.split('&')[1] #username
            password=decrypted.split('&')[2] #password
            room=decrypted.split('&')[3] #roomi (i = str(integer))
            policynumber=int(decrypted.split('&')[4])
            roomnumber = int(room[4])

            policy = res["Rooms"][roomnumber-1]["Policies"][policynumber-1]["policy_devices"] #string
            nr_devices_used = len(policy)

            print("In room " + str(roomnumber) + "Policy number: "+ str(policynumber) + " Policy: "+ str(policy))


            #Logging in to AWS Cognito with boto3 client
            os.environ['AWS_DEFAULT_REGION'] = 'eu-west-2'

            #For the first authentication, force password change
            client=boto3.client('cognito-idp')
            #aws=AWSSRP(username=username,password=password,pool_id='eu-west-2_vOzTJEXe5',client_id='4n1c0ofkoaiteqofmaqgjrq1it',client=client)
            #aws=AWSSRP(username=username,password=password,pool_id='eu-west-2_EPo75R9IM',client_id='7cou5f59oe1o0ag5pvkcqigkmo',client=client)
            aws=AWSSRP(username=username,password=password,pool_id='eu-west-2_9CgbiajJZ',client_id='7h69776od4m5biv8aju5cakcn',client=client)

            tokens=aws.authenticate_user()

            s.sendto("AUTH_SUCCES".encode(), addr)

            #get nr of offline and online devices in policy
            #also make dictionary to help next step 'get needed addresses'
            nr_offline_devices = 0
            nr_online_devices = 0
            onlinetypedict = {}
            for device in res["Rooms"][roomnumber-1]["Devices"]:
                if device["status"] == "offline" and (device["name"] in policy):
                    nr_offline_devices += 1
                if device["status"] == "online" and (device["name"] in policy):
                    nr_online_devices +=1
                    onlinetypedict[device["name"]] = device["type"]


            print("Number offline devices in policy: " + str(nr_offline_devices) + " Number online devices in policy: " + str(nr_online_devices))

            #get needed addresses for this policy
            for device in policy:
                if device in onlinetypedict:
                    dtype = onlinetypedict.get(device)

                    if dtype == "Visual":
                        message, server_visual = s.recvfrom(2048) #wait for RSP visual
                        print("message : ", message.decode())
                    elif dtype == "BLE":
                        message, server_beacon = s.recvfrom(2048) #wait for RSP bluetooth beacon
                        print("message : ", message.decode())
                    elif dtype == "PairedBL":
                        message, server_bluetooth = s.recvfrom(2048) #wait for RSP bluetooth paired
                        print("message : ", message.decode())
                    elif dtype == "Audio":
                        message, server_audio = s.recvfrom(2048) #wait for RSP audio
                        print("message : ", message.decode())

            confirmationms, _ = s.recvfrom(2048) #wait for confirmation client
            confirmationms = confirmationms.decode()

            if confirmationms == "ADDRCOMMS_DONE":
                print("ADDRCOMMS_DONE received")
                pass
            else:
                raise AssertionError("Did not receive right message after receiving addresses of online devices")







            #Secret sharing part
            f = 10 ** 77
            sec = system_random.randrange(f)
            #sec =58180532491236541444287395032816871065368084765292838761769880049643509438734
            print("Secret is:"+ str(sec))

            salt = b'\t\xee2\xf7\xc4Jz\x1ez.\x1e6\x08K\n\xef'   #fixed
            print("Salt is:"+ str(salt))



            kdf = PBKDF2HMAC(
                        algorithm=hashes.SHA256(),
                        length=32,
                        salt=salt,
                        iterations=100000)

            key = base64.urlsafe_b64encode(kdf.derive(str(sec).encode()))
            fernetkey = Fernet(key)
            data = tokens['AuthenticationResult']['AccessToken']
            data_id = tokens['AuthenticationResult']['IdToken']
            print("Unencrypted Access Id: " + data)
            print("Unencrypted Id Token: " + data_id)
            data = str(fernetkey.encrypt(data.encode()))
            data_id = str(fernetkey.encrypt(data_id.encode()))

            # Using list comprehension + string slicing
            # Splitting string into equal halves
            test_str=data
            if(len(data)%2==0):

                res_first = test_str[0:len(test_str)//2]
                res_second = test_str[len(test_str)//2 if len(test_str)%2 == 0
                                                else ((len(test_str)//2)+1):]
            else:
                res_first = test_str[0:len(test_str)//2+1]
                res_second = test_str[len(test_str)//2 if len(test_str)%2 == 0
                                                     else ((len(test_str)//2)+1):]
            print("Sending: " + str(res_first.encode('utf-8')))
            s.sendto(res_first.encode('utf-8'), addr)

            print("Sending: " + str(res_second.encode('utf-8')))
            s.sendto(res_second.encode('utf-8'), addr)

            print("1")
            # Using list comprehension + string slicing
            # Splitting string into equal halves
            test_str=data_id
            if(len(data_id)%2==0):

                res_first = test_str[0:len(test_str)//2]
                res_second = test_str[len(test_str)//2 if len(test_str)%2 == 0
                                                else ((len(test_str)//2)+1):]
            else:
                res_first = test_str[0:len(test_str)//2+1]
                res_second = test_str[len(test_str)//2 if len(test_str)%2 == 0
                                                else ((len(test_str)//2)+1):]
            print("Sending: " + str(res_first.encode('utf-8')))
            s.sendto(res_first.encode('utf-8'), addr)

            print("Sending: " + str(res_second.encode('utf-8')))
            s.sendto(res_second.encode('utf-8'), addr)

            print("2")



            #receiving j & id values from client and create offline shares
            j=[]
            id=[]
            offlineshares=[]
            if nr_offline_devices > 0:
                encrypted, addr = s.recvfrom(2048)
                message=encrypted.decode()
                message=message.split('&')


                print(str(len(message)) + " of js are received from" + str(addr))

                i = 0
                for deviceChar in policy:
                    for device in res["Rooms"][roomnumber-1]["Devices"]:
                        deviceName = device["name"]
                        if deviceName == deviceChar:
                            deviceStatus = device["status"]
                            deviceType = device["type"]
                            if deviceStatus == "offline":
                                #new changes on 18/8/2022
                                #j.append(int(message[i][:4]))
                                j.append(int(message[i][:10]))
                                t1 = str(j)
                                dt = datetime.now()
                                ts = datetime.timestamp(dt)
                                t2 = str(int(ts))
                                s1 = datetime.fromtimestamp(int(t1)).isoformat()
                                dt1 = maya.parse(s1).datetime()

                                s2 = datetime.fromtimestamp(int(t2)).isoformat()
                                dt2 = maya.parse(s2).datetime()

                                print('def-time = ', dt2 - dt1)
                                if dt2 >= dt1:
                                    if str(dt2 - dt1) > thr:
                                        print('Error: More than threshold')
                                        raise Exception("Error: More than threshold")                                    
                                    else:
                                        print('OK: Less than threshold')
                                else:
                                    print('Error: old timestamp')
                                    raise Exception("Error: old timestamp")
                                #########################
                                id.append(str(message[i][-2:]))
                                offKey = idkey_dict.get(id[i])
                                offlineshares.append(offlineShares(offKey,j[i],deviceType))
                                i += 1
            else:
                print("No offline devices")
            print("Offline shares are "+ str(offlineshares))


            # Generating shares s_a and .., and s_offline and s_box
            shares = getAdditiveShares(sec, nr_devices_used , f, offlineshares)
            print("Amount of devices used:",nr_devices_used, 'Shares are:', shares)
            # send red box share
            s.sendto(str(shares[-1]).encode(),addr)


            ##### send shares to the right devices ####
            sentIndex = 0 # sentIndex < #onlinedevices
            for deviceChar in policy:
                for device in res["Rooms"][roomnumber-1]["Devices"]:
                    deviceName = device["name"]
                    if deviceName == deviceChar:
                        deviceStatus = device["status"]
                        deviceType = device["type"]
                        break
                if deviceStatus == "online":
                    message=str(shares[sentIndex])
                    sentIndex+=1
                    print("Sending: " + str(message), "Share nr: " + str(sentIndex))
                    print(deviceType,type(deviceType))

                    if deviceType == "Visual":
                        s.sendto(message.encode(), server_visual)
                        print("sent to visual device")
                    elif deviceType == "Audio":
                        s.sendto(message.encode(), server_audio)
                    elif deviceType == "PairedBL":
                        s.sendto(message.encode(), server_bluetooth)
                        print("sent to paired bluetooth pi")
                    elif deviceType == "BLE":
                        s.sendto(message.encode(), server_beacon)
                        print("sent to beacon")



            #Ending procedure

            access_token_dec, addr = s.recvfrom(2048)
            id_token_dec, addr = s.recvfrom(2048)


            #u = Cognito('eu-west-2_vOzTJEXe5','4n1c0ofkoaiteqofmaqgjrq1it',
            #u = Cognito('eu-west-2_EPo75R9IM','7cou5f59oe1o0ag5pvkcqigkmo',
            u = Cognito('eu-west-2_9CgbiajJZ','7h69776od4m5biv8aju5cakcn',


                id_token=id_token_dec.decode('utf-8'),
                access_token=access_token_dec.decode('utf-8'))
            u.verify_tokens()

            connection = mysql.connector.connect(host='loggingdb.cdqsjcaidv6a.eu-west-2.rds.amazonaws.com',
                                                    database='LogDB',
                                                    user='C2C',
                                                    password='scs13SCS!#')

            if (connection.is_connected()):
                message = "Succesfully connected to meeting room database :=)"
                s.sendto(message.encode('utf-8'), addr)
                mySql_query= "SELECT `log_block_number` FROM `ELogTable` LIMIT 2"
                cursor = connection.cursor()
                cursor.execute(mySql_query)
                records=cursor.fetchall()
                records=str(records)
                print(records)
                s.sendto(records.encode('utf-8'), addr)
                connection.close()
                s.settimeout(None)
            else:
                raise Exception("Not connected to SQL")




        elif message_type == "2": #then register message
            s.settimeout(50)
            #splitting arriving messsage from client
            username=decrypted.split('&')[1] #username
            password=decrypted.split('&')[2] #password
            email=decrypted.split('&')[3] #email

            os.environ['AWS_DEFAULT_REGION'] = 'eu-west-2'

            #u = warrantCognito('eu-west-2_vOzTJEXe5','4n1c0ofkoaiteqofmaqgjrq1it')
            #u = warrantCognito('eu-west-2_EPo75R9IM','7cou5f59oe1o0ag5pvkcqigkmo')
            u = warrantCognito('eu-west-2_9CgbiajJZ','7h69776od4m5biv8aju5cakcn')
            u.add_base_attributes(email=email)
            u.register(username, password)

            """
            data= "Please type user confirmation code sent to your e-mail adress:"
            print("Sending: " + data)
            s.sendto(data.encode('utf-8'), addr)
            """
            encrypted, addr = s.recvfrom(2048)
            decrypted=encrypted
            decrypted = decrypted.decode('utf-8')
            print("Message from: " + str(addr))
            print("Message: " + decrypted)

            u.confirm_sign_up(decrypted,username=username)

            data= "User succesfully confirmed"
            s.settimeout(12)
            print("Sending: " + data)
            s.sendto(data.encode('utf-8'), addr)
            s.settimeout(None)

    except client.exceptions.NotAuthorizedException:
        print("Authentication failed")
        s.sendto("AUTH_FAIL".encode(),addr)
        traceback.print_exc()
    except socket.timeout:
        print("Timeout raised and caught")
        traceback.print_exc()
        s.settimeout(None)
    except Exception as e:
        traceback.print_exc()
        s.settimeout(None)
        pass



if __name__=='__main__':
    host = '0.0.0.0' #Server ip
    port = 6666
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    s.bind((host, port))
    timeout = 12 #recvfrom: socket will timeout if after {timeout} seconds nothing is received yet (not for initial recvfrom)
    thr = '0:10:00' #threshold new changes on 18/10/2022
    while True:
        system_random = random.SystemRandom()
        print("Ready to go")
        #ID-OffKey dictionary
        idkey_dict = {"01":12995835, "12":5542399,"44":15985867}

        Main()

