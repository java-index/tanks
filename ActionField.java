import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Objects;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

public class ActionField extends JPanel{
    private boolean COLORDED_MODE = true;
    private BattleField bf;
    private Bullet bullet;
    private Tank tank;

    /* directions of motions */
    private final int UP = 0;
    private final int DOWN = 1;
    private final int LEFT = 2;
    private final int RIGHT = 3;

    private int signX = 0;
    private int signY = 0;

    public void runTheGame() throws Exception {
        while(true){
            randomClean();
        }
    }

    public void getQuadrat(int x, int y){

    }

    public void getQuadratXY(int x, int y){

    }

    public void processMove(Tank tank) throws Exception {
        this.tank = tank;
        moveOneStep(tank);
    }

    private void moveOneStep(Tank tank) throws Exception{
        createSign(tank.getDirection());
        checkObstruction(signX, signY);
        tank.updateX(signX);
        tank.updateY(signY);
        repaint();
        Thread.sleep(tank.getSpeed());
    }

    private void moveOneStep(Bullet bullet) throws Exception {
        createSign(bullet.getDirection());
        bullet.updateX(signX);
        bullet.updateY(signY);
        repaint();
        Thread.sleep(bullet.getSpeed());
    }

    private void checkObstruction(int correctX, int correctY) throws Exception {
        if((tank.getX() % bf.getStep() != 0) || (tank.getY() % bf.getStep() != 0)) {
            return;
        }
        int checkQuadratX = getCoordQuadrant(tank.getX()) + correctX;
        int checkQuadratY = getCoordQuadrant(tank.getY()) + correctY;
        if (!bf.scanQuadrant(checkQuadratX, checkQuadratY).equals(" ")) {
            System.out.println("FIRE!");
            tank.fire();
        }
    }

    private void createSign(int direction){
       int[] mask = {1, -1, 1, -1}; // [0][1] for Y; [2][3] for X
       mask[direction] = 0;
       signX = mask[LEFT] + mask[RIGHT]; // x (-1 or 0 or +1)
       signY = mask[UP] + mask[DOWN]; // y (-1 or 0 or +1)
    }

    public void processTurn(Tank tank) throws Exception {
        repaint();
    }

    public void processFire(Bullet bullet) throws Exception {
        this.bullet = bullet;
        while(!bulletOutOfBattleField()) {
            moveOneStep(bullet);
            if (processInterception()) { // hit
                explioson();
            }
            repaint();
        } // while
    }

    private void explioson() {
        int x = getCoordQuadrant(bullet.getX());
        int y = getCoordQuadrant(bullet.getY());
        bf.updateQuadrant(x, y, " ");
        //bullet.destroy();
    }

    private boolean bulletOutOfBattleField() {
        //10 size bullet
        if ((bullet.getX() > -10 && bullet.getX() < bf.getBF_WIDTH() + 10) &&
            (bullet.getY() > -10 && bullet.getY() < bf.getBF_HEIGHT() + 10)) {
            return false;
        } else {
            return true;
        }
    }

    private boolean processInterception() {
        if (bullet.getX() < bf.getBF_WIDTH() && bullet.getY() < bf.getBF_HEIGHT()) {
            int x = getCoordQuadrant(bullet.getX());
            int y = getCoordQuadrant(bullet.getY());
            if (!bf.scanQuadrant(x, y).equals(" ")) {
                return true;
            }
        }
        return false;
    }

    private int getCoordQuadrant(int coordPixel) {
        return (int)(coordPixel / bf.getStep());  // 0 (0-63px); 1 (64-127px) etc.
    }

    private int getCoordPixel (int coordQuadrant) {
        return coordQuadrant * bf.getStep();
    }

    void moveToQuadrantXY(Tank tank, int xQuad, int yQuad) throws Exception {
        int newX = getCoordPixel(xQuad);
        int newY = getCoordPixel(yQuad);

        while (!tankAtNewCoordinate(tank, newX, newY)) {
            int newDirection = defineDirection(tank, newX, newY);
            tank.setDirection(newDirection);
            tank.move();
        }
    }

    private int defineDirection(Tank tank, int x, int y) {
        int direction = -1;
        if (x > tank.getX()) {
            direction = RIGHT;
        }
        if (x < tank.getX()) {
            direction = LEFT;
        }
        if (y > tank.getY()) {
            direction = DOWN;
        }
        if (y < tank.getY()) {
            direction = UP;
        }
        return direction;
    }

    boolean tankAtNewCoordinate(Tank tank, int newX, int newY){
        if (tank.getX() == newX && tank.getY() == newY){
            return true;
        } else {
            return false;
        }
    }

