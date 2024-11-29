package com.sagardhakal.dbadmin.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.Data;
import org.springframework.data.jpa.repository.Meta;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class DynamicDatabaseService {

    public List<String> getAllTables(String username, String host, String password, String databaseName) throws SQLException {
        // Step 1: Ensure the password is empty if not provided
        if (password == null) {
            password = ""; // Explicitly set the password to an empty string
        }

        // Step 2: Create a dynamic DataSource
        DataSource dataSource = createDataSource(username, host, password, databaseName);

        // Step 3: Create an EntityManagerFactory dynamically
        EntityManagerFactory entityManagerFactory = createEntityManagerFactory(dataSource, password,username);

        // Step 4: Create an EntityManager
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        try {
            // Step 5: Query database metadata for tables
            String query = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = :databaseName";
            return entityManager.createNativeQuery(query)
                    .setParameter("databaseName", databaseName)
                    .getResultList();
        } finally {
            // Close the EntityManager when done
            entityManager.close();
        }
    }

    public void insertIntoData(String userName, String hostName, String password, String databaseName, String tablename, Map<String, Object> request) throws SQLException {
        // Step 1: Ensure the password is empty if not provided
        if (password == null) {
            password = ""; // Explicitly set the password to an empty string
        }

        // Step 2: Create a dynamic DataSource
        DataSource dataSource = createDataSource(userName, hostName, password, databaseName);

        // Step 3: Create an EntityManagerFactory dynamically
        EntityManagerFactory entityManagerFactory = createEntityManagerFactory(dataSource, password, userName);

        // Step 4: Create an EntityManager (if you need it for metadata purposes)
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        try {
            // Step 5: Get column names
            List<String> columnNames = getColumnNames(entityManager, databaseName, tablename);

            // Step 6: Build the SQL insert query with placeholders for the values
            StringBuilder query = new StringBuilder("INSERT INTO " + tablename + " (");

            // Append column names
            for (int i = 0; i < columnNames.size(); i++) {
                query.append(columnNames.get(i));
                if (i != columnNames.size() - 1) {
                    query.append(", ");
                }
            }
            query.append(") VALUES (");

            // Append placeholders for values
            for (int i = 0; i < columnNames.size(); i++) {
                query.append("?");
                if (i != columnNames.size() - 1) {
                    query.append(", ");
                }
            }
            query.append(")");

            // Step 7: Get Connection from DataSource and prepare the statement
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(query.toString())) {

                // Step 8: Set the values for the placeholders in the prepared statement
                for (int i = 0; i < columnNames.size(); i++) {
                    // Get the value from the request map and bind it to the statement
                    Object value = request.get(columnNames.get(i));

                    if (value != null) {
                        preparedStatement.setObject(i + 1, value); // Use setObject to handle various types
                    } else {
                        preparedStatement.setNull(i + 1, java.sql.Types.NULL); // Set null if the value is null
                    }
                }

                // Step 9: Execute the insert
                preparedStatement.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();  // This will print the stack trace to the console
                // Handle SQL exception (for debugging or logging)
                throw new SQLException("Error executing insert statement", e);
            }

        } finally {
            // Close the EntityManager when done (if used for metadata)
            entityManager.close();
        }
    }

    @Data
    public class MetaData{
        private List<String>keys;
        private List<List<Cell>>datas;
    }

    @Data
    public class Cell{
        private String key;
        private Object value;
    }



    public MetaData getAllTableRows(String username, String host, String password, String databaseName, String tableName) throws SQLException {
        // Step 1: Ensure the password is empty if not provided
        if (password == null) {
            password = ""; // Explicitly set the password to an empty string
        }

        // Step 2: Create a dynamic DataSource
        DataSource dataSource = createDataSource(username, host, password, databaseName);

        // Step 3: Create an EntityManagerFactory dynamically
        EntityManagerFactory entityManagerFactory = createEntityManagerFactory(dataSource, password, username);

        // Step 4: Create an EntityManager
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        try {
            String query = "SELECT * FROM " + tableName;
            List<String>columnNames=getColumnNames(entityManager,databaseName,tableName);
           List<Object[]> rows = entityManager.createNativeQuery(query).getResultList();
           List<List<Cell>>result=new ArrayList<>();
            for (Object[] row : rows) {
                List<Cell>cells=new ArrayList<>();
                for (int i = 0; i < columnNames.size(); i++) {
                    Cell rowMap = new Cell();
                    rowMap.setKey(columnNames.get(i));
                    rowMap.setValue(row[i]);
                    cells.add(rowMap);
                }
                result.add(cells);
            }
            MetaData metaData = new MetaData();
            metaData.setDatas(result);
            metaData.setKeys(columnNames);
           return metaData;
        } finally {
            // Close the EntityManager when done
            entityManager.close();
        }
    }

    private DataSource createDataSource(String username, String host, String password, String databaseName) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        System.out.println(host);
        System.out.println(databaseName);
        dataSource.setUrl("jdbc:mysql://" + host + ":3306/" + databaseName + "?useSSL=false");
        System.out.println(dataSource.getUrl());
        dataSource.setUsername(username);
        dataSource.setPassword(password); // Set password as an empty string if none is provided
        return dataSource;
    }

    private List<String> getColumnNames(EntityManager entityManager,String databaseName,String tableName) throws SQLException {
        String queryForColumns= """
                    SELECT COLUMN_NAME
                    FROM INFORMATION_SCHEMA.COLUMNS
                    WHERE TABLE_SCHEMA = :databaseName
                      AND TABLE_NAME = :tableName;
                    
                    """;
        List<String> columnNames = entityManager.createNativeQuery(queryForColumns)
                .setParameter("databaseName", databaseName)
                .setParameter("tableName", tableName)
                .getResultList();
        return columnNames;
    }

    private EntityManagerFactory createEntityManagerFactory(DataSource dataSource, String password,String username) throws SQLException {
        Map<String, Object> properties = new HashMap<>();

        // JDBC properties
        System.out.println(dataSource.getConnection().getMetaData().getURL());
        System.out.println( dataSource.getConnection().getMetaData().getUserName());
        properties.put("javax.persistence.jdbc.driver", "com.mysql.cj.jdbc.Driver");
        properties.put("javax.persistence.jdbc.url", dataSource.getConnection().getMetaData().getURL());
        properties.put("javax.persistence.jdbc.user",username);
        properties.put("javax.persistence.jdbc.password", password); // Ensure password is passed correctly

        // Remove hibernate.dialect since it will be detected automatically for MySQL 8

        // Create the EntityManagerFactory using the dynamic dataSource and JPA properties
        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setJpaPropertyMap(properties);
        factoryBean.setPackagesToScan("com.sagardhakal.dbadmin"); // Update to your entity package if necessary
        factoryBean.setPersistenceProviderClass(org.hibernate.jpa.HibernatePersistenceProvider.class); // Specify Hibernate as the provider
        factoryBean.afterPropertiesSet();  // Ensure properties are initialized

        return factoryBean.getObject();
    }
}
