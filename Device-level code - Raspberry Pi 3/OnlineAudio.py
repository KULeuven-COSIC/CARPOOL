# -*- coding: utf-8 -*-


# 
# This code does:
#  1. Connect to the EC2 to get the share of the key.
#  2. Expand the key to have 3330 representations of each bit.
#  3. Modulate the expanded key into a carrier wave using
#     Amplitude-shift Keying (ASK) and playing the audio file
#
# Make sure the device is not muted!
#

import os
import base64
import socket as sc

import numpy as np
import wavio as wv
from math import pi

#   UN-COMMENT if you want to plot the wave for a visual representation
#import matplotlib.pyplot as plt
#plt.close('all')
#

LocalIP     = '0.0.0.0'
LocalPort   = 6665
bufferSize  = 2048

RedboxAdr = ("35.176.23.80",6666)

s = np.empty(128,    dtype = int)
d = np.empty(441000, dtype = int)

# Create a datagram socket
UDPServerSocket = sc.socket(family=sc.AF_INET, type=sc.SOCK_DGRAM)
# Bind to address and ip
UDPServerSocket.bind((LocalIP, LocalPort))

# Listen for incoming datagrams
while(True):
    # Send message to redbox to open communication
    input("Enter to establish connection with RB, or Ctrl+C to stop")
    UDPServerSocket.sendto("Message to open comms".encode(), RedboxAdr)

    print("Waiting for share")
    message, address = UDPServerSocket.recvfrom(bufferSize)
    clientIP  = "Client IP Address:{}".format(address)
    print("Rec'd online share: ", message.decode())
    print(clientIP)

    s = "".join(["{:08b}".format(x) for x in base64.b64decode(message)])    #Base64 converted into binary array

    #   Carrier wave properties
    Fs  =   44100                   # sampling frequency
    fc  =   18000                   # carrier frequency
    T   =   10                      # simulation time (sec)
    t   =   np.arange(0, T, 1/Fs)
    x   =   np.sin(2*pi*fc*t)       # carrier wave generated

    for i in range(6880):
        d[i] =  0
    for i in range(433120,441000):
        d[i] =  0

    k = 6880
    l = 3330
    m = 433120
    for j in range(128):
        for i in range(k,l):
            d[i] = s[j]
        k = l
        l = l + 3330
    
    
    ask =   x*d                     # ASK modulated wave
    
    # Sound file
    wv.write("OnlineAudioShare.wav", ask, Fs, sampwidth=3)

    # Play the audio file
    os.system("sudo aplay OnlineAudioShare.wav")

    print("End of interaction")

# TO PLOT the wave, UNCOMMENT the following lines of code
#plt.subplot(2,1,1)
#plt.plot(t,data);
#plt.xlabel('Time(s)');
#plt.ylabel('Amplitude')
#plt.title('Binary Signal')
#plt.grid()
#
#plt.subplot(2,1,2)
#plt.plot(t,ask);
#plt.xlabel('Time(s)');
#plt.ylabel('Amplitude')
#plt.title('ASK')
#plt.grid()
#plt.show()
