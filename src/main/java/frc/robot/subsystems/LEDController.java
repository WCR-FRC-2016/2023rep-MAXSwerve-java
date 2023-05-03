// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import java.util.ArrayList;
import java.util.List;

import edu.wpi.first.math.Pair;

import frc.robot.Constants.OIConstants;
import frc.robot.game.GameHandler;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj.AddressableLED;
import edu.wpi.first.wpilibj.AddressableLEDBuffer;

public class LEDController extends SubsystemBase {
  public int kLength = 256;  // Length of LED panel
  int kLength2 = 100; // Length of LED strip
  int kTotalLength = kLength + kLength2; // Total length of all LEDs

  private AddressableLED m_led = new AddressableLED(0);
  private AddressableLEDBuffer m_ledBuffer = new AddressableLEDBuffer(kTotalLength);

  int i = 0;
  int j = 0;
  int y = 0;
  int state = 8;
  int overrideState = -1;
  int prevState = 0;
  double angle = 0;
  boolean m_allianceIsRed;

  // aperture LED strip animation variables
  double d1y = 10;
  double d1yv = 0.4;
  boolean d1s = false;
  double d2y = 22.2;
  double d2yv = 0.8;
  boolean d2s = true;
  boolean drop_mode = true;

  // LED Brightness from config (shortened to fit more easily in expressions).
  double bright = OIConstants.kLEDBrightness;

  String word = "TERRY THOMAS";

  List<Pair<Integer,Integer>> aperture_points = new ArrayList<>();

  /** Creates a new LEDController. */
  public LEDController() {
    GameHandler.create();

    m_led.setLength(kTotalLength);
    m_led.setData(m_ledBuffer);
    m_led.start();

    //     XXXX X
    //    XXXXXX 
    //  XX  XXXX
    //  XXXX  XXX
    // XXXXXX
    // XXX
    {
        // Well, you know the old formula...
        aperture_points.add(Pair.of(4,0));
        aperture_points.add(Pair.of(5,0));
        aperture_points.add(Pair.of(6,0));
        aperture_points.add(Pair.of(7,0));
        aperture_points.add(Pair.of(9,0));
        aperture_points.add(Pair.of(3,1));
        aperture_points.add(Pair.of(4,1));
        aperture_points.add(Pair.of(5,1));
        aperture_points.add(Pair.of(6,1));
        aperture_points.add(Pair.of(7,1));
        aperture_points.add(Pair.of(8,1));
        aperture_points.add(Pair.of(1,2));
        aperture_points.add(Pair.of(2,2));
        aperture_points.add(Pair.of(5,2));
        aperture_points.add(Pair.of(6,2));
        aperture_points.add(Pair.of(7,2));
        aperture_points.add(Pair.of(8,2));
        aperture_points.add(Pair.of(1,3));
        aperture_points.add(Pair.of(2,3));
        aperture_points.add(Pair.of(3,3));
        aperture_points.add(Pair.of(4,3));
        aperture_points.add(Pair.of(7,3));
        aperture_points.add(Pair.of(8,3));
        aperture_points.add(Pair.of(9,3));
        aperture_points.add(Pair.of(0,4));
        aperture_points.add(Pair.of(1,4));
        aperture_points.add(Pair.of(2,4));
        aperture_points.add(Pair.of(3,4));
        aperture_points.add(Pair.of(4,4));
        aperture_points.add(Pair.of(5,4));
        aperture_points.add(Pair.of(0,5));
        aperture_points.add(Pair.of(1,5));
        aperture_points.add(Pair.of(2,5));
    }
}

  @Override
  public void periodic() {
    switch (overrideState==-1?state:overrideState) {
        case 0:
            circles();
            break;
        case 1:
            cone();
            break;
        case 2:
            cube2();
            break;
        case 3:
            drawWord();
            break;
        case 4: // Field-relative (blue)
            fill(0,0,255);
            pulse(0,0,255,20);
            drawLetter('F', 7, 6);
            flush();
            i++; i%=20;
            break;
        case 5: // Robot-relative (red)
            fill(255,0,0);
            pulse(255,0,0,20);
            drawLetter('R', 7, 6);
            flush();
            i++; i%=20;
            break;
        case 6: // Angles (for autobalance)
            drawAngle();
            break;
        case 7: // Confirmation (temporary state)
            flashConfirmation();
            break;
        case 8:
            aperture();
            break;
        case 9:
            GameHandler.handleGames(this);
        //snake();
            break;
        default:
            clear();
            flush();
            break;
    }
  }

