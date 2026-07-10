# Summary: Kafka Authentication and Authorization

**Source:** `raw/authenticate authorize.md` (RU)
**Date Ingested:** 2026-07-09

## Key Takeaways
- Kafka security splits into **Authentication (аутентификация, AuthN)** — who you are — and **Authorization (авторизация, AuthZ)** — what you may do.
- **Authentication methods:** **SSL/TLS (mTLS)** (certificate exchange via a CA — most secure), and **SASL** framework: **PLAIN** (login/password, must be over SSL), **SCRAM** (hashed passwords), **GSSAPI/Kerberos** (Active Directory/LDAP integration).
- **Authorization:** **ACLs (списки контроля доступа)** bind a **Principal (участник)** to a resource (topic, group, cluster) and operation (`READ`, `WRITE`, `DESCRIBE`, `ALTER`). The `Authorizer` interface has ACL as default; Enterprise often uses **RBAC**.

### Best Practices
- **Least privilege (наименьшие привилегии):** producers get `WRITE` only, consumers `READ`/`DESCRIBE` only; never grant `All`.
- Always encrypt in transit (TLS); prefer **mTLS** to avoid per-service passwords via PKI.
- Keep `super.users` minimal.

### Case Studies
- **Finance (PCI DSS/SOC2):** Kerberos + TLS ties Kafka rights to Active Directory, so access is revoked automatically when an employee leaves.
- **Multi-tenant SaaS:** ACLs grant each tenant Principal access only to topics prefixed with its ID (`tenant1-*`).
- **Hybrid cloud:** SASL/SCRAM for on-prem↔cloud password auth without managing certificates everywhere.

### Production-Ready Recommendations
- Manage ACLs via IaC (Terraform / Strimzi operator), not manual CLI per topic.
- Enable authorization audit logging; budget +10–15% CPU for TLS encryption.
- Set `authorizer.class.name` to a real authorizer (e.g. `AclAuthorizer`) — never leave `AllowAllAuthorizer`.
- Combine with [Quotas](../concepts/Quotas.md) to defend against DoS.

## Concepts Covered
- [Security](../concepts/Security.md)
- [Quotas](../concepts/Quotas.md)

