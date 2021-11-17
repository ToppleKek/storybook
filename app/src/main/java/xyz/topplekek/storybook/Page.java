package xyz.topplekek.storybook;

/**
 * A page in the storybook.
 * @author Braeden Hong
 * @since 12-06-2020
 */
public class Page {
    private String text, image;
    private int choice1, choice2;

    /**
     * Construct a new page.
     * @param text The page text
     * @param image The base64 string of the page image
     * @param choice1 The 1st choice
     * @param choice2 The 2nd choice
     */
    public Page(String text, String image, int choice1, int choice2) {
        this.text = text;
        this.image = image;
        this.choice1 = choice1;
        this.choice2 = choice2;
    }

    /**
     * Construct a new page with default values.
     */
    public Page() {
        this.text = "Page text";
    }

    /**
     * Get the page text.
     * @return The page text
     */
    public String getText() {
        return text;
    }

    /**
     * Get the page base64 image string.
     * @return The image string
     */
    public String getImage() {
        return image;
    }

    /**
     * Get the first choice.
     * @return The first choice
     */
    public int getChoice1() {
        return choice1;
    }

    /**
     * Get the second choice.
     * @return The second choice
     */
    public int getChoice2() {
        return choice2;
    }

    /**
     * Set the text for this page.
     * @param text The new text
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Set the first choice for this page.
     * @param choice1 The first choice
     */
    public void setChoice1(int choice1) {
        this.choice1 = choice1;
    }

    /**
     * Set the second choice for this page.
     * @param choice2 The second choice
     */
    public void setChoice2(int choice2) {
        this.choice2 = choice2;
    }

    /**
     * Set the baes64 image string for this page.
     * @param image The image string
     */
    public void setImage(String image) {
        this.image = image;
    }
}
