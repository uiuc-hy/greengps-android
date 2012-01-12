import bluetooth
import random
import time

uuid = "00001101-0000-1000-8000-00805F9B34FB"
server_sock=bluetooth.BluetoothSocket( bluetooth.RFCOMM )
server_sock.bind(("", bluetooth.PORT_ANY))
server_sock.listen(1)

bluetooth.advertise_service(server_sock, "OBD Simulator",
							service_id = uuid,
                     service_classes=[uuid, bluetooth.SERIAL_PORT_CLASS],
                     profiles=[bluetooth.SERIAL_PORT_PROFILE])

while True:
	client_sock, address = server_sock.accept()
	print "Accepted connection from", address
	
	while True:
		try :
			data = client_sock.recv(1024)
		except Exception :
			break;
			
		if not data: 
			print "null data, exit inner loop"
			break
		print "received [%s]" % data
		

		if str(data).rfind("atz") != -1:
			print "Sending FakeCar v1.x>"
			time.sleep(0.13)
			client_sock.send("FakeCar v1.x>")
		elif str(data).rfind("at") != -1:
			print "Sending OK"
			time.sleep(0.13)
			client_sock.send("OK>")
		elif str(data).rfind("0100") != -1:
			s = '41 00 BE 1F B8 11 \r\r>'
			time.sleep(0.13)
			client_sock.send(s)
			print "Sending", s
		elif str(data).rfind("0110") != -1:
			s = '41 10 03 10 \r\r>'
			time.sleep(0.13)
			client_sock.send(s)
			print "Sending maf", s
		elif str(data).rfind("010d") != -1:
			s = '41 0D 1A \r\r>'
			time.sleep(0.13)
			client_sock.send(s)
			print "Sending speed", s
		elif str(data).rfind("0144") != -1:
			s = '41 44 7A 04\r\r>'
			time.sleep(0.13)
			client_sock.send(s)
			print "Sending equiv", s
		elif str(data).rfind("010c") != -1:
			s = '41 0C 1A F8\r\r41 0C 1A F8\r\r>'
			time.sleep(0.13)
			client_sock.send(s)
			print "Sending rpm", s
		elif str(data).rfind("0111") != -1:
			s = '41 11 0D \r\r>'
			time.sleep(0.13)
			client_sock.send(s)
			print "Sending throttle", s
		else:
			s = 'NO DATA\r\r>'
			time.sleep(0.2)
			client_sock.send(s)
			print "Sending", s
			
	print "Closing connection from", address
	client_sock.close()
server_sock.close()
