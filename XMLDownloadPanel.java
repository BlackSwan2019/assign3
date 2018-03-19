import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.SwingWorker.StateValue;
import javax.swing.table.*;
import java.util.Timer;
import java.util.TimerTask;

class XMLDownloadPanel extends JPanel {
    // task that will download and parse XML and display it in the JTextArea
    private XMLDownloadTask task;

    // primary UI elements
    JTextArea albumList = new JTextArea();
    private JButton getAlbums = new JButton("Get Albums");
    private JPanel buttonPanel = new JPanel();
    private JScrollPane albumScroll;

    JTable albumTable = new JTable() {
        private static final long serialVersionUID = 1L;

        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    // menu strings for URL building
    private String type = "";
    private String limit = "";
    private String explicit = "";

    // timer variables and output field
    private int minutes = 0;
    private int ones = 0;
    private int tens = 0;
    private JLabel timeOutput = new JLabel();

    XMLDownloadPanel() {
        this.setLayout(new BorderLayout());

        this.add(buttonPanel, BorderLayout.PAGE_START);
        getAlbums.addActionListener(new albumButtonListener());

        buttonPanel.add(getAlbums, BorderLayout.CENTER);
        getAlbums.setPreferredSize(getAlbums.getPreferredSize());
        buttonPanel.add(new JLabel("Elapsed Time: "));
        buttonPanel.add(timeOutput);
        timeOutput.setText(Integer.toString(minutes) + ":" + Integer.toString(ones) + Integer.toString(tens));

        // Set up JTable properties.
        albumTable.setShowGrid(false);
        albumTable.setFillsViewportHeight(true);
        albumTable.setFont(new Font("Helvetica Neue", Font.BOLD, 12));
        albumTable.setRowHeight(50);

        albumScroll = new JScrollPane(albumTable);

        //albumScroll = new JScrollPane(albumList);

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
            albumTable.setModel(new DefaultTableModel());

            download();
        }
    }

    private void download() {
        String name = "";

        if (!type.equals("") && !limit.equals("") && !explicit.equals("")) {
            // Build URL string.
            name = "https://rss.itunes.apple.com/api/v1/us/itunes-music/" +  type + "/all/" + limit + "/" + explicit + ".atom";
        }
        else {
            albumList.setText("Missing menu selection(s).");
        }

        task = new XMLDownloadTask(name, this);

        Timer timer = new Timer();

        task.addPropertyChangeListener(event -> {
            switch ((StateValue) event.getNewValue()) {
                case STARTED:
                    getAlbums.setText("Working...");
                    getAlbums.setEnabled(false);
                    minutes = 0;
                    ones = 0;
                    tens = 0;
                    timer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            timeOutput.setText(Integer.toString(minutes) + ":" + Integer.toString(ones) + Integer.toString(tens));
                            tens++;
                            if (tens == 10) {
                                ones++;
                                tens = 0;
                            }
                            if (ones == 6) {
                                minutes++;
                                ones = 0;
                            }
                        }
                    }, 0, 1000);
                    break;
                case DONE:
                    getAlbums.setText("Get Albums");
                    getAlbums.setEnabled(true);
                    timer.cancel();
                    timer.purge();
                    break;
            }
        });

        task.execute();
    }
}
