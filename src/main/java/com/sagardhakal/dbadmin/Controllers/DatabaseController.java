package com.sagardhakal.dbadmin.Controllers;

import com.sagardhakal.dbadmin.Models.DatabaseDetail;
import com.sagardhakal.dbadmin.Request.DatabaseCreateRequest;
import com.sagardhakal.dbadmin.Service.DatabaseService;
import com.sagardhakal.dbadmin.Service.DynamicDatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class DatabaseController {

    @Autowired
    private DatabaseService databaseService;
    @Autowired
    private DynamicDatabaseService dynamicDatabaseService;

    @GetMapping()
    public String index(Model model) {
        DatabaseCreateRequest databaseCreateRequest = new DatabaseCreateRequest();
        databaseCreateRequest.setDatabaseName("");
        databaseCreateRequest.setHost("127.0.0");
        databaseCreateRequest.setUsername("root");
        List<DatabaseDetail>databases = databaseService.getAllDatabaseDetails();
        model.addAttribute("databases", databases);
        model.addAttribute("databaseCreateRequest", databaseCreateRequest);
        return "index";
    }

    @PostMapping("createDatabase")
    public String createNewDatabase(@ModelAttribute(name = "databaseCreateRequest") @Validated DatabaseCreateRequest databaseCreateRequest, BindingResult bindingResult,Model model) {
        try{
            DatabaseDetail databaseDetail = new DatabaseDetail();
            databaseDetail.setDatabaseName(databaseCreateRequest.getDatabaseName());
            databaseDetail.setDatabaseType("MySql");
            databaseDetail.setUserName(databaseCreateRequest.getUsername());
            databaseDetail.setPassword(databaseCreateRequest.getPassword());
            databaseDetail.setHostName(databaseCreateRequest.getHost());
            databaseDetail.setPort(3306L);
            databaseService.createNewDatabase(databaseDetail);
            return "redirect:/";
        } catch (Exception e) {
            bindingResult.addError(new FieldError("databaseCreateRequest", "databaseName", e.getMessage()));
            return "index";
        }
    }

    @GetMapping("/tables/{id}")
    public String getAllDatabaseTable(@PathVariable("id")Long id,Model model) {
        try{
            DatabaseDetail databaseDetail =databaseService.getDatabaseDetails(id);
            List<String>databaseTable=dynamicDatabaseService.getAllTables(databaseDetail.getUserName(),databaseDetail.getHostName(),databaseDetail.getPassword(),databaseDetail.getDatabaseName());
            databaseTable=databaseTable.stream().sorted() // By default, sorts in ascending order
                    .collect(Collectors.toList());
            model.addAttribute("databaseTable",databaseTable );
            model.addAttribute("id",id);
            return "databasetables";
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "redirect:/";
        }
    }

    @GetMapping("/open-tables/{id}/{tableName}")
    public String getDatabaseContent(@PathVariable("tableName")String databaseName,@PathVariable("id")Long id,Model model){
        try{
            DatabaseDetail databaseDetail =databaseService.getDatabaseDetails(id);
            DynamicDatabaseService.MetaData metaData=dynamicDatabaseService.getAllTableRows(
                    databaseDetail.getUserName(),
                    databaseDetail.getHostName(),
                    databaseDetail.getPassword(),
                    databaseDetail.getDatabaseName(),
                    databaseName
            );
            model.addAttribute("datas",metaData.getDatas());
            model.addAttribute("keys",metaData.getKeys());
            model.addAttribute("databaseid",id);
            model.addAttribute("tableName",databaseName);
            return "databasevalues";
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "redirect:/";
        }
    }

    @PostMapping("/delete-database/{id}")
    public String deleteDatabase(@PathVariable("id")Long id,Model model){
        try{
            DatabaseDetail databaseDetail =databaseService.getDatabaseDetails(id);
            databaseService.delete(databaseDetail);
            return "redirect:/";
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "redirect:/";
        }
    }
    @PostMapping("/insert-tables/{id}/{tableName}")
    public String insertIntoTable(@RequestBody Map<String,Object>request,@PathVariable("tableName")String databaseName,@PathVariable("id")Long id,Model model){
        try{
            System.out.println("testas");
            DatabaseDetail databaseDetail =databaseService.getDatabaseDetails(id);
            dynamicDatabaseService.insertIntoData(
                    databaseDetail.getUserName(),
                    databaseDetail.getHostName(),
                    databaseDetail.getPassword(),
                    databaseDetail.getDatabaseName(),
                    databaseName,
                    request
            );
            return "redirect:/";
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "redirect:/";
        }
    }
}
