
package com.mycompany.covidstatsapp.repository;

import com.mycompany.covidstatsapp.model.Report;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import java.util.List;

public class ReportRepository {

    private final EntityManager entityManager;

    public ReportRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void save(Report report) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            entityManager.persist(report);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
        }
    }

    public List<Report> findAll() {
        return entityManager.createQuery("SELECT r FROM Report r", Report.class).getResultList();
    }

    // Método para verificar si ya existe un reporte con la fecha dada
    public boolean existsByDate(String date) {
        // Consulta HQL/JPA que cuenta los reportes con la fecha especificada
        String jpql = "SELECT COUNT(r) FROM Report r WHERE r.date = :date";
        
        // Creamos la consulta con el EntityManager
        Query query = entityManager.createQuery(jpql);
        query.setParameter("date", date);  // Asignamos el valor de la fecha al parámetro
        
        Long count = (Long) query.getSingleResult();  // Ejecutamos la consulta y obtenemos el conteo
        
        return count > 0;  // Si el conteo es mayor a 0, significa que ya existe un reporte para esa fecha
    }
}
