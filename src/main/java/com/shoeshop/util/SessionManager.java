package com.shoeshop.util;

import com.shoeshop.model.User;

/**
 * Хранит текущего авторизованного пользователя на протяжении сессии.
 * currentUser == null означает режим «Гость».
 */
public final class SessionManager {

    private static User currentUser;

    private SessionManager() {}

    public static void setCurrentUser(User user) { currentUser = user; }
    public static User getCurrentUser()          { return currentUser; }

    /** Завершает сессию (переход на экран входа). */
    public static void logout() { currentUser = null; }

    public static boolean isGuest()   { return currentUser == null; }
    public static boolean isAdmin()   { return hasRole("ADMIN"); }
    public static boolean isManager() { return hasRole("MANAGER"); }

    /** Возвращает true для менеджера или администратора. */
    public static boolean isManagerOrAdmin() { return isManager() || isAdmin(); }

    /** Отображаемое имя для правого верхнего угла интерфейса. */
    public static String getDisplayName() {
        return currentUser == null ? "Гость" : currentUser.getFullName();
    }

    private static boolean hasRole(String roleName) {
        return currentUser != null && roleName.equals(currentUser.getRoleName());
    }
}
