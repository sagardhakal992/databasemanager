package com.sagardhakal.dbadmin.Request;

import lombok.Data;

@Data
public class DatabaseCreateRequest {
    public String databaseName;
    public String username;
    private String password;
    private String host;
}