  public void setState(int newState) {
    if (state!=newState) {
        prevState = state;
        state = newState;
        i = 0;
    }
  }
  public int getState() {return state;}
  public void setOverrideState(int newState) {
    if (overrideState!=newState) {
        overrideState = newState;
        i = 0;
    }
  }
  public int getOverrideState() {return overrideState;}
  public int getPrevState() {return prevState;}

  // con 0 = driver, 1 = manip
  public void gameControl(int con, int dir) {
    if (state==9) {
      GameHandler.povUpdate(con, dir);
      //if (con==0) {
      //  setSnakeDir(dir);
      //} else if (con==1) {
      //  setAppleDir(dir);
      //}
    } else { // TODO: switches
      if (con==0) {
        if (dir==0) {setState(9); GameHandler.setGame("frogger");} // TODO: Frogger
        else if (dir==1) {setState(9); GameHandler.setGame("snake");}
        else if (dir==2) {setState(9); GameHandler.setGame("galaxian");} // TODO: Galaxian
        else if (dir==3) {setState(9); GameHandler.setGame("pong");}
      } else if (con==1) {
        if (dir==0) setState(1);
        else if (dir==1) setState(0);
        else if (dir==2) setState(2);
        else if (dir==3) setState(8);
      }
    }
  }

  public void clear() {
    for (int x=0; x<16; x++) {
      for (int y=0; y<16; y++) {
        setRGB(x, y, 0, 0, 0);
      }
    }
  
    for (int n=0; n < kLength2; n++) {
      setRGB(n+kLength, 0, 0, 0);
    }
  }
  
  public void fill(int r, int g, int b) {
    for (int x=0; x<16; x++) {
      for (int y=0; y<16; y++) {
        setRGB(x, y, r, g, b);
      }
    }
  }

  public void pulse(int r, int g, int b, int loop) {
    for (int n=0; n < kLength2; n++) {
      double s = 0.4 + 0.6*Math.sin((n/10.0 + i*1.0/loop)*2*Math.PI);
      s = Math.min(Math.max(s, 0.0), 1.0);
      setRGB(n+kLength, r*s, g*s, b*s);
    }
  }
  
  private void circles() {
    clear();
    for (int x=0; x<16; x++) {
      for (int y=0; y<16; y++) {
        double d = Math.sqrt((x-8)*(x-8) + (y-8)*(y-8));
        double s = 90+110*Math.sin((i/200.0+d/10.0)*2*Math.PI);
        s = Math.max(s,0.0);
        if (m_allianceIsRed) {
          setRGB(x,y, s, 0, 0);
        } else {
          setRGB(x,y, 0, 0, s);
        }
      }
    }
  
    if (m_allianceIsRed) pulse(255,0,0, 20);
    else pulse(0,0,255, 20);
  
    flush();
    i++;
    i%=200;
  }
  
  private void cone() {
    clear();
    for (int x=0; x<16; x++) {
      int miny = 3*Math.abs(x-8)+3;
  
      // Triangle of cone
      if (miny>0) {
        for (int y=miny; y<16; y++) {
          setRGB(x, y, 255, 70, 0);
        }
      }
  
      // Cool stripes
      for (int y=0; y<miny; y++) {
        double d = miny-y;
        double s = Math.sin((i/100.0-d/10.0)*2*Math.PI);
        setRGB(x, y, 60+60*s, 10+10*s, 0);
      }
  
      // Base of cone
      if (x>2 && x<14) {
        for (int y=14; y<16; y++) {
          setRGB(x, y, 255, 70, 0);
        }
      }
    }
  
    pulse(255, 70, 0, 50);
  
    flush();
    i++;
    i%=100;
  }
  
  private void cube() {
    clear();
    for (int y = 0; y<16; y++) {
      int minx = 4;
      int maxx = 11;
      if (y == 0||y == 15) {
        minx = 5;
        maxx = 10;
      }
  
      for (int x=minx; x<=maxx; x++) {
        setRGB(x, y, 110, 0, 255);
      }
    }
    flush();
  }
  
