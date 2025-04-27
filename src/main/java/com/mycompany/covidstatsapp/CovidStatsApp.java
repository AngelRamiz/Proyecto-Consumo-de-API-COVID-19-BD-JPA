
package com.mycompany.covidstatsapp;

import com.mycompany.covidstatsapp.config.EntityManagerFactoryProvider;
import com.mycompany.covidstatsapp.repository.*;
import com.mycompany.covidstatsapp.service.*;
import com.mycompany.covidstatsapp.thread.DataCollectorThread;

import javax.persistence.EntityManager;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CovidStatsApp {

    public static void main(String[] args) {
        Logger logger = Logger.getLogger(CovidStatsApp.class.getName());
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);
        logger.addHandler(consoleHandler);

        EntityManager entityManager = null;

        try {
            // Obtener el EntityManager esde la clase de configuración
            entityManager = EntityManagerFactoryProvider.getEntityManager();

            // Inicializar repositorios y servicios
            RegionRepository regionRepository = new RegionRepository(entityManager);
            ProvinceRepository provinceRepository = new ProvinceRepository(entityManager);
            ReportRepository reportRepository = new ReportRepository(entityManager);

            RegionService regionService = new RegionService(regionRepository);
            ProvinceService provinceService = new ProvinceService(provinceRepository);
            ReportService reportService = new ReportService(reportRepository);

            // Iniciar el hilo
            DataCollectorThread dataCollectorThread = new DataCollectorThread(regionService, provinceService, reportService);
            dataCollectorThread.start();
            dataCollectorThread.join();

        } catch (Exception e) {
            logger.severe ("Error en la aplicacion: {0}"+ e.getMessage());
            e.printStackTrace();
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
            logger.info("Aplicacion finalizada.");
        }
    }
}
