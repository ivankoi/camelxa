package com.ivankoi;

import org.apache.camel.language.XPath;

/**
 * @version $Revision$
 */
public class PartnerServiceBean {

    public String toSql(@XPath("partner/@id") int partnerId,
                        @XPath("partner/date/text()") String date,
                        @XPath("partner/code/text()") int statusCode,
                        @XPath("partner/time/text()") long responseTime) {

        if (partnerId <= 0) {
            throw new IllegalArgumentException("PartnerId is invalid, was " + partnerId);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO PARTNER_METRIC (partner_id, time_occurred, status_code, perf_time) VALUES (");
        sb.append("'").append(partnerId).append("', ");
        sb.append("'").append(date).append("', ");
        sb.append("'").append(statusCode).append("', ");
        sb.append("'").append(responseTime).append("') ");

        return sb.toString();
    }
}