  private void cube2() {
    clear();
    for (int x = 0; x<16; x++) {
      for (int y = 0; y<16; y++) {
        if (x<3 || x>12 || y<3 || y>12) {
          double a = Math.atan2(y-8,x-8);
          double s = Math.sin((i/75.0-a/Math.PI)*2*Math.PI);
          double s2 = Math.sin((i/75.0-a/Math.PI)*Math.PI + Math.PI/4);
          setRGB(x, y, 60*Math.pow(s2,10), 0, 50+50*s);
        }
      }
    }
  
    for (int y = 4; y<12; y++) {
      for (int x = 4; x<12; x++) {
        setRGB(x, y, 110, 0, 255);
      }
    }
  
    for (int n=0; n < kLength2; n++) {
      double s = Math.sin((i/75.0+n/10.0)*2*Math.PI);
      double s2 = Math.sin((i/75.0+n/10.0)*Math.PI + Math.PI/4);
      setRGB(n+kLength, 60*Math.pow(s2,10), 0, 50+50*s);
    }
  
    flush();
  
    i++;
    i%=75;
  }
  
  private void flash(int i) {
    clear();
  
    
    for(int h=0; h < i; h++){
      int x = h % 32;
      int y = h / 32;
      
      // int blink = (i%2)*255;
      // setRGB(0, 0, blink, blink, blink);  
      
      
      setRGB(x, y, x+1, 0, i);
    }
    
  
    flush();
  }
  
  public void drawWord() {
    clear();
    for (int k=0; k<word.length(); k++) {
      drawLetter(word.charAt(k), (int) (4*k+16-Math.floor(i/4)), 6);
    }
  
    pulse(0, 0, 255, (4*word.length()+16)*4);
  
    flush();
  
    i++;
    i%=(4*word.length()+16)*4;
  }
  
