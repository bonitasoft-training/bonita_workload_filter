package com.bonitasoft.training.actorfilter;

import org.bonitasoft.engine.api.APIAccessor;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bonitasoft.training.actorfilter.WorkloadActorFilter.GROUP_PATH_INPUT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkloadActorFilterTest {

    @InjectMocks
    private WorkloadActorFilter filter;

    @Mock
    private APIAccessor apiAccessor;
    @Mock
    private ProcessAPI processApi;

    @Mock
    private IdentityAPI identityApi;

    @Mock
    SearchResult<User> userSearchResult;

    @Mock
    private Group group;

    @Mock
    private User william;

    @Mock
    private User cindy;

    @Mock
    private User helen;

    @Mock
    private User walter;

    @Test
    public void should_throw_exception_if_mandatory_input_is_missing() {
        assertThrows(ConnectorValidationException.class, () ->
                filter.validateInputParameters()
        );
    }

    @Test
    public void should_return_a_list_of_candidates() throws Exception {
        // Given
        final String marketingGroup = "/acme/Marketing";
        when(apiAccessor.getProcessAPI()).thenReturn(processApi);
        when(apiAccessor.getIdentityAPI()).thenReturn(identityApi);
        when(identityApi.getGroupByPath(marketingGroup)).thenReturn(group);
        when(identityApi.searchUsers(any(SearchOptions.class))).thenReturn(userSearchResult);
        List<User> users = List.of(william, helen, walter, cindy);
        when(userSearchResult.getResult()).thenReturn(users);
        when(william.getId()).thenReturn(45L);
        when(helen.getId()).thenReturn(67L);
        when(walter.getId()).thenReturn(89L);
        when(walter.getId()).thenReturn(92L);

        when(processApi.getNumberOfAssignedHumanTaskInstances(william.getId())).thenReturn(8L);
        when(processApi.getNumberOfAssignedHumanTaskInstances(cindy.getId())).thenReturn(4L);
        when(processApi.getNumberOfAssignedHumanTaskInstances(helen.getId())).thenReturn(13L);
        when(processApi.getNumberOfAssignedHumanTaskInstances(walter.getId())).thenReturn(4L);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put(GROUP_PATH_INPUT, marketingGroup);
        filter.setInputParameters(parameters);

        // When
        List<Long> candidates = filter.filter("MyActor");

        // Then
        assertThat(candidates)
                .as("Should return only users with smaller workload.")
                .containsExactly(walter.getId(), cindy.getId());
    }
}
