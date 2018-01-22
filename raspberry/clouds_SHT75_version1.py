import json
from firebase import firebase
import PyTango
import time
import threading
import requests
import json
from collections import deque
import pickle
import numpy as np
import base64

API_ACCESS_KEY='AAAAOMlP48U:APA91bGKJTeroa-J3A5BLVZxGn6-GIb93H786WvEUFapeWJRLJU3ges30EMbJOlwW2xtYfw-Ci0lijJh_A_PkfG-WyxTuR16Pb8Y8PkkL386ELhl-og8TgBaHOWABZYJREgs_G1aPAqQ'
registrationId = 'eza9LF51M9k:APA91bHMzSOIn29xQHi5eLpTz-sBaV7HLGP_v7gQWnKgBuocTTGHlUamGtiBcYDO_enNARSEcIM7LFqzkwCSUfGEIRk5VbnfJIDss8CErIbT2rXbdf_no-igoIufvPnOPOA9utlgJB8Q'
url = 'https://fcm.googleapis.com/fcm/send'

mynotification = {"body": "Start clouds", 'sound':'default'}

mynotification1 = {"body": "Sensor1. Humidity exceeded 70%", 'sound':'default'}
mynotification2 = {"body": "Sensor2. Humidity exceeded 70%", 'sound':'default'}
mynotification3 = {"body": "Sensor3. Humidity exceeded 70%", 'sound':'default'}
mynotification4 = {"body": "Sensor4. Humidity exceeded 70%", 'sound':'default'}
mynotification5 = {"body": "Sensor5. Humidity exceeded 70%", 'sound':'default'}

fields = {
'to': registrationId,
'notification': mynotification,
'priority': 'high'
}

fields1 = {
'to': registrationId,
'notification': mynotification1,
'priority': 'high'
}
fields2 = {
'to': registrationId,
'notification': mynotification2,
'priority': 'high'
}
fields3 = {
'to': registrationId,
'notification': mynotification3,
'priority': 'high'
}
fields4 = {
'to': registrationId,
'notification': mynotification4,
'priority': 'high'
}
fields5 = {
'to': registrationId,
'notification': mynotification5,
'priority': 'high'
}

headers = {'Content-Type':'application/json', 'Authorization':'key=' + API_ACCESS_KEY}
#r = requests.post(url, data=json.dumps(fields), headers=headers)
#print(r.content)

