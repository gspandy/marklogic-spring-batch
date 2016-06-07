package com.marklogic.spring.batch.core.repository;

import com.marklogic.appdeployer.command.Command;
import com.marklogic.appdeployer.command.databases.DeployDatabaseCommand;
import com.marklogic.appdeployer.command.restapis.DeployRestApiServersCommand;
import com.marklogic.appdeployer.command.security.DeployProtectedCollectionsCommand;
import com.marklogic.appdeployer.command.security.DeployRolesCommand;
import com.marklogic.appdeployer.command.security.DeployUsersCommand;
import com.marklogic.appdeployer.impl.AbstractAppDeployer;
import com.marklogic.client.helper.LoggingObject;
import com.marklogic.mgmt.api.API;
import com.marklogic.mgmt.api.restapi.RestApi;
import com.marklogic.mgmt.api.security.Role;
import com.marklogic.mgmt.api.security.User;
import com.marklogic.mgmt.databases.DatabaseManager;
import com.marklogic.mgmt.restapis.RestApiManager;
import com.marklogic.mgmt.security.RoleManager;
import com.sun.javafx.binding.Logging;
import org.springframework.http.HttpMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sstafford on 6/6/2016.
 */
public class MarkLogicSimpleJobRepositoryAppDeployer extends LoggingObject {

    private List<Command> commands;
    private MarkLogicSimpleJobRepositoryConfig config;

    public MarkLogicSimpleJobRepositoryAppDeployer(MarkLogicSimpleJobRepositoryConfig config) {
        this.config = config;
    }

    public void deploy(String host, int port) {
        config.getRestApi(port).save();

        DatabaseManager dbMgr = new DatabaseManager(config.getManageClient());
        dbMgr.save(config.getDatabase());

        for (String role : config.getRoles()) {
            RoleManager roleMgr = new RoleManager(config.getManageClient());
            roleMgr.save(role);
        }

        for (User user : config.getUsers()) {
            user.save();
        }

        config.getProtectedCollection().save();
    }

    public void undeploy(String host, int port) {
        for (User user : config.getUsers()) {
            user.delete();
        }

        for (String role : config.getRoles()) {
            RoleManager roleMgr = new RoleManager(config.getManageClient());
            roleMgr.delete(role);
        }

        config.getProtectedCollection().delete();

        config.getRestApi(port).delete();
    }
}
