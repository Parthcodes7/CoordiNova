package com.oopsproject.validator.ui;

import com.oopsproject.validator.exception.InvalidDatasetException;
import com.oopsproject.validator.exception.ValidationException;
import com.oopsproject.validator.model.Coordinate;
import com.oopsproject.validator.model.ValidationReport;
import com.oopsproject.validator.service.DatasetLoader;
import com.oopsproject.validator.service.DatasetLoaderFactory;
import com.oopsproject.validator.service.ValidationEngine;
import com.oopsproject.validator.util.ReportGenerator;

import javax.swing.SwingUtilities;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * Pure Java Interactive Console Menu for the AI Dataset Coordinate Validator.
 * 
 * Simple to explain to Sir:
 * - Uses java.util.Scanner to take user input from keyboard (System.in).
 * - A while loop keeps the menu running until option 7 (Exit) is selected.
 * - A switch-case structure routes user input to corresponding methods.
 */
public class ConsoleMenu {

    private final Scanner scanner;
    private final ValidationEngine validationEngine;
    private List<Coordinate> currentDataset;
    private ValidationReport currentReport;
    private String currentFilePath;

    public ConsoleMenu() {
        this.scanner = new Scanner(System.in);
        this.validationEngine = ValidationEngine.createDefaultEngine();
    }

    public void start() {
        System.out.println("==================================================================");
        System.out.println("   📍 AI DATASET COORDINATE VALIDATOR & ANOMALY DETECTOR");
        System.out.println("   Course: Object-Oriented Programming (Java 17)");
        System.out.println("==================================================================");

        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("👉 Enter your choice (1-7): ");
            if (!scanner.hasNextLine()) {
                break;
            }
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                    loadDatasetMenu();
                    break;
                case "2":
                    runValidationMenu();
                    break;
                case "3":
                    viewReportMenu();
                    break;
                case "4":
                    exportReportMenu();
                    break;
                case "5":
                    launchGuiMenu();
                    break;
                case "6":
                    launchWebMenu();
                    break;
                case "7":
                    running = false;
                    System.out.println("\nExiting Coordinate Validator. Thank you!\n");
                    break;
                default:
                    System.out.println("\n[!] Invalid option. Please enter a number between 1 and 7.");
            }
        }
    }

    private void printMenu() {
        System.out.println("\n----------------------- MAIN MENU -----------------------");
        System.out.println(" 1. Load Dataset File (CSV or JSON)");
        System.out.println(" 2. Run Validation Suite (Range, Format, Duplicates, etc.)");
        System.out.println(" 3. View Validation Report Summary");
        System.out.println(" 4. Export Report to File (TXT or CSV)");
        System.out.println(" 5. Launch Desktop GUI Window (Swing)");
        System.out.println(" 6. Launch Web UI (Browser)");
        System.out.println(" 7. Exit");
        if (currentFilePath != null) {
            System.out.println("---------------------------------------------------------");
            System.out.println(" Active Dataset: " + currentFilePath);
            System.out.println(" Rows Loaded:    " + (currentDataset != null ? currentDataset.size() : 0));
            System.out.println(" Status:         " + (currentReport != null ? "Validated (" + currentReport.getPassRate() + "% pass rate)" : "Ready to validate"));
        }
        System.out.println("---------------------------------------------------------");
    }

    public void loadDataset(String path) {
        try {
            DatasetLoader loader = DatasetLoaderFactory.getLoader(path);
            this.currentDataset = loader.load(path);
            this.currentFilePath = path;
            this.currentReport = null; // Reset previous report
            System.out.println("\n[✓] SUCCESS: Loaded " + currentDataset.size() + " rows from '" + path + "'.");
        } catch (InvalidDatasetException e) {
            System.err.println("\n[✗] ERROR loading dataset: " + e.getMessage());
        }
    }

    private void loadDatasetMenu() {
        System.out.print("\nEnter file path [Press Enter for default: data/sample_coordinates.csv]: ");
        String path = scanner.nextLine().trim();
        if (path.isEmpty()) {
            path = "data/sample_coordinates.csv";
        }
        loadDataset(path);
    }

    public void runValidation() {
        if (currentDataset == null || currentDataset.isEmpty()) {
            System.out.println("\n[!] No dataset loaded. Please choose Option 1 to load a dataset first.");
            return;
        }

        try {
            this.currentReport = validationEngine.validate(currentDataset);
            System.out.println("\n[✓] Validation completed successfully!");
            System.out.println("    Total Rows:    " + currentReport.getTotalCount());
            System.out.println("    Valid Rows:    " + currentReport.getValidCount());
            System.out.println("    Invalid Rows:  " + currentReport.getInvalidCount());
            System.out.printf("    Pass Rate:     %.2f%%%n", currentReport.getPassRate());
            System.out.println("    Duplicates:    " + currentReport.getDuplicateCount());
            System.out.println("    Outliers:      " + currentReport.getOutlierCount());
            System.out.println("    BBox Breaches: " + currentReport.getBoundingBoxBreachCount());
            System.out.println("\n(Tip: Select Option 3 to view row-by-row failure reasons)");
        } catch (ValidationException e) {
            System.err.println("\n[✗] ERROR during validation: " + e.getMessage());
        }
    }

    private void runValidationMenu() {
        runValidation();
    }

    private void viewReportMenu() {
        if (currentReport == null) {
            if (currentDataset == null) {
                System.out.println("\n[!] No dataset loaded. Please load a file first (Option 1).");
            } else {
                System.out.println("\n[!] Dataset is loaded but not yet validated. Please run validation first (Option 2).");
            }
            return;
        }

        System.out.println("\n" + ReportGenerator.generateConsoleReport(currentReport));
    }

    private void exportReportMenu() {
        if (currentReport == null) {
            System.out.println("\n[!] No validation report available. Load a file (Option 1) and run validation (Option 2) first.");
            return;
        }

        System.out.print("\nEnter export path [Default: report.txt]: ");
        String exportPath = scanner.nextLine().trim();
        if (exportPath.isEmpty()) {
            exportPath = "report.txt";
        }

        try {
            ReportGenerator.exportReport(currentReport, exportPath);
            System.out.println("\n[✓] SUCCESS: Report saved to '" + exportPath + "'.");
        } catch (IOException e) {
            System.err.println("\n[✗] ERROR exporting report: " + e.getMessage());
        }
    }

    private void launchGuiMenu() {
        System.out.println("\nLaunching Desktop Swing GUI window...");
        try {
            SwingUtilities.invokeLater(() -> {
                ValidatorGUI gui = new ValidatorGUI();
                gui.setVisible(true);
            });
            System.out.println("[✓] Desktop GUI window opened.");
        } catch (Exception e) {
            System.err.println("[✗] Could not open GUI window (headless/display issue): " + e.getMessage());
        }
    }

    private void launchWebMenu() {
        System.out.println("\nStarting internal Web UI Server...");
        try {
            WebUIServer webServer = new WebUIServer();
            webServer.startServer();
        } catch (Exception e) {
            System.err.println("[✗] Could not start Web Server: " + e.getMessage());
        }
    }
}
