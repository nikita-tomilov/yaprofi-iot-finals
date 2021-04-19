#define THIS_NODE_ADDRESS 02

#include "comm.h"

int piezoPin = 3;
bool alarmActive = false;

long alarmStateChange = 0;
int alarmState = 0;

void setup(void)
{
  commonSetup();
  tone(piezoPin, 1000, 500); 
}

void loop() {
  
  network.update();                         
  
  if (network.available()) {
    nrf_message_t msg;
    int from;
    read_msg(&from, &msg);

    if (msg.message_type == MSG_TYPE_ALARM_RQ) {
      int value = msg.payload[0];
      alarmActive = (value > 0);
      alarmStateChange = 0;
      nrf_message_t response;
      response.message_type = MSG_TYPE_ALARM_RP;
      response.reg_id = 0;
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

  if (millis() - alarmStateChange > 500) {
    if (alarmActive) {
      alarmState = 1 - alarmState;
      tone(piezoPin, 1000 + alarmState * 500);
    } else {
      noTone(piezoPin);  
    }
    alarmStateChange = millis();
  }
}
