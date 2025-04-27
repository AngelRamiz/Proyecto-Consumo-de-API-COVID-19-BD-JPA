
package com.mycompany.covidstatsapp.service;

import com.mycompany.covidstatsapp.model.Report;
import com.mycompany.covidstatsapp.repository.ReportRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Log4j2
public class ReportService {

    private final ReportRepository reportRepository;
    private final String baseUrl;
    private final String apiKey;
    private final String apiHost;
    private final String iso;
    private final String reportDate;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;

        Properties props = new Properties();
        try {
            props.load(getClass().getClassLoader().getResourceAsStream("application.properties"));
        } catch (Exception e) {
            log.error("Error al leer application.properties", e);
        }

        this.baseUrl = props.getProperty("api.base.url");
        this.apiKey = props.getProperty("api.key");
        this.apiHost = props.getProperty("api.host");
        this.iso = props.getProperty("api.country.iso");
        this.reportDate = props.getProperty("api.date");
    }

    public void fetchAndSaveReport() {
        try {
            String encodedIso = URLEncoder.encode(iso, StandardCharsets.UTF_8);
            String encodedDate = URLEncoder.encode(reportDate, StandardCharsets.UTF_8);
            String urlStr = String.format("%s/reports?iso=%s&date=%s", baseUrl, encodedIso, encodedDate);

            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("X-RapidAPI-Key", apiKey);
            connection.setRequestProperty("X-RapidAPI-Host", apiHost);

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                log.info("!!!Conexion exitosa a reports");
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder responseJson = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        responseJson.append(line);
                    }
                    parseAndSave(responseJson.toString());
                }
            } else {
                log.error("Error en la respuesta reports: " + responseCode);
            }

        } catch (Exception e) {
            log.error("Error al consumir reports", e);
        }
    }

    private void parseAndSave(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);
            JsonNode data = root.get("data");

            for (JsonNode node : data) {
                String reportDate = node.get("date").asText();

                if (reportRepository.existsByDate(reportDate)) {
                    log.info("Reporte ya existe para la fecha: " + reportDate);
                    continue;
                }

                Report report = new Report();
                report.setDate(reportDate);
                report.setConfirmed(node.get("confirmed").asInt());
                report.setDeaths(node.get("deaths").asInt());
                report.setRecovered(node.get("recovered").asInt());
                report.setActive(node.get("active").asInt());

                reportRepository.save(report);
                log.info("!!!Reporte guardado para fecha: " + report.getDate());
            }

        } catch (Exception e) {
            log.error("Error al parsear el JSON de reportes", e);
        }
    }
}