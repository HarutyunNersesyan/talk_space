package com.talk_space.shared.web;

import java.time.Instant;

/** Common shape for errors not already handled by legacy endpoints. */
public record ApiError(int status, String code, String message, Instant timestamp) {}