    public void randomClean() throws Exception {
        Random rand = new Random();
        for(int y = 0; y < bf.getDimentionY(); y++){ // y
            int x = rand.nextInt(bf.getDimentionX()); // quadrat x 0-8
            if(bf.scanQuadrant(x, y).equals(" ")){
                continue;
            } else {
                moveToQuadrantXY(tank, x, y);
            } // if
        }// for y
//        if(countBrick == 0){
//            exitGame();
//        }
    }

    public ActionField() throws Exception {
        bf = new BattleField();
        bullet = new Bullet(-100, -100, DOWN);
        tank = new Tank(0, 0, DOWN, bf, this);

        JFrame frame;
        frame = new JFrame("** WORD OF TANKS **");
        frame.setLocation(500, 150);
        frame.setMinimumSize(new Dimension(576, 576 + 22));
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(this);
        frame.pack();
        frame.setVisible(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

		/* create quadrant */
        int i = 0;
        int step = bf.getStep();
        Color cc;
        for (int v = 0; v < bf.getDimentionY(); v++) {
            for (int h = 0; h < bf.getDimentionY(); h++) {
                if (COLORDED_MODE) {
                    if (i % 2 == 0) {
                        cc = new Color(240, 240, 240);
                    } else {
                        cc = new Color(200, 240, 20);
                    }
                } else {
                    cc = new Color(220, 220, 220);
                }
                i++;
                g.setColor(cc);
                g.fillRect(h * step, v * step, step, step);
            } // for h
        } // for v

		/* fill brick */
        for (int j = 0; j < bf.getDimentionY(); j++) { // y
            for (int k = 0; k < bf.getDimentionX(); k++) { // x
                if (bf.scanQuadrant(k, j).equals("B")) {
                    int y = j * step;
                    int x = k * step;

                    g.setColor(new Color(190, 110, 70)); // color briks
                    g.fillRect(x, y, step, step);

					/* draw briks wall */
                    g.setColor(new Color(0, 0, 0));
					/* horizontal line */
                    g.drawLine(x, y+16, x+step-1, y+16);
                    g.drawLine(x, y+32, x+step-1, y+32);
                    g.drawLine(x, y+48, x+step-1, y+48);
                    g.drawLine(x, y+63, x+step-1, y+63);
					/* vertical line */
                    g.drawLine(x+16, y, x+16, y+step-1);
                    g.drawLine(x+32, y, x+32, y+step-1);
                    g.drawLine(x+48, y, x+48, y+step-1);
                    g.drawLine(x+63, y, x+63, y+step-1);
					/* lighter vertical line */
                    g.setColor(new Color(230, 170, 110));
                    g.drawLine(x, y, x, y+step-1);
                    g.drawLine(x+16+1, y, x+16+1, y+step-1);
                    g.drawLine(x+32+1, y, x+32+1, y+step-1);
                    g.drawLine(x+48+1, y, x+48+1, y+step-1);
					/* lighter horizontal line */
                    g.drawLine(x, y, x+step-1, y);
                    g.drawLine(x, y+16+1, x+step-1, y+16+1);
                    g.drawLine(x, y+32+1, x+step-1, y+32+1);
                    g.drawLine(x, y+48+1, x+step-1, y+48+1);
                } // if
            } // for k
        } // for j

		/* draw tank */
        int tankX = tank.getX();
        int tankY = tank.getY();
        g.setColor(new Color(70, 70, 70));
        g.fillRect(tankX+10, tankY+10, 44, 44);
        g.setColor(new Color(130, 130, 130));
        g.fillOval(tankX+15, tankY+15, 34, 34);

        if (tank.getDirection() == UP) {
            g.fillRect(tankX + 26, tankY, 12, 34);
            g.fillRect(tankX+2, tankY+2, 8, 58); // left track
            g.fillRect(tankX+step-10, tankY+2, 8, 58); // right track

        } else if (tank.getDirection() == DOWN) {
            g.fillRect(tankX + 26, tankY + 30, 12, 34);
            g.fillRect(tankX+2, tankY+2, 8, 58); // left track
            g.fillRect(tankX+step-10, tankY+2, 8, 58); // right track

        } else if (tank.getDirection() == LEFT) {
            g.fillRect(tankX, tankY + 26, 34, 12);
            g.fillRect(tankX+2, tankY+2, 58, 8); // left track
            g.fillRect(tankX+2, tankY+step-10, 58, 8); // right track

        } else { // right
            g.fillRect(tankX + 30, tankY + 26, 34, 12);
            g.fillRect(tankX+2, tankY+2, 58, 8); // left track
            g.fillRect(tankX+2, tankY+step-10, 58, 8); // right track
        }

//        g.setColor(new Color(0, 0, 0));
//        if (bullet[POWER] == 1){
//            g.setColor(new Color(255, 0, 0));
//        }
        g.fillOval(bullet.getX(), bullet.getY(), 10, 10);
    }

}