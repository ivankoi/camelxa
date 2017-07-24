package com.ivankoi.routes;

import com.ivankoi.PartnerServiceBean;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Created by ivankoi on 7/24/17.
 */
@Component
public class RollbackAfterDbRouteBuilder extends SpringRouteBuilder{
    public void configure() throws Exception {
        from("activemq:queue:partners")
                .transacted()
                .log("*** before database independent route def***")
                .bean(PartnerServiceBean.class, "toSql")
                .to("jdbc:myDataSource?resetAutoCommit=false")          // the usage of the resetAutoCommit option (available since 2.9.0) has the side effect of JDBC commit
                .log("*** after database independent route def***")     // not being called through JdbcProducer (We need this as we make use of global transaction boundaries)
                .throwException(new IllegalArgumentException("Forced failure after DB"));
    }
}