  public void drawLetter(char c, int x, int y) {
    switch (c) {
      case 'A':
        for (int y2=y+1;y2<y+5;y2++) {
          setRGB(x,y2,   0, 0, 255);
          setRGB(x+2,y2, 0, 0, 255);
        }
        setRGB(x+1,y,   0, 0, 255);
        setRGB(x+1,y+2, 0, 0, 255);
        break;
      case 'B':
        for (int y2=y;y2<y+5;y2++) {
          setRGB(x,y2, 0, 0, 255);
        }
        setRGB(x+1,y,   0, 0, 255);
        setRGB(x+2,y+1, 0, 0, 255);
        setRGB(x+1,y+2, 0, 0, 255);
        setRGB(x+2,y+3, 0, 0, 255);
        setRGB(x+1,y+4, 0, 0, 255);
        break;
      case 'C':
        for (int y2=y+1;y2<y+4;y2++) {
          setRGB(x,y2, 0, 0, 255);
        }
        for (int x2=x+1;x2<x+3;x2++) {
          setRGB(x2,y,   0, 0, 255);
          setRGB(x2,y+4, 0, 0, 255);
        }
        break;
      case 'D':
        for (int y2=y;y2<y+5;y2++) {
          setRGB(x,y2, 0, 0, 255);
        }
        setRGB(x+1,y,   0, 0, 255);
        setRGB(x+2,y+1, 0, 0, 255);
        setRGB(x+2,y+2, 0, 0, 255);
        setRGB(x+2,y+3, 0, 0, 255);
        setRGB(x+1,y+4, 0, 0, 255);
        break;
      case 'E':
        for (int y2=y;y2<y+5;y2++) {
          setRGB(x,y2, 0, 0, 255);
        }
        setRGB(x+1,y,   0, 0, 255);
        setRGB(x+2,y,   0, 0, 255);
        setRGB(x+1,y+2, 0, 0, 255);
        setRGB(x+1,y+4, 0, 0, 255);
        setRGB(x+2,y+4, 0, 0, 255);
        break;
      case 'F':
        for (int y2=y;y2<y+5;y2++) {
          setRGB(x,y2, 0, 0, 255);
        }
        setRGB(x+1,y,   0, 0, 255);
        setRGB(x+2,y,   0, 0, 255);
        setRGB(x+1,y+2, 0, 0, 255);
        break;
      case 'G':
        for (int y2=y+1;y2<y+4;y2++) {
          setRGB(x,y2, 0, 0, 255);
        }
        for (int x2=x+1;x2<x+3;x2++) {
          setRGB(x2,y,   0, 0, 255);
          setRGB(x2,y+4, 0, 0, 255);
        }
        setRGB(x+2,y+3, 0, 0, 255);
        setRGB(x+2,y+2, 0, 0, 255);
        break;
      case 'H':
        for (int y2=y;y2<y+5;y2++) {
          setRGB(x,y2, 0, 0, 255);
          setRGB(x+2,y2, 0, 0, 255);
        }
        setRGB(x+1,y+2, 0, 0, 255);
        break;
      case 'I':
        setRGB(x,y,     0, 0, 255);
        setRGB(x+2,y,   0, 0, 255);
        setRGB(x,y+4,   0, 0, 255);
        setRGB(x+2,y+4, 0, 0, 255);
        for (int y2=y;y2<y+5;y2++) {
          setRGB(x+1,y2, 0, 0, 255);
        }
        break;
      case 'J':
        for (int y2=y+1;y2<y+4;y2++) {
          setRGB(x,y2, 0, 0, 255);
        }
        for (int y2=y;y2<y+5;y2++) {
          setRGB(x+2,y2, 0, 0, 255);
        }
        setRGB(x+1,y+4, 0, 0, 255);
        break;
      case 'K':
        for (int y2=y;y2<y+5;y2++) {
          setRGB(x,y2, 0, 0, 255);
          if (y2!=2) setRGB(x+2,y2, 0, 0, 255);
        }
        setRGB(x+1,y+2, 0, 0, 255);
        break;
      case 'L':
        for (int y2=y;y2<y+5;y2++) {
          setRGB(x,y2, 0, 0, 255);
        }
        setRGB(x+1,y+4, 0, 0, 255);
        setRGB(x+2,y+4, 0, 0, 255);
        break;
      case 'M':
        for (int y2=y;y2<y+5;y2++) {
          setRGB(x,y2,   0, 0, 255);
          setRGB(x+2,y2, 0, 0, 255);
        }
        setRGB(x+1,y,   0, 0, 255);
        setRGB(x+1,y+1, 0, 0, 255);
        break;
      case 'N':
        for (int y2=y;y2<y+5;y2++) {
          setRGB(x,y2,   0, 0, 255);
          setRGB(x+2,y2, 0, 0, 255);
        }
        setRGB(x+1,y,   0, 0, 255);
        break;
      case 'O':
        for (int y2=y+1;y2<y+4;y2++) {
          setRGB(x,y2,   0, 0, 255);
          setRGB(x+2,y2, 0, 0, 255);
        }
        setRGB(x+1,y,   0, 0, 255);
        setRGB(x+1,y+4, 0, 0, 255);
        break;
      case 'P':
        for (int y2=y;y2<y+5;y2++) {
          setRGB(x,y2, 0, 0, 255);
        }
        setRGB(x+1,y,   0, 0, 255);
        setRGB(x+2,y+1, 0, 0, 255);
        setRGB(x+1,y+2, 0, 0, 255);
        break;
      case 'Q':
        // TODO: Make better Q
        //  X
        // X X
        // X X
        // XXX
        //  XX
        for (int y2=y+1;y2<y+4;y2++) {
          setRGB(x,y2,   0, 0, 255);
          setRGB(x+2,y2, 0, 0, 255);
        }
        setRGB(x+1,y,   0, 0, 255);
        setRGB(x+1,y+3, 0, 0, 255);
        setRGB(x+1,y+4, 0, 0, 255);
        setRGB(x+3,y+4, 0, 0, 255);
        break;
      case 'R':
        for (int y2=y;y2<y+5;y2++) {
          setRGB(x,y2, 0, 0, 255);
        }
        setRGB(x+1,y,   0, 0, 255);
        setRGB(x+2,y+1, 0, 0, 255);
        setRGB(x+1,y+2, 0, 0, 255);
        setRGB(x+2,y+3, 0, 0, 255);
        setRGB(x+2,y+4, 0, 0, 255);
        break;
      case 'S':
        setRGB(x+1,y,   0, 0, 255);
        setRGB(x+2,y,   0, 0, 255);
        setRGB(x,y+1,   0, 0, 255);
        setRGB(x,y+2,   0, 0, 255);
        setRGB(x+1,y+2, 0, 0, 255);
        setRGB(x+2,y+2, 0, 0, 255);
        setRGB(x+2,y+3, 0, 0, 255);
        setRGB(x,y+4,   0, 0, 255);
        setRGB(x+1,y+4, 0, 0, 255);
        break;
      case 'T':
        setRGB(x,y,   0, 0, 255);
        setRGB(x+2,y, 0, 0, 255);
        for (int y2=y;y2<y+5;y2++) {
          setRGB(x+1,y2, 0, 0, 255);
        }
        break;
      case 'U':
        for (int y2=y;y2<y+4;y2++) {
          setRGB(x,y2, 0, 0, 255);
          setRGB(x+2,y2, 0, 0, 255);
        }
        setRGB(x+1,y+4, 0, 0, 255);
        break;
      case 'V':
        for (int y2=y;y2<y+3;y2++) {
          setRGB(x,y2, 0, 0, 255);
          setRGB(x+2,y2, 0, 0, 255);
        }
        setRGB(x+1,y+3, 0, 0, 255);
        setRGB(x+1,y+4, 0, 0, 255);
        break;
      case 'W':
        for (int y2=y;y2<y+5;y2++) {
          setRGB(x,y2, 0, 0, 255);
          setRGB(x+2,y2, 0, 0, 255);
        }
        setRGB(x+1,y+3, 0, 0, 255);
        setRGB(x+1,y+4, 0, 0, 255);
        break;
      case 'X':
        for (int y2=y;y2<y+5;y2++) {
          if (y2!=2) setRGB(x,y2, 0, 0, 255);
          if (y2!=2) setRGB(x+2,y2, 0, 0, 255);
        }
        setRGB(x+1,y+2, 0, 0, 255);
        break;
      case 'Y':
        for (int y2=y;y2<y+2;y2++) {
          setRGB(x,y2,   0, 0, 255);
          setRGB(x+2,y2, 0, 0, 255);
        }
        for (int y2=y+2;y2<y+5;y2++) {
          setRGB(x+1,y2, 0, 0, 255);
        }
        break;
      case 'Z':
        setRGB(x,y,     0, 0, 255);
        setRGB(x+1,y,   0, 0, 255);
        setRGB(x+2,y,   0, 0, 255);
        setRGB(x+2,y+1, 0, 0, 255);
        setRGB(x+1,y+2, 0, 0, 255);
        setRGB(x,y+3,   0, 0, 255);
        setRGB(x,y+4,   0, 0, 255);
        setRGB(x+1,y+4, 0, 0, 255);
        setRGB(x+2,y+4, 0, 0, 255);
        break;
      default:
        return;
    }
  }
  
