package com.mnms.booking.util;

import java.util.List;

public record JwtPrincipal(Long userId, String loginId, List<String> roles) {}