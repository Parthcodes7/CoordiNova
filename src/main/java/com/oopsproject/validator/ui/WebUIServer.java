package com.oopsproject.validator.ui;

import com.oopsproject.validator.model.Coordinate;
import com.oopsproject.validator.model.ValidationReport;
import com.oopsproject.validator.service.CsvDatasetLoader;
import com.oopsproject.validator.service.ValidationEngine;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

/**
 * Built-in Pure Java Web Server.
 * No Tomcat or Maven needed! Keeps the project perfectly simple.
 */
public class WebUIServer {

    public void startServer() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);
            server.createContext("/", new UIHandler());
            server.setExecutor(null);
            server.start();
            System.out.println("=================================================");
            System.out.println("  Web UI is running!");
            System.out.println("  Open your browser to: http://localhost:8081");
            System.out.println("=================================================");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class UIHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String responseHTML = "";

            if ("GET".equalsIgnoreCase(method)) {
                responseHTML = generatePageHTML(null, null);
            } else if ("POST".equalsIgnoreCase(method)) {
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(isr);
                String formData = br.readLine();
                
                String csvData = "";
                if (formData != null && formData.startsWith("csvData=")) {
                    csvData = URLDecoder.decode(formData.substring(8), StandardCharsets.UTF_8.name());
                }

                try {
                    // Save to temp file
                    File tempFile = File.createTempFile("dataset", ".csv");
                    Files.writeString(tempFile.toPath(), csvData);

                    // Validate
                    CsvDatasetLoader loader = new CsvDatasetLoader();
                    List<Coordinate> coords = loader.load(tempFile.getAbsolutePath());
                    
                    ValidationEngine engine = ValidationEngine.createDefaultEngine();
                    ValidationReport report = engine.validate(coords);

                    responseHTML = generatePageHTML(report, null);
                    tempFile.delete();
                } catch (Exception e) {
                    responseHTML = generatePageHTML(null, "Error processing dataset: " + e.getMessage());
                }
            }

            exchange.sendResponseHeaders(200, responseHTML.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(responseHTML.getBytes());
            os.close();
        }

        private String generatePageHTML(ValidationReport report, String errorMsg) {
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html><head><title>AI Dataset Validator - Dashboard</title>");
            html.append("<meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
            html.append("<link href=\"https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap\" rel=\"stylesheet\">");
            html.append("<style>");
            html.append(":root { --primary: #3b82f6; --primary-hover: #2563eb; --bg: #f8fafc; --surface: #ffffff; --text: #0f172a; --text-muted: #64748b; --success: #10b981; --error: #ef4444; --border: #e2e8f0; }");
            html.append("body { font-family: 'Inter', sans-serif; background-color: var(--bg); color: var(--text); margin: 0; padding: 2rem; display: flex; justify-content: center; }");
            html.append(".container { max-width: 1200px; width: 100%; display: flex; flex-direction: column; gap: 2rem; }");
            html.append(".header { text-align: center; margin-bottom: 1rem; }");
            html.append(".header h1 { font-size: 2.5rem; font-weight: 700; margin: 0; background: linear-gradient(135deg, #3b82f6, #8b5cf6); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }");
            html.append(".header p { color: var(--text-muted); font-size: 1.1rem; margin-top: 0.5rem; }");
            html.append(".card { background: var(--surface); padding: 1.5rem; border-radius: 12px; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -2px rgba(0, 0, 0, 0.05); border: 1px solid var(--border); }");
            html.append(".kpi-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1rem; margin-bottom: 2rem; }");
            html.append(".kpi-card { padding: 1.5rem; border-radius: 12px; color: white; display: flex; flex-direction: column; justify-content: space-between; }");
            html.append(".kpi-card .label { font-size: 0.9rem; font-weight: 600; opacity: 0.9; text-transform: uppercase; }");
            html.append(".kpi-card .value { font-size: 2.5rem; font-weight: 700; margin-top: 0.5rem; }");
            html.append("textarea { width: 100%; height: 120px; padding: 1rem; border: 1px solid var(--border); border-radius: 8px; font-family: 'Courier New', monospace; font-size: 0.9rem; resize: vertical; box-sizing: border-box; margin-bottom: 1rem; }");
            html.append("button { background-color: var(--primary); color: white; padding: 0.75rem 1.5rem; border: none; border-radius: 8px; cursor: pointer; font-size: 1rem; font-weight: 600; transition: all 0.2s; }");
            html.append("button:hover { background-color: var(--primary-hover); transform: translateY(-1px); }");
            html.append(".controls { display: flex; justify-content: space-between; gap: 1rem; margin-bottom: 1rem; align-items: center; }");
            html.append("input, select { padding: 0.5rem; border: 1px solid var(--border); border-radius: 6px; font-size: 0.9rem; }");
            html.append("table { width: 100%; border-collapse: collapse; text-align: left; font-size: 0.9rem; }");
            html.append("th, td { padding: 0.75rem 1rem; border-bottom: 1px solid var(--border); }");
            html.append("th { background-color: #f1f5f9; font-weight: 600; color: var(--text-muted); position: sticky; top: 0; }");
            html.append("tbody tr:hover { background-color: #f8fafc; }");
            html.append(".status-valid { color: var(--success); font-weight: 600; }");
            html.append(".status-invalid { color: var(--error); font-weight: 600; }");
            html.append(".table-container { max-height: 500px; overflow-y: auto; border: 1px solid var(--border); border-radius: 8px; }");
            html.append("</style></head><body>");
            
            html.append("<div class='container'>");
            
            html.append("<div class='header'>");
            html.append("<h1>Dataset Coordinate Validator</h1>");
            html.append("<p>Modern Web Dashboard</p>");
            html.append("</div>");

            if (errorMsg != null) {
                html.append("<div style='background: #fee2e2; color: #ef4444; padding: 1rem; border-radius: 8px; margin-bottom: 1rem; border: 1px solid #f87171;'>").append(errorMsg).append("</div>");
            }
            
            html.append("<div class='card'>");
            html.append("<form method='POST' action='/'>");
            html.append("<h3 style='margin-top:0;'>Paste Dataset (CSV):</h3>");
            html.append("<textarea name='csvData' placeholder='id,label,latitude,longitude\n1,Point A,28.61,77.20\n2,Point B,-12.34,45.67\n3,Point C, 200.0, 90.0'></textarea>");
            html.append("<br><button type='submit'>Run Validation</button>");
            html.append("</form>");
            html.append("</div>");

            if (report != null) {
                double passRate = report.getTotalCount() > 0 ? (report.getValidCount() * 100.0 / report.getTotalCount()) : 0.0;
                
                html.append("<div class='kpi-grid'>");
                html.append("<div class='kpi-card' style='background-color: #3182ce;'><span class='label'>Total Rows</span><span class='value'>").append(report.getTotalCount()).append("</span></div>");
                html.append("<div class='kpi-card' style='background-color: #38a169;'><span class='label'>Valid Rows</span><span class='value'>").append(report.getValidCount()).append("</span></div>");
                html.append("<div class='kpi-card' style='background-color: #e53e3e;'><span class='label'>Invalid Rows</span><span class='value'>").append(report.getInvalidCount()).append("</span></div>");
                html.append("<div class='kpi-card' style='background-color: #805ad5;'><span class='label'>Pass Rate</span><span class='value'>").append(String.format("%.1f%%", passRate)).append("</span></div>");
                html.append("</div>");
                
                html.append("<div class='card'>");
                html.append("<div class='controls'>");
                html.append("<div><strong>Dataset Results Matrix</strong></div>");
                html.append("<div>");
                html.append("<label>Filter: </label>");
                html.append("<select id='statusFilter' onchange='filterTable()'><option value='ALL'>All Rows</option><option value='VALID'>Valid Only</option><option value='INVALID'>Invalid Only</option></select> ");
                html.append("<input type='text' id='searchInput' onkeyup='filterTable()' placeholder='Search labels or errors...'>");
                html.append("</div></div>");
                
                html.append("<div class='table-container'>");
                html.append("<table id='resultsTable'><thead><tr>");
                html.append("<th>Row #</th><th>Label</th><th>Latitude</th><th>Longitude</th><th>Status</th><th>Failure Reason(s)</th>");
                html.append("</tr></thead><tbody>");
                
                for (com.oopsproject.validator.model.ValidationResult res : report.getResults()) {
                    var c = res.getCoordinate();
                    String statusStr = res.isValid() ? "VALID" : "INVALID";
                    String statusClass = res.isValid() ? "status-valid" : "status-invalid";
                    String lat = c.isLatitudeMissing() ? "<MISSING>" : String.valueOf(c.getRawLatitude());
                    String lon = c.isLongitudeMissing() ? "<MISSING>" : String.valueOf(c.getRawLongitude());
                    String errors = res.isValid() ? "None" : String.join("; ", res.getErrorMessages());
                    
                    html.append("<tr class='datarow' data-status='").append(statusStr).append("'>");
                    html.append("<td>").append(c.getRowNumber()).append("</td>");
                    html.append("<td>").append(c.getLabel() == null ? "" : c.getLabel()).append("</td>");
                    html.append("<td>").append(lat).append("</td>");
                    html.append("<td>").append(lon).append("</td>");
                    html.append("<td class='").append(statusClass).append("'>").append(statusStr).append("</td>");
                    html.append("<td>").append(errors).append("</td>");
                    html.append("</tr>");
                }
                
                html.append("</tbody></table>");
                html.append("</div>"); // table-container
                html.append("</div>"); // card
                
                // Add JavaScript for filtering
                html.append("<script>");
                html.append("function filterTable() {");
                html.append("  var filter = document.getElementById('statusFilter').value;");
                html.append("  var search = document.getElementById('searchInput').value.toLowerCase();");
                html.append("  var rows = document.querySelectorAll('.datarow');");
                html.append("  rows.forEach(function(row) {");
                html.append("    var status = row.getAttribute('data-status');");
                html.append("    var text = row.textContent.toLowerCase();");
                html.append("    var matchesStatus = (filter === 'ALL' || status === filter);");
                html.append("    var matchesSearch = (search === '' || text.includes(search));");
                html.append("    row.style.display = (matchesStatus && matchesSearch) ? '' : 'none';");
                html.append("  });");
                html.append("}");
                html.append("</script>");
            }
            
            html.append("</div></body></html>");
            return html.toString();
        }
    }
}
