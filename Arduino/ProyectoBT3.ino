#include <GPS_5Hz.h>
#include <SoftwareSerial.h>
#include <SD.h>
#include <math.h>
#include <NewPing.h>

SoftwareSerial mySerial(2, 3); //Constructor de puerto Serial por software en los pines indicados
//#define mySerial Serial1;
//Adafruit_GPS GPS(&mySerial);
GPS_5Hz GPS(&mySerial); //Constructor de la clase GPS (comunicandose por el puerto previamente creado)

// Pon GPSECHO a 'false' para apagar la generación de Eco de los datos del GPS a la consola Serial
// Ponlo a 'true' si quieres debuggear y escuchar las sentencias del GPS en la consola
#define GPSECHO  true
/* Poner a 'true' para solamente loggear datos a la SD cuando el GPS tiene un "fix" (Encontró su ubicación)
para debuggear, mantenlo como 'false'*/
#define LOG_FIXONLY false  

// Pines utilizados
#define chipSelect 10

File logfile;

#define TRIGGER_PIN 6
#define ECHO_PING 7
#define MAX_DISTANCE 400

NewPing sonar(TRIGGER_PIN, ECHO_PING, MAX_DISTANCE);

float distL, distR, distF, distB;

float w1, w2, w3, w4, b;

float n1, n2, n3, n4, n5, p1, p2, p3, p4, p5;
//int p1=0, p2=0, p3=0, p4=0;

#define EULER 2.718281828459045235360287471352

char filename[15];

// Parpadea un mensaje de error
void error(uint8_t errno) {
  while(1) {
      testOutputs();
  }
}

char inbyte = 0;

uint32_t timer = millis();

boolean usingInterrupt = false;
void useInterrupt(boolean); // Prototipo de funcion, necesario para Arduino 0023

void setup() {
  // Conecta a 115200 para poder leer el GPS lo suficientemente rápido y
  // hacer eco sin perder caracteres
  Serial.begin(9600);
  //Serial.println("\r\nUltimate GPSlogger Shield");
  
  
  // Asegurate de que el pin de selección por default esta habilitado como salida, 
  // aunque no lo uses:
  pinMode(10, OUTPUT);
  
  // Mira si hay alguna tarjeta SD conectada y que deba ser inicializada:d:
  if (!SD.begin(chipSelect)) {      // if you're using an UNO, you can use this line instead
    //Serial.println("Card init. failed!");
    error(2);
  }
  
  strcpy(filename, "GPSLOG00.TXT"); //Nombre base para los archivos generados
  for (uint8_t i = 0; i < 100; i++) {
    filename[6] = '0' + i/10;
    filename[7] = '0' + i%10;
    //Crea el archivo si no existe, no abre uno existente
    if (! SD.exists(filename)) {
      break;
    }
  }
  
  // conecta con el GPS a la velocidad deseada
  GPS.begin(9600);

  // Descomenta esta linea para activar el RMC (mínimo recomendado) y GGA (información de fix) incluyendo altitud
  GPS.sendCommand(PMTK_SET_NMEA_OUTPUT_RMCGGA);
  // Descomenta esta linea para activar solamente la información mínima recomendada
  //GPS.sendCommand(PMTK_SET_NMEA_OUTPUT_RMCONLY);
  // Para hacer data logging, no es recomendable usar nada más allá de solo RMC o RMC+GGA
  // para mantener los archivos de log de un tamaño razonable
  // Indica la velocidad de actualización
  GPS.sendCommand(PMTK_SET_NMEA_UPDATE_1HZ);   // frecuencia de actualización de 1 o 5Hz

  // Si el firmware lo permite, desactiva la información sobre el estado de la antena para ahorrar espacio:
  GPS.sendCommand(PGCMD_NOANTENNA);

  useInterrupt(true);

  pinMode(8, OUTPUT);
  pinMode(9, OUTPUT);
  pinMode(5, OUTPUT);
  pinMode(4, OUTPUT);
  pinMode(0, OUTPUT);
  pinMode(13, OUTPUT);

  delay(1000);
}

void loop() {
  
  //readGPS();
  leerSensoresMux();   
  control();
  humedad();
  //testOutputs();

  /**
   * Test Bluetooth
   */

  /*if (Serial.available() > 0)
  {
    inbyte = Serial.read();
    Serial.println(inbyte);
    if (inbyte == '2')
    {
      fRobot();
    }
    if (inbyte == '1')
    {
      lRobot();
    }
  }

  delay(100);*/
  
}

