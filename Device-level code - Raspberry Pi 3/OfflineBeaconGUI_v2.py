# -*- coding: utf-8 -*-

# This code does:
#  1. display a GUI with 2 buttons.
#  2. takes a user's button push event from a touchscreen GUI.
#  3. generate a BLE beacon containing: 1) dev ID, 2) random "j value", 3) offlineshare.
#
# before running this script, do
#    pi@raspberrypi:$ hciconfig # and verify it's up
#    pi@raspberrypi:$ sudo hciconfig hci0 up # do this if it's not up yet.
#    pi@raspberrypi:$ sudo hciconfig hci0 leadv 3 # put it into beacon mode.
#
# to execute at the command line:
#    pi@raspberrypi:$ python3 ./OfflineBeaconGUI.py
#
# 
 
from datetime import datetime
import os
from cryptography.hazmat.primitives import hashes, hmac
from tkinter import *
import tkinter.font as tkFont

win = Tk()
myFont = tkFont.Font(family = 'Helvetica', size = 30, weight = 'bold')
offKey = 12995835
devID = "01" # device ID of the offline device

def generate_j_value():
    dt = datetime.now()
    ts = datetime.timestamp(dt)
    return int(ts) 
    

#
def offline_share(key,j):
    j1 = int(j/100000)
    j2 = j%100000
    print("j1, j2: ",j1,j2)
    digest = hmac.HMAC(bytes(key),hashes.SHA256())
    digest.update(bytes(j1))
    digest.update(bytes(j2))
    S_j = int.from_bytes(digest.finalize(), byteorder='big')
    return str(S_j)
    
def formatting(a):
    result = ""
    done = False
    i = 0
    while not done:
        result = result + a[i] + a[i+1] + " "
        if i == len(a)-2:
            done = True
        else:
            i += 2
    return result
    
def format_and_send_beacon(id, j, inStr):
    # see the accompanied Word doc about the details.
    str1 = "sudo hcitool -i hci0 cmd 0x08 0x0008 1f 02 01 06 03 03 aa fe 17 16 aa fe 10 00 02 "
    str2 = id + " "
    str3 = "07"
    str2 = str2 + formatting(j)
    str2 = str2 + formatting(inStr[57:]) # extract the last 20 digits (10 octets)
    cmd = str1 + str2 + str3
    print("cmd: ", cmd)
    os.system(cmd)

def gen_new_QR():
    print("button pressed - generating a new BLE message.")
    jVal = generate_j_value()
    print("j value: ", str(jVal))
    offDevShare = offline_share(offKey,jVal)
    print("offline share:", offDevShare)
    format_and_send_beacon(devID, str(jVal), offDevShare)
    
def exit_program():
    print("Exit Button pressed - goodbye!")
    win.destroy()

win.title("BLE Beacon GUI")
win.geometry('500x250')
exitButton  = Button(win, text = "Exit", font = myFont, command = exit_program, height =2 , width = 6) 
exitButton.pack(side = BOTTOM)
QrButton = Button(win, text = "Generate a new\noffline share", font = myFont, command = gen_new_QR, height = 2, width =15 )
QrButton.pack()

mainloop()

