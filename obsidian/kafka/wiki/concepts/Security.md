# Security

**Security (безопасность)** in Kafka splits into **Authentication (аутентификация, AuthN)** — verifying identity — and **Authorization (авторизация, AuthZ)** — controlling permitted actions.

- **Authentication:** **SSL/TLS (mTLS)** via CA-signed certificates (most secure); **SASL** framework — **PLAIN** (over SSL), **SCRAM** (hashed passwords), **GSSAPI/Kerberos** (AD/LDAP).
- **Authorization:** **ACLs (списки контроля доступа)** bind a **Principal (участник)** to a resource (topic, group, cluster) and operation (`READ`, `WRITE`, `DESCRIBE`, `ALTER`); the `Authorizer` interface also supports **RBAC** in Enterprise.

### Best Practices
- **Least privilege (наименьшие привилегии):** producers `WRITE` only, consumers `READ`/`DESCRIBE` only; never `All`.
- Always encrypt in transit (TLS); prefer mTLS via PKI; keep `super.users` minimal.

### Case Studies
- **Finance (PCI DSS/SOC2):** Kerberos + TLS tie rights to Active Directory (auto-revoke on offboarding).
- **Multi-tenant SaaS:** ACLs scope each tenant to `tenantN-*` topics.
- **Hybrid cloud:** SASL/SCRAM for on-prem↔cloud password auth.

### Production-Ready Recommendations
- Manage ACLs via IaC (Terraform/Strimzi); enable authorization audit logging; budget +10–15% CPU for TLS.
- Set `authorizer.class.name` to a real authorizer (e.g. `AclAuthorizer`) — never `AllowAllAuthorizer`; combine with [Quotas](Quotas.md) against DoS.

*References:*
- [Kafka Security (AuthN/AuthZ)](../summaries/019_kafka_security_authn_authz.md)
- [Quotas](Quotas.md)