void leerSensoresMux() {
  delay(100);
  digitalWrite(8, LOW);
  digitalWrite(9, LOW);
  p1 = value(sonar.ping_cm(), 20);

  delay(100);
  digitalWrite(8, LOW);
  digitalWrite(9, HIGH);
  p3 = value(sonar.ping_cm(), 20);

  delay(100);
  digitalWrite(8, HIGH);
  digitalWrite(9, LOW);
  p2 = value(sonar.ping_cm(), 20);

  delay(100);
  digitalWrite(8, HIGH);
  digitalWrite(9, HIGH);
  p4 = value(sonar.ping_cm(), 20);
  
}

// La interrupción es llamada cada 1ms, busca alguna información nueva del GPS, y la almacena
SIGNAL(TIMER0_COMPA_vect) {
  char c = GPS.read();
}

void useInterrupt(boolean v) {
  if (v) {
    // El Timer0 ya está siendo usado por la función millis() - Interrumpiremos en algún punto intermedio 
    // y llamaremos la función de arriba
    OCR0A = 0xAF;
    TIMSK0 |= _BV(OCIE0A);
    usingInterrupt = true;
  } else {
    // No llames a la función de COMPA más
    TIMSK0 &= ~_BV(OCIE0A);
    usingInterrupt = false;
  }
}

void readGPS() {
  // En caso de no estar usando la interrupción anterior, 
  // deberás leer la información "a mano" del GPS, no recomendable (Podrías perder información)
  if (! usingInterrupt) {
    // Lee la información del GPS en el Main loop
    char c = GPS.read();
    // Si quieres debuggear, es el momento de hacerlo!
    /*if (GPSECHO)
      if (c) Serial.print(c);*/
  }
  
  // Si una sentencia es recibida, podemos hacer checksum y analizarla...
  if (GPS.newNMEAreceived()) {
    if (!GPS.parse(GPS.lastNMEA()))   // Verifica que la información sea correcta, esto pone la bandera newNMEAreceived() en false
      return;  // Podemos fallar al leer una oración, en cuyo caso solo esperamos a la siguiente
  }

  // si la variable timer supera el valor de millis, solo lo reseteamos
  if (timer > millis())  timer = millis();

  // aproximadamente cada 2 ms, imprimimos el estado actual
  if (millis() - timer > 2000) { 
    timer = millis(); // resetea el timer
    
    /*Serial.print("\nTime: ");
    Serial.print(GPS.hour, DEC); Serial.print(':');
    Serial.print(GPS.minute, DEC); Serial.print(':');
    Serial.print(GPS.seconds, DEC); Serial.print('.');
    Serial.println(GPS.milliseconds);
    Serial.print("Date: ");
    Serial.print(GPS.day, DEC); Serial.print('/');
    Serial.print(GPS.month, DEC); Serial.print("/20");
    Serial.println(GPS.year, DEC);
    Serial.print("Fix: "); Serial.print((int)GPS.fix);
    Serial.print(" quality: "); Serial.println((int)GPS.fixquality); */
    if (GPS.fix) {
      //Serial.print("Location: ");
      Serial.print("{");
      Serial.print(GPS.latitude, 4); Serial.print(GPS.lat);
      Serial.print(", "); 
      Serial.print(GPS.longitude, 4); Serial.println(GPS.lon);
      Serial.println("}");
      /*Serial.print("Speed (knots): "); Serial.println(GPS.speed);
      Serial.print("Angle: "); Serial.println(GPS.angle);
      Serial.print("Altitude: "); Serial.println(GPS.altitude);
      Serial.print("Satellites: "); Serial.println((int)GPS.satellites);*/
      logfile = SD.open(filename, FILE_WRITE);
      if( ! logfile ) {
      //Serial.print("Couldnt create "); Serial.println(filename);
        error(3);
      }
    //Serial.print("Writing to "); Serial.println(stringptr);
    logfile.print("C:");
    logfile.print(GPS.latitude, 4); logfile.print(GPS.lat);
    logfile.print(", ");
    logfile.print(GPS.longitude, 4); logfile.print(GPS.lon);
    logfile.println();
    /*if (stringsize != logfile.write((uint8_t *)stringptr, stringsize)) {    //Escribe el string en la memoria SD
      //Serial.println("Entra al error");
      error(4);
    }*/

    logfile.close();
    }
  }
}

int value(float dist, float threshold) {
  int value = 1;

  if (dist >= threshold)
    value = 0;

  return value;
}

