from machine import Pin, ADC, I2C
import ssd1306
import utime
import network
import urequests
import rp2
import gc
import uasyncio
import machine
import secrets

class Relay:
    def __init__(self, pin_num):
        # Relay configured for NO (Normally Open)
        self.relay_pin = Pin(pin_num, Pin.OUT, value=1)

    def on(self):
        self.relay_pin.value(0)

    def off(self):
        self.relay_pin.value(1)


class Transistor:
    def __init__(self, pin_num):
        self.transistor_pin = Pin(pin_num, Pin.OUT, value=0)

    def on(self):
        self.transistor_pin.value(0)

    def off(self):
        self.transistor_pin.value(1)

class Session:
    def __init__(self, session_id, csrf_token):
        self.session_id = session_id
        self.csrf_token = csrf_token

    def toString(self):
        return f"Session id: {self.session_id}, csrf_token: {self.csrf_token}"


def get_session():
    url = "http://192.168.0.201:8080/api/csrf-token"
    headers = {
        'Content-Type': 'application/json',
        'Authorization': secrets.AUTHENTICATION_TOKEN
    }
    i = 0
    while i < 5:
        try:
            display.fill(0)
            display.show()
            display.text('Getting CSRF Token',0, 0)
            display.show()
            response = urequests.get(url, headers=headers, timeout=10)
            print_http_response(response)
            csrf_token = response.headers.get('X-CSRF-Token');
            session_id = get_session_id(response)
            return Session(session_id, csrf_token)
        except OSError as e:
            print("OS Error!")
            print(e)
            display.text('OS Error',0, 16)
            display.text(str(e),0,32)
            display.show()
            utime.sleep(5)
            i = i + 1
            display.text('i:' + str(i),0,48)
            display.show()
    return None

def get_session_id(response):
    # Get the value of the 'Set-Cookie' header from the response
    set_cookie_header = response.headers.get('Set-Cookie')

    # Extract the value of the 'JSESSIONID' cookie from the 'Set-Cookie' header
    jsessionid_value = None
    if set_cookie_header:
        cookie_parts = set_cookie_header.split(';')
        for part in cookie_parts:
            if part.startswith('JSESSIONID='):
                jsessionid_value = part.split('=')[1]

    return jsessionid_value


def send_moisture_data(moisture, moisture_adc, plant, session):
    url = "http://192.168.0.201:8080/api/measurements"

    payload = {
        "moisture" : moisture,
        "adc" : moisture_adc,
        "plant" : plant
    }

    headers = {
        'Content-Type': 'application/json',
        'Authorization': secrets.AUTHENTICATION_TOKEN,
        'X-CSRF-Token': session.csrf_token,
        'Cookie': 'JSESSIONID=' + session.session_id
    }

    display.fill(0)
    display.show()
    display.text('Sending request',0, 0)
    display.text('moisture: %.2f' % moisture,0,16)
    display.text('adc:'+str(moisture_adc),0, 32)
    display.show()
    print('Sending request')
    response = urequests.post(url, json=payload, headers=headers, timeout=10)
    print_http_response(response)
    return response

def send_moisture_data_with_retry(moisture, moisture_adc, plant, session):
    i = 0
    while i < 10:
        try:
            response = send_moisture_data(moisture, moisture_adc, plant, session)
            print(str(response))
            display.fill(0)
            display.show()
            display.text('Request successful',0, 0)
            display.text('retries:' + str(i), 0, 16)
            display.text('plant:' + plant,0, 32)
            display.text(str(response), 0, 48)
            display.show()
            utime.sleep(5)
            return "success"
        except uasyncio.TimeoutError:
            print("Request timeout!")
            display.fill(0)
            display.show()
            display.text('Request timeout',0, 0)
            display.show()
            time.sleep(3)
            i = i + 1
        except OSError as e:
            print("OS Error!")
            print(e)
            display.fill(0)
            display.show()
            display.text('plant:' + plant,0, 0)
            display.text('OS Error',0, 16)
            display.text(str(e),0,32)
            display.text('status: '+str(e.status),0,48)
            display.show()
            utime.sleep(5)
            i = i + 1
    return "fail"


