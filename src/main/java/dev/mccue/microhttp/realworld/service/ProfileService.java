package dev.mccue.microhttp.realworld.service;

public final class ProfileService {
    private final UserService userService;
    public ProfileService(UserService userService) {
        this.userService = userService;
    }

    public Object fetchProfile(String userNameOrId) {
        var user = userService.findByUsername(userNameOrId);
        return null;
    }
}
