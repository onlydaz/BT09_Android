#include <SoftwareSerial.h>

SoftwareSerial myserial(10, 11); // RX, TX
#define tbl 2
#define tb2 3
char val;
String statustbl, statustb2;

void setup() {
    pinMode(tbl, OUTPUT);
    digitalWrite(tbl, LOW);
    pinMode(tb2, OUTPUT);
    digitalWrite(tb2, LOW);
    myserial.begin(9600);
    Serial.begin(9600);
}

void loop() {
    // Kiểm tra dữ liệu từ Bluetooth
    if (myserial.available() > 0) {
        val = myserial.read();
        Serial.println(val);
        if (val == '1') {
            digitalWrite(tbl, HIGH);
            statustbl = "1";
        } else if (val == '2') {
            digitalWrite(tb2, HIGH);
            statustb2 = "2";
        } else if (val == 'A') {
            digitalWrite(tbl, LOW);
            statustbl = "A";
        } else if (val == 'B') {
            digitalWrite(tb2, LOW);
            statustb2 = "B";
        } else if (val == 's') {
            delay(500);
            myserial.println(statustbl + statustb2 + "J");
            val = '';
        }
    }
}
