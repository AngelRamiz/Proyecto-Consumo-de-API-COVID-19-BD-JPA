
package com.mycompany.covidstatsapp.thread;

import com.mycompany.covidstatsapp.service.RegionService;
import com.mycompany.covidstatsapp.service.ProvinceService;
import com.mycompany.covidstatsapp.service.ReportService;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class DataCollectorThread extends Thread {

    private final RegionService regionService;
    private final ProvinceService provinceService;
    private final ReportService reportService;

    public DataCollectorThread(RegionService regionService, ProvinceService provinceService, ReportService reportService) {
        this.regionService = regionService;
        this.provinceService = provinceService;
        this.reportService = reportService;
    }

    @Override
    public void run() {
        try {
            log.info("Esperando 15 segundos antes de iniciar la API ...");
            Thread.sleep(15000);

            log.info("Iniciando consumo de datos...");
            regionService.fetchAndSaveRegions();
            provinceService.fetchAndSaveProvinces();
            reportService.fetchAndSaveReport();

            log.info("Proceso de consumo de datos de la api !completado!.");
        } catch (InterruptedException e) {
            log.error("Hilo interrumpido", e);
        } catch (Exception e) {
            log.error("Error en el hilo de recolección de datos", e);
        }
    }
}
