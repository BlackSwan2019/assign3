import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.SwingWorker.StateValue;

class XMLDownloadPanel extends JPanel {
    private XMLDownloadTask task;

    JTextArea albumList = new JTextArea();
    private JButton getAlbums = new JButton("Get Albums");
    private JPanel buttonPanel = new JPanel();
    private JScrollPane albumScroll;

    private String type = "";
    private String limit = "";
    private String explicit = "";

    XMLDownloadPanel() {
        this.setLayout(new BorderLayout());

        this.add(buttonPanel, BorderLayout.PAGE_START);
        getAlbums.addActionListener(new albumButtonListener());

        buttonPanel.add(getAlbums, BorderLayout.CENTER);

        albumScroll = new JScrollPane(albumList);

        this.add(albumScroll, BorderLayout.CENTER);
    }

    void setType(String newType) {
        type = newType;
    }

    void setLimit(String newLimit) { limit = newLimit; }

    void setExplicit(String newExplicit) {
        explicit = newExplicit;
    }

    class albumButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            albumList.setText("");

            download();
        }
    }

    private void download() {
        String name = "";

        if (!type.equals("") && !limit.equals("") && !explicit.equals("")) {
            //Build URL string.
            name = "https://rss.itunes.apple.com/api/v1/us/itunes-music/" +  type + "/all/" + limit + "/" + explicit + ".atom";
        }
        else {
            albumList.setText("Missing menu selection(s).");
        }

        task = new XMLDownloadTask(name, this);

        task.addPropertyChangeListener(event -> {
            switch ((StateValue) event.getNewValue()) {
                case STARTED:
                    getAlbums.setText("Working...");
                    getAlbums.setEnabled(false);
                    break;
                case DONE:
                    getAlbums.setText("Get Albums");
                    getAlbums.setEnabled(true);
                    break;
            }
        });

        task.execute();
    }
}
