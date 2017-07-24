package com.ivankoi;

import org.apache.camel.CamelContext;
import org.apache.camel.ConsumerTemplate;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.test.spring.CamelSpringDelegatingTestContextLoader;
import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;

import javax.sql.DataSource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @version $Revision$
 */
@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring-context.xml"}, loader = CamelSpringDelegatingTestContextLoader.class)
public class AtomikosXARollbackAfterDbTest {

    private JdbcTemplate jdbc;

    @Autowired
    ProducerTemplate template;

    @Autowired
    ConsumerTemplate consumer;

    @Autowired
    CamelContext context;

    @Before
    public void setupDatabase() throws Exception {
        DataSource ds = context.getRegistry().lookupByNameAndType("myDataSource", DataSource.class);
        jdbc = new JdbcTemplate(ds);

        jdbc.execute("create table partner_metric "
                + "( partner_id varchar(10), time_occurred varchar(20), status_code varchar(3), perf_time varchar(10) )");
    }

    @After
    public void dropDatabase() throws Exception {
        jdbc.execute("drop table partner_metric");
    }

    @Test
    public void testXaRollbackAfterDb() throws Exception {
        // there should be 0 row in the database when we start
        assertEquals(0, jdbc.queryForInt("select count(*) from partner_metric"));

        String xml = "<?xml version=\"1.0\"?><partner id=\"123\"><date>200911150815</date><code>200</code><time>4387</time></partner>";
        template.sendBody("activemq:queue:partners", xml);

        // wait for the route to complete with failure
        Thread.sleep(15000);

        // data not inserted so there should be 0 rows
        assertEquals(0, jdbc.queryForInt("select count(*) from partner_metric"));

        // should be in DLQ
        // now check that the message is on the queue by consuming it again
        String dlq = consumer.receiveBodyNoWait("activemq:queue:ActiveMQ.DLQ", String.class);
        assertNotNull("Should not lose message", dlq);
    }

}