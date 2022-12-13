package com.bonitasoft.training.actorfilter;

import org.bonitasoft.engine.api.APIAccessor;
import org.bonitasoft.engine.identity.User;

public class UserWithWorkLoad {
    private final long workload;
    private final User user;

    public UserWithWorkLoad(APIAccessor apiAccessor, User user) {
        this.user = user;
        this.workload=apiAccessor.getProcessAPI().getNumberOfAssignedHumanTaskInstances(user.getId());
    }

    public User getUser() {
        return user;
    }

    public long getWorkload() {
        return workload;
    }
}