  private void drawAngle() {
    clear();
    for (int x = 0; x<16; x++) {
      for (int y = 0; y<16; y++) {
        double a = Math.atan2(y-8,x-8);
        double s = Math.sin((angle/18.0-2*a/Math.PI)*2*Math.PI);
        double s2 = Math.sin((angle/18.0-2*a/Math.PI)*Math.PI + Math.PI/4);
        setRGB(x, y, 60*Math.pow(s2,10), 0, 50+50*s);
      }
    }
  
    for (int n=0; n < kLength2; n++) {
      double s = Math.sin((angle/18.0+n/10.0)*2*Math.PI);
      double s2 = Math.sin((angle/18.0+n/10.0)*Math.PI + Math.PI/4);
      setRGB(n+kLength, 60*Math.pow(s2,10), 0, 50+50*s);
    }
  
    flush();
  }
  
  public void setAngle(double newAngle) {
    angle = newAngle;
  }

  private void flashConfirmation() {
    clear();
  
    double s = Math.sin(i/25.0 *2*Math.PI)*0.5 + 0.5;
  
    for (int n = 0; n<kTotalLength; n++) {
      setRGB(n, 50*s, 205*s, 50*s);
    }
  
    flush();
  
    i++;
    i%=100;
  }
  
  private void aperture() {
    clear();
  
    // LED panel aperture symbol
    for (int n = 0; n<aperture_points.size(); n++) {
      int x = aperture_points.get(n).getFirst();
      int y = aperture_points.get(n).getSecond();
  
      setRGB(x, y,       43, 56, 127); // Upper left
      setRGB(15-y, x,    43, 56, 127); // Upper right
      setRGB(15-x, 15-y, 43, 56, 127); // Lower right
      setRGB(y, 15-x,    43, 56, 127); // Lower left
    }
    
    /*
    for (int n=0; n < kLength2; n++) {
      double s = 0.4 + 0.6*Math.sin((n/10.0 + i/20.0)*2*Math.PI);
      s = std::clamp(s, 0.0, 1.0);
      if (((int) ((n+2)/10.0+i/20.0))%2==0) {
        setRGB(n+kLength, 0, 50*s, 255*s);
      } else {
        setRGB(n+kLength, 255*s, 50*s, 0);
      }
    }
    */
  
    /*
    setRGB(0+kLength, 0, 50, 255);
    setRGB(1+kLength, 0, 25, 127);
    setRGB(46+kLength, 127, 25, 0);
    setRGB(47+kLength, 255, 50, 0);
    setRGB(99+kLength, 0, 50, 255);
    setRGB(98+kLength, 0, 25, 127);
    setRGB(52+kLength, 127, 25, 0);
    setRGB(51+kLength, 255, 50, 0);
  
    int y = 46 - ((i*i)/100)%46;
    setRGB(y+kLength, 127, 127, 127);
    setRGB(99-y+kLength, 127, 127, 127);
    /*/
    // LED strip logic
    d1yv+=0.02; d1y+=d1yv;
    if (d1y>46) {
      if (drop_mode) d1y-=46;
      else {
        d1y = 92-d1y;
        d1yv = -d1yv;
        d1s = !d1s;
      }
    }
    if (d1y<0) {
      d1y+=46;
      if (!drop_mode) d1s = !d1s;
    }
    if (d1yv>3) d1yv=3;
    
    d2yv+=0.02; d2y+=d2yv;
    if (d2y>46) {
      if (drop_mode) d2y-=46;
      else {
        d2y = 92-d2y;
        d2yv = -d2yv;
        d2s = !d2s;
      }
    }
    if (d2y<0) {
      d2y+=46;
      if (!drop_mode) d2s = !d2s;
    }
    if (d2yv>3) d2yv=3;
  
    // LED strip portals
    if (drop_mode) {
      setRGB(kLength+0,    0, 100, 255);
      setRGB(kLength+1,    0,  50, 127);
      setRGB(kLength+46, 127,  50,   0);
      setRGB(kLength+47, 255, 100,   0);
    } else {
      setRGB(kLength+0,  255, 100,   0);
      setRGB(kLength+1,  127,  50,   0);
      setRGB(kLength+46,   0,  50, 127);
      setRGB(kLength+47,   0, 100, 255);
    }
    setRGB(kLength+51, 255, 100,   0);
    setRGB(kLength+52, 127,  50,   0);
    setRGB(kLength+97,   0,  50, 127);
    setRGB(kLength+98,   0, 100, 255);
    setRGB(kLength+99,   0,   0,   0);
    
    // LED strip dots
    if (d1s) setRGB((int) (kLength+46-Math.floor(d1y)),   0, 100, 255);
    else     setRGB((int) (kLength+52+Math.floor(d1y)),   0, 100, 255);
    
    if (d2s) setRGB((int) (kLength+46-Math.floor(d2y)), 255, 100,   0);
    else     setRGB((int) (kLength+52+Math.floor(d2y)), 255, 100,   0);
    //*/
  
    flush();
  
    i++;
    if (i==200) drop_mode = false;
    if (i==600) {
      drop_mode = true;
      i = 0;
    }
  }
  
