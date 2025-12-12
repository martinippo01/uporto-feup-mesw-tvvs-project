package pt.feup.tvvs.pacman.model.menu;

import com.googlecode.lanterna.TextColor;
import pt.feup.tvvs.pacman.model.menu.element.TextBox;

import java.util.List;

public abstract class Menu {
    private final TextBox title;
    private List<TextBox> options;
    private int selectedOption;

    public Menu() {
        selectedOption = 0;
        this.title = createTitle();
    }

    public List<TextBox> getOptions() {
        return options;
    }

    public void setOptions(List<TextBox> options) {
        this.options = options;
        if (!options.isEmpty()) {
            options.forEach(textBox -> textBox.setColor(new TextColor.RGB(255, 255, 255)));
            options.get(0).setColor(new TextColor.RGB(255, 255, 0));
            selectedOption = 0;
        }
    }

    public void initializeOptions() {
        setOptions(createOptions());
    }

    public int getSelectedOption() {
        return selectedOption;
    }

    public void setSelectedOption(int selectedOption) {
        this.selectedOption = selectedOption;
    }

    public void selectNextOption() {
        options.get(selectedOption).setColor(new TextColor.RGB(255, 255, 255));
        if (++selectedOption >= options.size()) selectedOption = 0;
        options.get(selectedOption).setColor(new TextColor.RGB(255, 255, 0));
    }

    public void selectPreviousOption() {
        options.get(selectedOption).setColor(new TextColor.RGB(255, 255, 255));
        if (--selectedOption < 0) selectedOption = options.size() - 1;
        options.get(selectedOption).setColor(new TextColor.RGB(255, 255, 0));
    }

    protected abstract List<TextBox> createOptions();

    protected abstract TextBox createTitle();

    public TextBox getTitle() {
        return title;
    }
}
