import javax.swing.*;
import java.awt.*;
import java.net.*;
import java.net.URL;
import java.io.*;
import java.util.ArrayList;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import javax.swing.table.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

/**
 * This class downloads XML data and parses it.
 */
public class XMLDownloadTask extends SwingWorker<ArrayList<Album>, Album> {
    private String site;                                        // URL of RSS feed

    private XMLDownloadPanel localPanel;                        // reference to XMLDownloadPanel for access to text area
    
    private ArrayList<Album> albumList = new ArrayList<>();     // complete list of albums to be displayed

    XMLDownloadTask(String newSite, XMLDownloadPanel panel) {
        site = newSite;

        localPanel = panel;
    }

    /**
     * Fills an array list with destinations read from a text file.
     *
     * @return  ArrayList<Album>    list of albums collected from RSS feed
     */
    @Override
    public ArrayList<Album> doInBackground() {
        HttpURLConnection connection = null;

        String xmlDataString;

        try {
            // Create a URL object from a String that contains a valid URL
            URL url = new URL(site);

            // Open an HTTP connection for the URL
            connection = (HttpURLConnection) url.openConnection();

            // Set HTTP request method
            connection.setRequestMethod("GET");

            // If the HTTP status code is 200, we have successfully connected
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // Use a mutable StringBuilder to store the downloaded text
                StringBuilder xmlResponse = new StringBuilder();

                // Create a BufferedReader to read the lines of XML from the
                // connection's input stream
                BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                // Read lines of XML and append them to the StringBuilder
                // object until the end of the stream is reached
                String strLine;

                while ((strLine = input.readLine()) != null) {
                    xmlResponse.append(strLine);
                }

                // Convert the StringBuilder object to a String
                xmlDataString = xmlResponse.toString();

                // Close the input stream
                input.close();

                // Create SAX parser.
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser saxParser = factory.newSAXParser();

                // Parse the XML string.
                saxParser.parse(new InputSource(new ByteArrayInputStream(xmlDataString.getBytes("utf-8"))), new AlbumHandler());

                // Create JTable structure.
                String[] columnNames = {"Name", "Artist", "Genre", "Album Cover"};
                Object[][] tableList = new Object[albumList.size()][4];

                int i = 0;      // row counter

                // Populate JTable cells with album data.
                for (Album a : albumList) {
                    tableList[i][0] = a.albumName;
                    tableList[i][1] = a.artistName;
                    tableList[i][2] = a.genre;
                    tableList[i][3] = a.albumCover;

                    i++;
                }

                // Temporary table.
                JTable tempTable = new JTable(new MyDefaultTableModel(tableList, columnNames));

                // Apply temporary table (and its model) to table referencing the main table in XMLDownloadPanel.
                localPanel.albumTable.setModel(tempTable.getModel());

                // Column widths are each a percentage of the window width.
                float[] columnWidthPercentage = {50.0f, 20.0f, 20.0f, 10.0f};

                // Get width of main panel component.
                int tableWidth = localPanel.albumTable.getWidth();

                TableColumn column;

                // Obtain column model of main panel.
                TableColumnModel jTableColumnModel = localPanel.albumTable.getColumnModel();

                // Get number of columns in the main panel.
                int numCols = jTableColumnModel.getColumnCount();

                // Resize each column according to the columnWidthPercentage array.
                for (i = 0; i < numCols; i++) {
                    column = jTableColumnModel.getColumn(i);
                    int pWidth = Math.round(columnWidthPercentage[i] * tableWidth);
                    column.setPreferredWidth(pWidth);
                }
            }
        }
        catch (Exception e) {
        // Handle MalformedURLException
        }
        finally {
            // close connection
            if (connection != null) {
                connection.disconnect();
            }
        }