  public int pos(int x, int y) {
    if (x<0 || y<0 || x>=16 || y>=16) return -1;
    int nx = 15-x; int ny = 15-y;
    int output = ((int) Math.floor(nx/2))*32;
    if (nx%2==0) {
      output+=ny;
    } else {
      output+=31-ny;
    }
    return output;
  }
  
  public void setRGB(int index, double r, double g, double b) {
    if (index<0 || index>=kTotalLength) return;
    m_ledBuffer.setRGB(index, (int) (r*bright), (int) (g*bright), (int) (b*bright));
    /*if (index >= kLength) {
      m_ledBuffer[index+kLength2].setRGB(r*bright, g*bright, b*bright);
    }*/
  }
  
  public void setRGB(int x, int y, double r, double g, double b) {
    setRGB(pos(x, y), r, g, b);
  }
  
  public void setHSV(int index, double h, double s, double v) {
    if (index<0 || index>=kTotalLength) return;
    m_ledBuffer.setHSV(index, (int) h, (int) s, (int) (v*bright));
    /*if (index >= kLength) {
      m_ledBuffer[index+kLength2].setHSV(h*bright, s*bright, v*bright);
    }*/
  }
  
  public void setHSV(int x, int y, double h, double s, double v) {
    setHSV(pos(x, y), h, s, v);
  }
  
  public void flush() {
    m_led.setData(m_ledBuffer);
  }
  
  public void setAlliance(boolean isRed) {
    m_allianceIsRed = isRed;
  }
}
