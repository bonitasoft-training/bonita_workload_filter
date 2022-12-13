package com.bonitasoft.training.actorfilter;

import java.util.Comparator;

public class WorkloadComparator implements Comparator<UserWithWorkLoad> {
    @Override
    public int compare(UserWithWorkLoad o1, UserWithWorkLoad o2) {
        return Long.compare(o1.getWorkload(), o2.getWorkload());
    }
}
