/*
 * Cell.java
 * Author: Max Faulks
 * 30 / 05 / 25
 * Portions of this script were generated or assisted by OpenAI's ChatGPT.
*/
package dungeon.engine;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import java.awt.*;

public class Cell extends StackPane {
    private Item item;
    private boolean isWall;

    public Cell() {
        Rectangle bg = new Rectangle(40, 40);
        bg.setFill(Color.WHITE);
        bg.setStroke(Color.BLACK);
        getChildren().add(bg);
        this.item = null;
        this.isWall = false;
    }

    public void setItem(Item item) {
        this.item = item;
        updateDisplay();
    }

    public Item getItem() {
        return item;
    }

    public void setWall(boolean isWall) {
        this.isWall = isWall;
        updateDisplay();
    }

    public boolean isWall() {
        return isWall;
    }

    private void updateDisplay() {
        getChildren().clear();
        Rectangle bg = new Rectangle(40, 40);

        if (isWall) {
            bg.setFill(Color.BLACK);
            Text text = new Text("#");
            text.setFill(Color.WHITE);
            getChildren().addAll(bg, text);
        } else {
            bg.setFill(Color.WHITE);
            bg.setStroke(Color.BLACK);
            getChildren().add(bg);

            if (item != null) {
                Text text = new Text(item.getSymbol());
                getChildren().add(text);
            }
        }
    }
}
