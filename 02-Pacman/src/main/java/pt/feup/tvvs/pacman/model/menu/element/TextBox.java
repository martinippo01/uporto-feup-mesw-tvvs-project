package pt.feup.tvvs.pacman.model.menu.element;

import com.googlecode.lanterna.TextColor;
import pt.feup.tvvs.pacman.model.Element;
import pt.feup.tvvs.pacman.model.Position;

public class TextBox extends Element {
    private String text;
    private TextColor color;

    public TextBox(String text, Position pos, TextColor color) {
        super(pos);
        this.text = text;
        this.color = color;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public TextColor getColor() {
        return color;
    }

    public void setColor(TextColor color) {
        this.color = color;
    }
}
