package com.halo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import java.awt.CardLayout;

public class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    // DAOs
    private MapDAO mapDAO = new MapDAO();
    private GameTypeDAO gameTypeDAO = new GameTypeDAO();
    private PlaySessionDAO sessionDAO = new PlaySessionDAO();

    // ── Filter Panel components ──
    private JSpinner playerCountSpinner;
    private JComboBox<GameType> gameTypeCombo;
    private JCheckBox ignoreParityCheckBox;
    private JButton searchButton;
    private JButton clearButton;
    private JComboBox<Map> mapNameSearchCombo;

    // ── Results Panel components ──
    private DefaultListModel<Map> resultsListModel;
    private JList<Map> resultsList;
    private JLabel resultsCountLabel;
    
    // ── Card Layout ──
    private CardLayout centerCardLayout;
    private JPanel centerPanel;
    private JLabel gnMapNameLabel;
    private JLabel gnSizePlayersLabel;
    private JLabel gnGameTypeLabel;
    private JLabel gnVariantLabel;
    private JTextArea gnDescriptionArea;
    private JTextArea gnNotesArea;
    private JButton backToMapsButton;

    // ── Detail Panel components ──
    private JLabel mapNameLabel;
    private JLabel mapImageLabel;
    private JLabel sizeLabel;
    private JLabel playersLabel;
    private JLabel lightingLabel;
    private JLabel dateAddedLabel;
    private JTextArea descriptionArea;
    private JTextArea gameTypesArea;
    private JButton randomizeButton;

    // ── Bottom Button Panel ──
    private JButton addMapButton;
    private JButton editMapButton;
    private JButton deactivateMapButton;
    private JButton logSessionButton;

    // ─────────────────────────────────────────────
    // CONSTRUCTOR
    // ─────────────────────────────────────────────

    public MainFrame() {
        setTitle("MAPPY — Halo Infinite Map Selector");
        setSize(1800, 1100);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        buildMenuBar();
        getRootPane().setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Build panels
        add(buildFilterPanel(), BorderLayout.WEST);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildDetailPanel(), BorderLayout.EAST);
        add(buildBottomPanel(), BorderLayout.SOUTH);

        // Load gametypes into combo box
        loadGameTypes();
        loadMapNameCombo();

        setVisible(true);
    }

    // ─────────────────────────────────────────────
    // FILTER PANEL (left side)
    // ─────────────────────────────────────────────

    private JPanel buildFilterPanel() {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(280, 0));
        panel.setLayout(null);  // absolute layout for precise control
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Search Filters",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14)
        ));

        int x = 20;
        int y = 30;
        int labelW = 240;
        int fieldW = 240;
        int gap = 10;

        // Player Count
        JLabel playerLabel = new JLabel("Number of Players:");
        playerLabel.setBounds(x, y, labelW, 25);
        panel.add(playerLabel);
        y += 28;

        playerCountSpinner = new JSpinner(new SpinnerNumberModel(8, 2, 50, 1));
        playerCountSpinner.setBounds(x, y, fieldW, 30);
        panel.add(playerCountSpinner);
        y += 40 + gap;

        // Game Type
        JLabel gtLabel = new JLabel("Game Type:");
        gtLabel.setBounds(x, y, labelW, 25);
        panel.add(gtLabel);
        y += 28;

        gameTypeCombo = new JComboBox<>();
        gameTypeCombo.setBounds(x, y, fieldW, 30);
        panel.add(gameTypeCombo);
        y += 40 + gap;

        // Ignore parity checkbox
        ignoreParityCheckBox = new JCheckBox("Allow all modes (ignore odd/even)");
        ignoreParityCheckBox.setBounds(x, y, fieldW, 25);
        panel.add(ignoreParityCheckBox);
        y += 35 + gap * 3;
        
        // Search by name
        JLabel nameSearchLabel = new JLabel("Search Map by Name:");
        nameSearchLabel.setBounds(x, y, labelW, 25);
        panel.add(nameSearchLabel);
        y += 28;

        mapNameSearchCombo = new JComboBox<>();
        mapNameSearchCombo.setBounds(x, y, fieldW, 30);
        mapNameSearchCombo.setToolTipText("Select a map by name");
        panel.add(mapNameSearchCombo);
        y += 40 + gap;

        // Search button
        searchButton = new JButton("Find Maps");
        searchButton.setBounds(x, y, fieldW, 40);
        searchButton.setFont(new Font("Arial", Font.BOLD, 14));
        searchButton.setBackground(new Color(0, 120, 215));
        searchButton.setForeground(Color.WHITE);
        searchButton.setOpaque(true);
        panel.add(searchButton);
        y += 50 + gap;

        // Clear button
        clearButton = new JButton("Clear Results");
        clearButton.setBounds(x, y, fieldW, 35);
        panel.add(clearButton);

        // Wire up actions
        searchButton.addActionListener(e -> performSearch());
        clearButton.addActionListener(e -> clearResults());
        mapNameSearchCombo.addActionListener(e -> performNameSearch());
        
        return panel;
    }
    
    private void loadMapNameCombo() {
        mapNameSearchCombo.removeAllItems();
        mapNameSearchCombo.addItem(null);  // blank first entry
        List<Map> allMaps = mapDAO.getAllMaps();
        for (Map m : allMaps) {
            mapNameSearchCombo.addItem(m);
        }
    }
    
    private void performNameSearch() {
        Map selected = (Map) mapNameSearchCombo.getSelectedItem();
        if (selected == null) return;

        resultsListModel.clear();
        resultsListModel.addElement(selected);
        resultsCountLabel.setText("Showing map: " + selected.getName());
        editMapButton.setEnabled(false);
        logSessionButton.setEnabled(false);
        clearDetailPanel();
    }

    // ─────────────────────────────────────────────
    // RESULTS PANEL (center)
    // ─────────────────────────────────────────────

    private JPanel buildResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setPreferredSize(new Dimension(900, 0));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Matching Maps",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14)
        ));

        // Results count label at top
        resultsCountLabel = new JLabel("No search performed yet.");
        resultsCountLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        resultsCountLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        panel.add(resultsCountLabel, BorderLayout.NORTH);

        // Results list
        resultsListModel = new DefaultListModel<>();
        resultsList = new JList<>(resultsListModel);
        resultsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultsList.setFont(new Font("Arial", Font.PLAIN, 14));
        resultsList.setFixedCellHeight(35);

        JScrollPane scrollPane = new JScrollPane(resultsList);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Wire up selection listener
        resultsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Map selected = resultsList.getSelectedValue();
                if (selected != null) {
                    displayMapDetail(selected);
                }
            }
        });

        return panel;
    }
    
    // ─────────────────────────────────────────────
    // GAME NIGHT PANEL (center)
    // ─────────────────────────────────────────────
    
    private JPanel buildCenterPanel() {
        centerCardLayout = new CardLayout();
        centerPanel = new JPanel(centerCardLayout);

        centerPanel.add(buildResultsPanel(), "SEARCH");
        centerPanel.add(buildGameNightPanel(), "GAME_NIGHT");

        return centerPanel;
    }
    
    private JPanel buildGameNightPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(new Color(20, 20, 40));
        panel.setBorder(BorderFactory.createLineBorder(new Color(120, 40, 180), 2));

        int x = 30;
        int y = 30;

        // Header
        JLabel header = new JLabel("🎮 GAME NIGHT");
        header.setFont(new Font("Arial", Font.BOLD, 22));
        header.setForeground(new Color(180, 120, 255));
        header.setBounds(x, y, 800, 35);
        panel.add(header);
        y += 50;

        // Map name
        JLabel mapTitle = new JLabel("MAP:");
        mapTitle.setFont(new Font("Arial", Font.BOLD, 14));
        mapTitle.setForeground(Color.GRAY);
        mapTitle.setBounds(x, y, 80, 25);
        panel.add(mapTitle);

        gnMapNameLabel = new JLabel("—");
        gnMapNameLabel.setFont(new Font("Arial", Font.BOLD, 20));
        gnMapNameLabel.setForeground(Color.WHITE);
        gnMapNameLabel.setBounds(x + 90, y, 700, 25);
        panel.add(gnMapNameLabel);
        y += 35;

        // Size / Players
        JLabel sizeTitle = new JLabel("SIZE:");
        sizeTitle.setFont(new Font("Arial", Font.BOLD, 14));
        sizeTitle.setForeground(Color.GRAY);
        sizeTitle.setBounds(x, y, 80, 25);
        panel.add(sizeTitle);

        gnSizePlayersLabel = new JLabel("—");
        gnSizePlayersLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        gnSizePlayersLabel.setForeground(Color.LIGHT_GRAY);
        gnSizePlayersLabel.setBounds(x + 90, y, 700, 25);
        panel.add(gnSizePlayersLabel);
        y += 50;

        // Divider label
        JLabel divider1 = new JLabel("────────────────────────────────────────");
        divider1.setForeground(new Color(80, 80, 100));
        divider1.setBounds(x, y, 800, 20);
        panel.add(divider1);
        y += 30;

        // Game Type
        JLabel gtTitle = new JLabel("GAME TYPE:");
        gtTitle.setFont(new Font("Arial", Font.BOLD, 14));
        gtTitle.setForeground(Color.GRAY);
        gtTitle.setBounds(x, y, 120, 25);
        panel.add(gtTitle);

        gnGameTypeLabel = new JLabel("—");
        gnGameTypeLabel.setFont(new Font("Arial", Font.BOLD, 20));
        gnGameTypeLabel.setForeground(new Color(100, 200, 255));
        gnGameTypeLabel.setBounds(x + 130, y, 660, 25);
        panel.add(gnGameTypeLabel);
        y += 35;

        // Variant
        JLabel varTitle = new JLabel("VARIANT:");
        varTitle.setFont(new Font("Arial", Font.BOLD, 14));
        varTitle.setForeground(Color.GRAY);
        varTitle.setBounds(x, y, 120, 25);
        panel.add(varTitle);

        gnVariantLabel = new JLabel("—");
        gnVariantLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gnVariantLabel.setForeground(new Color(180, 255, 150));
        gnVariantLabel.setBounds(x + 130, y, 660, 25);
        panel.add(gnVariantLabel);
        y += 50;

        // Divider
        JLabel divider2 = new JLabel("────────────────────────────────────────");
        divider2.setForeground(new Color(80, 80, 100));
        divider2.setBounds(x, y, 800, 20);
        panel.add(divider2);
        y += 30;

        // Description
        JLabel descTitle = new JLabel("RULES / DESCRIPTION:");
        descTitle.setFont(new Font("Arial", Font.BOLD, 14));
        descTitle.setForeground(Color.GRAY);
        descTitle.setBounds(x, y, 300, 25);
        panel.add(descTitle);
        y += 28;

        gnDescriptionArea = new JTextArea();
        gnDescriptionArea.setEditable(false);
        gnDescriptionArea.setLineWrap(true);
        gnDescriptionArea.setWrapStyleWord(true);
        gnDescriptionArea.setFont(new Font("Arial", Font.PLAIN, 15));
        gnDescriptionArea.setBackground(new Color(30, 30, 55));
        gnDescriptionArea.setForeground(Color.WHITE);
        gnDescriptionArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JScrollPane descScroll = new JScrollPane(gnDescriptionArea);
        descScroll.setBounds(x, y, 740, 100);
        descScroll.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 100)));
        panel.add(descScroll);
        y += 115;

        // Notes
        JLabel notesTitle = new JLabel("NOTES:");
        notesTitle.setFont(new Font("Arial", Font.BOLD, 14));
        notesTitle.setForeground(Color.GRAY);
        notesTitle.setBounds(x, y, 300, 25);
        panel.add(notesTitle);
        y += 28;

        gnNotesArea = new JTextArea();
        gnNotesArea.setEditable(false);
        gnNotesArea.setLineWrap(true);
        gnNotesArea.setWrapStyleWord(true);
        gnNotesArea.setFont(new Font("Arial", Font.ITALIC, 14));
        gnNotesArea.setBackground(new Color(30, 30, 55));
        gnNotesArea.setForeground(new Color(200, 200, 200));
        gnNotesArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JScrollPane notesScroll = new JScrollPane(gnNotesArea);
        notesScroll.setBounds(x, y, 740, 80);
        notesScroll.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 100)));
        panel.add(notesScroll);
        y += 90;

        // Back button
        backToMapsButton = new JButton("← Back to Maps");
        backToMapsButton.setFont(new Font("Arial", Font.BOLD, 14));
        backToMapsButton.setPreferredSize(new Dimension(180, 40));
        backToMapsButton.setBounds(x, y, 200, 40);
        backToMapsButton.setBackground(new Color(60, 60, 80));
        backToMapsButton.setForeground(Color.WHITE);
        backToMapsButton.setOpaque(true);
        backToMapsButton.addActionListener(e ->
                centerCardLayout.show(centerPanel, "SEARCH"));
        panel.add(backToMapsButton);
        
        final JScrollPane ds = descScroll;
        final JScrollPane ns = notesScroll;
        panel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int w = panel.getWidth() - 60;
                ds.setSize(w, ds.getHeight());
                ns.setSize(w, ns.getHeight());
            }
        });

        return panel;
    }
    
    private void activateGameNightMode(Map map, GameTypeVariant variant) {
        gnMapNameLabel.setText(map.getName());
        gnSizePlayersLabel.setText(map.getSize() + "  |  " +
                map.getMinPlayers() + "–" + map.getMaxPlayers() + " players");

        // Get parent game type name
        GameType parentType = gameTypeDAO.getGameTypeById(variant.getGameTypeId());
        gnGameTypeLabel.setText(parentType != null ? parentType.getName() : "—");

        gnVariantLabel.setText(variant.getEffectiveName());
        gnDescriptionArea.setText(variant.getDescription() != null ?
                variant.getDescription() : "No description entered yet.");
        gnDescriptionArea.setCaretPosition(0);
        gnNotesArea.setText(variant.getNotes() != null ?
                variant.getNotes() : "");
        gnNotesArea.setCaretPosition(0);

        centerCardLayout.show(centerPanel, "GAME_NIGHT");
    }

    // ─────────────────────────────────────────────
    // DETAIL PANEL (right side)
    // ─────────────────────────────────────────────

    private JPanel buildDetailPanel() {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(500, 0));
        panel.setLayout(null);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Map Detail",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14)
        ));

        int x = 20;
        int y = 30;

        // Map name
        mapNameLabel = new JLabel("Select a map from the results list");
        mapNameLabel.setFont(new Font("Arial", Font.BOLD, 20));
        mapNameLabel.setBounds(x, y, 450, 30);
        panel.add(mapNameLabel);
        y += 35;

        // Image
        mapImageLabel = new JLabel("No image available", SwingConstants.CENTER);
        mapImageLabel.setBounds(x, y, 450, 250);
        mapImageLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        mapImageLabel.setBackground(Color.DARK_GRAY);
        mapImageLabel.setOpaque(true);
        mapImageLabel.setForeground(Color.WHITE);
        mapImageLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        panel.add(mapImageLabel);
        y += 260;

        // Size / Players / Lighting row
        sizeLabel = new JLabel("Size: —");
        sizeLabel.setBounds(x, y, 100, 25);
        panel.add(sizeLabel);

        playersLabel = new JLabel("Players: —");
        playersLabel.setBounds(x + 105, y, 110, 25);
        panel.add(playersLabel);

        lightingLabel = new JLabel("Lighting: —");
        lightingLabel.setBounds(x + 220, y, 120, 25);
        panel.add(lightingLabel);

        dateAddedLabel = new JLabel("Added: —");
        dateAddedLabel.setBounds(x + 345, y, 130, 25);
        panel.add(dateAddedLabel);
        y += 30;

        // Game Types
        JLabel gtTitle = new JLabel("Supported Game Types:");
        gtTitle.setFont(new Font("Arial", Font.BOLD, 13));
        gtTitle.setBounds(x, y, 450, 25);
        panel.add(gtTitle);
        y += 25;

        gameTypesArea = new JTextArea();
        gameTypesArea.setEditable(false);
        gameTypesArea.setLineWrap(true);
        gameTypesArea.setWrapStyleWord(true);
        gameTypesArea.setFont(new Font("Arial", Font.PLAIN, 13));
        gameTypesArea.setBackground(panel.getBackground());
        JScrollPane gtScroll = new JScrollPane(gameTypesArea);
        gtScroll.setBounds(x, y, 450, 60);
        gtScroll.setBorder(null);
        panel.add(gtScroll);
        y += 55;
        
        // Randomizer button
        randomizeButton = new JButton("🎲 Randomize Game Type Variant");
        randomizeButton.setBounds(x, y, 450, 38);
        randomizeButton.setFont(new Font("Arial", Font.BOLD, 13));
        randomizeButton.setBackground(new Color(120, 40, 180));
        randomizeButton.setForeground(Color.WHITE);
        randomizeButton.setOpaque(true);
        randomizeButton.setEnabled(false);
        randomizeButton.addActionListener(e -> openRandomizerDialog());
        panel.add(randomizeButton);
        y += 48;

        // Description
        JLabel descTitle = new JLabel("Description:");
        descTitle.setFont(new Font("Arial", Font.BOLD, 13));
        descTitle.setBounds(x, y, 450, 25);
        panel.add(descTitle);
        y += 25;

        descriptionArea = new JTextArea();
        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setFont(new Font("Arial", Font.PLAIN, 13));
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setBounds(x, y, 450, 100);
        panel.add(descScroll);
        y += 60;

        return panel;
    }

    // ─────────────────────────────────────────────
    // BOTTOM PANEL
    // ─────────────────────────────────────────────

    private JPanel buildBottomPanel() {
        JPanel panel = new JPanel();
        panel.setMinimumSize(new Dimension(0, 60));
        panel.setPreferredSize(new Dimension(0, 60));
        panel.setBackground(new Color(45, 45, 45));

        addMapButton = new JButton("+ Add New Map");
        addMapButton.setFont(new Font("Arial", Font.BOLD, 13));
        addMapButton.setPreferredSize(new Dimension(180, 40));
        addMapButton.setBackground(new Color(0, 160, 80));
        addMapButton.setForeground(Color.WHITE);
        addMapButton.setOpaque(true);

        editMapButton = new JButton("✎ Edit Selected Map");
        editMapButton.setFont(new Font("Arial", Font.BOLD, 13));
        editMapButton.setPreferredSize(new Dimension(180, 40));
        editMapButton.setEnabled(false);  // disabled until a map is selected
        
        deactivateMapButton = new JButton("✖ Remove Map");
        deactivateMapButton.setFont(new Font("Arial", Font.BOLD, 13));
        deactivateMapButton.setPreferredSize(new Dimension(150, 40));
        deactivateMapButton.setBackground(new Color(180, 50, 50));
        deactivateMapButton.setForeground(Color.RED);
        deactivateMapButton.setOpaque(true);
        deactivateMapButton.setEnabled(false);

        logSessionButton = new JButton("📋 Log Game Session");
        logSessionButton.setFont(new Font("Arial", Font.BOLD, 13));
        logSessionButton.setPreferredSize(new Dimension(180, 40));
        logSessionButton.setEnabled(false);  // disabled until a map is selected
        logSessionButton.addActionListener(e -> openLogSessionDialog());

        panel.add(addMapButton);
        panel.add(editMapButton);
        panel.add(deactivateMapButton);
        panel.add(logSessionButton);

        // Wire up actions
        addMapButton.addActionListener(e -> openAddMapDialog());
        editMapButton.addActionListener(e -> openEditMapDialog());
        deactivateMapButton.addActionListener(e -> {
            Map selected = resultsList.getSelectedValue();
            if (selected == null) return;
            int confirm = javax.swing.JOptionPane.showConfirmDialog(this,
                    "Remove \"" + selected.getName() + "\" from active maps?\n" +
                    "It will be hidden but its session history preserved.",
                    "Confirm Remove", javax.swing.JOptionPane.YES_NO_OPTION,
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            if (confirm == javax.swing.JOptionPane.YES_OPTION) {
                mapDAO.deactivateMap(selected.getId());
                resultsListModel.removeElement(selected);
                clearDetailPanel();
                loadMapNameCombo();
            }
        }); // end of deactivate action

        return panel;
    } // end of buildBottomPanel()

    // ─────────────────────────────────────────────
    // LOAD GAME TYPES INTO COMBO
    // ─────────────────────────────────────────────

    private void loadGameTypes() {
        gameTypeCombo.removeAllItems();
        List<GameType> types;

        types = gameTypeDAO.getAllGameTypes();

        for (GameType gt : types) {
            gameTypeCombo.addItem(gt);
        }
    }

    // ─────────────────────────────────────────────
    // SEARCH
    // ─────────────────────────────────────────────

    private void performSearch() {
        int playerCount = (int) playerCountSpinner.getValue();
        GameType selectedType = (GameType) gameTypeCombo.getSelectedItem();
        boolean ignoreParity = ignoreParityCheckBox.isSelected();

        if (selectedType == null) {
            resultsCountLabel.setText("Please select a game type.");
            return;
        }

        List<Map> results = mapDAO.searchMaps(playerCount, selectedType.getId(), ignoreParity);
        resultsListModel.clear();

        for (Map m : results) {
            resultsListModel.addElement(m);
        }

        resultsCountLabel.setText(results.size() + " map(s) found for " +
                selectedType.getName() + " with " + playerCount + " players.");

        editMapButton.setEnabled(false);
        logSessionButton.setEnabled(false);
        clearDetailPanel();
    }

    // ─────────────────────────────────────────────
    // CLEAR
    // ─────────────────────────────────────────────

    private void clearResults() {
        resultsListModel.clear();
        resultsCountLabel.setText("No search performed yet.");
        clearDetailPanel();
        editMapButton.setEnabled(false);
        logSessionButton.setEnabled(false);
    }

    private void clearDetailPanel() {
        mapNameLabel.setText("Select a map from the results list");
        mapImageLabel.setIcon(null);
        mapImageLabel.setText("No image available");
        sizeLabel.setText("Size: —");
        playersLabel.setText("Players: —");
        lightingLabel.setText("Lighting: —");
        dateAddedLabel.setText("Added: —");
        gameTypesArea.setText("");
        descriptionArea.setText("");
        randomizeButton.setEnabled(false);
        deactivateMapButton.setEnabled(false);
    }

    // ─────────────────────────────────────────────
    // DISPLAY MAP DETAIL
    // ─────────────────────────────────────────────

    private void displayMapDetail(Map map) {
        mapNameLabel.setText(map.getName());
        sizeLabel.setText("Size: " + map.getSize());
        playersLabel.setText("Players: " + map.getMinPlayers() + " – " + map.getMaxPlayers());
        lightingLabel.setText("Lighting: " + (map.getLighting() != null ? map.getLighting() : "—"));
        dateAddedLabel.setText("Added: " + (map.getDateAdded() != null ? map.getDateAdded() : "—"));
        descriptionArea.setText(map.getDescription() != null ? map.getDescription() : "");
        descriptionArea.setCaretPosition(0);

        // Load game types for this map
        List<GameType> gts = mapDAO.getGameTypesForMap(map.getId());
        if (gts.isEmpty()) {
            gameTypesArea.setText("None assigned yet.");
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < gts.size(); i++) {
                sb.append(gts.get(i).getName());
                if (i < gts.size() - 1) sb.append(", ");
            }
            gameTypesArea.setText(sb.toString());
        }

        // Load image
        loadMapImage(map.getImageFilename());

        // Enable edit and log buttons
        editMapButton.setEnabled(true);
        deactivateMapButton.setEnabled(true);
        logSessionButton.setEnabled(true);
        randomizeButton.setEnabled(true);
    }

    // ─────────────────────────────────────────────
    // LOAD MAP IMAGE
    // ─────────────────────────────────────────────

    private void loadMapImage(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            mapImageLabel.setIcon(null);
            mapImageLabel.setText("No image available");
            return;
        }

        File imgFile = new File("images/" + filename);
        if (!imgFile.exists()) {
            mapImageLabel.setIcon(null);
            mapImageLabel.setText("Image not found: " + filename);
            return;
        }

        try {
            BufferedImage img = ImageIO.read(imgFile);
            Image scaled = img.getScaledInstance(480, 250, Image.SCALE_SMOOTH);
            mapImageLabel.setIcon(new ImageIcon(scaled));
            mapImageLabel.setText("");
        } catch (IOException e) {
            mapImageLabel.setIcon(null);
            mapImageLabel.setText("Error loading image.");
        }
    }

    // ─────────────────────────────────────────────
    // OPEN ADD MAP DIALOG
    // ─────────────────────────────────────────────

    private void openAddMapDialog() {
        MapEditDialog dialog = new MapEditDialog(this, null, mapDAO, gameTypeDAO);
        dialog.setVisible(true);
        resultsCountLabel.setText("Map added. Use Search to find it.");
        loadMapNameCombo();
    }

    // ─────────────────────────────────────────────
    // OPEN EDIT MAP DIALOG
    // ─────────────────────────────────────────────

    private void openEditMapDialog() {
        Map selected = resultsList.getSelectedValue();
        if (selected == null) return;
        MapEditDialog dialog = new MapEditDialog(this, selected, mapDAO, gameTypeDAO);
        dialog.setVisible(true);
    }
    
    // ─────────────────────────────────────────────
    // OPEN RANDOMIZER DIALOG
    // ─────────────────────────────────────────────
    private void openRandomizerDialog() {
        Map selected = resultsList.getSelectedValue();
        if (selected == null) return;
        RandomizerDialog dialog = new RandomizerDialog(this, selected,
                mapDAO, gameTypeDAO, new GameTypeVariantDAO());
        dialog.setVisible(true);

        GameTypeVariant accepted = dialog.getAcceptedVariant();
        if (accepted != null) {
            activateGameNightMode(selected, accepted);
        }
    }
    
    // ─────────────────────────────────────────────
    // BUILD MENU BAR
    // ─────────────────────────────────────────────
    private void buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // ── Database Menu ──
        JMenu databaseMenu = new JMenu("Database");

        JMenuItem manageGameTypes = new JMenuItem("Manage Game Types & Variants");
        JMenuItem viewStats = new JMenuItem("Database Snapshot");
        JMenuItem separatorItem = new JMenuItem("─────────────────");
        separatorItem.setEnabled(false);
        JMenuItem exitItem = new JMenuItem("Exit");

        databaseMenu.add(manageGameTypes);
        databaseMenu.add(viewStats);
        databaseMenu.addSeparator();
        databaseMenu.add(exitItem);

        // ── Maps Menu ──
        JMenu mapsMenu = new JMenu("Maps");

        JMenuItem addMap = new JMenuItem("Add New Map");
        JMenuItem viewAllMaps = new JMenuItem("View All Maps");
        JMenuItem manageRemovedMaps = new JMenuItem("Restore Removed Maps");

        mapsMenu.add(addMap);
        mapsMenu.add(viewAllMaps);
        mapsMenu.add(manageRemovedMaps);
        
        // ── Help Menu ──
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About MAPPY");
        helpMenu.add(aboutItem);

        menuBar.add(databaseMenu);
        menuBar.add(mapsMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        // Wire up actions
        manageGameTypes.addActionListener(e -> openGameTypeManager());
        addMap.addActionListener(e -> openAddMapDialog());
        manageRemovedMaps.addActionListener(e -> openRestoreMapDialog());
        exitItem.addActionListener(e -> System.exit(0));
        aboutItem.addActionListener(e -> showAboutDialog());
        
    }
    
    private void openGameTypeManager() {
        GameTypeManagerDialog dialog = new GameTypeManagerDialog(this, gameTypeDAO,
                new GameTypeVariantDAO());
        dialog.setVisible(true);
    }
    
    private void openRestoreMapDialog() {
        List<Map> inactiveMaps = mapDAO.getInactiveMaps();

        if (inactiveMaps.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "No removed maps found.",
                    "Restore Maps",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Map[] mapArray = inactiveMaps.toArray(new Map[0]);
        Map selected = (Map) javax.swing.JOptionPane.showInputDialog(
                this,
                "Select a map to restore:",
                "Restore Removed Maps",
                javax.swing.JOptionPane.PLAIN_MESSAGE,
                null,
                mapArray,
                mapArray[0]);

        if (selected != null) {
            mapDAO.reactivateMap(selected.getId());
            loadMapNameCombo();
            javax.swing.JOptionPane.showMessageDialog(this,
                    "\"" + selected.getName() + "\" has been restored.",
                    "Map Restored",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showAboutDialog() {
        javax.swing.JOptionPane.showMessageDialog(this,
                "MAPPY — Halo Infinite Map Selector\nBuilt for custom game nights.",
                "About MAPPY",
                javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void openLogSessionDialog() {
        Map selected = resultsList.getSelectedValue();
        if (selected == null) return;
        LogSessionDialog dialog = new LogSessionDialog(this, selected,
                sessionDAO, gameTypeDAO, new GameTypeVariantDAO());
        dialog.setVisible(true);
    }
    

    // ─────────────────────────────────────────────
    // MAIN ENTRY POINT
    // ─────────────────────────────────────────────

    public static void main(String[] args) {
        DatabaseManager.initializeDatabase();
        SwingUtilities.invokeLater(() -> new MainFrame());
    }
}