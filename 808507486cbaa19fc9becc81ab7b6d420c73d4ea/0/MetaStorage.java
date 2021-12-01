package com.bakdata.conquery.io.storage;

import com.bakdata.conquery.models.auth.entities.Group;
import com.bakdata.conquery.models.auth.entities.Role;
import com.bakdata.conquery.models.auth.entities.User;
import com.bakdata.conquery.models.config.StoreFactory;
import com.bakdata.conquery.models.execution.ManagedExecution;
import com.bakdata.conquery.models.forms.configs.FormConfig;
import com.bakdata.conquery.models.identifiable.CentralRegistry;
import com.bakdata.conquery.models.identifiable.ids.specific.*;
import com.bakdata.conquery.models.worker.DatasetRegistry;
import lombok.Getter;
import lombok.SneakyThrows;

import javax.validation.Validator;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class MetaStorage implements ConqueryStorage{

    private IdentifiableStore<ManagedExecution<?>> executions;
    private IdentifiableStore<FormConfig> formConfigs;
    private IdentifiableStore<User> authUser;
    private IdentifiableStore<Role> authRole;
    private IdentifiableStore<Group> authGroup;


    @Getter
    private DatasetRegistry datasetRegistry;
    @Getter
    protected final CentralRegistry centralRegistry = new CentralRegistry();
    @Getter
    protected final Validator validator;

    public MetaStorage(Validator validator, StoreFactory storageFactory, List<String> pathName, DatasetRegistry datasetRegistry) {
        this.datasetRegistry = datasetRegistry;
        this.validator = validator;

        executions = storageFactory.createExecutionsStore(centralRegistry, datasetRegistry, pathName);
        formConfigs = storageFactory.createFormConfigStore(centralRegistry, pathName);
        authUser = storageFactory.createUserStore(centralRegistry, pathName);
        authRole = storageFactory.createRoleStore(centralRegistry, pathName);
        authGroup = storageFactory.createGroupStore(centralRegistry, pathName);
    }

    @Override
    public void loadData() {
        executions.loadData();
        formConfigs.loadData();
        authUser.loadData();
        authRole.loadData();
        authGroup.loadData();
    }

    @Override
    public void clear() {
        centralRegistry.clear();

        executions.clear();
        formConfigs.clear();
        authUser.clear();
        authRole.clear();
        authGroup.clear();
    }

    @Override
    public void removeStorage() {
        executions.removeStore();
        formConfigs.removeStore();
        authUser.removeStore();
        authRole.removeStore();
        authGroup.removeStore();
    }

    public void addExecution(ManagedExecution<?> query) {
        executions.add(query);
    }

    public ManagedExecution<?> getExecution(ManagedExecutionId id) {
        return executions.get(id);
    }

    public Collection<ManagedExecution<?>> getAllExecutions() {
        return executions.getAll();
    }

    public void updateExecution(ManagedExecution<?> query) {
        executions.update(query);
    }

    public void removeExecution(ManagedExecutionId id) {
        executions.remove(id);
    }

    public void addUser(User user) {
        authUser.add(user);
    }

    public User getUser(UserId userId) {
        return authUser.get(userId);
    }

    public Collection<User> getAllUsers() {
        return authUser.getAll();
    }

    public void removeUser(UserId userId) {
        authUser.remove(userId);
    }

    public void addRole(Role role) {
        authRole.add(role);
    }

    public Role getRole(RoleId roleId) {
        return authRole.get(roleId);
    }

    public Collection<Role> getAllRoles() {
        return authRole.getAll();
    }

    public void removeRole(RoleId roleId) {
        authRole.remove(roleId);
    }

    public void updateUser(User user) {
        authUser.update(user);
    }

    public void updateRole(Role role) {
        authRole.update(role);
    }

    public void addGroup(Group group) {
        authGroup.add(group);
    }

    public Group getGroup(GroupId id) {
        return authGroup.get(id);
    }

    public Collection<Group> getAllGroups() {
        return authGroup.getAll();
    }

    public void removeGroup(GroupId id) {
        authGroup.remove(id);
    }

    public void updateGroup(Group group) {
        authGroup.update(group);
    }

    public FormConfig getFormConfig(FormConfigId id) {
        return formConfigs.get(id);
    }

    public Collection<FormConfig> getAllFormConfigs() {
        return formConfigs.getAll();
    }

    public void removeFormConfig(FormConfigId id) {
        formConfigs.remove(id);
    }

    @SneakyThrows
    public void updateFormConfig(FormConfig formConfig) {
        formConfigs.update(formConfig);
    }

    @SneakyThrows
    public void addFormConfig(FormConfig formConfig) {
        formConfigs.add(formConfig);
    }

    public void close() throws IOException {
        executions.close();
        formConfigs.close();
        authUser.close();
        authRole.close();
        authGroup.close();
    }
}
