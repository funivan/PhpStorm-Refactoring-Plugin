package com.funivan.phpstorm.refactoring.util;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by ivan on 27.01.16.
 */
public class UserNotification {


    public static void showError(@NotNull Project project, @NotNull String title, @NotNull String content, String group) {
        notify(project, title, content, group, NotificationType.ERROR);
    }

    public static void showInfo(@NotNull Project project, @NotNull String title, @NotNull String content, String group) {
        notify(project, title, content, group, NotificationType.INFORMATION);
    }

    public static void notify(@NotNull Project project, @NotNull String title, @NotNull String content, @NotNull String group, NotificationType errorType) {
        Notifications.Bus.notify(new Notification(group, title, content, errorType, null), project);
    }

}
