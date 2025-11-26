package main.auth;

public class UserSession {
    private static int userId;
    private static String username;
    private static String fullName;
    private static String role;
    private static boolean isLoggedIn = false;

    public static void login(int id, String user, String name, String userRole) {
        userId = id;
        username = user;
        fullName = name;
        role = userRole;
        isLoggedIn = true;
    }

    public static void logout() {
        userId = 0;
        username = null;
        fullName = null;
        role = null;
        isLoggedIn = false;
    }

    public static boolean isLoggedIn() { return isLoggedIn; }
    public static int getUserId() { return userId; }
    public static String getUsername() { return username; }
    public static String getFullName() { return fullName; }
    public static String getRole() { return role; }
    
    public static boolean hasRole(String requiredRole) {
        return role != null && role.equals(requiredRole);
    }
    
    public static boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}
