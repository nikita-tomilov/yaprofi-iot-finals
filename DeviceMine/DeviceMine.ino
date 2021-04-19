#define THIS_NODE_ADDRESS 01

#include "comm.h"

void setup(void)
{
  commonSetup();
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
}
