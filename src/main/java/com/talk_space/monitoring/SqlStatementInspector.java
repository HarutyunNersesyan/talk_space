package com.talk_space.monitoring;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.resource.jdbc.spi.StatementInspector;

@Slf4j
public class SqlStatementInspector implements StatementInspector {

    private static final long SLOW_SQL_THRESHOLD_MS = 500;

    @Override
    public String inspect(String sql) {
        // Log slow SQL queries for optimization
        if (isPotentialSlowQuery(sql)) {
            log.debug("Potential slow SQL detected: {}", shortenSql(sql));
        }
        return sql;
    }

    private boolean isPotentialSlowQuery(String sql) {
        if (sql == null) return false;

        String lowerSql = sql.toLowerCase();

        // Detect potential performance issues
        return lowerSql.contains(" like '%") || // Leading wildcard like
                lowerSql.contains(" or ") || // Multiple OR conditions
                lowerSql.contains(" not in ") || // NOT IN queries
                (countJoins(sql) > 3) || // Too many joins
                (sql.length() > 1000); // Very long queries
    }

    private int countJoins(String sql) {
        String lowerSql = sql.toLowerCase();
        int count = 0;
        count += countOccurrences(lowerSql, " join ");
        count += countOccurrences(lowerSql, " inner join ");
        count += countOccurrences(lowerSql, " left join ");
        count += countOccurrences(lowerSql, " right join ");
        return count;
    }

    private int countOccurrences(String text, String pattern) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(pattern, index)) != -1) {
            count++;
            index += pattern.length();
        }
        return count;
    }

    private String shortenSql(String sql) {
        if (sql.length() <= 200) return sql;
        return sql.substring(0, 200) + "...";
    }
}