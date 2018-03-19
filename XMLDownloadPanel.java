import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.SwingWorker.StateValue;
import javax.swing.table.*;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.table.DefaultTableCellRenderer;

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
        //getAlbums.setBackground(new Color(255, 255, 255));

        buttonPanel.add(getAlbums, BorderLayout.CENTER);
        getAlbums.setPreferredSize(getAlbums.getPreferredSize());
        buttonPanel.add(new JLabel("Elapsed Time: "));
        buttonPanel.add(timeOutput);
        timeOutput.setText(Integer.toString(minutes) + ":" + Integer.toString(ones) + Integer.toString(tens));

        // Set up JTable properties.
        albumTable.setShowGrid(false);
        albumTable.setFillsViewportHeight(true);
        albumTable.getTableHeader().setFont(new Font("Helvetica Neue", Font.BOLD, 11));
        albumTable.setFont(new Font("Helvetica Neue", Font.BOLD, 12));
        albumTable.setRowHeight(55);
        albumTable.setRowSelectionAllowed(false);

        // Make column labels left-justified.
        albumTable.getTableHeader().setDefaultRenderer(new HeaderRenderer(albumTable));

        // Make rows colors alternate between blue and white. Also, get rid of selected cell border.
        albumTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                final Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                Color blue = new Color(242, 242, 242);

                c.setBackground(row % 2 == 0 ? blue : Color.WHITE);

                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(noFocusBorder);

                return c;
            }
        });

        albumScroll = new JScrollPane(albumTable);

        //albumScroll = new JScrollPane(albumList);

        this.add(albumScroll, BorderLayout.CENTER);
    }

    void setType(String newType) {
        type = newType;
    }

    void setLimit(String newLimit) {
        limit = newLimit;
    }

    void setExplicit(String newExplicit) {
        explicit = newExplicit;
    }

    class albumButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            // Reset table to blank.
            albumTable.setModel(new DefaultTableModel());

            download();
        }
    }

    private void download() {
        String name = "";

        if (!type.equals("") && !limit.equals("") && !explicit.equals("")) {
            // Build URL string.
            name = "https://rss.itunes.apple.com/api/v1/us/itunes-music/" + type + "/all/" + limit + "/" + explicit + ".atom";
        } else {
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
}