class PostThread(threading.Thread):
    def __init__(self, firebase, sensors, containers, times):
        threading.Thread.__init__(self)
        self.firebase = firebase
        self.sensors = sensors
        self.running = True
        self.humidity1 = None
        self.temperatute1 = None
        self.humidity2 = None
        self.temperatute2 = None
        self.humidity3 = None
        self.temperatute3 = None
        self.humidity4 = None
        self.temperatute4 = None
        self.humidity5 = None
        self.temperatute5 = None
        self.containers = containers
        self.times = times
        #self.start = None
        self.trigger = True

    def run(self):
        self.startTime = time.time()
        timeToTrigger = time.time()
        while self.running:
            try:
                self.humidity1 = self.sensors[0].Humidity
            except Exception:
                self.humidity1 = 0.0
            try:
                self.temperature1 = self.sensors[0].Temperature
            except Exception as e:
                self.temperature1 = 0.0
            try:
                self.humidity2 = self.sensors[1].Humidity
            except Exception:
                self.humidity2 = -0.0
            try:
                self.temperature2 = self.sensors[1].Temperature
            except Exception:
                self.temperature2 = 0.0
            try:
                self.humidity3 = self.sensors[2].Humidity
            except Exception:
                self.humidity3 = 0.0
            try:
                self.temperature3 = self.sensors[2].Temperature
            except Exception:
                self.temperature3 = 0.0
            try:
                self.humidity4 = self.sensors[3].Humidity
            except Exception:
                self.humidity4 = 0.0
            try:
                self.temperature4 = self.sensors[3].Temperature
            except Exception:
                self.temperature4 = 0.0
            try:
                self.humidity5 = self.sensors[4].Humidity
            except Exception:
                self.humidity5 = 0.0
            try:
                self.temperature5 = self.sensors[4].Temperature
            except Exception:
                self.temperature5 = 0.0

            if self.humidity1 != -100.0:
                self.containers[0].append(self.humidity1)
            if self.humidity2 != -100.0:
                self.containers[1].append(self.humidity2)
            if self.humidity3 != -100.0:
                self.containers[2].append(self.humidity3)
            if self.humidity4 != -100.0:
                self.containers[3].append(self.humidity4)
            if self.humidity5 != -100.0:
                self.containers[4].append(self.humidity5)

            elapsedTime = (time.time() - self.startTime) / 60.
            self.times.append(elapsedTime)

            humidity1 = np.asarray(self.containers[0]).tolist()
            humidity2 = np.asarray(self.containers[1]).tolist()
            humidity3 = np.asarray(self.containers[2]).tolist()
            humidity4 = np.asarray(self.containers[3]).tolist()
            humidity5 = np.asarray(self.containers[4]).tolist()
            elapsedTimes = np.asarray(self.times).tolist()
            humidityArray = {'humidity1': humidity1, 'humidity2': humidity2, 'humidity3': humidity3, 'humidity4': humidity4, 'humidity5': humidity5, 'time':elapsedTimes}
            lastHumidity = {'humidity1': self.humidity1, 'humidity2': self.humidity2, 'humidity3': self.humidity3, 'humidity4': self.humidity4, 'humidity5': self.humidity5}
            lastTemperature = {'temperature1': self.temperature1, 'temperature2': self.temperature2, 'temperature3': self.temperature3, 'temperature4': self.temperature4, 'temperature5': self.temperature5}
            dataToFirebase = {'humidityArray' : humidityArray, 'lastHumidity': lastHumidity, 'lastTemperature': lastTemperature, 'trigger': self.trigger}
            #data = {'humidity1': humidity1, 'humidity2': humidity2, 'humidity3': humidity3, 'humidity4': humidity4, 'humidity5': humidity5, 'time':elapsedTimes, 'temperature1': self.temperature1, 'temperature2': self.temperature2, 'temperature3': self.temperature3, 'temperature4': self.temperature4, 'temperature5': self.temperature5, 'humidity1last': self.humidity1, 'humidity2last': self.humidity2, 'humidity3last': self.humidity3, 'humidity4last': self.humidity4, 'humidity5last': self.humidity5, 'trigger': self.trigger}
            

            try:
                #result = firebase.put('sht75', 'humidity' , data)
                result1 = firebase.put('sht75', 'data' , dataToFirebase)
                #result = firebase.put('sht75', 'humidity' , {'humidity1': np.asarray(self.containers[4]).tolist()})
            except Exception, e:
                print e

            #print (time.time() - timeToTrigger)
            if (time.time() - timeToTrigger) > 10:
                #print "triggered"
                timeToTrigger = time.time()
                self.trigger = not self.trigger

            time.sleep(1)
    def stop(self):
        self.running = False


"""mySht75 = SHT75(self.humidity1, self.temperature1, self.humidity2, self.temperature2, self.humidity3, self.temperature3,
                             self.humidity4, self.temperature4, self.humidity5, self.temperature5)"""
            
"""try:
print mySht75.toJSON()
except Exception, e:
print e"""

"""try:
#result = firebase.put('sht75', 'sensors', {'sensors': mySht75.toJSON()})
result = firebase.put('sht75', 'sensors' , {'humidity1': self.humidity1, 'temperature1': self.temperature1, 'humidity2': self.humidity2, 'temperature2': self.temperature2, 'humidity3': self.humidity3, 'temperature3': self.temperature3, 'humidity4': self.humidity4, 'temperature4': self.temperature4, 'humidity5': self.humidity5, 'temperature5': self.temperature5 })
except Exception, e:
print e"""

"""result2 = firebase.put('humidity', 'sensor2', {'humidity': self.humidity2, 'temperature': self.temperature2})
result3 = firebase.put('humidity', 'sensor3', {'humidity': self.humidity3, 'temperature': self.temperature3})
result4 = firebase.put('humidity', 'sensor4', {'humidity': self.humidity4, 'temperature': self.temperature4})
result5 = firebase.put('humidity', 'sensor5', {'humidity': self.humidity5, 'temperature': self.temperature5})"""



class listenThread(threading.Thread):
    def __init__(self, firebase, device):
        threading.Thread.__init__(self)
        self.firebase = firebase
        self.device = device
        self.running = True
        #self.humidity = None
        #self.temperatute = None
        self.value = firebase.get('/humidity', 'sensor2')
    def run(self):
        while self.running:
            #self.humidity = self.sensor.Humidity
            #self.temperature = self.sensor.Temperature
            result = firebase.get('/humidity', 'sensor2')
            if self.value['humidity'] != result['humidity']:
                #print result['humidity']
                #self.value['humidity'] = result['humidity']
                #self.device.set_humidity(result['humidity'])
                self.device.Unit = result['humidity']
            #print result
            #print result['humidity']
    def stop(self):
        self.running = False



firebase = firebase.FirebaseApplication('https://my-first-project-176712.firebaseio.com', None)

