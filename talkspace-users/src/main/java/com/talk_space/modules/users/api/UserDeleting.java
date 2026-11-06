package com.talk_space.modules.users.api;
/** Synchronous event: listeners delete their own data before the users module deletes the account. */
public record UserDeleting(String userName, String email) {}
