import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

public class ViewPetsFrame extends JFrame {
    private JList<Pet> petList;
    private DefaultListModel<Pet> petListModel;
    private JTextArea petDetailsArea;
    private JTable historyTable;
    private DefaultTableModel historyTableModel;
    private PetDAO petDAO;
    private CareLogDAO careLogDAO;
    private JLabel petImageLabel;

    public ViewPetsFrame() {
        this.petDAO = new PetDAO();
        this.careLogDAO = new CareLogDAO();
        initializeUI();
        loadPets();
    }

    private void initializeUI() {
        setTitle("View All Pets");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel utama dengan padding
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(mainPanel);

        // Split pane untuk memisahkan daftar dan detail
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        // Kiri: Daftar Peliharaan
        petListModel = new DefaultListModel<>();
        petList = new JList<>(petListModel);
        petList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        petList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof Pet) {
                    value = ((Pet) value).getName();
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });
        JScrollPane listScrollPane = new JScrollPane(petList);
        listScrollPane.setBorder(BorderFactory.createTitledBorder("All Pets"));
        splitPane.setLeftComponent(listScrollPane);

        // Kanan: Panel untuk Detail dan Riwayat
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));

        // PERBAIKAN: Membuat panel untuk bagian atas (detail teks dan gambar)
        JPanel topDetailsPanel = new JPanel(new BorderLayout(10, 10));

        // Area teks untuk detail
        petDetailsArea = new JTextArea(8, 30);
        petDetailsArea.setEditable(false);
        petDetailsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane detailsScrollPane = new JScrollPane(petDetailsArea);
        detailsScrollPane.setBorder(BorderFactory.createTitledBorder("Pet Details"));
        topDetailsPanel.add(detailsScrollPane, BorderLayout.CENTER);

        // Label untuk menampilkan gambar
        petImageLabel = new JLabel();
        petImageLabel.setPreferredSize(new Dimension(150, 150));
        petImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        petImageLabel.setBorder(BorderFactory.createTitledBorder("Photo"));
        topDetailsPanel.add(petImageLabel, BorderLayout.EAST);

        // Tabel untuk riwayat perawatan
        String[] historyColumns = {"Date", "Care Type", "Notes"};
        historyTableModel = new DefaultTableModel(historyColumns, 0){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Membuat tabel tidak bisa diedit
            }
        };
        historyTable = new JTable(historyTableModel);
        JScrollPane historyScrollPane = new JScrollPane(historyTable);
        historyScrollPane.setBorder(BorderFactory.createTitledBorder("Care History"));

        // Menambahkan panel atas dan tabel riwayat ke panel kanan
        rightPanel.add(topDetailsPanel, BorderLayout.NORTH);
        rightPanel.add(historyScrollPane, BorderLayout.CENTER);

        splitPane.setRightComponent(rightPanel);
        splitPane.setDividerLocation(200);

        // Listener untuk menampilkan detail saat peliharaan dipilih
        petList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                displayPetDetails(petList.getSelectedValue());
            }
        });
    }

    private void loadPets() {
        // Menggunakan SwingWorker agar UI tidak freeze saat memuat data dari database
        SwingWorker<List<Pet>, Void> worker = new SwingWorker<List<Pet>, Void>() {
            @Override
            protected List<Pet> doInBackground() throws Exception {
                return petDAO.getAllPets();
            }

            @Override
            protected void done() {
                try {
                    List<Pet> pets = get();
                    petListModel.clear();
                    for (Pet pet : pets) {
                        petListModel.addElement(pet);
                    }
                } catch (Exception e) {
                    handleError("Failed to load pets", e);
                }
            }
        };
        worker.execute();
    }

    private void displayPetDetails(Pet pet) {
        if (pet == null) {
            petDetailsArea.setText("");
            historyTableModel.setRowCount(0);
            petImageLabel.setIcon(null);
            petImageLabel.setText("");
            return;
        }

        // Menampilkan detail teks
        StringBuilder details = new StringBuilder();
        details.append(String.format("ID      : %d\n", pet.getId()));
        details.append(String.format("Name    : %s\n", pet.getName()));
        details.append(String.format("Type    : %s\n", pet.getType()));
        details.append(String.format("Gender  : %s\n", pet.getGender()));
        details.append(String.format("Birth   : %s\n", pet.getBirthDate()));
        details.append(String.format("Weight  : %.2f kg\n", pet.getWeight()));
        details.append(String.format("Notes   : %s\n", pet.getNotes()));
        petDetailsArea.setText(details.toString());

        // PERBAIKAN: Menambahkan logika untuk menampilkan gambar
        String imagePath = pet.getImagePath();
        if (imagePath != null && !imagePath.isEmpty()) {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                ImageIcon icon = new ImageIcon(imagePath);
                Image image = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                petImageLabel.setIcon(new ImageIcon(image));
                petImageLabel.setText(""); // Hapus teks placeholder
            } else {
                petImageLabel.setIcon(null);
                petImageLabel.setText("Image not found");
            }
        } else {
            petImageLabel.setIcon(null);
            petImageLabel.setText("No Photo");
        }


        // Menampilkan riwayat perawatan di thread terpisah
        SwingWorker<List<CareLog>, Void> historyWorker = new SwingWorker<List<CareLog>, Void>() {
            @Override
            protected List<CareLog> doInBackground() throws Exception {
                return careLogDAO.getCareLogsByPetId(pet.getId());
            }

            @Override
            protected void done() {
                try {
                    List<CareLog> logs = get();
                    historyTableModel.setRowCount(0); // Bersihkan tabel
                    for (CareLog log : logs) {
                        Vector<Object> row = new Vector<>();
                        row.add(log.getCompletedAt());
                        row.add(log.getCareType());
                        row.add(log.getNotes());
                        historyTableModel.addRow(row);
                    }
                } catch (Exception e) {
                    handleError("Failed to load care history", e);
                }
            }
        };
        historyWorker.execute();
    }

    private void handleError(String message, Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, message + ": " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}