def connect_to_wlan():
    rp2.country('DE')
    wlan = network.WLAN(network.STA_IF)
    wlan.active(True)
    wlan.connect(secrets.SSID, secrets.WIFI_PASSWORD)
    i = 5
    while i > 0 and not wlan.isconnected() and wlan.status() != 3:
        display.fill(0)
        display.show()
        print("Waiting to connect:" + str(wlan.status()))
        display.text('Connecting to network',0, 16)
        display.text('Status:' + str(wlan.status()),0,32)
        display.show()
        utime.sleep(5)
        i = i - 1
    print(wlan.ifconfig())
    display.text(wlan.ifconfig()[0],0,48)
    display.show()
    utime.sleep(5)
    return wlan

class MoistureValue:
    def __init__(self, moisture, moisture_adc):
        self.moisture = moisture
        self.moisture_adc = moisture_adc

    def toString(self):
        return f"Moisture: {self.moisture}, Moisture ADC: {self.moisture_adc}"

def measure_moisture(pin, plant):
    soil = ADC(Pin(pin))
    utime.sleep(3) #to prevent power peak while connecting
    min_moisture=0
    max_moisture=65535


    moisture_adc = soil.read_u16()
    moisture = (max_moisture-moisture_adc)*100/(max_moisture-min_moisture)

    print("moisture: " + "%.2f" % moisture +"% (adc: "+str(moisture_adc)+") plant " + plant)

    display.fill(0)
    display.show()
    display.text('moisture: %.2f' % moisture,0,0)
    display.text('adc:'+str(moisture_adc),0, 16)
    display.text('plant: ' + plant,0,32)
    display.show()
    utime.sleep(5)
    display.fill(0)
    display.show()
    return MoistureValue(moisture, moisture_adc)

#example from: https://peppe8o.com/capacitive-soil-moisture-sensor-with-raspberry-pi-pico-wiring-code-and-calibrating-with-micropython/

def run_pump(relay, moisture, desired_moisture):
    pumpTime = 5
    if moisture < desired_moisture:
        relay.on()
        utime.sleep(pumpTime)
        relay.off()

def setup_display():
    # setup the I2C communication
    i2c = I2C(0, sda=Pin(16), scl=Pin(17));
    display = ssd1306.SSD1306_I2C(128, 64, i2c)
    display.text('Hello',0, 0)
    display.show()
    return display

def calculate_sleep_time(sending_result, display):
    sleep_time = 30 * 60 * 1000 #30 minutes
    if(sending_result != "success"):
        sleep_time = 17000
    return sleep_time

def power_off_devices_and_sleep(wlan, display, transistor, sleep_time):
    wlan.disconnect()
    display.fill(0)
    display.show()
    display.text('sleep',0,0)
    display.text(str(sleep_time / (1000 * 60)) + ' minutes ',0,16)
    display.show()
    print("sleeping for:" + str(sleep_time / (1000 * 60)) +' minutes')
    utime.sleep(5)
    display.poweroff()
    transistor.off()
    machine.deepsleep(sleep_time)

def run_irrigation_system(moisture_pin, relay, plant, desired_moisture, session):
    moisture = measure_moisture(moisture_pin, plant)
    sending_result = send_moisture_data_with_retry(moisture.moisture, moisture.moisture_adc, plant, session)
    run_pump(relay, moisture.moisture, desired_moisture)
    sleep_time = calculate_sleep_time(sending_result, display)
    return sleep_time

def print_http_response(response):
    print('Response status code:', response.status_code)

    # Print the response headers
    print('Response headers:')
    for header in response.headers:
        print(header + ': ' + response.headers[header])

    # Print the response body
    print('Response body:')
    print(response.text)

peripherals_power = Transistor(12)
peripherals_power.on()
utime.sleep(5)

display = setup_display()

#init relays
relay_15 = Relay(15)
relay_13 = Relay(13)
relay_14 = Relay(14)

wlan = connect_to_wlan()
if(wlan.status() != 3):
    power_off_devices_and_sleep(wlan, display, peripherals_power, 60 * 1000)

session = get_session()
if(session == None):
    power_off_devices_and_sleep(wlan, display, peripherals_power, 60 * 1000)

sleep_time_1 = run_irrigation_system(26, relay_15,'basil',47.0, session)
sleep_time_2 = run_irrigation_system(27, relay_13,'parsley',35.0, session)
sleep_time_3 = run_irrigation_system(28, relay_14,'thymus',20.0, session)

min_sleep_time = min(sleep_time_1, sleep_time_2, sleep_time_3)

power_off_devices_and_sleep(wlan, display, peripherals_power, min_sleep_time)
