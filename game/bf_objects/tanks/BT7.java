package game.bf_objects.tanks;

import game.*;
import game.bf_objects.*;

import java.awt.*;

public class BT7 extends AbstractTank {
    public BT7(int x, int y, Direction tankDirection, BattleField bf){
        super(x, y, tankDirection, bf);
        speed -= 4;
        this.tankColor = new Color(205, 178, 83);
    }


    @Override
    public Action setUp() {
        return Action.MOVE_RIGHT;
    }

    @Override
    public String getNameTank() {
        return "BT7";
    }
}
