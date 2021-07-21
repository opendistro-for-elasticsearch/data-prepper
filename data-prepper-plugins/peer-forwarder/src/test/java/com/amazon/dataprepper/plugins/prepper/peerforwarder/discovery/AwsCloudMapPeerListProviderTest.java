package com.amazon.dataprepper.plugins.prepper.peerforwarder.discovery;

import com.amazon.dataprepper.metrics.PluginMetrics;
import com.linecorp.armeria.client.Endpoint;
import com.linecorp.armeria.client.retry.Backoff;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import software.amazon.awssdk.services.servicediscovery.ServiceDiscoveryAsyncClient;
import software.amazon.awssdk.services.servicediscovery.model.DiscoverInstancesRequest;
import software.amazon.awssdk.services.servicediscovery.model.DiscoverInstancesResponse;
import software.amazon.awssdk.services.servicediscovery.model.HttpInstanceSummary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

class AwsCloudMapPeerListProviderTest {

    private ServiceDiscoveryAsyncClient awsServiceDiscovery;
    private String namespaceName;
    private String serviceName;
    private int timeToRefreshSeconds;
    private Backoff backoff;
    private PluginMetrics pluginMetrics;

    @BeforeEach
    void setUp() {
        awsServiceDiscovery = mock(ServiceDiscoveryAsyncClient.class);
        namespaceName = RandomStringUtils.randomAlphabetic(10);
        serviceName = RandomStringUtils.randomAlphabetic(10);

        timeToRefreshSeconds = 1;
        backoff = mock(Backoff.class);
        pluginMetrics = mock(PluginMetrics.class);
    }

    private AwsCloudMapPeerListProvider createObjectUnderTest() {
        return new AwsCloudMapPeerListProvider(awsServiceDiscovery, namespaceName, serviceName, timeToRefreshSeconds, backoff, pluginMetrics);
    }

    @Test
    void constructor_throws_with_null_AWSServiceDiscovery() {
        awsServiceDiscovery = null;

        assertThrows(NullPointerException.class,
                this::createObjectUnderTest);
    }

    @Test
    void constructor_throws_with_null_Namespace() {
        namespaceName = null;

        assertThrows(NullPointerException.class,
                this::createObjectUnderTest);
    }

    @Test
    void constructor_throws_with_null_ServiceName() {
        serviceName = null;

        assertThrows(NullPointerException.class,
                this::createObjectUnderTest);
    }

    @Test
    void constructor_throws_with_null_Backoff() {
        backoff = null;

        assertThrows(NullPointerException.class,
                this::createObjectUnderTest);
    }

    @ParameterizedTest
    @ValueSource(ints = {Integer.MIN_VALUE, -10, -1, 0})
    void constructor_throws_with_non_positive_timeToRefreshSeconds(int badTimeToRefresh) {
        timeToRefreshSeconds = badTimeToRefresh;

        assertThrows(IllegalArgumentException.class,
                this::createObjectUnderTest);
    }

    @Test
    void constructor_should_DiscoverInstances_with_correct_request() throws InterruptedException {
        createObjectUnderTest();

        Thread.sleep(100);

        ArgumentCaptor<DiscoverInstancesRequest> requestArgumentCaptor =
                ArgumentCaptor.forClass(DiscoverInstancesRequest.class);

        then(awsServiceDiscovery)
                .should()
                .discoverInstances(requestArgumentCaptor.capture());

        DiscoverInstancesRequest actualRequest = requestArgumentCaptor.getValue();

        assertThat(actualRequest.namespaceName(), equalTo(namespaceName));
        assertThat(actualRequest.serviceName(), equalTo(serviceName));
        assertThat(actualRequest.healthStatusAsString(), nullValue());
    }

    @Test
    void getPeerList_is_empty_before_populated() throws InterruptedException {
        AwsCloudMapPeerListProvider objectUnderTest = createObjectUnderTest();

        // Ensure that there is an opportunity to make the request
        Thread.sleep(100);

        List<String> peerList = objectUnderTest.getPeerList();

        assertThat(peerList, notNullValue());
        assertThat(peerList.size(), equalTo(0));
    }

    @Nested
    class WithDiscoverInstances {

        private DiscoverInstancesResponse discoverInstancesResponse;

        @BeforeEach
        void setUp() {
            discoverInstancesResponse = mock(DiscoverInstancesResponse.class);

            CompletableFuture<DiscoverInstancesResponse> discoverFuture =
                    CompletableFuture.completedFuture(discoverInstancesResponse);

            given(awsServiceDiscovery.discoverInstances(any(DiscoverInstancesRequest.class)))
                    .willReturn(discoverFuture);
        }

        @Test
        void getPeerList_returns_empty_when_DiscoverInstances_has_no_instances() throws InterruptedException {
            given(discoverInstancesResponse.instances()).willReturn(Collections.emptyList());

            AwsCloudMapPeerListProvider objectUnderTest = createObjectUnderTest();

            Thread.sleep(100);

            List<String> peerList = objectUnderTest.getPeerList();
            assertThat(peerList, notNullValue());
            assertThat(peerList.size(), equalTo(0));

        }

        @Test
        void getPeerList_returns_list_as_found() throws InterruptedException {

            List<String> knownIpPeers = IntStream.range(0, 3)
                    .mapToObj(i -> generateRandomIp())
                    .collect(Collectors.toList());

            List<HttpInstanceSummary> instances = knownIpPeers
                    .stream()
                    .map(ip -> {
                        HttpInstanceSummary instanceSummary = mock(HttpInstanceSummary.class);
                        given(instanceSummary.attributes()).willReturn(
                                Collections.singletonMap("AWS_INSTANCE_IPV4", ip));
                        return instanceSummary;
                    })
                    .collect(Collectors.toList());

            given(discoverInstancesResponse.instances()).willReturn(instances);

            AwsCloudMapPeerListProvider objectUnderTest = createObjectUnderTest();

            Thread.sleep(100);

            List<String> actualPeers = objectUnderTest.getPeerList();
            assertThat(actualPeers, notNullValue());
            assertThat(actualPeers.size(), equalTo(instances.size()));

            assertThat(new HashSet<>(actualPeers), equalTo(new HashSet<>(knownIpPeers)));
        }

