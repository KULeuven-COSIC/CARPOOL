# -*- coding: utf-8 -*-


import time
import socket
import pyqrcode
from PIL import Image

LocalIP     = '0.0.0.0'
LocalPort   = 6665
bufferSize  = 2048

RedboxAdr = ("35.176.23.80",6666)

greetings = "Rec'd a device share from client: "
FILENAME_PNG = "qr_code.png"

# Create a datagram socket
UDPServerSocket = socket.socket(family=socket.AF_INET, type=socket.SOCK_DGRAM)
# Bind to address and ip
UDPServerSocket.bind((LocalIP, LocalPort))
print("Bound, now going into while loop:")

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
    val = pyqrcode.create(message.decode())
    val.png(FILENAME_PNG, scale = 6)
    im = Image.open(FILENAME_PNG)
    im.show()
    
    print("End of interaction")

    # Sending a reply to client
    # respMsg = greetings + message.decode()
    # UDPServerSocket.sendto(str.encode(respMsg), address)
