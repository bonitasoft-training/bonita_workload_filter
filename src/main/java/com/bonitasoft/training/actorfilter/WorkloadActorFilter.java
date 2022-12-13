package com.bonitasoft.training.actorfilter;

import org.bonitasoft.engine.api.APIAccessor;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.filter.AbstractUserFilter;
import org.bonitasoft.engine.filter.UserFilterException;
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.GroupNotFoundException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserSearchDescriptor;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class WorkloadActorFilter extends AbstractUserFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkloadActorFilter.class.getName());

    public static final String GROUP_PATH_INPUT = "groupPath";

    /**
     * Perform validation on the inputs defined on the actorfilter definition (src/main/resources/actor-filter-workload.def)
     * You should:
     * - validate that mandatory inputs are presents
     * - validate that the content of the inputs is coherent with your use case (e.g: validate that a date is / isn't in the past ...)
     */
    @Override
    public void validateInputParameters() throws ConnectorValidationException {
        getPathInput();
    }

    protected String getPathInput() throws ConnectorValidationException {
        String value = getStringInputParameter(GROUP_PATH_INPUT);
        if (value == null) {
            throw new ConnectorValidationException(String.format("Mandatory String parameter '%s' is required.", GROUP_PATH_INPUT));
        }
        return value;
    }

    /**
     * @return a list of {@link User} id that are the candidates to execute the task where this filter is defined.
     * If the result contains a unique user, the task will automaticaly be assigned.
     */
    @Override
    public List<Long> filter(String actorName) throws UserFilterException {
        String groupPath;
        Comparator<UserWithWorkLoad> comparator = new WorkloadComparator();
        try {
            groupPath = getPathInput();
            LOGGER.info("Group path parameter:{}", groupPath);
            APIAccessor apiAccessor = getAPIAccessor();
            final IdentityAPI identityAPI = apiAccessor.getIdentityAPI();
            final List<User> usersInGroup = getUsersInGroup(identityAPI, identityAPI.getGroupByPath(groupPath));
            LOGGER.info("Found {} user(s) in group", usersInGroup.size());
            final List<UserWithWorkLoad> sortedUsers = usersInGroup.stream()
                    .map(user -> new UserWithWorkLoad(apiAccessor, user))
                    .sorted(comparator)
                    .collect(Collectors.toList());
            final long minimumWorkload = sortedUsers.get(0).getWorkload();
            LOGGER.info("Minimum workload: {}", minimumWorkload);
            final List<Long> userIds = sortedUsers.stream()
                    .filter(u -> u.getWorkload() == minimumWorkload)
                    .map(u -> u.getUser().getId()
                    )
                    .collect(Collectors.toList());
            LOGGER.info("Found {} matching user(s)", userIds.size());
            return userIds;
        } catch (ConnectorValidationException | GroupNotFoundException | SearchException e) {
            throw new UserFilterException(e);
        }
    }

    protected List<User> getUsersInGroup(IdentityAPI identityAPI, Group group) throws SearchException {
        SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 100);
        builder.filter(UserSearchDescriptor.GROUP_ID, group.getId());
        final SearchResult<User> userSearchResult = identityAPI.searchUsers(builder.done());
        return userSearchResult.getResult();
    }
}

