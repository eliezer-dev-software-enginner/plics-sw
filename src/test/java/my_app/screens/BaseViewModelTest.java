package my_app.screens;

import my_app.db.services.BaseServiceTest;

public abstract class BaseViewModelTest extends BaseServiceTest {

    protected void waitForAsync() {
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
