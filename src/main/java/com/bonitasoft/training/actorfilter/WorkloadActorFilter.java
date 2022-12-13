package com.bonitasoft.training.actorfilter;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.bonitasoft.engine.api.APIAccessor;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.bonitasoft.engine.filter.AbstractUserFilter;
import org.bonitasoft.engine.filter.UserFilterException;
import org.bonitasoft.engine.identity.User;

public class WorkloadActorFilter extends AbstractUserFilter {

    private static final Logger LOGGER = Logger.getLogger(WorkloadActorFilter.class.getName());

    static final String GROUP_PATH_INPUT = "groupPath";

    /**
     * Perform validation on the inputs defined on the actorfilter definition (src/main/resources/actor-filter-workload.def)
     * You should: 
     * - validate that mandatory inputs are presents
     * - validate that the content of the inputs is coherent with your use case (e.g: validate that a date is / isn't in the past ...)
     */
    @Override
    public void validateInputParameters() throws ConnectorValidationException {
        getRequiredStringInput(GROUP_PATH_INPUT);
    }

    protected String getRequiredStringInput(String inputName) throws ConnectorValidationException {
        String value = (String) getInputParameter(GROUP_PATH_INPUT);
        if (value == null ) {
            throw new ConnectorValidationException(String.format("Mandatory parameter '%s' is required.", inputName));
        }
        return value;
    }

    /**
     * @return a list of {@link User} id that are the candidates to execute the task where this filter is defined. 
     * If the result contains a unique user, the task will automaticaly be assigned.
     * @see AbstractUserFilter.shouldAutoAssignTaskIfSingleResult
     */
    @Override
    public List<Long> filter(String groupPath) throws UserFilterException {
        LOGGER.info(String.format("%s input = %s", getRequiredStringInput(GROUP_PATH_INPUT)));
        APIAccessor apiAccessor = getAPIAccessor();
        ProcessAPI processAPI = apiAccessor.getProcessAPI();
        List<Long> users = processAPI.getUserIdsForActor(getExecutionContext().getProcessDefinitionId(), actorName, 0, Integer.MAX_VALUE);
        return users.stream()
                .filter( userId -> !isOverloaded(userId, apiAccessor))
                .collect(Collectors.toList());
    }
    

    private boolean isOverloaded(Long userId, APIAccessor apiAccessor) {
        return apiAccessor.getProcessAPI().getNumberOfAssignedHumanTaskInstances(userId) >= (Integer) getInputParameter(MAXIMUM_WORKLOAD_INPUT);
    }
}

