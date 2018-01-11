/*
 * This is free and unencumbered software released into the public domain.
 * 
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 * 
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 * 
 * For more information, please refer to <http://unlicense.org/>
 */
package org.vaadin.backend;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import java.time.LocalDate;
import org.vaadin.backend.domain.Customer;
import org.vaadin.backend.domain.CustomerStatus;
import org.vaadin.backend.domain.Gender;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;
import java.util.Random;

@Stateless
public class CustomerService {

    @PersistenceContext(unitName = "customer-pu")
    private EntityManager entityManager;

    public void saveOrPersist(Customer entity) {
        if (entity.getId() > 0) {
            entityManager.merge(entity);
        } else {
            entityManager.persist(entity);
        }
    }

    public void deleteEntity(Customer entity) {
        if (entity.getId() > 0) {
            // reattach to remove
            entity = entityManager.merge(entity);
            entityManager.remove(entity);
        }
    }

    public List<Customer> findAll() {
        CriteriaQuery<Customer> cq = entityManager.getCriteriaBuilder().
                createQuery(Customer.class);
        cq.select(cq.from(Customer.class));
        return entityManager.createQuery(cq).getResultList();
    }

    public List<Customer> findByName(String filter) {
        if (filter == null || filter.isEmpty()) {
            return findAll();
        }
        filter = filter.toLowerCase();
        return entityManager.createNamedQuery("Customer.findByName",
                Customer.class)
                .setParameter("filter", filter + "%").getResultList();
    }

    /**
     * Sample data generation
     */
    public void ensureTestData() {
        if (findAll().isEmpty()) {
            final double latBase = 42.3791618;
            final double lonBase = -71.1138139;
            GeometryFactory factory = new GeometryFactory();
            final String[] names = new String[]{"Gabrielle Patel", "Brian Robinson", "Eduardo Haugen", "Koen Johansen", "Alejandro Macdonald", "Angel Karlsson", "Yahir Gustavsson", "Haiden Svensson", "Emily Stewart", "Corinne Davis", "Ryann Davis", "Yurem Jackson", "Kelly Gustavsson", "Eileen Walker", "Katelyn Martin", "Israel Carlsson", "Quinn Hansson", "Makena Smith", "Danielle Watson", "Leland Harris", "Gunner Karlsen", "Jamar Olsson", "Lara Martin", "Ann Andersson", "Remington Andersson", "Rene Carlsson", "Elvis Olsen", "Solomon Olsen", "Jaydan Jackson", "Bernard Nilsen"};
            Random r = new Random(0);
            for (String name : names) {
                String[] split = name.split(" ");
                Customer c = new Customer();
                c.setEngagementName(split[0]);
                c.setDescription(split[1]);
                c.setEmail(split[0].toLowerCase() + "@" + split[1].
                        toLowerCase() + ".com");
                c.setStatus(CustomerStatus.values()[r.nextInt(CustomerStatus.
                        values().length)]);
                LocalDate d = LocalDate.now();
                int daysOld = 0 - r.nextInt(365 * 15 + 365 * 60);
                d = d.plusDays(daysOld);
                c.setEngagementDate(d);

                c.setGender(r.nextDouble() < 0.7 ? Gender.Female : Gender.Male);

                double lat = latBase + 0.02 * r.nextDouble() - 0.02 * r.
                        nextDouble();
                double lon = lonBase + 0.02 * r.nextDouble() - 0.02 * r.
                        nextDouble();
                c.setLocation(factory.createPoint(new Coordinate(lon, lat)));
                saveOrPersist(c);
            }
        }
    }

    public void resetTestData() {
        if(!findAll().isEmpty()) {
            entityManager.createQuery("DELETE FROM Customer c WHERE c.id > 0").
                    executeUpdate();
        }
        ensureTestData();
    }

}
