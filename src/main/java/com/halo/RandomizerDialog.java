package com.halo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
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
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

public class RandomizerDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private Map selectedMap;
    private MapDAO mapDAO;
    private GameTypeDAO gameTypeDAO;
    private GameTypeVariantDAO variantDAO;
    private GameTypeVariant acceptedVariant = null;

    // ── Controls ──
    private JSpinner resultCountSpinner;
    private JButton randomizeButton;
    private JComboBox<GameType> gameTypeCombo;

    // ── Results ──
    private DefaultListModel<GameTypeVariant> resultsModel;
    private JList<GameTypeVariant> resultsList;
    private JLabel poolSizeLabel;
    private JLabel mapNameLabel;

    // ─────────────────────────────────────────────
    // CONSTRUCTOR
    // ─────────────────────────────────────────────

    public RandomizerDialog(JFrame parent, Map map, MapDAO mapDAO,
            GameTypeDAO gameTypeDAO, GameTypeVariantDAO variantDAO) {
        super(parent, "🎲 Game Type Randomizer", true);
        this.selectedMap = map;
        this.mapDAO = mapDAO;
        this.gameTypeDAO = gameTypeDAO;
        this.variantDAO = variantDAO;

        setSize(700, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        add(buildTopPanel(), BorderLayout.NORTH);
        add(buildResultsPanel(), BorderLayout.CENTER);
        add(buildBottomPanel(), BorderLayout.SOUTH);
    }
    
    public GameTypeVariant getAcceptedVariant() {
        return acceptedVariant;
    }

    // ─────────────────────────────────────────────
    // TOP PANEL — map info and controls
    // ─────────────────────────────────────────────

    private JPanel buildTopPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Map name header
        mapNameLabel = new JLabel("Map: " + selectedMap.getName());
        mapNameLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
        panel.add(mapNameLabel, gbc);
        gbc.gridwidth = 1;

        // Pool size info
        poolSizeLabel = new JLabel("Calculating pool...");
        poolSizeLabel.setFont(new Font("Arial", Font.ITALIC, 13));
        poolSizeLabel.setForeground(Color.GRAY);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 3;
        panel.add(poolSizeLabel, gbc);
        gbc.gridwidth = 1;

        // How many results to show
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        panel.add(new JLabel("Number of suggestions:"), gbc);

        gbc.gridx = 1; gbc.weightx = 0;
        resultCountSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 20, 1));
        resultCountSpinner.setPreferredSize(new Dimension(70, 30));
        panel.add(resultCountSpinner, gbc);

        // Randomize button
        gbc.gridx = 2; gbc.weightx = 1.0;
        randomizeButton = new JButton("[R] Randomize!");
        randomizeButton.setFont(new Font("Arial", Font.BOLD, 14));
        randomizeButton.setBackground(new Color(120, 40, 180));
        randomizeButton.setForeground(Color.WHITE);
        randomizeButton.setOpaque(true);
        randomizeButton.setPreferredSize(new Dimension(180, 38));
        panel.add(randomizeButton, gbc);

        // Wire up
        randomizeButton.addActionListener(e -> performRandomize());

        // Calculate pool size on open
        updatePoolSizeLabel();

        return panel;
    }

    // ─────────────────────────────────────────────
    // RESULTS PANEL
    // ─────────────────────────────────────────────

    private JPanel buildResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Suggested Game Type Variants",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 13)
        ));

        resultsModel = new DefaultListModel<>();
        resultsList = new JList<>(resultsModel);
        resultsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultsList.setFont(new Font("Arial", Font.PLAIN, 15));
        resultsList.setFixedCellHeight(38);

        JScrollPane scroll = new JScrollPane(resultsList);
        panel.add(scroll, BorderLayout.CENTER);

        // Placeholder label
        JLabel placeholder = new JLabel(
                "Press Randomize above to get suggestions or find specific game types with dropdown below \u2193", SwingConstants.CENTER);
        placeholder.setFont(new Font("Arial", Font.ITALIC, 14));
        placeholder.setForeground(Color.GRAY);
        panel.add(placeholder, BorderLayout.SOUTH);

        return panel;
    }

    // ─────────────────────────────────────────────
    // BOTTOM PANEL
    // ─────────────────────────────────────────────

    private JPanel buildBottomPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(45, 45, 45));
        panel.setPreferredSize(new Dimension(0, 55));

        gameTypeCombo = new JComboBox<>();
        gameTypeCombo.setPreferredSize(new Dimension(220, 38));
        List<GameType> allTypes = gameTypeDAO.getAllGameTypes();
        for (GameType gt : allTypes) {
            gameTypeCombo.addItem(gt);
        }

        JButton acceptButton = new JButton("[OK] Accept");
        acceptButton.setFont(new Font("Arial", Font.BOLD, 13));
        acceptButton.setPreferredSize(new Dimension(140, 38));
        acceptButton.setBackground(new Color(0, 160, 80));
        acceptButton.setForeground(Color.WHITE);
        acceptButton.setOpaque(true);
        acceptButton.setEnabled(false);

        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("Arial", Font.BOLD, 13));
        closeButton.setPreferredSize(new Dimension(120, 38));

        panel.add(new JLabel("  Browse by Type:  "));
        panel.add(gameTypeCombo);
        panel.add(acceptButton);
        panel.add(closeButton);

        gameTypeCombo.addActionListener(e -> {
            GameType selected = (GameType) gameTypeCombo.getSelectedItem();
            if (selected == null) return;
            acceptButton.setEnabled(false);
            loadVariantsForGameType(selected);
        });
        closeButton.addActionListener(e -> dispose());
        acceptButton.addActionListener(e -> {
            acceptedVariant = resultsList.getSelectedValue();
            dispose();
        });

        // Enable accept when a variant is selected
        resultsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                acceptButton.setEnabled(resultsList.getSelectedValue() != null);
            }
        });

        return panel;
    }
    
    private void loadVariantsForGameType(GameType gameType) {
        List<GameTypeVariant> variants = variantDAO.getVariantsForGameType(gameType.getId())
                .stream()
                .filter(v -> !v.getStatus().equals("INACTIVE"))
                .collect(java.util.stream.Collectors.toList());

        resultsModel.clear();
        for (GameTypeVariant v : variants) {
            resultsModel.addElement(v);
        }

        poolSizeLabel.setText("Showing " + variants.size() + " variants for " +
                gameType.getName() + " — select one and click Accept");
        poolSizeLabel.setForeground(Color.GRAY);
    }

    // ─────────────────────────────────────────────
    // BUILD VARIANT POOL
    // ─────────────────────────────────────────────

    private List<GameTypeVariant> buildVariantPool() {
        List<GameTypeVariant> pool = new ArrayList<>();
        java.util.Set<Integer> addedIds = new java.util.HashSet<>();

        // Get gametypes assigned to this map
        List<GameType> assignedTypes = mapDAO.getGameTypesForMap(selectedMap.getId());

        for (GameType gt : assignedTypes) {
            List<GameTypeVariant> variants = variantDAO.getRandomizerPool(gt.getId());
            for (GameTypeVariant v : variants) {
                if (addedIds.add(v.getId())) {
                    pool.add(v);
                }
            }
        }

        // Always add all Slayer variants regardless of map assignment
        List<GameType> allTypes = gameTypeDAO.getAllGameTypes();
        for (GameType gt : allTypes) {
            if (gt.getName().toLowerCase().contains("slayer")) {
                List<GameTypeVariant> slayerVariants =
                        variantDAO.getRandomizerPool(gt.getId());
                for (GameTypeVariant v : slayerVariants) {
                    if (addedIds.add(v.getId())) {
                        pool.add(v);
                    }
                }
            }
        }

        // Add slayer adjacent variants from any gametype
        List<GameTypeVariant> adjacent = variantDAO.getSlayerAdjacentPool();
        for (GameTypeVariant v : adjacent) {
            if (addedIds.add(v.getId())) {
                pool.add(v);
            }
        }

        return pool;
    }

    // ─────────────────────────────────────────────
    // UPDATE POOL SIZE LABEL
    // ─────────────────────────────────────────────

    private void updatePoolSizeLabel() {
        List<GameTypeVariant> pool = buildVariantPool();
        poolSizeLabel.setText("Eligible variant pool: " + pool.size() +
                " variants (ACTIVE + INVESTIGATING, excluding INACTIVE)");
    }

    // ─────────────────────────────────────────────
    // PERFORM RANDOMIZE
    // ─────────────────────────────────────────────

    private void performRandomize() {
        List<GameTypeVariant> pool = buildVariantPool();

        if (pool.isEmpty()) {
            resultsModel.clear();
            poolSizeLabel.setText("No eligible variants found for this map.");
            poolSizeLabel.setForeground(Color.RED);
            return;
        }

        Collections.shuffle(pool);

        int count = (int) resultCountSpinner.getValue();
        int actualCount = Math.min(count, pool.size());

        resultsModel.clear();
        for (int i = 0; i < actualCount; i++) {
            resultsModel.addElement(pool.get(i));
        }

        poolSizeLabel.setForeground(Color.GRAY);
        poolSizeLabel.setText("Eligible variant pool: " + pool.size() +
                " variants — showing " + actualCount + " suggestions");

    }
}