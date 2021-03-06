package ru.yandex.qatools.allure.events;

import org.junit.Before;
import org.junit.Test;
import ru.yandex.qatools.allure.model.*;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;


/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 14.12.13
 */
public class TestCaseEventTest {

    private TestCaseResult testCase;

    @Before
    public void setUp() throws Exception {
        testCase = mock(TestCaseResult.class);
    }

    @Test
    public void testCaseStartedEvent() throws Exception {
        new TestCaseStartedEvent("suite.uid", "name").process(testCase);
        verify(testCase).setName("name");
        verify(testCase).setTitle(null);
        verify(testCase).setDescription(null);
        verify(testCase).setLabels(Collections.<Label>emptyList());
        verify(testCase).setStart(anyLong());
        verify(testCase).setSeverity(SeverityLevel.NORMAL);
        verify(testCase).setStatus(Status.PASSED);
        verifyNoMoreInteractions(testCase);
    }

    @Test
    public void testCaseStartedEventSeverity() throws Exception {
        new TestCaseStartedEvent("suite.uid", "name").withSeverity(SeverityLevel.BLOCKER).process(testCase);
        verify(testCase).setName("name");
        verify(testCase).setTitle(null);
        verify(testCase).setDescription(null);
        verify(testCase).setLabels(Collections.<Label>emptyList());
        verify(testCase).setStart(anyLong());
        verify(testCase).setSeverity(SeverityLevel.BLOCKER);
        verify(testCase).setStatus(Status.PASSED);
        verifyNoMoreInteractions(testCase);
    }

    @Test
    public void testCaseStartedEventTitle() throws Exception {
        new TestCaseStartedEvent("suite.uid", "name").withTitle("some.title").process(testCase);
        verify(testCase).setName("name");
        verify(testCase).setTitle("some.title");
        verify(testCase).setDescription(null);
        verify(testCase).setLabels(Collections.<Label>emptyList());
        verify(testCase).setStart(anyLong());
        verify(testCase).setSeverity(SeverityLevel.NORMAL);
        verify(testCase).setStatus(Status.PASSED);
        verifyNoMoreInteractions(testCase);
    }

    @Test
    public void testCaseStartedEventDescription() throws Exception {
        new TestCaseStartedEvent("suite.uid", "name").withDescription("some.description").process(testCase);
        verify(testCase).setName("name");
        verify(testCase).setTitle(null);
        verify(testCase).setDescription("some.description");
        verify(testCase).setLabels(Collections.<Label>emptyList());
        verify(testCase).setStart(anyLong());
        verify(testCase).setSeverity(SeverityLevel.NORMAL);
        verify(testCase).setStatus(Status.PASSED);
        verifyNoMoreInteractions(testCase);
    }

    @Test
    public void testCaseStartedEventBehavior() throws Exception {
        Label label = new Label().withName("label.name").withValue("label.value");
        new TestCaseStartedEvent("suite.uid", "name").withLabels(label).process(testCase);
        verify(testCase).setName("name");
        verify(testCase).setTitle(null);
        verify(testCase).setDescription(null);
        verify(testCase).setLabels(Arrays.asList(label));
        verify(testCase).setStart(anyLong());
        verify(testCase).setSeverity(SeverityLevel.NORMAL);
        verify(testCase).setStatus(Status.PASSED);
        verifyNoMoreInteractions(testCase);
    }

    @Test
    public void testCaseFinishedEvent() throws Exception {
        new TestCaseFinishedEvent().process(testCase);
        verify(testCase).setStop(anyLong());
        verifyNoMoreInteractions(testCase);
    }

    @Test
    public void testCaseSkippedEvent() throws Exception {
        Throwable throwable = new Exception("atata");
        new TestCaseSkippedEvent().withThrowable(throwable).process(testCase);
        verify(testCase).setFailure(any(Failure.class));
        verify(testCase).setStatus(Status.SKIPPED);
        verifyNoMoreInteractions(testCase);
    }

    @Test
    public void testCaseFailureEventFailed() throws Exception {
        Throwable throwable = new AssertionError("atata");
        new TestCaseFailureEvent().withThrowable(throwable).process(testCase);
        verify(testCase).setFailure(any(Failure.class));
        verify(testCase).setStatus(Status.FAILED);
        verifyNoMoreInteractions(testCase);
    }

    @Test
    public void testCaseFailureEventBroken() throws Exception {
        Throwable throwable = new Exception("atata");
        new TestCaseFailureEvent().withThrowable(throwable).process(testCase);
        verify(testCase).setFailure(any(Failure.class));
        verify(testCase).setStatus(Status.BROKEN);
        verifyNoMoreInteractions(testCase);
    }
}
