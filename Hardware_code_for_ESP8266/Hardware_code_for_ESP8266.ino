#include <ESP8266WiFi.h>
#include <FirebaseESP8266.h>
#include <NTPClient.h>
#include <WiFiUdp.h>
#include <LiquidCrystal_I2C.h>
#include <Wire.h>

# define IR_SENSOR 14

#define BUZZER 0

#define WIFI_SSID "ASV DEN"

#define WIFI_PASSWORD "87554321"

#define FIREBASE_HOST "monetization-of-parking-slots-default-rtdb.firebaseio.com"

#define FIREBASE_AUTH "cOFMUAxCPtylPWvRIVBUEDDjG8lvjKoO9uFFPvVN"

String SLOT_NAME = "slot1";

FirebaseData fbdo;

FirebaseJson setJson;

FirebaseJsonData getJson;

String generated_otp_number="";

unsigned long previousMillis = 0;

const long interval = 10000;

LiquidCrystal_I2C lcd(0x27, 16, 2);

// Define NTP Client to get time
WiFiUDP ntpUDP;

NTPClient timeClient(ntpUDP, "pool.ntp.org");

struct Flags {
  bool generate_otp = false;
  bool uid_found = false;
  bool is_timer_started = false;
}flags;

struct User {
  String uid = "";
  String duration = "";
  float currentprice = 0;
  int currenthour = 0;
  int currentminute = 0;
  int currentsecond = 0;
  int currentminutes = 0;
  int initial_time_hour = 0;
  int initial_time_min = 0;
  int initial_time_sec = 0;
  int initial_minutes = 0;
} user;

void setup() {

  // initialize the LCD
	lcd.begin();

	// Turn on the blacklight and print a message.
  lcd.setCursor(0,0);
	lcd.print("MONETIZATION OF");
  lcd.setCursor(1,1);
  lcd.print("PARKING SLOTS");
  Serial.begin(115200);

  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("Connecting to Wi-Fi");
  while (WiFi.status() != WL_CONNECTED){
    Serial.print(".");
    delay(300);
  }
  Serial.println();
  Serial.print("Connected with IP: ");
  Serial.println(WiFi.localIP());
  Serial.println();

  timeClient.begin();
    // Set offset time in seconds to adjust for your timezone, for example:
    // GMT +1 = 3600
    // GMT +8 = 28800
    // GMT -1 = -3600
    // GMT 0 = 0
    timeClient.setTimeOffset(((5*(3600))+1800));
    
  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
  Firebase.reconnectWiFi(true);

  //Set the size of WiFi rx/tx buffers in the case where we want to work with large data.
  fbdo.setBSSLBufferSize(1024, 1024);

  //Set the size of HTTP response buffers in the case where we want to work with large data.
  fbdo.setResponseSize(1024);

  //Set database read timeout to 1 minute (max 15 minutes)
  Firebase.setReadTimeout(fbdo, 1000 * 60);
  //tiny, small, medium, large and unlimited.
  //Size and its write timeout e.g. tiny (1s), small (10s), medium (30s) and large (60s).
  Firebase.setwriteSizeLimit(fbdo, "tiny");

  //optional, set the decimal places for float and double data to be stored in database
  Firebase.setFloatDigits(2);
  Firebase.setDoubleDigits(6);

  //lcd.clear();

  pinMode(IR_SENSOR, INPUT);

  pinMode(BUZZER, OUTPUT);

}

bool STATE = true;

void loop() {
 if(digitalRead(IR_SENSOR)) {
    if(!flags.generate_otp) {
        generated_otp_number = String(random(1000, 9999));
        lcd.clear();
        lcd.setCursor(1, 0);
        lcd.print("ENTER THE OTP");
        lcd.setCursor(6, 1);
        lcd.print(generated_otp_number);
        delay(10);
        digitalWrite(BUZZER, LOW);
        digitalWrite(BUZZER, HIGH);
        delay(50);
        digitalWrite(BUZZER, LOW);
        Serial.println("The generated otp is : " + generated_otp_number);
        if(Firebase.setString(fbdo, "/OTP/"+SLOT_NAME, generated_otp_number)){
          flags.generate_otp = true;
          flags.is_timer_started = true;
          previousMillis = millis();
        }
        

    }


    if(flags.is_timer_started){
      if(flags.generate_otp){
      if(!flags.uid_found){
        if(Firebase.getString(fbdo, "/SLOTS/"+SLOT_NAME+"/uid")){
        String uid = fbdo.stringData();
        if(uid!="None"){
          lcd.clear();
          lcd.setCursor(0,0);
          lcd.print("OTP VERIFICATION");
          lcd.setCursor(3,1);
          lcd.print("SUCCESSFUL");
          Serial.println("The uid is " + uid);
          user.uid = uid;
          flags.uid_found = true;
          flags.is_timer_started = false;
          timeClient.update();
          user.initial_time_hour = timeClient.getHours();
          user.initial_time_min = timeClient.getMinutes();
          user.initial_time_sec = timeClient.getSeconds();
          user.initial_minutes = user.initial_time_min + (user.initial_time_hour*60);
          digitalWrite(BUZZER, LOW);
          digitalWrite(BUZZER, HIGH);
          delay(50);
          digitalWrite(BUZZER, LOW);
          delay(50);
          digitalWrite(BUZZER, HIGH);
          delay(50);
          digitalWrite(BUZZER, LOW);
          delay(50);
          
        }
        if(!flags.uid_found){
          unsigned long currentMillis = millis();
          if(currentMillis - previousMillis >= interval){
            Serial.println("PLEASE PUT THE OTP SOOON");
            lcd.clear();
            lcd.setCursor(1,0);
            lcd.print("ENTER OTP SOON");
            lcd.setCursor(6,1);
            lcd.print(generated_otp_number);
            STATE = !STATE;
            digitalWrite(BUZZER, STATE);
          }
        }
      }
      }

    }
    }

    if(flags.uid_found){
      digitalWrite(BUZZER, LOW);
      // Keep updating the time and the price
      timeClient.update();
      user.currentminutes = (timeClient.getHours()*60 + timeClient.getMinutes()) - user.initial_minutes;
      //Serial.println(user.currentminutes);

      lcd.clear();
      lcd.setCursor(0, 0);
      user.duration = String(user.currentminutes/60)+":"+String(user.currentminutes%60)+":"+String(timeClient.getSeconds());
      String temp = "SPENT = "+String(user.duration);
      lcd.print(temp);
      
      user.currentprice = float(user.currentminutes)/2 + (10);

      lcd.setCursor(0,1);
      temp = "PRICE = Rs "+String(user.currentprice);
      lcd.print(temp);

      setJson.clear();
      setJson.add("duration", String(user.duration));
      setJson.add("price", String(user.currentprice));
      Firebase.updateNode(fbdo, "SLOTS/"+SLOT_NAME, setJson);
      Serial.println("Updated the firebase");

    }
    


  }else{
  lcd.setCursor(0,0);
  lcd.print("   STAY  SAFE   ");
  lcd.setCursor(0,1);
  lcd.print("                ");
  flags.generate_otp = false;
  flags.uid_found = false;
  flags.is_timer_started = false;
  digitalWrite(BUZZER, LOW);
 }
 
}
