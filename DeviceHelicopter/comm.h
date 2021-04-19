#include <SPI.h>
#include <RF24Network.h>
#include "RF24.h"
#include "printf.h"

//NOTE: I do know that writing actual code in header file is generally
//not a good idea, but for quick prototyping I think its OK

RF24 radio(9,10);
RF24Network network(radio);


#define MSG_TYPE_GET_RQ 0
#define MSG_TYPE_SET_RQ 1
#define MSG_TYPE_GET_RP 2
#define MSG_TYPE_SET_RP 3
#define MSG_TYPE_PING 4
#define MSG_TYPE_PONG 5
#define MSG_TYPE_ALARM_RQ 6
#define MSG_TYPE_ALARM_RP 7

#define MAX_REWRITE_ATTEMPTS 5
#define SEND_PING_EVERY_MS 3000

typedef struct {
    uint8_t message_type;
    uint8_t reg_id;
    uint8_t payload[8];
} nrf_message_t;


long last_ping_ts = 0;

void read_msg(int* from, nrf_message_t* msg) {
  RF24NetworkHeader in_header;
  network.read(in_header, msg, sizeof(*msg));
  
  Serial.print("Incoming msg from "); 
  Serial.print((int)in_header.from_node); 
  Serial.print(" msgId: ");
  Serial.println(msg->message_type);

  *from = (int)in_header.from_node;
}

void write_msg(int to, nrf_message_t msg) {
  RF24NetworkHeader out_header(to);
  bool ok = false;
  int attempts = 0;

  while (!ok) {
    ok = network.write(out_header, &msg, sizeof(msg));
     if (ok)
      Serial.println("ok.");
    else
      Serial.println("failed.");
    attempts++;
    if (attempts > MAX_REWRITE_ATTEMPTS) {
      ok = true;
      Serial.println("Failed too many times.");
    }
  }
}

void write_ping() {
  nrf_message_t out_msg;
  out_msg.message_type = MSG_TYPE_PING;
  out_msg.reg_id = 0;
  for (int i = 0; i < 8; i++) {
    out_msg.payload[i] = 0;
  }
  write_msg(00, out_msg);
}

void commonSetup() {
  Serial.begin(115200);
  while (!Serial); 
  Serial.print("Starting node ");
  Serial.println(THIS_NODE_ADDRESS);
 
  SPI.begin();
  radio.begin();
  printf_begin();
  radio.printDetails();
 
  network.begin(90, THIS_NODE_ADDRESS);
}