sensor1 = PyTango.DeviceProxy('p11/raspberry/sht75.01')
if sensor1.state() == PyTango.DevState.OFF:
    sensor1.On()
sensor2 = PyTango.DeviceProxy('p11/raspberry/sht75.02')
if sensor2.state() == PyTango.DevState.OFF:
    sensor2.On()
sensor3 = PyTango.DeviceProxy('p11/raspberry/sht75.03')
if sensor3.state() == PyTango.DevState.OFF:
    sensor3.On()
sensor4 = PyTango.DeviceProxy('p11/raspberry/sht75.04')
if sensor4.state() == PyTango.DevState.OFF:
    sensor4.On()
sensor5 = PyTango.DeviceProxy('p11/raspberry/sht75.05')
if sensor5.state() == PyTango.DevState.OFF:
    sensor5.On()

myPostThread = None
sensors = [sensor1, sensor2 ,sensor3 ,sensor4, sensor5]
containers = []
for i in range(len(sensors)):
    containers.append(deque(maxlen = 111))
times = deque(maxlen = 111)
myPostThread = PostThread(firebase, sensors, containers, times)
myPostThread.start()

"""myPostThread1 = None
myPostThread2 = None
myPostThread3 = None
myPostThread4 = None
myPostThread5 = None

if sensor1.state() == PyTango.DevState.ON:
    myPostThread1 = postThread1(firebase, sensor1, 1)
    myPostThread1.start()
if sensor2.state() == PyTango.DevState.ON:
    myPostThread2 = postThread2(firebase, sensor2, 2)
    myPostThread2.start()
if sensor3.state() == PyTango.DevState.ON:
    myPostThread3 = postThread3(firebase, sensor3, 3)
    myPostThread3.start()
if sensor4.state() == PyTango.DevState.ON:
    myPostThread4 = postThread4(firebase, sensor4, 4)
    myPostThread4.start()
if sensor5.state() == PyTango.DevState.ON:
    myPostThread5 = postThread5(firebase, sensor5, 5)
    myPostThread5.start()"""

#listenThread = listenThread(firebase, optidew)
#listenThread.start()

while True:
    i = raw_input('Enter to quit\r\n')
    if not i:
        #postThread.stop()
        #listenThread.stop()
        if myPostThread is not None:
            myPostThread.stop()
        """if myPostThread1 is not None:
            myPostThread1.stop()
        if myPostThread2 is not None:
            myPostThread2.stop()
        if myPostThread3 is not None:
            myPostThread3.stop()
        if myPostThread4 is not None:
            myPostThread4.stop()
        if myPostThread5 is not None:
            myPostThread5.stop()"""
        time.sleep(1)
        break









"""class postThread(threading.Thread):
    def __init__(self, firebase, sensors):
        threading.Thread.__init__(self)
        self.firebase = firebase
        self.sensors = sensors
        self.running = True
        self.humidity1 = None
        self.temperatute1 = None
        self.humidity2 = None
        self.temperatute2 = None
        self.humidity3 = None
        self.temperatute3 = None
        self.humidity4 = None
        self.temperatute4 = None
        self.humidity5 = None
        self.temperatute5 = None
        self.timeToWait1 = time.time()
        self.timeToWait2 = time.time()
        self.timeToWait3 = time.time()
        self.timeToWait4 = time.time()
        self.timeToWait5 = time.time()
    def run(self):
        while self.running:
            self.humidity1 = self.sensors[0].Humidity
            self.temperature1 = self.sensor[0].Temperature
            self.humidity2 = self.sensors[1].Humidity
            self.temperature2 = self.sensor[1].Temperature
            self.humidity3 = self.sensors[2].Humidity
            self.temperature3 = self.sensor[2].Temperature
            self.humidity4 = self.sensors[3].Humidity
            self.temperature4 = self.sensor[3].Temperature
            self.humidity5 = self.sensors[4].Humidity
            self.temperature5 = self.sensor[4].Temperature
            result1 = firebase.put('humidity', 'sensor1', {'humidity': self.humidity1, 'temperature': self.temperature1})
            result2 = firebase.put('humidity', 'sensor2', {'humidity': self.humidity2, 'temperature': self.temperature2})
            result3 = firebase.put('humidity', 'sensor3', {'humidity': self.humidity3, 'temperature': self.temperature3})
            result4 = firebase.put('humidity', 'sensor4', {'humidity': self.humidity4, 'temperature': self.temperature4})
            result5 = firebase.put('humidity', 'sensor5', {'humidity': self.humidity5, 'temperature': self.temperature5})
            if self.humidity1 == 0:
                if ((time.time() - self.TimeToWrite1)) > 10:
                    self.TimeToWrite1 = time.time()
                    requests.post(url, data=json.dumps(fields1), headers=headers)
            if self.humidity2 == 0:
                if ((time.time() - self.TimeToWrite2)) > 10:
                    self.TimeToWrite2 = time.time()
                    requests.post(url, data=json.dumps(fields2), headers=headers)
            if self.humidity3 == 0:
                if ((time.time() - self.TimeToWrite3)) > 10:
                    self.TimeToWrite3 = time.time()
                    requests.post(url, data=json.dumps(fields3), headers=headers)
            if self.humidity4 == 0:
                if ((time.time() - self.TimeToWrite4)) > 10:
                    self.TimeToWrite4 = time.time()
                    requests.post(url, data=json.dumps(fields4), headers=headers)
            if self.humidity5 == 0:
                if ((time.time() - self.TimeToWrite5)) > 10:
                    self.TimeToWrite5 = time.time()
                    requests.post(url, data=json.dumps(fields5), headers=headers)
            time.sleep(1)
    def stop(self):
        self.running = False"""

