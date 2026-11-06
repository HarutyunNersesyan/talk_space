> Maven մոդուլների, ֆայլերի տեղադրության և գործարկման ընթացիկ հրահանգները՝ [README.md](README.md)։

> Նախորդ փուլի նկարագրություն։ Ընթացիկ կառուցվածքը՝ [MODULAR_MONOLITH_HY.md](MODULAR_MONOLITH_HY.md)։

# TalkSpace — architecture refactor, phase 1

## What changed
- Extracted login business logic from `api.controller.AuthController` to `auth.service.AuthService`.
- Preserved `/account/auth` and `/account/profile/logout`, method signatures, response DTO and behavior.
- Added `FrontendContractTest` regression checks for login URLs, response DTO fields, and WebSocket endpoint.

## What did NOT change
- Other controllers, security filters, JWT, database entities, JPA mapping, frontend URLs, CORS, Docker and STOMP config.

## Run
`mvn test -Dtest=FrontendContractTest`
`mvn clean package`

## Notes
- No Maven execution was available in the artifact build environment; run tests locally before deploying.
- This phase is intentionally limited. Future steps can extract User, Chat, Matching and Admin services incrementally.
- Never commit mail app passwords; use `MAIL_PASSWORD` environment variable.