float tansig(float x)
{
  float a;
  a = 2/(1+pow(EULER,-2*x))-1;
  return a;
}

void control() {
  /**
   * SI -> p1
   * SD -> p3
   * SA -> p2
   * SR -> p4
   */

  n1=p1*(-6.224482374666963)+p2*(-6.184933698378943)+p3*(-5.747946648694639)+p4*(0.033329839845467)+(17.815910232369220);
  n2=p1*(-5.946119069032946)+p2*(19.684575572350557)+p3*(-6.046736531986289)+p4*(1.954834347793514)+(-8.280398097282674);
  n3=p1*(-10.042205331258758)+p2*(-20.690534859858161)+p3*(10.148478356479245)+p4*(-10.114336229197034)+(25.728317397479557);
  n4=p1*(0.216478819054352)+p2*(11.813200244541614)+p3*(0.287509912897564)+p4*(-0.015173467788630)+(-6.071420188007603);
  
  
  p1=tansig(n1);
  p2=tansig(n2);
  p3=tansig(n3);
  p4=tansig(n4);

  n1=p1*(-0.373243378185899)+p2*(3.967025074442417)+p3*(2.782239072909795)+p4*(-1.184797167120527)+(0.373240491675556);
  n2=p1*(-0.000006075100775)+p2*(0.000000267116108)+p3*(0.000000297868074)+p4*(-3.173222388595657)+(3.173199263032028);
  n3=p1*(4.667192167803770)+p2*(-4.055766245089394)+p3*(-2.779558904159566)+p4*(4.055800005212671)+(-1.887635635168812);
  n4=p1*(0.318718736211031)+p2*(-3.378708362424669)+p3*(-0.000000126354994)+p4*(3.378738438144571)+(-0.318720128159110);
  n5=p1*(-3.945690959821942)+p2*(3.429134008878486)+p3*(0.000001535288461)+p4*(-3.429165742162901)+(3.945689345488998);

  p1 = tansig(n1);
  p2 = tansig(n2);
  p3 = tansig(n3);
  p4 = tansig(n4);
  p5 = tansig(n5);

  int sens;

  Serial.print("+");
  if (p1 >= 0.8) {
    lRobot();
    sens = 2;
  }
  if (p2 >= 0.8) {
    fRobot();
    sens = 1;
  }
  if (p3 >= 0.8) {
    rRobot();
    sens = 3;
  }
  if (p4 >= 0.8) {
    bRobot();
    sens = 4;
  }
  if (p5 >= 0.8) {
    stopRobot();
    sens = 0;
  }
  Serial.print(sens);
  Serial.println("?");
    
}

void testOutputs() {
  //digitalWrite(0, LOW);
  digitalWrite(5, LOW);
  digitalWrite(4, LOW);
  delay(500);

  //digitalWrite(0, LOW);
  digitalWrite(5, HIGH);
  digitalWrite(4, LOW);
  delay(500);

  //digitalWrite(0, LOW);
  digitalWrite(5, LOW);
  digitalWrite(4, HIGH);
  delay(500);

  //digitalWrite(0, LOW);
  digitalWrite(5, HIGH);
  digitalWrite(4, HIGH);
  delay(500);

  /*digitalWrite(0, HIGH);
  digitalWrite(5, LOW);
  digitalWrite(4, LOW);
  delay(500);*/
}

void humedad() {
  
  /*------------------LECTURA DE SENSOR DE HUMEDAD----------------*/

  float porcentaje = abs (((analogRead(A0)*(5.0/1023)*(100.0))/5.0)-100);

  //Serial.println(porcentaje);
  if (porcentaje > 10) {
    stopRobot();
    for (int i = 0; i<2; i++) 
      readGPS();
    delay(100);
    fRobot();
  }

 
/*------------------------------------------------------------*/
}

void stopRobot() {
  //digitalWrite(13, HIGH);
  digitalWrite(5, LOW);
  digitalWrite(4, LOW);
}

void fRobot() {
  //digitalWrite(0, LOW);
  digitalWrite(5, HIGH);
  digitalWrite(4, LOW);
}

void lRobot() {
  //digitalWrite(0, LOW);
  digitalWrite(5, LOW);
  digitalWrite(4, HIGH);
}
void rRobot() {
  //digitalWrite(0, LOW);
  digitalWrite(5, LOW);
  digitalWrite(4, LOW);
}
void bRobot() {
  //digitalWrite(0, LOW);
  digitalWrite(5, HIGH);
  digitalWrite(4, HIGH);
}
