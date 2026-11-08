package com.Iuer.Pacman.denylist;

import java.util.Date;

public interface DenyListService {
    void deny(String jti, long ttlMillis);
    boolean isDenied(String jti);
    void revokeAllOfUser(String username);
    boolean isIssuedBeforeRevoke(String username, Date issuedAt);
}