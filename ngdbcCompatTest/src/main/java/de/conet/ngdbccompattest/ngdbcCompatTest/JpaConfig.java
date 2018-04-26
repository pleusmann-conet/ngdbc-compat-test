package de.conet.ngdbccompattest.ngdbcCompatTest;

import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import com.sap.db.jdbcext.DataSourceSAP;
import com.sap.xs.env.Credentials;
import com.sap.xs.env.Service;
import com.sap.xs.env.VcapServices;

@Configuration
public class JpaConfig {
    @Bean(name = "serviceDs")
    public DataSource dataSource() {
        
        String hanaServiceName = "hana";
        
        Credentials credentials = getDBService(hanaServiceName).getCredentials();
        
        for (Map.Entry<String, Object> cred : credentials.any().entrySet()) {
            System.out.println(cred.getKey() + " : " + cred.getValue());
        }
        
        DataSourceSAP dataSource = new DataSourceSAP();
        dataSource.setUser(credentials.getUser());
        dataSource.setPassword(credentials.getPassword());
        dataSource.setPort(Integer.parseInt(credentials.getPort()));
        dataSource.setServerName(credentials.getHost());
        dataSource.setSchema((String) credentials.get("schema"));
        
        return dataSource;
    }
    
    private Service getDBService(String hanaServiceName) {
        VcapServices services = VcapServices.fromEnvironment();
        
        Service service = services.findService(hanaServiceName, "", "");
        if (service == null) {
            return null;
        }
        return service;
    }
    
    @Bean(name = "entityManagerFactory")
    LocalContainerEntityManagerFactoryBean entityManagerFactory(@Qualifier("serviceDs") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setDataSource(dataSource);
        entityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        entityManagerFactoryBean.setPackagesToScan("de.conet.ngdbccompattest.domain");
        
        Properties jpaProperties = new Properties();
        
        // Configures the used database dialect. This allows Hibernate to create SQL
        // that is optimized for the used database.
        jpaProperties.put("hibernate.dialect", "org.hibernate.dialect.HANARowStoreDialect");
        // jpaProperties.put("hibernate.physical_naming_strategy", HanaPhysicalNamingStrategy.class.getName());
        
        jpaProperties.put("hibernate.hbm2ddl.auto", "validate");
        
        String schema = getDBService("hana").getCredentials().get("schema").toString();
        jpaProperties.put("hibernate.default_schema", schema);
        
        entityManagerFactoryBean.setJpaProperties(jpaProperties);
        
        return entityManagerFactoryBean;
    }
    
    @Bean
    JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;
    }
}
