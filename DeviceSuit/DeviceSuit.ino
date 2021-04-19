#define THIS_NODE_ADDRESS 03

#include "comm.h"

uint8_t measurements[6];

long lastPinUpdate = 0;

void setup(void)
{
  commonSetup();
  for (int i = 2; i <= 5; i++) {
    pinMode(i, INPUT_PULLUP);
  }
}

void loop() {
  
  network.update();                         
  
  if (network.available()) {
    nrf_message_t msg;
    int from;
    read_msg(&from, &msg);
    if (msg.message_type == MSG_TYPE_SET_RQ) {
      int pin = msg.reg_id;
      int value = (msg.payload[0] > 0) ? HIGH : LOW;
      pinMode(pin, OUTPUT);
      digitalWrite(pin, value);
      
      nrf_message_t response;
      response.message_type = MSG_TYPE_SET_RP;
      response.reg_id = pin;
      for (int i = 0; i < 8; i++) {
        response.payload[i] = 0;
      }
      response.payload[0] = value;
      
      write_msg(from, response);
    }
  }

  if (millis() - last_ping_ts > SEND_PING_EVERY_MS) {
    write_ping();
    last_ping_ts = millis();
  }

  if (millis() - lastPinUpdate > 250) {

    measurements[0] = analogRead(A5) / 4;
    measurements[1] = analogRead(A4) / 4;
    for (int i = 2; i <= 5; i++) {
      measurements[i] = 1 - digitalRead(i);
    }

    /*Serial.println("====");
    for (int i = 0; i <= 5; i++) {
      Serial.println(measurements[i]);
    }*/
    
    nrf_message_t msg;
    msg.message_type = MSG_TYPE_GET_RP;
    msg.reg_id = 0;
    for (int i = 0; i < 6; i++) {
      msg.payload[i] = measurements[i];
    }
    msg.payload[7] = 0;
    msg.payload[8] = 0;

    write_msg(0, msg);

    lastPinUpdate = millis();
  }
}
