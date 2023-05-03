package frc.robot.game;

import frc.robot.subsystems.LEDController;

import java.util.Random;

public final class PongGame extends ScreenGame {
    int paddle1y = 6;
    int paddle2y = 6;
    int ballx = 8;
    int bally = 8;
    int ballxv = 1;
    int ballyv = 1;
    int score1 = 0;
    int score2 = 0;
    int i = 0;
    Random random = new Random();

    @Override
    public String getIdentifier() {
        return "pong";
    }

    @Override
    public void init() {
        reset();

        score1 = 0;
        score2 = 0;
    }
    
    @Override
    public void update() {
        i++;
        i%=40;
    
        if (i%10==0) {
          ballx+=ballxv;
          bally+=ballyv;
    
          if (ballxv<0 && ballx>1 && ballx<3 && bally>=paddle1y && bally<paddle1y+4) {
            ballxv*=-1;
          } else if (ballxv>0 && ballx>12 && ballx<14 && bally>=paddle2y && bally<paddle2y+4) {
            ballxv*=-1;
          } else if (ballx<=1) {
            reset();
            score2++;
          } else if (ballx>=14) {
            reset();
            score1++;
          }

          if (ballyv<0 && bally<1) {
            ballyv*=-1;
          } else if (ballyv>0 && bally>14) {
            ballyv*=-1;
          }
        }
    }

    @Override
    public void povUpdate(int con, int dir) {
        if (con==0) {
            switch (dir) {
                case 0: paddle1y--; break;
                case 2: paddle1y++; break;
            }
            if (paddle1y<0)  paddle1y = 0;
            if (paddle1y>12) paddle1y = 12;
        } else if (con==1) {
            switch (dir) {
                case 0: paddle2y--; break;
                case 2: paddle2y++; break;
            }
            if (paddle2y<0)  paddle2y = 0;
            if (paddle2y>12) paddle2y = 12;
        }
    }

    @Override
    public void draw(LEDController controller) {
        controller.clear();
    
        // paddles
        for (int yo = 0; yo<4; yo++) {
            controller.setRGB(1, paddle1y+yo, 255, 0, 0);
            controller.setRGB(14, paddle2y+yo, 0, 0, 255);
        }
    
        controller.setRGB(ballx, bally, 255, 255, 255);
    
        controller.pulse(255, 255, 255, 40);
    
        for (int i = 0; i < score1; i++) {
            controller.setRGB(controller.kLength+i, 255, 0, 0);
            controller.setRGB(controller.kLength+51+i, 255, 0, 0);
        }
    
        for (int i = 0; i < score2; i++) {
            controller.setRGB(controller.kLength+47-i, 0, 0, 255);
            controller.setRGB(controller.kLength+99-i, 0, 0, 255);
        }
    
        controller.flush();
    }

    private void reset() {
        paddle1y = 6;
        paddle2y = 6;
        ballx = 8;
        bally = 8;
        ballxv = random.nextDouble()<0.5?-1:1;
        ballyv = random.nextDouble()<0.5?-1:1;
    }

    @Override
    public boolean isFinished() {
        return false;
    }
}
