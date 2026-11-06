# TalkSpace — Maven multi-module modular monolith

Այս տարբերակը **առանձին Maven մոդուլներով** backend է։ Յուրաքանչյուր մոդուլ ունի իր `pom.xml`-ն ու `src`-ը։ Արմատում միայն parent/aggregator `pom.xml` է, արմատային `src` չկա։

| Maven module | Բովանդակություն |
| --- | --- |
| talkspace-app | Միակ Spring Boot main class, configuration, resources և համակարգի համատեղ թեստեր |
| talkspace-shared | Ընդհանուր ենթակառուցվածք և սխալներ |
| talkspace-users | Օգտատերեր և պրոֆիլներ |
| talkspace-auth | JWT, մուտք, refresh session-ներ |
| talkspace-network | Կապեր և բլոկավորում |
| talkspace-chat | Նամակներ և չատ |
| talkspace-notifications | Ծանուցումներ |
| talkspace-discovery | Matching և որոնում |
| talkspace-feedback | Կարծիքներ |
| talkspace-presence | Online կարգավիճակ |

## Windows

Backend-ի արմատային պանակում՝

```powershell
.\mvnw.cmd clean verify
java -jar talkspace-app/target/talkspace-app.jar
```

## Linux / macOS

```bash
./mvnw clean verify
java -jar talkspace-app/target/talkspace-app.jar
```

Կարգավորումները՝ `talkspace-app/src/main/resources/application.properties`։ IntelliJ-ում բացիր **արմատային `pom.xml`-ը**։ Frontend-ը մնում է առանձին և այս փոփոխության համար չի պահանջում նոր API հասցեներ։

Արխիվը բացիր նոր պանակում և վերականգնիր քո տեղային կարգավորումներն ու նկարները։ Հին `src` պանակը նոր կառուցվածքի մաս չէ։

Մանրամասներ՝ [MODULAR_MONOLITH_HY.md](MODULAR_MONOLITH_HY.md)։ Ստուգումներ՝ [TEST_RESULTS_HY.md](TEST_RESULTS_HY.md)։
