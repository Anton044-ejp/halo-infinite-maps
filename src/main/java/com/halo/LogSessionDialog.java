package com.halo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;

public class LogSessionDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private Map selectedMap;
    private PlaySessionDAO sessionDAO;
    private GameTypeDAO gameTypeDAO;
    private GameTypeVariantDAO variantDAO;

    // ── Form fields ──
    private JComboBox<GameType> gameTypeCombo;
    private JTextField variantNameField;
    private JSpinner playerCountSpinner;
    private JTextField datefield;
    private JTextArea notesArea;

    // ── Session history ──
    private DefaultListModel<String> historyModel;
    private JList<String> historyList;

    // ── Buttons ──
    private JButton saveButton;
    private JButton cancelButton;

    // ─────────────────────────────────────────────
    // CONSTRUCTOR
    // ─────────────────────────────────────────────

    public LogSessionDialog(JFrame parent, Map map, PlaySessionDAO sessionDAO,
            GameTypeDAO gameTypeDAO, GameTypeVariantDAO variantDAO) {
        super(parent, "Log Game Session — " + map.getName(), true);
        this.selectedMap = map;
        this.sessionDAO = sessionDAO;
        this.gameTypeDAO = gameTypeDAO;
        this.variantDAO = variantDAO;

        setSize(750, 700);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        add(buildTopPanel(), BorderLayout.NORTH);
        add(buildFormPanel(), BorderLayout.CENTER);
        add(buildBottomPanel(), BorderLayout.SOUTH);

        loadSessionHistory();
    }

    // ─────────────────────────────────────────────
    // TOP PANEL — map info
    // ─────────────────────────────────────────────

    private JPanel buildTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));
        panel.setBackground(new Color(45, 45, 45));

        JLabel mapNameLabel = new JLabel("Map: " + selectedMap.getName());
        mapNameLabel.setFont(new Font("Arial", Font.BOLD, 18));
        mapNameLabel.setForeground(Color.WHITE);
        panel.add(mapNameLabel, BorderLayout.WEST);

        JLabel sizeLabel = new JLabel(selectedMap.getSize() + "  |  " +
                selectedMap.getMinPlayers() + "–" +
                selectedMap.getMaxPlayers() + " players");
        sizeLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        sizeLabel.setForeground(Color.LIGHT_GRAY);
        panel.add(sizeLabel, BorderLayout.EAST);

        return panel;
    }

    // ─────────────────────────────────────────────
    // FORM PANEL
    // ─────────────────────────────────────────────

    private JPanel buildFormPanel() {
        JPanel outer = new JPanel(new BorderLayout(10, 10));
        outer.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // ── Log form (top half) ──
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Log This Session",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 13)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Game Type
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        form.add(new JLabel("Game Type:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        gameTypeCombo = new JComboBox<>();
        loadGameTypes();
        form.add(gameTypeCombo, gbc);
        row++;

        // Variant Name
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        form.add(new JLabel("Variant Played:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        variantNameField = new JTextField();
        variantNameField.setToolTipText(
                "e.g. Slayer BRs, Team Rockets — exact variant name");
        form.add(variantNameField, gbc);
        row++;

        // Player Count
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        form.add(new JLabel("Player Count:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        playerCountSpinner = new JSpinner(new SpinnerNumberModel(8, 2, 50, 1));
        form.add(playerCountSpinner, gbc);
        row++;

        // Date
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        form.add(new JLabel("Date Played:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        datefield = new JTextField(LocalDate.now().toString());
        datefield.setToolTipText("Format: YYYY-MM-DD");
        form.add(datefield, gbc);
        row++;

        // Notes
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        form.add(new JLabel("Notes:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        notesArea = new JTextArea(3, 20);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setToolTipText("Any notes about this session");
        form.add(new JScrollPane(notesArea), gbc);

        outer.add(form, BorderLayout.NORTH);

        // ── Session history (bottom half) ──
        JPanel historyPanel = new JPanel(new BorderLayout(5, 5));
        historyPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Previous Sessions on this Map",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 13)
        ));

        historyModel = new DefaultListModel<>();
        historyList = new JList<>(historyModel);
        historyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyList.setFont(new Font("Arial", Font.PLAIN, 13));
        historyList.setFixedCellHeight(28);

        JScrollPane historyScroll = new JScrollPane(historyList);
        historyScroll.setPreferredSize(new Dimension(0, 180));
        historyPanel.add(historyScroll, BorderLayout.CENTER);

        outer.add(historyPanel, BorderLayout.CENTER);

        return outer;
    }

    // ─────────────────────────────────────────────
    // BOTTOM PANEL
    // ─────────────────────────────────────────────

    private JPanel buildBottomPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(45, 45, 45));
        panel.setPreferredSize(new Dimension(0, 55));

        saveButton = new JButton("Log Session");
        saveButton.setFont(new Font("Arial", Font.BOLD, 14));
        saveButton.setPreferredSize(new Dimension(160, 38));
        saveButton.setBackground(new Color(0, 160, 80));
        saveButton.setForeground(Color.WHITE);
        saveButton.setOpaque(true);

        cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Arial", Font.BOLD, 14));
        cancelButton.setPreferredSize(new Dimension(120, 38));

        panel.add(saveButton);
        panel.add(cancelButton);

        saveButton.addActionListener(e -> saveSession());
        cancelButton.addActionListener(e -> dispose());

        return panel;
    }

    // ─────────────────────────────────────────────
    // LOAD GAME TYPES
    // ─────────────────────────────────────────────

    private void loadGameTypes() {
        gameTypeCombo.removeAllItems();
        List<GameType> types = gameTypeDAO.getAllGameTypes();
        for (GameType gt : types) {
            gameTypeCombo.addItem(gt);
        }
    }

    // ─────────────────────────────────────────────
    // LOAD SESSION HISTORY
    // ─────────────────────────────────────────────

    private void loadSessionHistory() {
        historyModel.clear();
        List<PlaySession> sessions = sessionDAO.getSessionsForMap(selectedMap.getId());

        if (sessions.isEmpty()) {
            historyModel.addElement("No sessions logged for this map yet.");
            return;
        }

        for (PlaySession s : sessions) {
            String gt = getGameTypeName(s.getGameTypeId());
            String variant = s.getVariantName() != null ?
                    " — " + s.getVariantName() : "";
            String entry = s.getDatePlayed() + "  |  " + gt + variant +
                    "  |  " + s.getPlayerCount() + " players";
            if (s.getNotes() != null && !s.getNotes().isEmpty()) {
                entry += "  |  " + s.getNotes();
            }
            historyModel.addElement(entry);
        }
    }

    // ─────────────────────────────────────────────
    // GET GAME TYPE NAME BY ID
    // ─────────────────────────────────────────────

    private String getGameTypeName(int gameTypeId) {
        List<GameType> types = gameTypeDAO.getAllGameTypes();
        for (GameType gt : types) {
            if (gt.getId() == gameTypeId) return gt.getName();
        }
        return "Unknown";
    }

    // ─────────────────────────────────────────────
    // SAVE SESSION
    // ─────────────────────────────────────────────

    private void saveSession() {
        GameType selectedType = (GameType) gameTypeCombo.getSelectedItem();
        if (selectedType == null) return;

        String variantName = variantNameField.getText().trim();
        int playerCount = (int) playerCountSpinner.getValue();
        String date = datefield.getText().trim();
        String notes = notesArea.getText().trim();

        if (date.isEmpty()) {
            date = LocalDate.now().toString();
        }

        PlaySession session = new PlaySession(
                selectedMap.getId(),
                selectedType.getId(),
                variantName.isEmpty() ? null : variantName,
                playerCount,
                notes.isEmpty() ? null : notes
        );
        session.setDatePlayed(date);

        int result = sessionDAO.insertSession(session);
        if (result != -1) {
            loadSessionHistory();
            variantNameField.setText("");
            notesArea.setText("");
            datefield.setText(LocalDate.now().toString());
        }
    }
}