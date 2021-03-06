package ru.yandex.qatools.allure.events;

import ru.yandex.qatools.allure.model.Status;
import ru.yandex.qatools.allure.model.Step;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 11.11.13
 */
public class StepFailureEvent implements StepEvent {
    private Throwable throwable;

    public StepFailureEvent() {
    }

    @Override
    public void process(Step step) {
        if (throwable instanceof AssertionError) {
            step.setStatus(Status.FAILED);
        } else {
            step.setStatus(Status.BROKEN);
        }
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public StepFailureEvent withThrowable(Throwable throwable) {
        setThrowable(throwable);
        return this;
    }

}
