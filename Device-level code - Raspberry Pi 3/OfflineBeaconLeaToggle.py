# -*- coding: utf-8 -*-

# 
# This code does:
#  1. turn the hci0 up.
#  2. toggle BLE advertisement mode by executing:
#     "sudo hciconfig hci0 leadv 3" to turn it on.
#     "sudo hciconfig hci0 noleadv" to turn it off.
#
# to execute at the command line:
#    pi@raspberrypi:$ python3 ./OfflineBeaconLeaToggle.py
#
# 

import os
from tkinter import *
import tkinter.font as tkFont

win = Tk()
myFont = tkFont.Font(family = 'Helvetica', size = 30, weight = 'bold')
status = "OFF"

def toggle_lea():
    global status
    if status == "OFF":
        cmd = "sudo hciconfig hci0 leadv 3"
        status = "ON"
        LeaButton["text"] = "Turn off LEA"
    else:
        cmd = "sudo hciconfig hci0 noleadv"
        status = "OFF"
        LeaButton["text"] = "Turn on LEA"
    os.system(cmd)
    print("LE Advertisement is ", status)

def exit_program():
    print("Exit Button pressed - goodbye!")
    win.destroy()

os.system("sudo hciconfig hci0 up")
win.title("BLE Beacon GUI")
win.geometry('500x250')
exitButton  = Button(win, text = "Exit", font = myFont, command = exit_program, height =2 , width = 6) 
exitButton.pack(side = BOTTOM)
LeaButton = Button(win, text = "Toggle LEA", font = myFont, command = toggle_lea, height = 2, width =15 )
LeaButton.pack()

mainloop()
