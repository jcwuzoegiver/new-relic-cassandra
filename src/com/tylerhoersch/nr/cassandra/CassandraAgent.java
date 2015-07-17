package com.tylerhoersch.nr.cassandra;

import com.newrelic.metrics.publish.Agent;
import com.newrelic.metrics.publish.util.Logger;
import com.tylerhoersch.nr.cassandra.templates.Cassandra2xInstances;
import com.tylerhoersch.nr.cassandra.templates.Cassandra2xMetrics;

import java.util.ArrayList;
import java.util.List;

public class CassandraAgent extends Agent {
    private static final Logger logger = Logger.getLogger(CassandraAgent.class);
    private static final String GUID = "com.tylerhoersch.nr.cassandra";
    private static final String VERSION = "1.0.0";
    private final String name;
    private final String host;
    private final String port;
    private final String username;
    private final String password;
    private final JMXRunner jmxRunner;

    public CassandraAgent(String name, String host, String port, String username, String password) {
        super(GUID, VERSION);

        this.name = name;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        jmxRunner = new JMXRunner(host, port, username, password);
    }

    @Override
    public String getAgentName() {
        return name;
    }

    @Override
    public void pollCycle() {
        List<Metric> metrics = new ArrayList<>();
        try {
            List<String> cassandraInstances = getCassandraInstances();
            for(String instance : cassandraInstances) {
                metrics = getCassandraMetrics(instance);
            }

        } catch (Exception e) {
            logger.error("Error Polling Cassandra: ", e);
        } finally {
            metrics.stream()
                   .filter(m -> m.value != null && !m.value.toString().equals("NaN"))
                   .forEach(m -> reportMetric(m.name, m.valueType, m.value));
        }
    }

    private List<String> getCassandraInstances() throws Exception {
        return jmxRunner.run(new Cassandra2xInstances());
    }

    private List<Metric> getCassandraMetrics(String instance) throws Exception {
        return jmxRunner.run(new Cassandra2xMetrics(instance));
    }
}