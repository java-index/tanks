package game;

import game.bf_objects.*;
import game.bf_objects.tanks.*;
import game.bf_objects.tanks.Action;
import game.view.MainFrame;
import game.view.StartGamePanel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.*;

public class ActionField extends JPanel {

    private boolean pause;
    private boolean startGame;

    private BattleField bf;
    private Bullet bullet;
    private Tank deffender;
    private Tank agressor;

    private JPanel cards;
    private JPanel gameOverPanel;
    private JPanel startGamePanel;
    private MainFrame mainFrame;

    final static String BF_PANEL = "BF_PANEL";
    final static String START_PANEL = "START_PANEL";

    private int signX = 0;
    private int signY = 0;

    private ConcurrentLinkedQueue<ActionEntity> ActionslinkedQueue;

    private void processAction() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    ActionEntity actionEntity = getActionFromQueue();
                    if (actionEntity == null || pause) continue;

                    if (actionEntity.getAction() == Action.MOVE) {
                        processMove(actionEntity.getTank());
                    } else if (actionEntity.getAction() == Action.FIRE) {
                        processFire(actionEntity.getTank());
                    }
                }
            }
        }).start();
    }

    private void pingTank(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                addActionToQueue(agressor.setUp(), agressor);
                }
            }
        }).start();
    }

    private void addActionToQueue(Action action, Tank tank){
        ActionslinkedQueue.add(new ActionEntity(tank, action));
    }

    private ActionEntity getActionFromQueue(){
        return  ActionslinkedQueue.poll();
    }

    public void processMove(Tank tank) {
        moveOneStep(tank);
    }

    private void moveOneStep(Tank tank) {
        createSign(tank.getDirection());
        checkInterception(tank, signX, signY);
        tank.updateX(signX);
        tank.updateY(signY);
        repaint();
        sleep(tank.getSpeed());
    }

    private void moveOneStep(Bullet bullet) {
        createSign(bullet.getDirection());
        bullet.updateX(signX);
        bullet.updateY(signY);
        repaint();
        sleep(bullet.getSpeed());
    }

    private void createSign(Direction direction) {
        int[] mask = {1, -1, 1, -1}; // [0][1] for Y; [2][3] for X
        mask[direction.ordinal()] = 0;
        signX = mask[2] + mask[3]; // x (-1 or 0 or +1)
        signY = mask[0] + mask[1]; // y (-1 or 0 or +1)
    }

    private void checkInterception(Tank tank, int correctX, int correctY) {
        if ((tank.getX() % bf.getStep() != 0) || (tank.getY() % bf.getStep() != 0)) {
            return;
        }
        int checkQuadratX = getCoordQuadrant(tank.getX()) + correctX;
        int checkQuadratY = getCoordQuadrant(tank.getY()) + correctY;
        if (!bf.scanQuadrant(checkQuadratX, checkQuadratY).isDestroyed()) {
            System.out.println("FIRE!");
            processFire(tank);
        }
    }

    public void processTurn(Tank tank) throws Exception {
        repaint();
    }

    public void processFire(Tank bulletOwner) {
        this.bullet = bulletOwner.fire();
        this.bullet.setOwner(bulletOwner);
        while (!bulletOutOfBattleField()) {
            moveOneStep(bullet);
            if (processInterception(bulletOwner)) { // hit
                destroy();
            }
            repaint();
        } // while
    }

    private boolean processInterception(Tank bulletOwner) {
        int x = getCoordQuadrant(bullet.getX());
        int y = getCoordQuadrant(bullet.getY());
        if (!bf.scanQuadrant(x, y).isDestroyed() || checkInterceptionTank(bullet.getX(), bullet.getY() )) {
            return true;
        }
        return false;
    }

    private boolean checkInterceptionTank(int bulletX, int bulletY) {
        return (bulletX >= agressor.getX() && bulletY >= agressor.getY() &&
                bulletX <= agressor.getX()+bf.getStep() && bulletY <= agressor.getY()+bf.getStep());
    }

    private void destroy() {
        int x = getCoordQuadrant(bullet.getX());
        int y = getCoordQuadrant(bullet.getY());
        if (checkInterceptionTank(bullet.getX(), bullet.getY() )) {
            agressor.updateArmor(-1);
            if(agressor.getArmor() == 0) agressor.destroy();
        } else {
            bf.scanQuadrant(x, y).destroy();
        }
        bullet.destroy();
    }

    private boolean bulletOutOfBattleField() {
        //number 10 is size bullet
        if ((bullet.getX() > -10 && bullet.getX() < bf.getBF_WIDTH()-1) &&
                (bullet.getY() > -10 && bullet.getY() < bf.getBF_HEIGHT()-1)) {
            return false;
        } else {
            return true;
        }
    }

    public int getCoordQuadrant(int coordPixel) {
        return (int) (coordPixel / bf.getStep());  // 0 (0-63px); 1 (64-127px) etc.
    }

    public int getCoordPixel(int coordQuadrant) {
        return coordQuadrant * bf.getStep();
    }

    void moveToQuadrantXY(int xQuad, int yQuad, Tank t) throws Exception {
        int newX = getCoordPixel(xQuad);
        int newY = getCoordPixel(yQuad);

        while (!tankAtNewCoordinate(newX, newY, t)) {
            Direction newDirection = defineDirection(newX, newY, t);
            //setDirection(newDirection);
            //processMove(t);
        }
    }

    private Direction defineDirection(int newX, int newY, Tank tank) {
        Direction newDirection = Direction.NONE;

        if (newX > tank.getX()) {
            newDirection = Direction.RIGHT;
        }
        if (newX < tank.getX()) {
            newDirection = Direction.LEFT;
        }
        if (newY > tank.getY()) {
            newDirection = Direction.DOWN;
        }
        if (newY < tank.getY()) {
            newDirection = Direction.UP;
        }
        return newDirection;
    }

    boolean tankAtNewCoordinate(int newX, int newY, Tank tank){
        return (tank.getX() == newX && tank.getY() == newY);
    }

    /***** change panels ******/

    private void selectActivePanel(String activePanel){
        CardLayout cl = (CardLayout)cards.getLayout();
        cl.show(cards, activePanel);
    }
    private void setActiveBFpanel(boolean active){

    }

    private void setStartGamePanel(boolean active){

    }

    private void startGame() {
        System.out.println("start");
        if (startGame){
            return;
        } else {
            this.startGame = true;
        }
        selectActivePanel(BF_PANEL);
        pingTank();
        processAction();
    }

    private void stopGame(){
        System.out.println("stop");
        this.startGame = false;
        selectActivePanel(START_PANEL);
    }

    private void pauseGame(){
        System.out.println("pause");
        pause = !pause;
    }

    private void exitGame(){
        System.exit(0);
    }

    private void sleep(int time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            throw new RuntimeException("sleep interrupted");
        }
    }

    /***** init section *****/

    public ActionField() throws Exception {
        renderSplashScreen();
        initBFObjects();
        initViews();
        initFrame();
        this.ActionslinkedQueue = new ConcurrentLinkedQueue<>();
    }

    private void initBFObjects(){
        bf = new BattleField();
        bullet = new Bullet(-100, -100, Direction.DOWN);

        TankFactory tankFactory = new TankFactory(bf);
        deffender = tankFactory.createTank(TypeTank.DEFFENDER);
        agressor = tankFactory.createTank(TypeTank.AGRESSOR);
    }

    private void setTankPosition(Tank tank){


//        tank.setX();
//        tank.setY();
    }

    private void initViews() {
        this.cards = new JPanel(new CardLayout());
        this.cards.add(new StartGamePanel(), START_PANEL);
        this.cards.add(this, BF_PANEL);
    }

    private void initFrame(){
        mainFrame = new MainFrame();
        mainFrame.addFrameMenuListener(new menuFrameListener());
        mainFrame.addKeyControlListener(new KeyControlListener());
        mainFrame.setContentPane(cards);
    }

    private void renderSplashScreen() throws NullPointerException{
        SplashScreen splash = SplashScreen.getSplashScreen();
        if (splash == null) {
            throw new NullPointerException("SplashScreen.getSplashScreen() returned null");
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        bf.draw(g);
        deffender.draw(g);
        agressor.draw(g);
        bullet.draw(g);
    }


    class KeyControlListener implements KeyListener {

        private boolean buttonPressed = false;

        public void keyTyped(KeyEvent e){
            System.out.println("event" + e.getKeyCode());
        }

        public void keyPressed(KeyEvent e){
            Action action = Action.NONE;

            switch(e.getKeyCode()){
                case KeyEvent.VK_LEFT:
                    deffender.setDirection(Direction.LEFT);
                    action = Action.MOVE;
                    break;
                case KeyEvent.VK_UP:
                    deffender.setDirection(Direction.UP);
                    action = Action.MOVE;
                    break;
                case KeyEvent.VK_RIGHT:
                    deffender.setDirection(Direction.RIGHT);
                    action = Action.MOVE;
                    break;
                case KeyEvent.VK_DOWN:
                    deffender.setDirection(Direction.DOWN);
                    action = Action.MOVE;
                    break;
            }
            addActionToQueue(action, deffender);
        }

        public void keyReleased(KeyEvent e){
            buttonPressed = false;
        }
    }

    class ActionEntity{
        private Tank tank;
        private Action action;

        public ActionEntity(Tank tank, Action action){
            this.tank = tank;
            this.action = action;
        }

        public Tank getTank() {
            return tank;
        }

        public Action getAction() {
            return action;
        }
    }

    class menuFrameListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            switch (e.getActionCommand()){
                case ("start"):
                    startGame();
                    break;
                case ("stop"):
                    stopGame();
                    break;
                case ("pause"):
                    pauseGame();
                    break;
                case ("quit"):
                    exitGame();
            }
        }
    }
}