        @Test
        void constructor_continues_to_discover_instances() throws InterruptedException {

            createObjectUnderTest();

            Thread.sleep(2_500);

            ArgumentCaptor<DiscoverInstancesRequest> requestArgumentCaptor =
                    ArgumentCaptor.forClass(DiscoverInstancesRequest.class);

            then(awsServiceDiscovery)
                    .should(atLeast(2))
                    .discoverInstances(requestArgumentCaptor.capture());

            for (DiscoverInstancesRequest actualRequest : requestArgumentCaptor.getAllValues()) {
                assertThat(actualRequest.namespaceName(), equalTo(namespaceName));
                assertThat(actualRequest.serviceName(), equalTo(serviceName));
                assertThat(actualRequest.healthStatusAsString(), nullValue());
            }
        }
    }

    @Nested
    class WithSeveralFailedAttempts {

        private List<String> knownIpPeers;

        @BeforeEach
        void setUp() {
            DiscoverInstancesResponse discoverInstancesResponse = mock(DiscoverInstancesResponse.class);

            knownIpPeers = IntStream.range(0, 3)
                    .mapToObj(i -> generateRandomIp())
                    .collect(Collectors.toList());

            List<HttpInstanceSummary> instances = knownIpPeers
                    .stream()
                    .map(ip -> {
                        HttpInstanceSummary instanceSummary = mock(HttpInstanceSummary.class);
                        given(instanceSummary.attributes()).willReturn(
                                Collections.singletonMap("AWS_INSTANCE_IPV4", ip));
                        return instanceSummary;
                    })
                    .collect(Collectors.toList());

            given(discoverInstancesResponse.instances()).willReturn(instances);

            CompletableFuture<DiscoverInstancesResponse> failedFuture1 = new CompletableFuture<>();
            failedFuture1.completeExceptionally(mock(Throwable.class));
            CompletableFuture<DiscoverInstancesResponse> failedFuture2 = new CompletableFuture<>();
            failedFuture2.completeExceptionally(mock(Throwable.class));
            CompletableFuture<DiscoverInstancesResponse> successFuture = CompletableFuture.completedFuture(discoverInstancesResponse);

            given(awsServiceDiscovery.discoverInstances(any(DiscoverInstancesRequest.class)))
                    .willReturn(failedFuture1)
                    .willReturn(failedFuture2)
                    .willReturn(successFuture);

            given(backoff.nextDelayMillis(anyInt()))
                    .willReturn(100L);

        }

        @Test
        void getPeerList_returns_value_after_several_failed_attempts() throws InterruptedException {

            AwsCloudMapPeerListProvider objectUnderTest = createObjectUnderTest();

            Thread.sleep(100);

            List<String> expectedEmpty = objectUnderTest.getPeerList();

            assertThat(expectedEmpty, notNullValue());
            assertThat(expectedEmpty.size(), equalTo(0));

            Thread.sleep(100);

            List<String> expectedEmptyAgain = objectUnderTest.getPeerList();

            assertThat(expectedEmptyAgain, notNullValue());
            assertThat(expectedEmptyAgain.size(), equalTo(0));

            Thread.sleep(110);

            List<String> expectedPopulated = objectUnderTest.getPeerList();

            assertThat(expectedPopulated, notNullValue());
            assertThat(expectedPopulated.size(), equalTo(knownIpPeers.size()));

            assertThat(new HashSet<>(expectedPopulated), equalTo(new HashSet<>(knownIpPeers)));

            InOrder inOrder = inOrder(backoff);
            then(backoff)
                    .should(inOrder)
                    .nextDelayMillis(1);
            then(backoff)
                    .should(inOrder)
                    .nextDelayMillis(2);
            then(backoff)
                    .shouldHaveNoMoreInteractions();
        }

        @Test
        void listener_gets_list_after_several_failed_attempts() throws InterruptedException {

            final List<Endpoint> listenerEndpoints = new ArrayList<>();

            AwsCloudMapPeerListProvider objectUnderTest = createObjectUnderTest();

            objectUnderTest.addListener(listenerEndpoints::addAll);

            Thread.sleep(100);

            assertThat(listenerEndpoints.size(), equalTo(0));

            Thread.sleep(100);

            assertThat(listenerEndpoints.size(), equalTo(0));

            Thread.sleep(110);

            assertThat(listenerEndpoints.size(), equalTo(knownIpPeers.size()));

            Set<String> observedIps = listenerEndpoints.stream()
                    .map(Endpoint::ipAddr)
                    .collect(Collectors.toSet());

            assertThat(observedIps, equalTo(new HashSet<>(knownIpPeers)));

            InOrder inOrder = inOrder(backoff);
            then(backoff)
                    .should(inOrder)
                    .nextDelayMillis(1);
            then(backoff)
                    .should(inOrder)
                    .nextDelayMillis(2);
            then(backoff)
                    .shouldHaveNoMoreInteractions();
        }
    }

    private static String generateRandomIp() {
        Random random = new Random();

        return IntStream.range(0, 4)
                .map(i -> random.nextInt(255))
                .mapToObj(Integer::toString)
                .collect(Collectors.joining("."));
    }
}