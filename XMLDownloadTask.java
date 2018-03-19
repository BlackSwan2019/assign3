import javax.swing.*;
import java.net.*;
import java.net.URL;
import java.io.*;
import java.util.ArrayList;

import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.swing.table.*;
import java.awt.Component;

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

                // Create SAX parser.
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser saxParser = factory.newSAXParser();

                // Parse the XML string.
                saxParser.parse(new InputSource(new ByteArrayInputStream(xmlDataString.getBytes("utf-8"))), new AlbumHandler());

                String[] columnNames = {"Name", "Artist", "Genre"};
                Object[][] tableList = new String[albumList.size()][3];

                int i = 0;

                for (Album a : albumList) {
                    tableList[i][0] = a.albumName;
                    tableList[i][1] = a.artistName;
                    tableList[i][2] = a.genre;

                    i++;
                }

                JTable tempTable = new JTable(tableList, columnNames);

                localPanel.albumTable.setModel(tempTable.getModel());

                JTableHeader header = localPanel.albumTable.getTableHeader();
                header.setDefaultRenderer(new HeaderRenderer(localPanel.albumTable));

                float[] columnWidthPercentage = {50.0f, 30.0f, 20.0f};

                int tableWidth = localPanel.albumTable.getWidth();

                TableColumn column;

                TableColumnModel jTableColumnModel = localPanel.albumTable.getColumnModel();

                int numCols = jTableColumnModel.getColumnCount();

                System.out.println(numCols);

                for (i = 0; i < numCols; i++) {
                    column = jTableColumnModel.getColumn(i);
                    int pWidth = Math.round(columnWidthPercentage[i] * tableWidth);
                    column.setPreferredWidth(pWidth);
                }

                // Close the input stream
                input.close();
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

    private class AlbumHandler extends DefaultHandler {
        private boolean bName = false;
        private boolean bArtist = false;

        private String name;
        private String artist;
        private String genre;
        private ImageIcon albumCover;

        int cat = 0;    // For making sure to only get the first "category" element for "genre".

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
        }

        // This method may be called multiple times for a given element.
        @Override
        public void characters(char ch[], int start, int length) {
            if (bName)
                name = name + new String(ch, start, length);

            if (bArtist)
                artist = artist + new String(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            if (qName.equalsIgnoreCase("im:name"))
                bName = false;

            if (qName.equalsIgnoreCase("im:artist"))
                bArtist = false;

            if (qName.equalsIgnoreCase("entry")) {
                // Compile album name, artist, and genre into an Album object.
                Album album = new Album(name, artist, genre);

                // Add the object album to a list, publish it, etc.
                albumList.add(album);

                // Reset category counter.
                cat = 0;
            }
        }
    }

    private static class HeaderRenderer implements TableCellRenderer {
        DefaultTableCellRenderer renderer;

        HeaderRenderer(JTable table) {
            renderer = (DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer();
            renderer.setHorizontalAlignment(JLabel.LEFT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            return renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
        }
    }

    class MyDefaultTableModel extends DefaultTableModel {
        @Override
        public Class<?> getColumnClass(int column) {
            switch(column) {
                case 0:
                case 1: return String.class;
                case 2: return String.class;
                case 3: return ImageIcon.class;
                default: return Object.class;
            }
        }
    }
}