"""def callback_get(response):
    print response
data = {'name':'Maxim', 'age':'32'}
snapshot = firebase.post('/humidity', data)
firebase.get_async('humidity', snapshot['name'], callback=callback_get)"""


"""from pyfcm import FCMNotification
API_KEY = "AAAAOMlP48U:APA91bFPgkW84MiOEBF88Tw3KKl-c5OtDAba7-xyYiQzACx_HojZeBXDOHHbLDI40jTwvKri-2y77cFnehRcVPEKzTVT_Yj2fRV6f-kmfH5u9o3Idt7KaxRgWRJcUFemW_HK-zJD-gOZ"
push_service = FCMNotification(api_key=API_KEY)
message_title = "Uber update"
message_body = "Hope you're having fun this weekend, don't forget to check today's news"
registration_id = "eza9LF51M9k:APA91bHMzSOIn29xQHi5eLpTz-#sBaV7HLGP_v7gQWnKgBuocTTGHlUamGtiBcYDO_enNARSEcIM7LFqzkwCSUfGEIRk5VbnfJIDss8CErIbT2rXbdf_no-igoIufvPnOPOA9utlgJB8Q"
#restricted_package_name = "https://my-first-project-176712.firebaseio.com"
registration_id = []
result = push_service.notify_single_device(registration_id=registration_id, message_title=message_title, message_body=message_body)"""







"""class SHT75():
    def __init__(self, humidity1, temperature1, humidity2, temperature2, humidity3, temperature3, humidity4, temperature4,
                  humidity5, temperature5):
        self.humidity1 = humidity1
        self.temperature1 = temperature1
        self.humidity2 = humidity2
        self.temperature2 = temperature2
        self.humidity3 = humidity3
        self.temperature3 = temperature3
        self.humidity4 = humidity4
        self.temperature4 = temperature4
        self.humidity5 = humidity5
        self.temperature5 = temperature5

    def toJSON(self):
        return json.dumps({'humidity1': self.humidity1, 'humidity2': self.humidity2, 'humidity3': self.humidity3, 'humidity4': self.humidity4, 'humidity5': self.humidity5, 'temperature1': self.temperature1, 'temperature2': self.temperature2, 'temperature3': self.temperature3, 'temperature4': self.temperature4, 'temperature5': self.temperature5}, sort_keys=True)
        
    def getHumidity1(self):
        return self.humidity1
    def setHumidity1(self, humidity):
        self.humidity1 = humidity
        
    def getHumidity2(self):
        return self.humidity2
    def setHumidity2(self, humidity):
        self.humidity2 = humidity
        
    def getHumidity3(self):
        return self.humidity3
    def setHumidity3(self, humidity):
        self.humidity3 = humidity
        
    def getHumidity4(self):
        return self.humidity4
    def setHumidity4(self, humidity):
        self.humidity4 = humidity
        
    def getHumidity5(self):
        return self.humidity5
    def setHumidity5(self, humidity):
        self.humidity5 = humidity
        
    def getTemperature1(self):
        return self.temperature1
    def setTemperature1(self, temperature):
        self.temperature1 = temperature
        
    def getTemperature2(self):
        return self.temperature2
    def setTemperature2(self, temperature):
        self.temperature2 = temperature
        
    def getTemperature3(self):
        return self.temperature3
    def setTemperature3(self, temperature):
        self.temperature3 = temperature
        
    def getTemperature4(self):
        return self.temperature4
    def setTemperature4(self, temperature):
        self.temperature4 = temperature
        
    def getTemperature5(self):
        return self.temperature5
    def setTemperature5(self, temperature):
        self.temperature5 = temperature"""
