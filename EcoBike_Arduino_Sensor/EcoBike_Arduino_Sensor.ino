

#include <SoftwareSerial.h>

int val =0;
int mVperAmp = 185; // use 100 for 20A Module and 66 for 30A Module
int RawValue= 0;
int ACSoffset = 2500; 
double Voltage = 0;
double Amps = 0;
char c = ' ';
SoftwareSerial BTserial(2,3);
void setup() {
   BTserial.begin(9600);
  // put your setup code here, to run once:
  Serial.begin(9600);          //  setup serial
}

void loop() {
  if (BTserial.available())
  {
    c = BTserial.read();
  }

     float average = 0;
     for(int i = 0; i < 1000; i++) {
         average = average + (.0264 * analogRead(A0) -13.51);//for the 5A mode,  
    //   average = average + (.049 * analogRead(A0) -25);// for 20A mode
    // average = average + (.742 * analogRead(A0) -37.8);// for 30A mode

       delay(1);
     }
     
     Serial.print(average/1000);
     Serial.print("\n");
     if (average <= 0) average = 0;
     else average = (average/1000) + 0.01;
     BTserial.print("s");
     BTserial.print( average);
     BTserial.print("\n");

}
