package xyz.topplekek.storybook;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;

/**
 * An object representing a story in .storybook format.
 * See STORYBOOK.md for information on this format.
 * @author Braeden Hong
 * @since 12-06-2020
 */
public class Story {
    private ArrayList<Page> pages;
    private String title;
    private String author;
    private int currentPage;

    /**
     * Construct a new blank story.
     * @param title The title of this story
     * @param author The author of this story
     */
    public Story(String title, String author) {
        this.title = title;
        this.author = author;
        currentPage = 0;
        pages = new ArrayList<>();
    }

    /**
     * Construct a new story from a .storybook file.
     * Will read garbage data if the format is malformed.
     * @param input An input stream of a .storybook file.
     */
    public Story(FileInputStream input) throws IOException {
        StringBuilder titleBuilder = new StringBuilder();
        StringBuilder authorBuilder = new StringBuilder();
        byte b;

        // Read the whole title string
        while ((b = (byte) input.read()) != '\0')
            titleBuilder.append((char) b);

        title = titleBuilder.toString();

        // Read the whole author string
        while ((b = (byte) input.read()) != '\0')
            authorBuilder.append((char) b);

        author = authorBuilder.toString();

        byte[] numPagesBuffer = new byte[2];
        input.read(numPagesBuffer);

        short numPages = (short) (((numPagesBuffer[0] & 0xFF) << 8) | (numPagesBuffer[1] & 0xFF));

        pages = new ArrayList<>();

        // Read all pages
        for (int i = 0; i < numPages; i++) {
            String image = null, text = null;
            int imageLen, textLen;
            short choice1, choice2;

            // Read each choice (uint16)
            byte[] choiceBuffer = new byte[2];
            input.read(choiceBuffer);
            choice1 = (short) (((choiceBuffer[0] & 0xFF) << 8) | (choiceBuffer[1] & 0xFF));
            input.read(choiceBuffer);
            choice2 = (short) (((choiceBuffer[0] & 0xFF) << 8) | (choiceBuffer[1] & 0xFF));

            // Read the length of the image (uint32)
            byte[] lengthBuffer = new byte[4];
            input.read(lengthBuffer);
            imageLen = ((lengthBuffer[0] & 0xFF) << 24) |
                       ((lengthBuffer[1] & 0xFF) << 16) |
                       ((lengthBuffer[2] & 0xFF) << 8) |
                       lengthBuffer[3] & 0xFF;

            // Decompress the image if it exists
            if (imageLen > 0) {
                byte[] imageBuffer = new byte[imageLen];
                input.read(imageBuffer);
                image = new String(decompress(imageBuffer));
            }

            // Read the length of the text (uint32)
            input.read(lengthBuffer);
            textLen = ((lengthBuffer[0] & 0xFF) << 24) |
                      ((lengthBuffer[1] & 0xFF) << 16) |
                      ((lengthBuffer[2] & 0xFF) << 8) |
                      lengthBuffer[3] & 0xFF;

            // Decompress the text if it exists
            if (textLen > 0) {
                byte[] textBuffer = new byte[textLen];
                input.read(textBuffer);
                text = new String(decompress(textBuffer));
            }

            pages.add(new Page(text, image, choice1, choice2));
        }

        currentPage = 0;
    }

    /**
     * Add a new page to the story.
     * @param p The page to add
     */
    public void addPage(Page p) {
        pages.add(p);
    }

    /**
     * Set the title of the story.
     * @param title The new title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Set the author of the story.
     * @param author The new author of the story
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Get the page at a specific index.
     * @param i The index of the page
     * @return The page
     */
    public Page getPageAt(int i) {
        return pages.get(i);
    }

    /**
     * Get the title of the story.
     * @return The title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Get the author of the story.
     * @return The author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Get the index of the current page.
     * @return The index of the current page
     */
    public int getCurrentPageIndex() {
        return currentPage;
    }

    /**
     * Turn the story to a specific page.
     * @param i The index of the page to turn to
     * @return The page that was turned to
     */
    public Page turnToPage(int i) {
        currentPage = i;
        return pages.get(i);
    }

    /**
     * Get the size of the story (number of pages).
     * @return The number of pages in the story
     */
    public int size() {
        return pages.size();
    }

    /**
     * Save the story to the provided output stream in .storybook format.
     * @param output A stream to output the data to.
     */
    public void saveStory(FileOutputStream output) throws IOException {
        output.write(title.getBytes());
        output.write('\0');
        output.write(author.getBytes());
        output.write('\0');

        output.write((pages.size() & 0xFF00) >>> 8);
        output.write(pages.size() & 0xFF);

        // Write each page
        for (int i = 0; i < pages.size(); i++) {
            Page p = pages.get(i);

            // Write the choices (uint16)
            output.write((p.getChoice1() & 0xFF00) >>> 8);
            output.write(p.getChoice1() & 0xFF);
            output.write((p.getChoice2() & 0xFF00) >>> 8);
            output.write(p.getChoice2() & 0xFF);

            if (p.getImage() == null) {
                // Wite a uint32 of 0's
                output.write(0);
                output.write(0);
                output.write(0);
                output.write(0);
            } else {
                byte[] compressed = compress(p.getImage().getBytes());

                // Write compressed size (uint32)
                output.write((compressed.length & 0xFF000000) >>> 24);
                output.write((compressed.length & 0xFF0000) >>> 16);
                output.write((compressed.length & 0xFF00) >>> 8);
                output.write(compressed.length & 0xFF);

                output.write(compressed);
            }

            byte[] compressed = compress(p.getText().getBytes());

            // Write compressed size (uint32)
            output.write((compressed.length & 0xFF000000) >>> 24);
            output.write((compressed.length & 0xFF0000) >>> 16);
            output.write((compressed.length & 0xFF00) >>> 8);
            output.write(compressed.length & 0xFF);

            output.write(compressed);
        }

        output.flush();
        output.close();
    }

    /**
     * Compress a byte array with ZLIB compression.
     * @param data The data to compress
     * @return The compressed data
     */
    private byte[] compress(byte[] data) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        DeflaterOutputStream compressedOut = new DeflaterOutputStream(output);

        compressedOut.write(data);
        compressedOut.close();

        return output.toByteArray();
    }

    /**
     * Decompress a byte array with ZLIB compression.
     * @param data The data to decompress
     * @return The decompressed data
     */
    private byte[] decompress(byte[] data) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        InflaterOutputStream decompressedOut = new InflaterOutputStream(output);

        decompressedOut.write(data);
        decompressedOut.close();

        return output.toByteArray();
    }
}
