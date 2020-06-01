package android.support.v4.app;

import android.app.Notification;
import android.app.Notification.Builder;

public interface NotificationBuilderWithBuilderAccessor {
    Notification build();

    Builder getBuilder();
}