        return albumList;
    }

    /**
     * Gets and converts image to ImageIcon.
     *
     * @param   sourceImage   single image read from the XML
     *
     * @return  BufferedImage   source image that is now a buffered image
     */
    private BufferedImage getScaledImage(Image sourceImage) {
        // The size of the image we want.
        BufferedImage resizedImage = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);       // image container

        // Create Graphics2D object from resizedImage.
        Graphics2D g2 = resizedImage.createGraphics();

        // Set key properties of image.
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Draw Graphics@D object using passed-in image.
        g2.drawImage(sourceImage, 0, 0, 50, 50, null);

        // Delete Graphics2D object.
        g2.dispose();

        return resizedImage;
    }

    /*
    * This class handles Album data as the SAX Parser parses the Apple XML.
     */
    private class AlbumHandler extends DefaultHandler {
        private boolean bName = false;      // Status if XML tag is found. Default false.
        private boolean bArtist = false;    // Status if XML tag is found. Default false.
        private boolean bImage = false;     // Status if XML tag is found. Default false.

        private String name;        // name of album
        private String artist;      // nName of artist
        private String genre;       // music genre
        private String image;       // album image source URL

        ImageIcon albumCover;       // image of album

        int cat = 0;            // For making sure to only get the first "category" element for "genre".
        int imageCount = 0;     // Keeps track of output images.

        /**
         * Fills an array list with destinations read from a text file.
         *
         * @param   uri      URL of where the XML is being read from
         * @param   localName      name of tag within the XML document.
         * @param   qName      same as localName but with "html:" prepended
         * @param   attributes   a tag's attribute (if it has one)
         */
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            if (qName.equalsIgnoreCase("im:name")) {
                bName = true;
                name = "";
            }

            if (qName.equalsIgnoreCase("im:artist")) {
                bArtist = true;
                artist = "";
            }

            if (qName.equalsIgnoreCase("category")) {
                if (cat == 0)
                    genre = attributes.getValue("label");

                cat++;
            }

            if (qName.equalsIgnoreCase("im:image")) {
                bImage = true;
                image = "";
            }
        }

        // This method may be called multiple times for a given element.
        @Override
        public void characters(char ch[], int start, int length) {
            if (bName)
                name = name + new String(ch, start, length);

            if (bArtist)
                artist = artist + new String(ch, start, length);

            if (bImage)
                image = image + new String(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            if (qName.equalsIgnoreCase("im:name"))
                bName = false;

            if (qName.equalsIgnoreCase("im:artist"))
                bArtist = false;

            if (qName.equalsIgnoreCase("im:image"))
                bImage = false;

            if (qName.equalsIgnoreCase("entry")) {
                try {
                    // Make URL string into a proper URL object.
                    URL url = new URL(image);
                    BufferedImage bufferedImage = ImageIO.read(url);

                    // Obtain user's working directory.
                    String path = System.getProperty("user.dir");

                    // Append images to end of path.
                    path += "/images";

                    // Make path into file.
                    File directory = new File(path);

                    // If path (folder) doesn't exist, make it. This folder will hold images.
                    if (! directory.exists()){
                        directory.mkdir();
                    }

                    // A file named OutputImage%d.png will be created in local directory.
                    ImageIO.write(bufferedImage, "png", new FileOutputStream("images/OutputImage" + imageCount + ".png"));
                } catch(Exception e) {
                    e.printStackTrace();
                }

                // This string will be what images are written into the folder as.
                String fileName = "images/OutputImage" + imageCount + ".png";

                BufferedImage img = null;

                // Read an album image from folder.
                try {
                    img = ImageIO.read(new File(fileName));
                } catch (IOException e) {
                    System.out.println("Can't read image files.");
                }

                imageCount++;

                // Scale image.
                BufferedImage scaledImage = getScaledImage(img);

                // Put scaled image into album field 'albumCover' as an ImageIcon.
                albumCover = new ImageIcon(scaledImage);

                // Compile album name, artist, and genre into an Album object.
                Album album = new Album(name, artist, genre, albumCover);

                // Add the object album to a list, publish it, etc.
                albumList.add(album);

                // Reset category counter.
                cat = 0;
            }
        }
    }

    /*
    * This class is a custom table model for allowing images of ImageIcon type to be included.
     */
    class MyDefaultTableModel extends DefaultTableModel {
        MyDefaultTableModel(Object[][] newArray, String[] newHeaders) {
            setDataVector(newArray, newHeaders);
        }

        @Override
        public Class<?> getColumnClass(int column) {
            switch(column) {
                case 0: return String.class;
                case 1: return String.class;
                case 2: return String.class;
                case 3: return ImageIcon.class;
                default: return Object.class;
            }
        }
    }
}
