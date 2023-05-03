package frc.robot.game;

import frc.robot.subsystems.LEDController;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.wpi.first.math.Pair;

public final class SnakeGame extends ScreenGame {
    List<Pair<Integer,Integer>> snake_points = new ArrayList<>();
    Pair<Integer,Integer> apple = new Pair<Integer,Integer>(0, 0);
    Pair<Integer,Integer> applev = new Pair<Integer,Integer>(0, 0);
    int snakeDir = 0;
    Random random = new Random();
    int i = 0;
    int gameOverTimer = 0;

    @Override
    public String getIdentifier() {
        return "snake";
    }

    @Override
    public void init() {
        snake_points.clear();
        snake_points.add(Pair.of(8,8));
        snakeDir = 1;
        resetApple();
    }
    
    @Override
    public void update() {
        if (gameOverTimer>0) {
            gameOverTimer--;
            if (gameOverTimer==0) {
                init();
            }
            return;
        }

        i++;
        i%=50;
    
        if (i%5==0) {
            int x = snake_points.get(0).getFirst();
            int y = snake_points.get(0).getSecond();

            if (snakeDir==0) y--;
            if (snakeDir==1) x++;
            if (snakeDir==2) y++;
            if (snakeDir==3) x--;

            if (i%25==0) {
                int ax = apple.getFirst() + applev.getFirst();
                if (ax<0) ax=0; if (ax>15) ax=15;
                int ay = apple.getSecond() + applev.getSecond();
                if (ay<0) ay=0; if (ay>15) ay=15;
                apple = Pair.of(ax, ay);
            }

            if (x<0 || x>=16 || y<0 || y>=16 || inSnake(x,y)) {
                gameOverTimer = 50;
            } else {
                snake_points.add(0,Pair.of(x,y));

                if (x==apple.getFirst() && y==apple.getSecond()) {
                    resetApple();
                } else {
                    snake_points.remove(snake_points.size()-1);
                }
            }
        }
    }

    @Override
    public void povUpdate(int con, int dir) {
        if (con==0) {
            if (dir%2 != snakeDir%2) snakeDir = dir;
        } else if (con==1) {
            switch (dir) {
                case 0: applev = Pair.of(-1,0); break;
                case 1: applev = Pair.of(0,1); break;
                case 2: applev = Pair.of(1,0); break;
                case 3: applev = Pair.of(0,-1); break;
            }
        }
    }

    @Override
    public void draw(LEDController controller) {
        controller.clear();
      
        if (gameOverTimer>0) {
            controller.drawLetter('G', 0,  1);
            controller.drawLetter('A', 4,  1);
            controller.drawLetter('M', 8,  1);
            controller.drawLetter('E', 12, 1);
            
            controller.drawLetter('O', 0,  9);
            controller.drawLetter('V', 4,  9);
            controller.drawLetter('E', 8,  9);
            controller.drawLetter('R', 12, 9);
        
            controller.pulse(127, 0, 0, 50);
        } else {
            // snake
            for (int n = 0; n<snake_points.size(); n++) {
                int x = snake_points.get(n).getFirst();
                int y = snake_points.get(n).getSecond();
            
                //setRGB(x, y, 0, 127, 0);
                controller.setRGB(x, y, (31*x)%256, (41*y)%256, (49*n)%256);
            }
            
            int x = apple.getFirst();
            int y = apple.getSecond();
            
            controller.setRGB(x, y, 127, 0, 0);
            
            controller.pulse(0, 127, 0, 50);
        }
        
        controller.flush();
    }
  
    private void resetApple() {
      int x,y;
      do {
        x = random.nextInt()%16;
        y = random.nextInt()%16;
    
        if (x<0) x=-x;
        if (y<0) y=-y;
      } while (inSnake(x,y));
  
      apple = Pair.of(x,y);
      applev = Pair.of(0, 0);
    }
  
    private boolean inSnake(int x, int y) {
      for (var item : snake_points) {
        if (x == item.getFirst() && y == item.getSecond()) return true;
      }
      return false;
    }

    @Override
    public boolean isFinished() {
        return false;
    }
}
